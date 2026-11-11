package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public final class CompatToolboxEntity extends ToolboxBlockEntity {
    public CompatToolboxEntity(BlockPos pos, BlockState state) {
        super(CreateCompat.TOOLBOX_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : getBlockState().getBlock().getName();
    }
}
