package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.satisfy.lilis_pottery.core.block.*;
import net.satisfy.lilis_pottery.core.block.entity.AbstractStorageBlockEntity;
import net.satisfy.lilis_pottery.core.block.entity.UrnBlockEntity;

public final class LilisPotteryCompat {
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DyenamicsCompat.MOD_ID);
    public static final java.util.function.Supplier<BlockEntityType<PaletteStorageEntity>> STORAGE = ENTITIES.register("letsdo/lilis_pottery/storage",
            () -> BlockEntityType.Builder.of(PaletteStorageEntity::new, LetsDoModule.blocks("lilis_pottery", "planter", "plant_bowl", "cupella", "bud_vase", "vase", "garden_pot", "square_pot")).build(null));
    public static final java.util.function.Supplier<BlockEntityType<PaletteUrnEntity>> URN = ENTITIES.register("letsdo/lilis_pottery/urn",
            () -> BlockEntityType.Builder.of(PaletteUrnEntity::new, LetsDoModule.blocks("lilis_pottery", "urn", "amphore")).build(null));

    public static void register(IEventBus bus) {
        var module = new LetsDoModule("lilis_pottery", "lilis_pottery");
        module.family("clay", "%s_clay", "white", false, (color, properties) -> new Block(properties));
        module.family("bricks", "%s_bricks", "white", false, (color, properties) -> new Block(properties));
        module.family("chiseled_bricks", "chiseled_%s_bricks", "white", false, (color, properties) -> new Block(properties));
        module.family("brick_slab", "%s_brick_slab", "white", false, (color, properties) -> new SlabBlock(properties));
        module.family("brick_stairs", "%s_brick_stairs", "white", false, (color, properties) -> new StairBlock(LetsDoModule.variant("lilis_pottery", "bricks", color).block().get().defaultBlockState(), properties));
        module.family("brick_wall", "%s_brick_wall", "white", false, (color, properties) -> new WallBlock(properties));
        module.family("planter", "%s_planter", "white", false, (color, properties) -> new PlanterBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("plant_bowl", "%s_plant_bowl", "white", false, (color, properties) -> new PlantBowlBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("cupella", "%s_cupella", "white", false, (color, properties) -> new CupellaBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("bud_vase", "%s_bud_vase", "white", false, (color, properties) -> new BudVaseBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("vase", "%s_vase", "white", false, (color, properties) -> new VaseBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("garden_pot", "%s_garden_pot", "white", false, (color, properties) -> new GardenPotBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("square_pot", "%s_square_pot", "white", false, (color, properties) -> new SquarePotBlock(properties) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteStorageEntity(pos, state); }
        });
        module.family("urn", "%s_urn", "white", false, (color, properties) -> new UrnBlock(properties, UrnBlock.Variant.URN) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteUrnEntity(pos, state); }
        });
        module.family("amphore", "%s_amphore", "white", false, (color, properties) -> new UrnBlock(properties, UrnBlock.Variant.AMPHORE) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaletteUrnEntity(pos, state); }
        });
        module.itemFamily("clay_ball", "%s_clay_ball");
        module.itemFamily("brick", "%s_brick");
        module.register(bus);
        ENTITIES.register(bus);
    }
    public static final class PaletteStorageEntity extends AbstractStorageBlockEntity {
        public PaletteStorageEntity(BlockPos pos, BlockState state) { super(pos, state, ((AbstractStorageBlock) state.getBlock()).size()); }
        @Override public BlockEntityType<?> getType() { return STORAGE.get(); }
    }
    public static final class PaletteUrnEntity extends UrnBlockEntity {
        public PaletteUrnEntity(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public BlockEntityType<?> getType() { return URN.get(); }
    }
    private LilisPotteryCompat() {}
}
