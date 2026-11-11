package ca.emilybelnavis.mcmods.dyenamicscompat.create.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.create.CompatValveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ValveRenderer extends KineticBlockEntityRenderer<CompatValveEntity> {
    public ValveRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override
    protected void renderSafe(CompatValveEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        // Always use the fallback renderer: this type has no Flywheel visual registered.
        kineticRotationTransform(be.getRenderedHandle(), be,
                be.getBlockState().getValue(BlockStateProperties.FACING).getAxis(),
                AngleHelper.rad(be.getIndependentAngle(partialTicks)), light)
                .renderInto(pose, buffers.getBuffer(RenderType.solid()));
    }
}
