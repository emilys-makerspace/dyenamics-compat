package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.hearth_and_timber;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;

public final class HearthAndTimberCompat {
    public static void register(IEventBus bus) {
        var module = new PaletteModule("hearth_and_timber", "hearth_and_timber");
        module.family("plaster", "%s_plaster", "white", false, (color, properties) -> new Block(properties));
        module.register(bus);
    }
    private HearthAndTimberCompat() {}
}
