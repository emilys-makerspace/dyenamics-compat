package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.simibubi.create.content.kinetics.crank.ValveHandleBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class CompatValveEntity extends ValveHandleBlockEntity {
    public CompatValveEntity(BlockPos pos, BlockState state) {
        super(CreateCompat.VALVE_ENTITY.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // Create's stock short-click handler only recognizes ValveHandleBlock.
        behaviours.remove(angleInput);
        angleInput = new ValveHandleScrollValueBehaviour(this) {
            @Override
            public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
                if (!getWorld().isClientSide) activate(player.isShiftKeyDown());
            }
        }.between(-180, 180);
        angleInput.onlyActiveWhen(this::showValue);
        angleInput.setValue(45);
        behaviours.add(angleInput);
    }
}
