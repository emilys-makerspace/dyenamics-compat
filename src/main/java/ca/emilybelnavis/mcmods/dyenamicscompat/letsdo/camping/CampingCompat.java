package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.satisfy.camping.core.world.block.SleepingBagBlock;
import net.satisfy.camping.core.world.block.TentBlock;
import net.satisfy.camping.core.world.block.entity.TentBlockEntity;

public final class CampingCompat {
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DyenamicsCompat.MOD_ID);
    public static final java.util.function.Supplier<BlockEntityType<PaletteTentEntity>> TENT = ENTITIES.register("letsdo/camping/tent",
            () -> BlockEntityType.Builder.of(PaletteTentEntity::new, PaletteModule.blocks("camping", "tent")).build(null));

    public static void register(IEventBus bus) {
        var module = new PaletteModule("camping", "creative_tab");
        module.family("sleeping_bag", "sleeping_bag_%s", "white", false, (color, properties) -> new SleepingBagBlock(color.analogue));
        module.family("tent", "tent_%s", "white", false, (color, properties) -> new TentBlock(properties, color.analogue) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                return state.getValue(PART) == TentPart.MAIN && state.getValue(HALF) == DoubleBlockHalf.LOWER ? new PaletteTentEntity(pos, state) : null;
            }
        });
        module.register(bus);
        ENTITIES.register(bus);
    }

    public static final class PaletteTentEntity extends TentBlockEntity {
        public PaletteTentEntity(BlockPos pos, BlockState state) { super(pos, state); }
        @Override public BlockEntityType<?> getType() { return TENT.get(); }
    }
    private CampingCompat() {}
}
