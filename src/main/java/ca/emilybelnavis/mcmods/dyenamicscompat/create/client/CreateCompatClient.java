package ca.emilybelnavis.mcmods.dyenamicscompat.create.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.create.CreateCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class CreateCompatClient {
    public static void register(IEventBus bus) {
        // Initialize partial models before model discovery/baking.
        ToolboxRenderer.initModels();
        bus.addListener(CreateCompatClient::renderers);
        bus.addListener(PalettePack::findPacks);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateCompat.VALVE_ENTITY.get(), ValveRenderer::new);
        event.registerBlockEntityRenderer(CreateCompat.TOOLBOX_ENTITY.get(), ToolboxRenderer::new);
    }

    private CreateCompatClient() {}
}
