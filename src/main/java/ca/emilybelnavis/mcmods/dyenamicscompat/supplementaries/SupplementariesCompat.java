package ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.*;

public final class SupplementariesCompat {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, DyenamicsCompat.MOD_ID);
    public static final Supplier<RecipeSerializer<ContainerColoringRecipe>> COLORING = RECIPES.register("supplementaries/present_dye", () -> new SimpleCraftingRecipeSerializer<>(category -> new ContainerColoringRecipe(category,false)));
    public static final Supplier<RecipeSerializer<TrappedPresentRecipe>> TRAPPING = RECIPES.register("supplementaries/trapped_present", TrappedPresentRecipe.Serializer::new);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, DyenamicsCompat.MOD_ID);
    public static final Supplier<BlockEntityType<PresentEntity>> PRESENT = ENTITIES.register("supplementaries/present", () -> BlockEntityType.Builder.of(PresentEntity::new, PaletteModule.blocks("supplementaries", "present")).build(null));
    public static final Supplier<BlockEntityType<TrappedEntity>> TRAPPED = ENTITIES.register("supplementaries/trapped_present", () -> BlockEntityType.Builder.of(TrappedEntity::new, PaletteModule.blocks("supplementaries", "trapped_present")).build(null));
    public static final Supplier<BlockEntityType<FlagEntity>> FLAG = ENTITIES.register("supplementaries/flag", () -> BlockEntityType.Builder.of(FlagEntity::new, PaletteModule.blocks("supplementaries", "flag")).build(null));
    public static void register(IEventBus bus) {
        var module = new PaletteModule("supplementaries", null, "supplementaries");
        module.family("awning", "awning_%s", "white", false, (c,p) -> new AwningBlock(c.analogue, p));
        module.family("candle_holder", "candle_holder_%s", "white", false, (c,p) -> new CandleHolderBlock(c.analogue, p, () -> net.minecraft.core.particles.ParticleTypes.FLAME));
        module.family("bunting_wall", "bunting_wall_%s", "white", false, (c,p) -> new BuntingWallBlock(c.analogue, p) {
            @Override public Item asItem() { return PaletteModule.variant("supplementaries", "bunting", c).item().get(); }
        }, null);
        module.family("bunting", "bunting_%s", "white", false, (c,p) -> new BuntingCeilingBlock(c.analogue, p), block -> {
            var v = PaletteModule.variant(block);
            return new StandingAndWallBlockItem(block, PaletteModule.variant("supplementaries", "bunting_wall", v.color()).block().get(), new Item.Properties(), Direction.UP);
        });
        module.family("present", "present_%s", "white", false, (c,p) -> new PresentBlock(c.analogue, p) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PresentEntity(pos,state); }
        }, block -> new PresentItem(block,new Item.Properties()));
        module.family("trapped_present", "trapped_present_%s", "white", false, (c,p) -> new TrappedPresentBlock(c.analogue, p) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TrappedEntity(pos,state); }
        }, block -> new TrappedPresentItem(block,new Item.Properties()));
        module.family("flag", "flag_%s", "white", false, (c,p) -> new FlagBlock(c.analogue, p) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FlagEntity(pos,state); }
        }, block -> new FlagItem(block,new Item.Properties().stacksTo(16).component(net.minecraft.core.component.DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)));
        module.register(bus);
        ENTITIES.register(bus);
        RECIPES.register(bus);
    }
    public static class PresentEntity extends PresentBlockTile {
        public PresentEntity(BlockPos pos, BlockState state) { super(pos,state); }
        @Override public BlockEntityType<?> getType() { return PRESENT.get(); }
    }
    public static class TrappedEntity extends TrappedPresentBlockTile {
        public TrappedEntity(BlockPos pos, BlockState state) { super(pos,state); }
        @Override public BlockEntityType<?> getType() { return TRAPPED.get(); }
    }
    public static class FlagEntity extends FlagBlockTile {
        public FlagEntity(BlockPos pos, BlockState state) { super(pos,state); }
        @Override public BlockEntityType<?> getType() { return FLAG.get(); }
    }
    private SupplementariesCompat() {}
}
