package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingRecolor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.satisfy.camping.core.world.block.TentBlock;
import net.satisfy.camping.core.world.block.entity.TentBlockEntity;
import net.satisfy.lilis_pottery.core.block.entity.AbstractStorageBlockEntity;

@GameTestHolder("dyenamicscompat_probe")
@PrefixGameTestTemplate(false)
public final class LetsDoWorldTests {
    private static Block nativeBlock(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)); }
    private static ItemStack dye(String id) { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)), 8); }
    private static Block block(String addon, String family, DyenamicColor color) { return PaletteModule.variant(addon, family, color).block().get(); }
    private static Player player(GameTestHelper helper) { return helper.makeMockPlayer(GameType.SURVIVAL); }
    private static void floor(GameTestHelper helper) { for (int x = 1; x < 8; x++) for (int z = 1; z < 8; z++) helper.setBlock(x, 0, z, Blocks.STONE); }
    private static void click(GameTestHelper helper, Player player, BlockPos absolute, ItemStack held) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        var event = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, absolute,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        NeoForge.EVENT_BUS.post(event);
        helper.assertTrue(event.isCanceled(), "Compatibility dye/cloth interaction was not handled");
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void tentKeepsAllPartsAndBag(GameTestHelper helper) {
        floor(helper);
        Player player = player(helper);
        BlockPos main = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = nativeBlock("camping:tent_white").defaultBlockState().setValue(TentBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(main, state, 3);
        state.getBlock().setPlacedBy(helper.getLevel(), main, state, player, new ItemStack(state.getBlock()));
        var bag = new ItemStack(block("camping", "sleeping_bag", DyenamicColor.WINE));
        ((TentBlockEntity)helper.getLevel().getBlockEntity(main)).setBag(bag);
        for (String color : new String[]{"dyenamics:mint_dye", "dyenamics:lavender_dye", "minecraft:red_dye"}) {
            click(helper, player, main.above(), dye(color));
            Block expected = color.equals("minecraft:red_dye") ? nativeBlock("camping:tent_red") : block("camping", "tent", color.contains("mint") ? DyenamicColor.MINT : DyenamicColor.LAVENDER);
            var positions = CampingRecolor.positions(helper.getLevel(), main, helper.getLevel().getBlockState(main));
            helper.assertTrue(positions.size() == 8 && positions.stream().allMatch(p -> helper.getLevel().getBlockState(p).is(expected)), "Tent recoloring broke or mismatched its eight parts");
            helper.assertTrue(ItemStack.isSameItemSameComponents(((TentBlockEntity)helper.getLevel().getBlockEntity(main)).getBag(), bag), "Tent lost its stored sleeping bag");
        }
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void sleepingBagKeepsBothParts(GameTestHelper helper) {
        floor(helper);
        Player player = player(helper);
        BlockPos foot = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockState state = nativeBlock("camping:sleeping_bag_white").defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(foot, state, 3);
        state.getBlock().setPlacedBy(helper.getLevel(), foot, state, player, new ItemStack(state.getBlock()));
        click(helper, player, foot.north(), dye("dyenamics:peach_dye"));
        helper.assertTrue(helper.getLevel().getBlockState(foot).is(block("camping", "sleeping_bag", DyenamicColor.PEACH)) && helper.getLevel().getBlockState(foot.north()).is(block("camping", "sleeping_bag", DyenamicColor.PEACH)), "Sleeping bag lost a part while recoloring");
        click(helper, player, foot, dye("minecraft:blue_dye"));
        helper.assertTrue(helper.getLevel().getBlockState(foot).is(nativeBlock("camping:sleeping_bag_blue")) && helper.getLevel().getBlockState(foot.north()).is(nativeBlock("camping:sleeping_bag_blue")), "Sleeping bag failed to return to a native color");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void potteryKeepsContentsWithoutDrops(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.getLevel().setBlock(pos, nativeBlock("lilis_pottery:white_planter").defaultBlockState(), 3);
        var pot = (AbstractStorageBlockEntity)helper.getLevel().getBlockEntity(pos);
        pot.setStack(0, new ItemStack(Items.DIAMOND, 37)); pot.setSideColorRgb(0x123456); pot.setGlazed(true); pot.setGlazeStrength(1.75F);
        Player player = player(helper);
        for (String color : new String[]{"dyenamics:rose_dye", "dyenamics:navy_dye", "minecraft:blue_dye"}) {
            click(helper, player, pos, dye(color));
            var after = (AbstractStorageBlockEntity)helper.getLevel().getBlockEntity(pos);
            helper.assertTrue(after.getInventory().getFirst().is(Items.DIAMOND) && after.getInventory().getFirst().getCount() == 37, "Recoloring lost planter inventory");
            helper.assertTrue(after.getSideColorRgb() == 0x123456 && after.isGlazed() && after.getGlazeStrength() == 1.75F, "Recoloring lost pottery glaze/paint");
        }
        helper.assertEntityNotPresent(EntityType.ITEM);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void breweryReturnsCorrectCloth(GameTestHelper helper) {
        Player player = player(helper);
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.getLevel().setBlock(pos, nativeBlock("brewery:table").defaultBlockState(), 3);
        var carpet = new ItemStack(block("brewery", "patterned_carpet", DyenamicColor.AQUAMARINE));
        click(helper, player, pos, carpet.copy());
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(block("brewery", "table", DyenamicColor.AQUAMARINE)), "Extra-color tablecloth was not attached");
        helper.getLevel().getBlockState(pos).useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(nativeBlock("brewery:table")), "Removing the cloth did not restore the native table");
        helper.assertTrue(player.getInventory().contains(carpet), "Removing the cloth returned a vanilla-colored carpet");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void craftingRetainsPotteryComponents(GameTestHelper helper) {
        ItemStack original = new ItemStack(nativeBlock("lilis_pottery:white_urn"));
        CompoundTag custom = new CompoundTag(); custom.putInt("sideColorRgb", 0x123456); custom.putBoolean("glazed", true);
        original.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
        original.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Keepsake"));
        var input = CraftingInput.of(2, 1, java.util.List.of(original, dye("dyenamics:mint_dye")));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
        var result = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(block("lilis_pottery", "urn", DyenamicColor.MINT).asItem()), "Generated coloring recipe has the wrong result");
        helper.assertTrue(result.getComponentsPatch().equals(original.getComponentsPatch()), "Generated recipe dropped pottery components");
        var table = new ItemStack(nativeBlock("brewery:table"));
        table.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Dining table"));
        var tableInput = CraftingInput.of(3, 1, java.util.List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("brewery:patterned_carpet"))), dye("dyenamics:mint_dye"), table));
        var tableRecipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, tableInput, helper.getLevel()).orElseThrow();
        var coloredTable = tableRecipe.value().assemble(tableInput, helper.getLevel().registryAccess());
        helper.assertTrue(coloredTable.getComponentsPatch().equals(table.getComponentsPatch()), "Tablecloth crafting copied carpet components instead of the table's components");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void furnitureKeepsRedstoneAndLampItems(GameTestHelper helper) {
        floor(helper);
        var curtain = block("furniture", "curtain", DyenamicColor.MINT);
        var pos = new BlockPos(4, 1, 4);
        helper.setBlock(pos, curtain);
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(helper.getBlockState(pos).getValue(BlockStateProperties.OPEN) && helper.getBlockState(pos).getValue(BlockStateProperties.POWERED), "Colored curtain lost redstone behavior");
        helper.setBlock(pos.east(), Blocks.AIR);
        helper.assertFalse(helper.getBlockState(pos).getValue(BlockStateProperties.OPEN), "Colored curtain did not close after losing power");
        var lamp = block("furniture", "lamp", DyenamicColor.PEACH);
        var wall = block("furniture", "lamp_wall", DyenamicColor.PEACH);
        helper.assertTrue(lamp.asItem() != Items.AIR && lamp.asItem() == wall.asItem(), "Standing and wall lamps do not share their placement item");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void coloredMaterialsKeepNativeDrops(GameTestHelper helper) {
        var slab = block("lilis_pottery", "brick_slab", DyenamicColor.ICY_BLUE);
        var pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, slab.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.DOUBLE));
        helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
        helper.assertItemEntityCountIs(slab.asItem(), pos, 1.5, 2);
        var clay = block("lilis_pottery", "clay", DyenamicColor.NAVY);
        var clayPos = new BlockPos(6, 1, 6);
        helper.setBlock(clayPos, clay);
        helper.getLevel().destroyBlock(helper.absolutePos(clayPos), true);
        var balls = PaletteModule.EXTRA_ITEMS.stream().filter(v -> v.family().equals("clay_ball") && v.color() == DyenamicColor.NAVY).findFirst().orElseThrow().item().get();
        helper.assertItemEntityCountIs(balls, clayPos, 1.5, 4);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void variantsUseUpstreamCreativeTabs(GameTestHelper helper) {
        var tabs = java.util.Map.of("brewery", "brewery", "camping", "creative_tab", "furniture", "furniture", "hearth_and_timber", "hearth_and_timber", "lilis_pottery", "lilis_pottery");
        for (var entry : tabs.entrySet()) {
            var tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.fromNamespaceAndPath(entry.getKey(), entry.getValue()));
            helper.assertTrue(tab != null, "Upstream creative tab missing: " + entry.getKey());
            tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET, true, helper.getLevel().registryAccess()));
            PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals(entry.getKey()) && v.item() != null).forEach(v ->
                    helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(v.item().get())), "Variant missing from upstream creative tab: " + v.id()));
            PaletteModule.EXTRA_ITEMS.stream().filter(v -> v.addon().equals(entry.getKey())).forEach(v ->
                    helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(v.item().get())), "Material missing from upstream creative tab: " + v.id()));
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void coloredPotteryWorksInNativeKiln(GameTestHelper helper) {
        var item = new ItemStack(block("lilis_pottery", "urn", DyenamicColor.MINT));
        var input = new net.satisfy.lilis_pottery.core.recipe.FiringRecipe.FiringInput(item, new ItemStack(Items.RED_DYE));
        var type = net.satisfy.lilis_pottery.core.registry.RecipeRegistry.FIRING.get();
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(type, input, helper.getLevel()).orElseThrow();
        var fired = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(fired.is(item.getItem()), "The native kiln cannot paint an extra-color pottery item");
        helper.assertTrue(fired.has(DataComponents.CUSTOM_DATA) && fired.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("painted"), "The native kiln lost its paint operation");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void furnitureSideTabsKeepVariantsVisible(GameTestHelper helper) {
        var tab = net.satisfy.furniture.core.registry.TabRegistry.FURNITURE_TAB.get();
        tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET, true, helper.getLevel().registryAccess()));
        // The real client installs these two Foundation filters; reproduce those exact callbacks.
        net.satisfy.foundation.client.creative.CreativeSideTabs.register(net.satisfy.furniture.core.registry.TabRegistry.FURNITURE_TAB.getKey(),
                new net.satisfy.foundation.client.creative.CreativeSideTabs.SideTab(net.minecraft.network.chat.Component.literal("Furniture"), () -> new ItemStack(Items.OAK_PLANKS), net.satisfy.furniture.core.registry.TabRegistry::acceptMain),
                new net.satisfy.foundation.client.creative.CreativeSideTabs.SideTab(net.minecraft.network.chat.Component.literal("Compat Layer"), net.satisfy.furniture.core.registry.TabRegistry::compatIcon, net.satisfy.furniture.core.registry.TabRegistry::acceptCompat));
        var before = net.satisfy.foundation.client.creative.CreativeSideTabs.get(tab);
        for (String family : new String[]{"curtain", "lamp", "pouffe", "sofa"}) {
            helper.assertFalse(before.getFirst().contains(new ItemStack(block("furniture", family, DyenamicColor.MINT))), "Fixture must reproduce Foundation hiding our registered variants");
        }
        ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture.client.FurnitureSideTabs.install();
        var filters = net.satisfy.foundation.client.creative.CreativeSideTabs.get(tab);
        for (var variant : PaletteModule.VARIANTS) {
            if (!variant.addon().equals("furniture") || variant.item() == null) continue;
            ItemStack item = new ItemStack(variant.item().get());
            boolean patterned = !variant.nativePattern().contains("%s");
            helper.assertTrue(filters.get(patterned ? 1 : 0).contains(item), "Side-tab filter hid " + variant.id());
            helper.assertFalse(filters.get(patterned ? 0 : 1).contains(item), "Furniture item leaked into the wrong side tab: " + variant.id());
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(item.getItem())), "Filter contains an item absent from the parent tab: " + variant.id());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void creativeVariantsFollowOriginalFamilies(GameTestHelper helper) {
        for (var entry : java.util.Map.of("camping", "creative_tab", "hearth_and_timber", "hearth_and_timber", "lilis_pottery", "lilis_pottery", "furniture", "furniture").entrySet()) {
            var tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation.fromNamespaceAndPath(entry.getKey(), entry.getValue()));
            tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET, true, helper.getLevel().registryAccess()));
            var items = new java.util.ArrayList<>(tab.getDisplayItems());
            for (var variant : PaletteModule.VARIANTS) {
                if (!variant.addon().equals(entry.getKey()) || variant.item() == null || variant.color() != DyenamicColor.PEACH) continue;
                assertFamilyGroup(helper, items, variant.source(), color -> PaletteModule.variant(variant.addon(), variant.family(), color).item().get());
            }
            for (var extra : PaletteModule.EXTRA_ITEMS) {
                if (!extra.addon().equals(entry.getKey()) || extra.color() != DyenamicColor.PEACH) continue;
                assertFamilyGroup(helper, items, extra.source(), color -> PaletteModule.EXTRA_ITEMS.stream().filter(v -> v.addon().equals(extra.addon()) && v.family().equals(extra.family()) && v.color() == color).findFirst().orElseThrow().item().get());
            }
        }
        helper.succeed();
    }

    private static void assertFamilyGroup(GameTestHelper helper, java.util.List<ItemStack> items, ResourceLocation original, java.util.function.Function<DyenamicColor, net.minecraft.world.item.Item> variant) {
        int first = -1;
        for (int index = 0; index < items.size(); index++) if (items.get(index).is(variant.apply(DyenamicColor.PEACH))) { first = index; break; }
        helper.assertTrue(first > 0, "Missing grouped family for " + original);
        ResourceLocation preceding = BuiltInRegistries.ITEM.getKey(items.get(first - 1).getItem());
        // A native counterpart, rather than another compatibility family or unrelated tab item, must anchor the group.
        helper.assertTrue(preceding.getNamespace().equals(original.getNamespace()), "Family is detached from its native counterparts: " + original);
        String shape = original.getPath().replace("white", "<color>");
        boolean nativeCounterpart = preceding.getPath().equals(original.getPath()) || java.util.Arrays.stream(net.minecraft.world.item.DyeColor.values()).anyMatch(c -> shape.replace("<color>", c.getName()).equals(preceding.getPath()));
        helper.assertTrue(nativeCounterpart, "Wrong native anchor for " + original + ": " + preceding);
        for (int offset = 0; offset < DyenamicColor.values().length; offset++) {
            helper.assertTrue(items.get(first + offset).is(variant.apply(DyenamicColor.values()[offset])), "Color group is reversed or interleaved for " + original);
        }
    }

}
