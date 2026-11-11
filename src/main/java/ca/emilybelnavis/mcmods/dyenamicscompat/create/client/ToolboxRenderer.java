package ca.emilybelnavis.mcmods.dyenamicscompat.create.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.create.CompatToolboxEntity;
import ca.emilybelnavis.mcmods.dyenamicscompat.create.CreateCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.EnumMap;
import java.util.Map;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/** Animation transforms follow Create's MIT-licensed ToolboxRenderer. */
public final class ToolboxRenderer extends SmartBlockEntityRenderer<CompatToolboxEntity> {
    private static final Map<DyenamicColor, PartialModel> LIDS = new EnumMap<>(DyenamicColor.class);

    public static void initModels() {
        for (DyenamicColor color : DyenamicColor.values()) {
            LIDS.put(color, PartialModel.of(CreateCompat.id("block/toolbox/lid/" + color.id)));
        }
    }

    public ToolboxRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override
    protected void renderSafe(CompatToolboxEntity be, float partialTicks, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        var state = be.getBlockState();
        var variant = CreateCompat.variant(state.getBlock());
        if (variant == null) return;
        Direction facing = state.getValue(ToolboxBlock.FACING).getOpposite();
        SuperByteBuffer lid = CachedBuffers.partial(LIDS.get(variant.color()), state);
        SuperByteBuffer drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, state);
        var vertices = buffers.getBuffer(RenderType.cutoutMipped());
        lid.center().rotateYDegrees(-facing.toYRot()).uncenter()
                .translate(0, 0.375F, 0.75F).rotateXDegrees(135 * be.lid.getValue(partialTicks))
                .translate(0, -0.375F, -0.75F).light(light).renderInto(pose, vertices);
        float opening = be.drawers.getValue(partialTicks);
        for (int offset = 0; offset < 2; offset++) {
            // Get a fresh buffer transform for each drawer.
            drawer = CachedBuffers.partial(AllPartialModels.TOOLBOX_DRAWER, state);
            drawer.center().rotateYDegrees(-facing.toYRot()).uncenter()
                    .translate(0, offset / 8F, -opening * 0.175F * (2 - offset))
                    .light(light).renderInto(pose, vertices);
        }
    }
}
