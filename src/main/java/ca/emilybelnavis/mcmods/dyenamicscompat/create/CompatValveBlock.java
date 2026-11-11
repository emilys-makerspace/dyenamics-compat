package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.crank.HandCrankBlock;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Uses Create's valve block entity without changing or extending DyeColor. */
public final class CompatValveBlock extends HandCrankBlock {
    public CompatValveBlock(Properties properties) { super(properties); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return AllShapes.VALVE_HANDLE.get(state.getValue(FACING));
    }

    @Override
    public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
        return CreateCompat.VALVE_ENTITY.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        withBlockEntityDo(level, pos, be -> ((ValveHandleBlockEntity) be).activate(player.isShiftKeyDown()));
        return ItemInteractionResult.SUCCESS;
    }
}
