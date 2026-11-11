package ca.emilybelnavis.mcmods.dyenamicscompat.create.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import ca.emilybelnavis.mcmods.dyenamicscompat.create.CompatColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.create.CreateCompat;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/** Generates textures locally from the user's installed Create; no Create artwork is shipped. */
public final class PalettePack extends AbstractPackResources {
    private record Texture(String white, String red, int rgb) {}
    private final Map<ResourceLocation, Texture> textures = new HashMap<>();
    private final Map<ResourceLocation, byte[]> cache = new HashMap<>();
    private static final String[] PATTERNS = {
        "seat/top_%s", "seat/side_%s", "sail/canvas_%s", "valve_handle/valve_handle_%s", "toolbox/%s"
    };

    private PalettePack(PackLocationInfo location) {
        super(location);
        for (CompatColor color : CompatColor.values()) {
            for (String pattern : PATTERNS) {
                textures.put(CreateCompat.id("textures/block/" + pattern.formatted(color.id) + ".png"),
                        new Texture(pattern.formatted("white"), pattern.formatted("red"), color.rgb));
            }
        }
    }

    public static void findPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        var location = new PackLocationInfo("dyenamicscompat_palette", Component.literal("Dyenamics Compat palette"),
                PackSource.BUILT_IN, Optional.empty());
        var supplier = new Pack.ResourcesSupplier() {
            @Override public PalettePack openPrimary(PackLocationInfo info) { return new PalettePack(info); }
            @Override public PalettePack openFull(PackLocationInfo info, Pack.Metadata metadata) { return new PalettePack(info); }
        };
        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
        if (pack == null) throw new IllegalStateException("Could not create Dyenamics Compat palette pack");
        event.addRepositorySource(consumer -> consumer.accept(pack.hidden()));
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length != 1 || !path[0].equals("pack.mcmeta")) return null;
        return () -> new ByteArrayInputStream("{\"pack\":{\"pack_format\":34,\"description\":\"Dyenamics Compat palette\"}}".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.CLIENT_RESOURCES || !textures.containsKey(id)) return null;
        return () -> {
            synchronized (cache) {
                byte[] png = cache.get(id);
                if (png == null) {
                    Texture texture = textures.get(id);
                    var file = ModList.get().getModFileById("create").getFile();
                    png = PaletteTextures.recolor(
                            Files.readAllBytes(file.findResource("assets/create/textures/block/" + texture.white() + ".png")),
                            Files.readAllBytes(file.findResource("assets/create/textures/block/" + texture.red() + ".png")), texture.rgb());
                    cache.put(id, png);
                }
                return new ByteArrayInputStream(png);
            }
        };
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type == PackType.CLIENT_RESOURCES && namespace.equals(DyenamicsCompat.MOD_ID)) {
            textures.keySet().stream().filter(id -> id.getPath().startsWith(prefix))
                    .forEach(id -> output.accept(id, getResource(type, id)));
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(DyenamicsCompat.MOD_ID) : Set.of();
    }

    @Override
    public void close() { synchronized (cache) { cache.clear(); } }
}
