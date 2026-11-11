package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

public final class LetsDoClient {
    public static void register(IEventBus bus) {
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var variant : LetsDoModule.VARIANTS) {
                var source = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(variant.source());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(variant.block().get(),
                        net.minecraft.client.renderer.ItemBlockRenderTypes.getChunkRenderType(source.defaultBlockState()));
            }
        }));
        if (ModList.get().isLoaded("furniture")) {
            bus.addListener((net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent event) -> event.enqueueWork(
                    ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture.client.FurnitureSideTabs::install));
        }
        bus.addListener(LetsDoClient::blockColors);
        bus.addListener(LetsDoClient::itemColors);
        if (ModList.get().isLoaded("camping")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.client.CampingClient.register(bus);
        if (ModList.get().isLoaded("lilis_pottery")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.client.PotteryClient.register(bus);
    }
    private static void blockColors(RegisterColorHandlersEvent.Block event) {
        LetsDoModule.VARIANTS.stream().filter(LetsDoModule.Variant::tinted).forEach(v ->
                event.register((state, level, pos, tint) -> 0xFF000000 | v.color().rgb, v.block().get()));
    }
    private static void itemColors(RegisterColorHandlersEvent.Item event) {
        LetsDoModule.VARIANTS.stream().filter(v -> v.tinted() && v.item() != null).forEach(v ->
                event.register((stack, tint) -> 0xFF000000 | v.color().rgb, v.item().get()));
    }
    private LetsDoClient() {}
}
