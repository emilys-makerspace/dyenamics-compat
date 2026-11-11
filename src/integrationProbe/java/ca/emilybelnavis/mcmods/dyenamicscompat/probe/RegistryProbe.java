package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PalettePack;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Release-excluded, actual mod-loader checks; exits before a server/world is started. */
@Mod("dyenamicscompat_probe")
public final class RegistryProbe {
    public RegistryProbe(IEventBus bus) { bus.addListener(this::complete); }
    private void complete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            JsonObject report = new JsonObject();
            try {
                int blocks = 0, states = 0, entities = 0;
                Map<String, Integer> families = new java.util.HashMap<>(Map.of("supplementaries", 7, "suppsquared", 2, "quark", 7, "brewery", 3, "camping", 2, "furniture", 5, "hearth_and_timber", 1, "lilis_pottery", 15));
                if (ModList.get().isLoaded("furniture") && ModList.get().isLoaded("dyenamics")) families.put("furniture", FurnitureChecks.expectedFamilies());
                int expected = ModList.get().isLoaded("dyenamics") ? families.entrySet().stream().filter(e -> ModList.get().isLoaded(e.getKey())).mapToInt(e -> e.getValue() * 18).sum() : 0;
                check(PaletteModule.VARIANTS.size() == expected, "Optional-mod gate registered an incorrect number of variants");
                var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                for (var variant : PaletteModule.VARIANTS) {
                    blocks++;
                    check(BuiltInRegistries.BLOCK.getKey(variant.block().get()).equals(variant.id()), "Registry identity mismatch");
                    for (var state : variant.block().get().getStateDefinition().getPossibleStates()) {
                        states++;
                        if (!(state.getBlock() instanceof EntityBlock entityBlock)) continue;
                        BlockEntity entity = entityBlock.newBlockEntity(BlockPos.ZERO, state);
                        if (entity == null) continue;
                        entities++;
                        check(entity.getType().isValid(state), "Invalid block-entity state: " + variant.id());
                        var saved = entity.saveWithFullMetadata(registries);
                        BlockEntity restored = BlockEntity.loadStatic(BlockPos.ZERO, state, saved, registries);
                        check(restored != null && restored.getType() == entity.getType(), "Could not restore " + variant.id());
                        check(restored.saveWithFullMetadata(registries).equals(saved), "Block entity changed during save/reload: " + variant.id());
                    }
                }
                report.addProperty("blocks", blocks); report.addProperty("states", states); report.addProperty("blockEntityRoundTrips", entities);
                if (expected > 0 && ModList.get().isLoaded("camping")) CampingChecks.verify();
                if (expected > 0 && ModList.get().isLoaded("lilis_pottery")) PotteryChecks.verify();
                report.add("resources", resources());
                report.addProperty("status", "passed");
                System.out.println("DYENAMICS_COMPAT_PROBE_PASS " + report);
                write(report);
                if (Boolean.getBoolean("dyenamicscompat.probeExit")) System.exit(0);
            } catch (Throwable failure) {
                report.addProperty("status", "failed"); report.addProperty("error", failure.toString());
                write(report); failure.printStackTrace(); System.exit(1);
            }
        });
    }
    private static JsonObject resources() throws Exception {
        JsonObject counts = new JsonObject();
        if (PaletteModule.VARIANTS.isEmpty()) return counts;
        for (PackType type : PackType.values()) {
            List<Pack> packs = new ArrayList<>();
            PalettePack.findPacks(new AddPackFindersEvent(type, source -> source.loadPacks(packs::add), false));
            for (Pack pack : packs) try (PackResources resources = pack.open()) {
                Map<ResourceLocation, net.minecraft.server.packs.resources.IoSupplier<InputStream>> all = new LinkedHashMap<>();
                for (String namespace : resources.getNamespaces(type)) resources.listResources(type, namespace, "", all::put);
                int images = 0;
                for (var entry : all.entrySet()) {
                    String path = entry.getKey().getPath();
                    try (InputStream input = entry.getValue().get()) {
                        if (path.endsWith(".png")) {
                            check(path.startsWith("textures/block/"), "Texture outside the block atlas: " + entry.getKey());
                            check(javax.imageio.ImageIO.read(input) != null, "Invalid generated PNG: " + entry.getKey());
                            images++;
                        } else if (path.endsWith(".json")) {
                            JsonElement json = JsonParser.parseReader(new InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
                            if (path.startsWith("models/") || path.startsWith("blockstates/")) checkReferences(json, resources, type);
                            if (path.startsWith("recipe/")) {
                                var codec = net.minecraft.world.item.crafting.Recipe.CODEC;
                                var ops = registries().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
                                var decoded = codec.parse(ops, json);
                                check(decoded.result().isPresent(), "Invalid generated recipe " + entry.getKey() + ": " + decoded.error());
                            }
                        }
                    }
                }
                if (type == PackType.CLIENT_RESOURCES) {
                    int patterned = 0;
                    for (var variant : PaletteModule.VARIANTS) {
                        if (!variant.addon().equals("furniture") || variant.nativePattern().contains("%s") || variant.color() != ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor.PEACH) continue;
                        String prefix = "textures/block/" + variant.id().getPath() + "/derived/";
                        var peers = all.entrySet().stream().filter(e -> e.getKey().getPath().startsWith(prefix) && e.getKey().getPath().endsWith(".png")).toList();
                        check(!peers.isEmpty(), "Patterned family has no recolored texture: " + variant.family());
                        boolean distinct = false;
                        for (var texture : peers) {
                            ResourceLocation mint = texture.getKey().withPath(texture.getKey().getPath().replace("/peach_" + variant.family() + "/", "/mint_" + variant.family() + "/"));
                            check(all.containsKey(mint), "Missing mint counterpart for " + texture.getKey());
                            try (InputStream peachInput = texture.getValue().get(); InputStream mintInput = all.get(mint).get()) {
                                var peach = javax.imageio.ImageIO.read(peachInput);
                                var green = javax.imageio.ImageIO.read(mintInput);
                                for (int y = 0; y < peach.getHeight(); y++) for (int x = 0; x < peach.getWidth(); x++) {
                                    check((peach.getRGB(x, y) >>> 24) == (green.getRGB(x, y) >>> 24), "Pattern recoloring changed transparency");
                                    if (peach.getRGB(x, y) != green.getRGB(x, y)) distinct = true;
                                }
                            }
                        }
                        check(distinct, "Different dyes produced identical patterned textures: " + variant.family());
                        patterned++;
                    }
                    counts.addProperty("patternedTextureFamilies", patterned);
                }
                counts.addProperty(type.getDirectory(), all.size()); counts.addProperty(type.getDirectory() + "Images", images);
            }
        }
        return counts;
    }
    private static RegistryAccess.Frozen registries() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
    private static void checkReferences(JsonElement json, PackResources resources, PackType type) {
        if (json.isJsonArray()) { json.getAsJsonArray().forEach(e -> checkReferences(e, resources, type)); return; }
        if (!json.isJsonObject()) return;
        for (var e : json.getAsJsonObject().entrySet()) {
            if ((e.getKey().equals("model") || e.getKey().equals("parent")) && e.getValue().isJsonPrimitive()) {
                ResourceLocation id = ResourceLocation.parse(e.getValue().getAsString());
                if (id.getNamespace().equals("dyenamicscompat")) check(resources.getResource(type, id.withPath("models/" + id.getPath() + ".json")) != null, "Missing derived model " + id);
            }
            if (e.getKey().equals("textures") && e.getValue().isJsonObject()) {
                for (var texture : e.getValue().getAsJsonObject().entrySet()) {
                    String value = texture.getValue().getAsString();
                    if (!value.startsWith("dyenamicscompat:")) continue;
                    ResourceLocation id = ResourceLocation.parse(value);
                    check(resources.getResource(type, id.withPath("textures/" + id.getPath() + ".png")) != null, "Missing derived texture " + id);
                }
            }
            checkReferences(e.getValue(), resources, type);
        }
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void write(JsonObject report) {
        try { Path path = Path.of(System.getProperty("dyenamicscompat.probeReport")); Files.createDirectories(path.getParent()); Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n"); }
        catch (IOException failure) { throw new UncheckedIOException(failure); }
    }
}
