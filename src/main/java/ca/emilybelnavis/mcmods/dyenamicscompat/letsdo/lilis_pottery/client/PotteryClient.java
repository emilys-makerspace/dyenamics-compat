package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.LilisPotteryCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.satisfy.lilis_pottery.client.renderer.StorageBlockEntityRenderer;
import net.satisfy.lilis_pottery.client.renderer.UrnGlazeOverlayRenderer;

public final class PotteryClient {
    public static void register(IEventBus bus) { bus.addListener(PotteryClient::renderers); }
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(LilisPotteryCompat.STORAGE.get(), context -> new StorageBlockEntityRenderer());
        event.registerBlockEntityRenderer(LilisPotteryCompat.URN.get(), context -> new UrnGlazeOverlayRenderer());
    }
}
