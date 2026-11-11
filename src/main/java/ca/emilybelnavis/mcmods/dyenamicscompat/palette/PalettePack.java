package ca.emilybelnavis.mcmods.dyenamicscompat.palette;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.PaletteTextures;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Derives resources locally from installed upstream jars; no upstream artwork is redistributed. */
public final class PalettePack extends AbstractPackResources {
    private final PackType type;
    private final Map<ResourceLocation, IoSupplier<InputStream>> resources = new LinkedHashMap<>();
    private final Map<String, List<String>> files = new HashMap<>();
    private final Map<ResourceLocation, JsonArray> tags = new LinkedHashMap<>();
    private final Map<String, JsonElement> sourceJson = new HashMap<>();
    private static final Gson GSON = new Gson();

    private PalettePack(PackLocationInfo location, PackType type, boolean full) {
        super(location);
        this.type = type;
        if (full) build();
    }
    public static void findPacks(AddPackFindersEvent event) {
        PackType type = event.getPackType();
        var location = new PackLocationInfo("dyenamicscompat_letsdo_" + type.getDirectory(), Component.literal("Dyenamics Compat: Palettes"), PackSource.BUILT_IN, Optional.empty());
        var supplier = new Pack.ResourcesSupplier() {
            @Override public PalettePack openPrimary(PackLocationInfo info) { return new PalettePack(info, type, false); }
            @Override public PalettePack openFull(PackLocationInfo info, Pack.Metadata metadata) { return new PalettePack(info, type, true); }
        };
        Pack pack = Pack.readMetaAndCreate(location, supplier, type, new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (pack == null) throw new IllegalStateException("Unable to initialize palette resources");
        event.addRepositorySource(consumer -> consumer.accept(pack.hidden()));
    }
    private void build() {
        JsonObject language = new JsonObject();
        for (var variant : PaletteModule.VARIANTS) {
            if (type == PackType.CLIENT_RESOURCES) {
                String name = "blockstates/" + variant.id().getPath() + ".json";
                json(PaletteModule.id(name), remapModels(read(variant.addon(), "assets/" + variant.addon() + "/blockstates/" + variant.source().getPath() + ".json"), variant));
                if (variant.item() != null) model(variant.source().withPath("item/" + variant.source().getPath()), variant, "item/" + variant.id().getPath());
                String label = title(variant.color().id + " " + variant.family());
                language.addProperty("block.dyenamicscompat." + variant.id().getPath().replace('/', '.'), label);
            } else {
                addData(variant);
            }
        }
        for (var item : PaletteModule.EXTRA_ITEMS) {
            var representative = PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals(item.addon()) && v.color() == item.color()).findFirst().orElseThrow();
            if (type == PackType.CLIENT_RESOURCES) {
                model(item.source().withPath("item/" + item.source().getPath()), representative, "item/" + item.id().getPath());
                language.addProperty("item.dyenamicscompat." + item.id().getPath().replace('/', '.'), title(item.color().id + " " + item.family()));
            } else {
                itemData(item);
            }
        }
        if (type == PackType.CLIENT_RESOURCES) json(PaletteModule.id("lang/en_us.json"), language);
        else {
            addUpstreamTags();
            if (PaletteModule.VARIANTS.stream().anyMatch(v -> v.addon().equals("supplementaries"))) {
                bridgeRecipe("supplementaries", "present_dye");
                bridgeRecipe("supplementaries", "trapped_present");
            }
            if (PaletteModule.VARIANTS.stream().anyMatch(v -> v.addon().equals("suppsquared"))) bridgeRecipe("suppsquared", "sack_dye");
            if (PaletteModule.EXTRA_ITEMS.stream().anyMatch(i -> i.addon().equals("quark") && i.family().equals("shard"))) {
                // Quark's mixed-shard recipe excludes each single-color glass recipe.
                // Extend that list so matching extra shards cannot also turn into dirty glass.
                String recipe = "recipe/tweaks/crafting/glass/mixed_dirty_glass.json";
                var mixed = read("quark", "data/quark/" + recipe).getAsJsonObject();
                for (var item : PaletteModule.EXTRA_ITEMS) if (item.addon().equals("quark") && item.family().equals("shard"))
                    mixed.getAsJsonArray("exclusions").add(item.id() + "/glass");
                json(ResourceLocation.fromNamespaceAndPath("quark", recipe), mixed);
            }
            tags.forEach((id, values) -> {
                JsonObject tag = new JsonObject(); tag.addProperty("replace", false); tag.add("values", values); json(id, tag);
            });
        }
    }
    private JsonElement remapModels(JsonElement input, PaletteModule.Variant variant) {
        if (input.isJsonObject()) {
            JsonObject out = new JsonObject();
            input.getAsJsonObject().entrySet().forEach(e -> {
                if (e.getKey().equals("model") && e.getValue().isJsonPrimitive()) {
                    ResourceLocation source = ResourceLocation.parse(e.getValue().getAsString());
                    out.addProperty(e.getKey(), model(source, variant, null).toString());
                } else out.add(e.getKey(), remapModels(e.getValue(), variant));
            });
            return out;
        }
        if (input.isJsonArray()) { JsonArray out = new JsonArray(); input.getAsJsonArray().forEach(e -> out.add(remapModels(e, variant))); return out; }
        return input.deepCopy();
    }
    private ResourceLocation model(ResourceLocation source, PaletteModule.Variant variant, String overridePath) {
        if (source.getNamespace().equals("minecraft")) return source;
        ResourceLocation target = PaletteModule.id(overridePath != null ? overridePath : variant.id().getPath() + "/derived/" + source.getNamespace() + "/" + source.getPath());
        ResourceLocation resource = target.withPath("models/" + target.getPath() + ".json");
        if (resources.containsKey(resource)) return target;
        String path = "assets/" + source.getNamespace() + "/models/" + source.getPath() + ".json";
        JsonObject data = read(source.getNamespace(), path).getAsJsonObject();
        // Reserve the key before following parents, so malformed cycles cannot recurse forever.
        json(resource, data);
        if (data.has("parent")) data.addProperty("parent", model(ResourceLocation.parse(data.get("parent").getAsString()), variant, null).toString());
        if (data.has("textures") && !variant.tinted()) {
            JsonObject textures = data.getAsJsonObject("textures");
            for (var entry : new ArrayList<>(textures.entrySet())) {
                String value = entry.getValue().getAsString();
                if (!value.startsWith("#")) textures.addProperty(entry.getKey(), texture(ResourceLocation.parse(value), variant).toString());
            }
        }
        json(resource, data);
        return target;
    }
    private ResourceLocation texture(ResourceLocation source, PaletteModule.Variant variant) {
        if (!ModList.get().isLoaded(source.getNamespace())) return source;
        String whitePath = source.getPath();
        boolean patterned = variant.addon().equals("furniture") && !variant.nativePattern().contains("%s");
        if (patterned) {
            String token = variant.source().getPath().replaceFirst("^(curtain_|pouffe_|sofa_|lamp_wall_|lamp_)", "");
            whitePath = source.getPath().replaceAll("(?<=_|/)" + java.util.regex.Pattern.quote(token) + "(?=_|/|$)", "white");
            if (whitePath.equals(source.getPath())) return source;
        }
        String redPath = whitePath.replaceAll("(?<![a-z])white(?=_|/|$)", "red");
        if (redPath.equals(whitePath)) return source;
        String original = "assets/" + source.getNamespace() + "/textures/" + source.getPath() + ".png";
        String white = "assets/" + source.getNamespace() + "/textures/" + whitePath + ".png";
        String red = "assets/" + source.getNamespace() + "/textures/" + redPath + ".png";
        if (!exists(source.getNamespace(), white) || !exists(source.getNamespace(), red)) return source;
        ResourceLocation target = PaletteModule.id("block/" + variant.id().getPath() + "/derived/" + source.getNamespace() + "/" + source.getPath());
        ResourceLocation resource = target.withPath("textures/" + target.getPath() + ".png");
        resources.computeIfAbsent(resource, key -> new IoSupplier<>() {
            private byte[] png;
            @Override public synchronized InputStream get() throws IOException {
                if (png == null) png = PaletteTextures.recolorPattern(bytes(source.getNamespace(), original), bytes(source.getNamespace(), white), bytes(source.getNamespace(), red), variant.color().rgb);
                return new ByteArrayInputStream(png);
            }
        });
        return target;
    }
    private void addData(PaletteModule.Variant v) {
        String sourceLoot = "data/" + v.addon() + "/loot_table/blocks/" + v.source().getPath() + ".json";
        if (exists(v.addon(), sourceLoot)) json(v.id().withPath("loot_table/blocks/" + v.id().getPath() + ".json"), replaceIds(read(v.addon(), sourceLoot), replacements(v.addon(), v.color())));
        if (v.item() == null) return;
        String tag = PaletteModule.prefix(v.addon()) + "/" + v.family();
        addTag(PaletteModule.id("tags/item/" + tag + ".json"), v.id().toString());
        for (var dye : net.minecraft.world.item.DyeColor.values()) {
            var source = ResourceLocation.fromNamespaceAndPath(v.addon(), v.nativePattern().formatted(dye.getName()));
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(source)) addTag(PaletteModule.id("tags/item/" + tag + ".json"), source.toString());
        }
        if (java.util.Set.of("supplementaries", "suppsquared", "quark").contains(v.addon())) {
            var blank = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(v.addon(), v.family());
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(blank)) addTag(PaletteModule.id("tags/item/" + tag + ".json"), blank.toString());
        }
        JsonArray ingredients = new JsonArray();
        ingredients.add(object("tag", "dyenamicscompat:" + tag));
        ingredients.add(object("item", "dyenamics:" + v.color().id + "_dye"));
        // A tablecloth needs cloth, rather than conjuring fabric from a dye and a bare table.
        if (v.addon().equals("brewery") && v.family().equals("table")) ingredients.add(object("item", "brewery:patterned_carpet"));
        coloring(v.id().getPath() + "/dyeing", v.id(), ingredients);
        if (v.addon().equals("brewery") && v.family().equals("patterned_carpet")) {
            addTag(PaletteModule.id("tags/item/" + tag + ".json"), "brewery:patterned_carpet");
        }
        if (v.color() == DyenamicColor.PEACH) {
            for (var dye : net.minecraft.world.item.DyeColor.values()) {
                if (v.addon().equals("furniture") && !v.nativePattern().contains("%s") && dye != net.minecraft.world.item.DyeColor.WHITE) continue;
                ResourceLocation nativeItem = ResourceLocation.fromNamespaceAndPath(v.addon(), v.nativePattern().formatted(dye.getName()));
                if (v.addon().equals("brewery") && v.family().equals("patterned_carpet")) nativeItem = ResourceLocation.parse("brewery:patterned_carpet");
                if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(nativeItem)) continue;
                if (v.addon().equals("brewery") && v.family().equals("table")) continue;
                JsonArray reverse = new JsonArray();
                reverse.add(object("tag", "dyenamicscompat:" + tag + "_extra"));
                reverse.add(object("item", "minecraft:" + dye.getName() + "_dye"));
                coloring(PaletteModule.prefix(v.addon()) + "/" + v.family() + "/vanilla_" + dye.getName(), nativeItem, reverse);
            }
        }
        addTag(PaletteModule.id("tags/item/" + tag + "_extra.json"), v.id().toString());
        copyCrafting(v.addon(), v.source(), v.id(), v.color());
    }
    private void itemData(PaletteModule.ExtraItem item) {
        String tag = PaletteModule.prefix(item.addon()) + "/" + item.family();
        addTag(PaletteModule.id("tags/item/" + tag + ".json"), item.id().toString());
        addTag(PaletteModule.id("tags/item/" + tag + ".json"), item.source().toString());
        JsonArray ingredients = new JsonArray(); ingredients.add(object("tag", "dyenamicscompat:" + tag)); ingredients.add(object("item", "dyenamics:" + item.color().id + "_dye"));
        coloring(item.id().getPath() + "/dyeing", item.id(), ingredients);
        if (item.addon().equals("quark") && item.family().equals("shard")) {
            addTag(ResourceLocation.parse("quark:tags/item/shards.json"), item.id().toString());
            for (var dye : net.minecraft.world.item.DyeColor.values())
                addTag(PaletteModule.id("tags/item/" + tag + ".json"), "quark:" + dye.getName() + "_shard");
            addTag(PaletteModule.id("tags/item/" + tag + "_extra.json"), item.id().toString());
            if (item.color() == DyenamicColor.PEACH) for (var dye : net.minecraft.world.item.DyeColor.values()) {
                JsonArray reverse = new JsonArray(); reverse.add(object("tag", "dyenamicscompat:" + tag + "_extra")); reverse.add(object("item", "minecraft:" + dye.getName() + "_dye"));
                coloring(PaletteModule.prefix(item.addon()) + "/shard/vanilla_" + dye.getName(), ResourceLocation.parse("quark:" + dye.getName() + "_shard"), reverse);
            }
            JsonObject recycle = object("type", "minecraft:crafting_shaped"); recycle.add("pattern", GSON.toJsonTree(List.of("##", "##")));
            JsonObject key = new JsonObject(); key.add("#", object("item", item.id().toString())); recycle.add("key", key);
            recycle.add("result", object("id", "dyenamics:" + item.color().id + "_stained_glass"));
            json(item.id().withPath("recipe/" + item.id().getPath() + "/glass.json"), recycle);
        }
        copyCrafting(item.addon(), item.source(), item.id(), item.color());
    }
    private void copyCrafting(String addon, ResourceLocation source, ResourceLocation target, DyenamicColor color) {
        if (addon.equals("brewery") && source.getPath().equals("table")) return;
        if (addon.equals("furniture") && PaletteModule.VARIANTS.stream().anyMatch(v -> v.source().equals(source) && !v.nativePattern().contains("%s"))) return;
        Map<String, String> substitutions = replacements(addon, color);
        for (String path : list(addon)) {
            if (!path.startsWith("data/" + addon + "/recipe/") || !path.endsWith(".json")) continue;
            JsonObject data = read(addon, path).getAsJsonObject();
            // Native kiln recipes have no result field: they return the same pottery item with paint data.
            if (addon.equals("lilis_pottery") && data.get("type").getAsString().equals("lilis_pottery:firing")
                    && data.has("base") && data.getAsJsonObject("base").has("item")
                    && data.getAsJsonObject("base").get("item").getAsString().equals(source.toString())) {
                json(target.withPath("recipe/" + target.getPath() + "/upstream/" + path.substring(path.indexOf("/recipe/") + 8)), replaceIds(data, substitutions));
                continue;
            }
            JsonElement result = data.has("result") ? data.get("result") : data.get("outputItem");
            if (result == null || !result.isJsonObject() || !result.getAsJsonObject().has("id") || !(result.getAsJsonObject().get("id").getAsString().equals(source.toString()) || (source.toString().equals("brewery:patterned_carpet_block") && result.getAsJsonObject().get("id").getAsString().equals("brewery:patterned_carpet")))) continue;
            // Shapeless native recoloring recipes overlap the component-preserving recipes above.
            if (data.get("type").getAsString().equals("minecraft:crafting_shapeless")
                    && (!java.util.Set.of("supplementaries", "suppsquared", "quark").contains(addon)
                        || (data.has("ingredients") && data.get("ingredients").toString().contains(source.toString())))) continue;
            json(target.withPath("recipe/" + target.getPath() + "/upstream/" + path.substring(path.indexOf("/recipe/") + 8)), replaceIds(data, substitutions));
        }
    }
    private Map<String, String> replacements(String addon, DyenamicColor color) {
        Map<String, String> map = new HashMap<>();
        PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals(addon) && v.color() == color).forEach(v -> map.put(v.source().toString(), v.id().toString()));
        PaletteModule.EXTRA_ITEMS.stream().filter(v -> v.addon().equals(addon) && v.color() == color).forEach(v -> map.put(v.source().toString(), v.id().toString()));
        if (addon.equals("brewery")) map.put("brewery:patterned_carpet", PaletteModule.variant("brewery", "patterned_carpet", color).id().toString());
        map.put("minecraft:white_dye", "dyenamics:" + color.id + "_dye");
        map.put("minecraft:white_wool", "dyenamics:" + color.id + "_wool");
        if (java.util.Set.of("supplementaries", "suppsquared", "quark").contains(addon)) {
            for (String material : List.of("terracotta", "stained_glass", "stained_glass_pane", "candle", "banner", "carpet"))
                map.put("minecraft:white_" + material, "dyenamics:" + color.id + "_" + material);
        }
        if (addon.equals("brewery")) {
            map.remove("minecraft:white_wool");
            map.put("minecraft:blue_wool", "dyenamics:" + color.id + "_wool");
        }
        map.put("c:dyes/white", "c:dyes/" + color.id);
        return map;
    }
    private void addUpstreamTags() {
        for (String addon : PaletteModule.VARIANTS.stream().map(PaletteModule.Variant::addon).distinct().toList()) {
            for (String path : list(addon)) {
                if (!path.startsWith("data/") || !path.contains("/tags/") || !path.endsWith(".json")) continue;
                JsonObject data = read(addon, path).getAsJsonObject();
                if (!data.has("values")) continue;
                String namespace = path.split("/")[1];
                ResourceLocation target = ResourceLocation.fromNamespaceAndPath(namespace, path.substring(path.indexOf("/tags/") + 1));
                for (DyenamicColor color : DyenamicColor.values()) {
                    Map<String, String> map = replacements(addon, color);
                    for (JsonElement entry : data.getAsJsonArray("values")) {
                        String id = entry.isJsonPrimitive() ? entry.getAsString() : entry.getAsJsonObject().get("id").getAsString();
                        if (map.containsKey(id) && id.startsWith(addon + ":")) addTag(target, map.get(id));
                    }
                }
            }
        }
    }
    private void bridgeRecipe(String addon, String name) {
        String path="recipe/" + name + ".json";
        JsonObject recipe=read(addon,"data/" + addon + "/" + path).getAsJsonObject();
        recipe.addProperty("type", "dyenamicscompat:" + addon + "/" + name);
        json(ResourceLocation.fromNamespaceAndPath(addon,path),recipe);
    }
    private void coloring(String path, ResourceLocation result, JsonArray ingredients) {
        JsonObject data = object("type", "dyenamicscompat:letsdo_coloring"); data.addProperty("category", "misc"); data.add("ingredients", ingredients);
        JsonObject output = object("id", result.toString()); output.addProperty("count", 1); data.add("result", output);
        json(PaletteModule.id("recipe/" + path + ".json"), data);
    }
    private void addTag(ResourceLocation path, String value) {
        JsonArray values = tags.computeIfAbsent(path, key -> new JsonArray());
        JsonObject entry = object("id", value); entry.addProperty("required", false);
        if (!values.contains(entry)) values.add(entry);
    }
    private static JsonObject object(String key, String value) { JsonObject object = new JsonObject(); object.addProperty(key, value); return object; }
    private static JsonElement replaceIds(JsonElement value, Map<String, String> replacements) {
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) return new JsonPrimitive(replacements.getOrDefault(value.getAsString(), value.getAsString()));
        if (value.isJsonArray()) { JsonArray out = new JsonArray(); value.getAsJsonArray().forEach(e -> out.add(replaceIds(e, replacements))); return out; }
        if (value.isJsonObject()) { JsonObject out = new JsonObject(); value.getAsJsonObject().entrySet().forEach(e -> out.add(e.getKey(), e.getKey().equals("type") ? e.getValue().deepCopy() : replaceIds(e.getValue(), replacements))); return out; }
        return value.deepCopy();
    }
    private static String title(String value) { return Arrays.stream(value.replace('_', ' ').split(" ")).map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1)).collect(java.util.stream.Collectors.joining(" ")); }
    private void json(ResourceLocation path, JsonElement value) { byte[] bytes = GSON.toJson(value).getBytes(StandardCharsets.UTF_8); resources.put(path, () -> new ByteArrayInputStream(bytes)); }
    private JsonElement read(String addon, String path) {
        try { String key = addon + ":" + path;
            JsonElement value = sourceJson.get(key);
            if (value == null) { value = JsonParser.parseString(new String(bytes(addon, path), StandardCharsets.UTF_8)); sourceJson.put(key, value); }
            return value.deepCopy(); }
        catch (IOException e) { throw new IllegalStateException("Missing pinned palette resource: " + addon + ":" + path, e); }
    }
    private static byte[] bytes(String addon, String path) throws IOException { return Files.readAllBytes(ModList.get().getModFileById(addon).getFile().findResource(path)); }
    private static boolean exists(String addon, String path) { return Files.isRegularFile(ModList.get().getModFileById(addon).getFile().findResource(path)); }
    private List<String> list(String addon) {
        return files.computeIfAbsent(addon, key -> {
            Path root = ModList.get().getModFileById(addon).getFile().findResource("");
            try (var paths = Files.walk(root.resolve("data"))) { return paths.filter(Files::isRegularFile).map(p -> root.relativize(p).toString().replace('\\', '/')).toList(); }
            catch (IOException e) { throw new IllegalStateException("Could not inspect palette data for " + addon, e); }
        });
    }
    @Override public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length != 1 || !path[0].equals("pack.mcmeta")) return null;
        return () -> new ByteArrayInputStream(("{\"pack\":{\"pack_format\":" + (type == PackType.CLIENT_RESOURCES ? 34 : 48) + ",\"description\":\"Dyenamics Compat: Let's Do\"}}").getBytes(StandardCharsets.UTF_8));
    }
    @Override public IoSupplier<InputStream> getResource(PackType requested, ResourceLocation id) { return requested == type ? resources.get(id) : null; }
    @Override public void listResources(PackType requested, String namespace, String prefix, ResourceOutput output) {
        if (requested == type) resources.forEach((id, supplier) -> { if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix)) output.accept(id, supplier); });
    }
    @Override public Set<String> getNamespaces(PackType requested) {
        return requested == type ? resources.keySet().stream().map(ResourceLocation::getNamespace).collect(java.util.stream.Collectors.toSet()) : Set.of();
    }
    @Override public void close() { resources.clear(); files.clear(); tags.clear(); sourceJson.clear(); }
}
