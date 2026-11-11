package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture.client;

import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.satisfy.foundation.client.creative.CreativeSideTabs;
import net.satisfy.foundation.client.creative.CreativeSideTabs.SideTab;
import net.satisfy.furniture.core.registry.TabRegistry;

/** Extends Foundation's public membership API; no GUI mixin or registry-map injection. */
public final class FurnitureSideTabs {
    public static void install() {
        var tab = TabRegistry.FURNITURE_TAB.get();
        var originals = CreativeSideTabs.get(tab);
        if (originals.isEmpty()) return;
        SideTab[] expanded = originals.stream().map(original -> new SideTab(original.title(), original::icon, output -> {
            // Preserve the native filter, including native optional-content settings.
            tab.getDisplayItems().stream().filter(original::contains).forEach(output::accept);
            LetsDoModule.VARIANTS.stream().filter(v -> v.addon().equals("furniture") && v.item() != null).forEach(variant -> {
                var source = BuiltInRegistries.BLOCK.get(variant.source()).asItem();
                if (original.contains(new ItemStack(source))) output.accept(variant.item().get());
            });
        })).toArray(SideTab[]::new);
        CreativeSideTabs.register(TabRegistry.FURNITURE_TAB.getKey(), expanded);
    }
    private FurnitureSideTabs() {}
}
