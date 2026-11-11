package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.satisfy.camping.client.world.block.renderer.TentRenderer;

public final class CampingClient {
    public static void register(IEventBus bus) { bus.addListener(CampingClient::renderers); }
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CampingCompat.TENT.get(), TentRenderer::new);
    }
}
