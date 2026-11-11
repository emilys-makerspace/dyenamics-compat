package ca.emilybelnavis.mcmods.dyenamicscompat.suppsquared;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CandleHolderBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.suppsquared.common.ColoredSackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import net.minecraft.world.item.crafting.*;

public final class SuppSquaredCompat {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, DyenamicsCompat.MOD_ID);
    public static final Supplier<RecipeSerializer<ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries.ContainerColoringRecipe>> COLORING = RECIPES.register("suppsquared/sack_dye", () -> new SimpleCraftingRecipeSerializer<>(category -> new ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries.ContainerColoringRecipe(category,true)));
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, DyenamicsCompat.MOD_ID);
    public static final Supplier<BlockEntityType<SackEntity>> SACK = ENTITIES.register("suppsquared/sack", () -> BlockEntityType.Builder.of(SackEntity::new, PaletteModule.blocks("suppsquared", "sack")).build(null));
    public static void register(IEventBus bus) {
        var module = new PaletteModule("suppsquared", null, "suppsquared");
        module.family("gold_candle_holder", "gold_candle_holder_%s", "white", false, (c,p) -> new CandleHolderBlock(c.analogue,p, () -> net.minecraft.core.particles.ParticleTypes.FLAME));
        module.family("sack", "sack_%s", "white", false, (c,p) -> new ColoredSackBlock(p,c.analogue) {
            @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SackEntity(pos,state); }
        }, block -> new SackItem(block,new Item.Properties().stacksTo(1).component(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY)));
        module.register(bus);
        ENTITIES.register(bus);
        RECIPES.register(bus);
    }
    public static class SackEntity extends SackBlockTile {
        public SackEntity(BlockPos pos, BlockState state) { super(pos,state); }
        @Override public BlockEntityType<?> getType() { return SACK.get(); }
    }
    private SuppSquaredCompat() {}
}
