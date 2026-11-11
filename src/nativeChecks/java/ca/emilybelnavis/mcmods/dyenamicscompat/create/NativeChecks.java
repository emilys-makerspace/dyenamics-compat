package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import ca.emilybelnavis.mcmods.dyenamicscompat.create.client.PaletteTextures;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Standalone checks use the actual mapped Minecraft/Create classes, without starting a world. */
public final class NativeChecks {
    public static void main(String[] args) throws Exception {
        if (net.neoforged.fml.loading.LoadingModList.get() == null) {
            net.neoforged.fml.loading.LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        toolboxComponents();
        paletteTextures();
        optionalClassLoading();
        System.out.println("PASS: toolbox components, 90 palette textures, and entry-point construction for none/Create-only/Dyenamics-only profiles");
    }

    private static void toolboxComponents() {
        var blocks = (net.minecraft.core.MappedRegistry<net.minecraft.world.level.block.Block>) BuiltInRegistries.BLOCK;
        var items = (net.minecraft.core.MappedRegistry<Item>) BuiltInRegistries.ITEM;
        blocks.unfreeze();
        items.unfreeze();
        BlockItem item;
        try {
            var block = Registry.register(blocks, ResourceLocation.parse("compat_test:toolbox"),
                    new ToolboxBlock(BlockBehaviour.Properties.of(), DyeColor.WHITE));
            item = Registry.register(items, ResourceLocation.parse("compat_test:toolbox"), new BlockItem(block, new Item.Properties()));
        } finally {
            blocks.freeze();
            items.freeze();
        }
        var original = new ItemStack(item);
        original.set(DataComponents.CUSTOM_NAME, Component.literal("My supplies"));
        original.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 37))));
        original.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        // Create's inventory/UUID components are copied by this same component-patch operation.
        var ingredients = NonNullList.of(Ingredient.EMPTY, Ingredient.of(item), Ingredient.of(Items.RED_DYE));
        var recipe = new ToolboxColoringRecipe(new ShapelessRecipe("", CraftingBookCategory.MISC,
                new ItemStack(Items.CHEST), ingredients));
        var input = CraftingInput.of(2, 1, List.of(original, new ItemStack(Items.RED_DYE)));
        var result = recipe.assemble(input, null);
        check(result.is(Items.CHEST), "Recoloring changed the intended output item");
        check(result.getComponentsPatch().equals(original.getComponentsPatch()), "Toolbox components were lost");
        check(original.getCount() == 1 && original.getHoverName().getString().equals("My supplies"), "Recoloring mutated the input");
    }

    private static void paletteTextures() throws Exception {
        String[] patterns = {"seat/top_%s", "seat/side_%s", "sail/canvas_%s", "valve_handle/valve_handle_%s", "toolbox/%s"};
        try (ZipFile create = new ZipFile("libs/create-1.21.1-6.0.10.jar")) {
            for (String pattern : patterns) {
                byte[] whitePng = create.getInputStream(create.getEntry("assets/create/textures/block/" + pattern.formatted("white") + ".png")).readAllBytes();
                byte[] redPng = create.getInputStream(create.getEntry("assets/create/textures/block/" + pattern.formatted("red") + ".png")).readAllBytes();
                BufferedImage white = ImageIO.read(new ByteArrayInputStream(whitePng));
                BufferedImage red = ImageIO.read(new ByteArrayInputStream(redPng));
                for (CompatColor color : CompatColor.values()) {
                    BufferedImage output = ImageIO.read(new ByteArrayInputStream(PaletteTextures.recolor(whitePng, redPng, color.rgb)));
                    boolean changed = false;
                    for (int y = 0; y < white.getHeight(); y++) {
                        for (int x = 0; x < white.getWidth(); x++) {
                            int before = white.getRGB(x, y), after = output.getRGB(x, y);
                            check((before >>> 24) == (after >>> 24), "Texture transparency changed");
                            if (before == red.getRGB(x, y)) check(before == after, "Uncolored texture detail changed");
                            if (before != after) changed = true;
                        }
                    }
                    check(changed, "Palette did not change " + pattern + " for " + color.id);
                }
            }
        }
    }

    private static void optionalClassLoading() throws Exception {
        // Child-first only for our mod; explicitly hide all compatibility APIs.
        URL production = ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{production}, NativeChecks.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("com.simibubi.create.") || name.startsWith("cy.jdkdigital.dyenamics.")) throw new ClassNotFoundException(name);
                if (name.startsWith("ca.emilybelnavis.mcmods.dyenamicscompat.")) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> type = findLoadedClass(name);
                        if (type == null) type = findClass(name);
                        if (resolve) resolveClass(type);
                        return type;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            Class<?> entry = Class.forName("ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat", true, loader);
            check(entry.getDeclaredConstructors().length == 1, "Entry point failed to link with compatibility APIs absent");
            var list = net.neoforged.fml.ModList.of(List.of(), List.of());
            var indexed = net.neoforged.fml.ModList.class.getDeclaredField("indexedMods");
            indexed.setAccessible(true);
            for (String profile : List.of("none", "create", "dyenamics")) {
                var present = new java.util.HashMap<String, net.neoforged.fml.ModContainer>();
                if (!profile.equals("none")) present.put(profile, null);
                indexed.set(list, present);
                // A no-op constructor must not touch the event bus or load any compatibility class.
                entry.getConstructor(net.neoforged.bus.api.IEventBus.class).newInstance((Object) null);
            }

        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
