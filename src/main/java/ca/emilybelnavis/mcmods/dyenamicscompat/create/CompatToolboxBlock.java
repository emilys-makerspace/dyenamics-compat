package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class CompatToolboxBlock extends ToolboxBlock {
    public CompatToolboxBlock(Properties properties, CompatColor color) {
        super(properties, color.analogue);
    }

    @Override
    public BlockEntityType<? extends ToolboxBlockEntity> getBlockEntityType() {
        return CreateCompat.TOOLBOX_ENTITY.get();
    }
}
