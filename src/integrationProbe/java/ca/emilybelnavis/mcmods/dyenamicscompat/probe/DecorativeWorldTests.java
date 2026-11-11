package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("dyenamicscompat_decorative")
@PrefixGameTestTemplate(false)
public final class DecorativeWorldTests {
    private static Block nativeBlock(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)); }
    private static Block block(String addon,String family,DyenamicColor color) { return PaletteModule.variant(addon,family,color).block().get(); }
    private static void click(GameTestHelper h, Player player, BlockPos pos, String dye) {
        var held = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(dye)),8);
        player.setItemInHand(InteractionHand.MAIN_HAND,held);
        var event = new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        NeoForge.EVENT_BUS.post(event);
        h.assertTrue(event.isCanceled() && held.getCount()==7,"Dye was not consumed exactly once: " + dye);
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void storageKeepsContentsAndComponents(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(4,2,4));
        h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var entry : Map.of("present","supplementaries","trapped_present","supplementaries","sack","suppsquared").entrySet()) {
            String family=entry.getKey(), addon=entry.getValue();
            h.getLevel().setBlock(pos,nativeBlock(addon+":"+family+"_white").defaultBlockState(),3);
            if (family.equals("trapped_present")) h.getLevel().setBlockAndUpdate(pos,h.getLevel().getBlockState(pos).setValue(net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractPresentBlock.PACKED,true));
            var container = (Container)h.getLevel().getBlockEntity(pos);
            var stored = family.equals("trapped_present") ? new ItemStack(Items.TNT) : new ItemStack(Items.DIAMOND,17);
            container.setItem(0,stored.copy());
            for (String dye : List.of("dyenamics:mint_dye","dyenamics:wine_dye","minecraft:blue_dye")) {
                click(h,player,pos,dye);
                var expected = dye.contains("minecraft") ? nativeBlock(addon+":"+family+"_blue") : block(addon,family,dye.contains("mint")?DyenamicColor.MINT:DyenamicColor.WINE);
                h.assertTrue(h.getLevel().getBlockState(pos).is(expected),"Wrong storage color: "+family);
                container=(Container)h.getLevel().getBlockEntity(pos);
                h.assertTrue(ItemStack.matches(container.getItem(0),stored),"Inventory lost while recoloring "+family);
            }
            container.clearContent();
        }
        h.assertEntityNotPresent(EntityType.ITEM);
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void craftingKeepsSealedPresentData(GameTestHelper h) {
        for (String family : List.of("present","trapped_present")) {
            var source = new ItemStack(nativeBlock("supplementaries:"+family+"_white"));
            source.set(DataComponents.CUSTOM_NAME, Component.literal("For Emily"));
            source.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,7))));
            for (String dye : List.of("dyenamics:lavender_dye","minecraft:green_dye")) {
                var pigment = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(dye)));
                var input=CraftingInput.of(2,1,List.of(source,pigment));
                var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
                var output=recipe.value().assemble(input,h.getLevel().registryAccess());
                var expected = dye.contains("minecraft") ? nativeBlock("supplementaries:"+family+"_green") : block("supplementaries",family,DyenamicColor.LAVENDER);
                h.assertTrue(output.is(expected.asItem()),"Wrong present recipe result");
                h.assertTrue(output.get(DataComponents.CONTAINER).equals(source.get(DataComponents.CONTAINER)) && output.getHoverName().equals(source.getHoverName()),"Sealed present data lost during crafting");
                if (family.equals("present") && dye.startsWith("dyenamics:")) {
                    var trapInput=CraftingInput.of(2,1,List.of(output,new ItemStack(Items.TRIPWIRE_HOOK)));
                    var trapping=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,trapInput,h.getLevel()).orElseThrow();
                    var trapped=trapping.value().assemble(trapInput,h.getLevel().registryAccess());
                    h.assertTrue(trapped.is(block("supplementaries","trapped_present",DyenamicColor.LAVENDER).asItem()) && trapped.get(DataComponents.CONTAINER).equals(output.get(DataComponents.CONTAINER)),"Trapping changed the present color or contents");
                }
                source=output;
            }
        }
        var sack = new ItemStack(nativeBlock("supplementaries:sack"));
        sack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.EMERALD,13))));
        for (String dye : List.of("dyenamics:wine_dye","minecraft:blue_dye")) {
            var input=CraftingInput.of(2,1,List.of(sack,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(dye)))));
            var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            var output=recipe.value().assemble(input,h.getLevel().registryAccess());
            var expected=dye.startsWith("dyenamics:") ? block("suppsquared","sack",DyenamicColor.WINE) : nativeBlock("suppsquared:sack_blue");
            h.assertTrue(output.is(expected.asItem()) && output.get(DataComponents.CONTAINER).equals(sack.get(DataComponents.CONTAINER)),"Sack crafting changed its color or contents");
            sack=output;
        }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void variantsFollowNativeCreativeFamilies(GameTestHelper h) {
        Set<String> seen=new HashSet<>();
        for (var tab:BuiltInRegistries.CREATIVE_MODE_TAB) {
            if (tab.getType()!=CreativeModeTab.Type.CATEGORY) continue;
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(net.minecraft.world.flag.FeatureFlags.VANILLA_SET,true,h.getLevel().registryAccess()));
            var items=new ArrayList<>(tab.getDisplayItems());
            for (var v:PaletteModule.VARIANTS) {
                if (!Set.of("quark","supplementaries","suppsquared").contains(v.addon()) || v.item()==null || v.color()!=DyenamicColor.PEACH) continue;
                int nativeIndex=-1;
                for (int i=0;i<items.size();i++) {
                    var id=BuiltInRegistries.ITEM.getKey(items.get(i).getItem());
                    if (id.getNamespace().equals(v.addon()) && Arrays.stream(DyeColor.values()).anyMatch(c -> id.getPath().equals(v.nativePattern().formatted(c.getName())))) nativeIndex=i;
                }
                if (nativeIndex<0) continue;
                seen.add(v.addon()+":"+v.family());
                for (var color:DyenamicColor.values()) h.assertTrue(items.get(++nativeIndex).is(PaletteModule.variant(v.addon(),v.family(),color).item().get()),"Variants not grouped beside native family: "+v.id());
            }
        }
        h.assertTrue(seen.size()==15,"Some families were hidden in creative tabs: "+seen);
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void buntingAndStoolsKeepNativeBehavior(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(4,2,4));
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var stool=block("quark","stool",DyenamicColor.ULTRAMARINE);
        h.getLevel().setBlockAndUpdate(pos,stool.defaultBlockState());
        stool.defaultBlockState().useWithoutItem(h.getLevel(),player,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        h.assertTrue(player.getVehicle() instanceof org.violetmoon.quark.content.building.entity.Stool,"Stool did not provide native seating");
        player.stopRiding();
        var bunting=PaletteModule.variant("supplementaries","bunting",DyenamicColor.MINT);
        var wall=block("supplementaries","bunting_wall",DyenamicColor.MINT);
        h.assertTrue(wall.asItem()==bunting.item().get() && bunting.item().get() instanceof StandingAndWallBlockItem,"Bunting wall and ceiling did not share a placement item");
        var flag=new ItemStack(block("supplementaries","flag",DyenamicColor.WINE));
        h.assertTrue(flag.has(DataComponents.BANNER_PATTERNS) && flag.getMaxStackSize()==16,"Flag lost native pattern defaults");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void quarkMaterialRecipesUseMatchingColors(GameTestHelper h) {
        var color=DyenamicColor.PEACH;
        var shard=PaletteModule.EXTRA_ITEMS.stream().filter(v -> v.addon().equals("quark") && v.color()==color).findFirst().orElseThrow().item().get();
        var shards=CraftingInput.of(2,2,Collections.nCopies(4,new ItemStack(shard)));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,shards,h.getLevel()).orElseThrow();
        var output=recipe.value().assemble(shards,h.getLevel().registryAccess());
        h.assertTrue(BuiltInRegistries.ITEM.getKey(output.getItem()).toString().equals("dyenamics:peach_stained_glass"),"Shards did not recycle to matching glass");
        var stoneInput=new SingleRecipeInput(new ItemStack(block("quark","shingles",color)));
        h.assertTrue(h.getLevel().getRecipeManager().getAllRecipesFor(RecipeType.STONECUTTING).stream().anyMatch(r -> r.value().matches(stoneInput,h.getLevel()) && r.value().getResultItem(h.getLevel().registryAccess()).is(block("quark","shingles_vertical_slab",color).asItem())),"Missing matching-color vertical slab stonecutting");
        h.succeed();
    }
}
