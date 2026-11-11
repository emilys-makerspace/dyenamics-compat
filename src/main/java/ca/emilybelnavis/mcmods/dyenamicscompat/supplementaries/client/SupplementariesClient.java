package ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries.SupplementariesCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class SupplementariesClient {
    public static void register(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(SupplementariesCompat.FLAG.get(), context -> {
            var nativeRenderer = new FlagBlockTileRenderer(context);
            return new net.minecraft.client.renderer.blockentity.BlockEntityRenderer<SupplementariesCompat.FlagEntity>() {
                @Override public void render(SupplementariesCompat.FlagEntity tile, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
                    nativeRenderer.render(tile,partial,pose,flagBuffers(buffers,PaletteModule.variant(tile.getBlockState().getBlock()).color().rgb),light,overlay);
                }
                @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(SupplementariesCompat.FlagEntity tile) { return nativeRenderer.getRenderBoundingBox(tile); }
                @Override public int getViewDistance() { return nativeRenderer.getViewDistance(); }
            };
        }));
        bus.addListener((RegisterClientExtensionsEvent event) -> {
            var renderer = new FlagItemRenderer() {
                @Override public void renderByItem(ItemStack stack, ItemDisplayContext display, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
                    var v = PaletteModule.variant(((BlockItem)stack.getItem()).getBlock());
                    super.renderByItem(stack,display,pose,flagBuffers(buffers,v.color().rgb),light,overlay);
                }
            };
            PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals("supplementaries") && v.family().equals("flag")).forEach(v ->
                event.registerItem(new IClientItemExtensions() {
                    @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
                },v.item().get()));
        });
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals("supplementaries")).forEach(v -> {
                if (v.family().equals("present")) ItemProperties.register(v.item().get(), ResourceLocation.parse("supplementaries:packed"), (s,l,e,i) -> 1);
                if (v.family().equals("trapped_present")) ItemProperties.register(v.item().get(), ResourceLocation.parse("supplementaries:primed"), (s,l,e,i) -> TrappedPresentBlockTile.isPrimed(s) ? 1 : 0);
            });
        }));
    }
    /** Color only the canvas. Native pattern layers keep their own dye colors. */
    public static MultiBufferSource flagBuffers(MultiBufferSource original, int rgb) {
        var flatBase = ModMaterials.FLAG_BASE_MATERIAL.renderType(RenderType::entitySolid);
        var bannerLayers = Sheets.BANNER_BASE.renderType(RenderType::entityNoOutline);
        boolean[] bannerBaseSeen = {false};
        return type -> {
            VertexConsumer target = original.getBuffer(type);
            boolean canvas = type == flatBase;
            if (type == bannerLayers && !bannerBaseSeen[0]) { bannerBaseSeen[0] = true; canvas = true; }
            return canvas ? new CanvasConsumer(target,rgb) : target;
        };
    }
    private record CanvasConsumer(VertexConsumer target, int rgb) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z) { target.addVertex(x,y,z); return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { target.setColor((rgb>>16)&255,(rgb>>8)&255,rgb&255,a); return this; }
        public VertexConsumer setUv(float u,float v) { target.setUv(u,v); return this; }
        public VertexConsumer setUv1(int u,int v) { target.setUv1(u,v); return this; }
        public VertexConsumer setUv2(int u,int v) { target.setUv2(u,v); return this; }
        public VertexConsumer setNormal(float x,float y,float z) { target.setNormal(x,y,z); return this; }
    }
    private SupplementariesClient() {}
}
