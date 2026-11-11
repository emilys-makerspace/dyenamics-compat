package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Common registration contains no references to optional mod APIs. */
public final class LetsDoModule {
    public record Variant(String addon, String family, DyenamicColor color, ResourceLocation source,
            String nativePattern, DeferredBlock<? extends Block> block, Supplier<? extends Item> item, boolean tinted) {
        public ResourceLocation id() { return block.getId(); }
    }
    public record ExtraItem(String addon, String family, DyenamicColor color, ResourceLocation source,
            Supplier<? extends Item> item, ResourceLocation id) {}
    public static final List<Variant> VARIANTS = new ArrayList<>();
    public static final List<ExtraItem> EXTRA_ITEMS = new ArrayList<>();
    private final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(DyenamicsCompat.MOD_ID);
    private final DeferredRegister.Items items = DeferredRegister.createItems(DyenamicsCompat.MOD_ID);
    private final String addon;
    private final String tab;

    public LetsDoModule(String addon, String tab) { this.addon = addon; this.tab = tab; }

    public void family(String family, String nativePattern, String referenceColor, boolean tinted,
            BiFunction<DyenamicColor, BlockBehaviour.Properties, ? extends Block> factory) {
        family(family, nativePattern, referenceColor, tinted, factory, block -> new BlockItem(block, new Item.Properties()));
    }

    public void family(String family, String nativePattern, String referenceColor, boolean tinted,
            BiFunction<DyenamicColor, BlockBehaviour.Properties, ? extends Block> factory, Function<Block, Item> itemFactory) {
        ResourceLocation source = ResourceLocation.fromNamespaceAndPath(addon, nativePattern.formatted(referenceColor));
        for (DyenamicColor color : DyenamicColor.values()) {
            String path = "letsdo/" + addon + "/" + color.id + "_" + family;
            DeferredBlock<Block> block = blocks.register(path, () -> {
                Block original = BuiltInRegistries.BLOCK.get(source);
                if (original == net.minecraft.world.level.block.Blocks.AIR) throw new IllegalStateException("Missing pinned upstream block: " + source);
                // Legacy copy deliberately excludes the original loot-table ID, so our entries have their own drops.
                return factory.apply(color, BlockBehaviour.Properties.ofLegacyCopy(original));
            });
            Supplier<? extends Item> item = itemFactory == null ? null : items.register(path, () -> itemFactory.apply(block.get()));
            VARIANTS.add(new Variant(addon, family, color, source, nativePattern, block, item, tinted));
        }
    }

    public void itemFamily(String family, String nativePattern) {
        for (DyenamicColor color : DyenamicColor.values()) {
            String path = "letsdo/" + addon + "/" + color.id + "_" + family;
            var item = items.register(path, () -> new Item(new Item.Properties()));
            EXTRA_ITEMS.add(new ExtraItem(addon, family, color, ResourceLocation.fromNamespaceAndPath(addon, nativePattern.formatted("white")), item, id(path)));
        }
    }

    public void register(IEventBus bus) {
        blocks.register(bus);
        items.register(bus);
        bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST, this::creativeTab);
    }

    private void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath(addon, tab))) return;
        // Anchor each family to the last native counterpart in the existing tab order.
        var groups = new java.util.LinkedHashMap<String, List<net.minecraft.world.item.ItemStack>>();
        var representatives = new java.util.LinkedHashMap<String, ResourceLocation>();
        var patterns = new java.util.LinkedHashMap<String, String>();
        VARIANTS.stream().filter(v -> v.addon().equals(addon) && v.item() != null).forEach(v -> {
            groups.computeIfAbsent(v.family(), key -> new ArrayList<>()).add(new net.minecraft.world.item.ItemStack(v.item().get()));
            representatives.putIfAbsent(v.family(), v.source());
            patterns.putIfAbsent(v.family(), v.nativePattern());
        });
        EXTRA_ITEMS.stream().filter(v -> v.addon().equals(addon)).forEach(v -> {
            groups.computeIfAbsent(v.family(), key -> new ArrayList<>()).add(new net.minecraft.world.item.ItemStack(v.item().get()));
            representatives.putIfAbsent(v.family(), v.source());
            patterns.putIfAbsent(v.family(), v.source().getPath().replace("white", "%s"));
        });
        var parents = List.copyOf(event.getParentEntries());
        var search = List.copyOf(event.getSearchEntries());
        groups.forEach((family, additions) -> {
            ResourceLocation source = representatives.get(family);
            String pattern = patterns.get(family);
            java.util.function.Predicate<net.minecraft.world.item.ItemStack> matches = stack -> {
                ResourceLocation id = stack.getItem() instanceof BlockItem blockItem
                        ? BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()) : BuiltInRegistries.ITEM.getKey(stack.getItem());
                return id.equals(source) || (id.getNamespace().equals(addon)
                        && java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).anyMatch(color -> pattern.formatted(color.getName()).equals(id.getPath())));
            };
            insertFamily(event, parents, matches, additions, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            insertFamily(event, search, matches, additions, net.minecraft.world.item.CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        });
    }

    private static void insertFamily(BuildCreativeModeTabContentsEvent event,
            List<net.minecraft.world.item.ItemStack> originals, java.util.function.Predicate<net.minecraft.world.item.ItemStack> matches,
            List<net.minecraft.world.item.ItemStack> additions, net.minecraft.world.item.CreativeModeTab.TabVisibility visibility) {
        net.minecraft.world.item.ItemStack anchor = originals.stream().filter(matches).reduce((first, last) -> last).orElse(null);
        for (var addition : additions) {
            if (anchor == null) event.accept(addition, visibility);
            else event.insertAfter(anchor, addition, visibility);
            anchor = addition;
        }
    }

    public static Variant variant(Block block) {
        return VARIANTS.stream().filter(v -> v.block().get() == block).findFirst().orElse(null);
    }
    public static Variant variant(String addon, String family, DyenamicColor color) {
        return VARIANTS.stream().filter(v -> v.addon().equals(addon) && v.family().equals(family) && v.color() == color).findFirst().orElseThrow();
    }
    public static Block[] blocks(String addon, String... families) {
        return VARIANTS.stream().filter(v -> v.addon().equals(addon) && List.of(families).contains(v.family())).map(v -> v.block().get()).toArray(Block[]::new);
    }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(DyenamicsCompat.MOD_ID, path); }
}
