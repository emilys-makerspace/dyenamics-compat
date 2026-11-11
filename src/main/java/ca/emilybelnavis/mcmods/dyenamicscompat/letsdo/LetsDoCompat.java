package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public final class LetsDoCompat {
    public static void register(IEventBus bus) {
        ModList mods = ModList.get();
        if (mods.isLoaded("brewery")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.brewery.BreweryCompat.register(bus);
        if (mods.isLoaded("camping")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingCompat.register(bus);
        if (mods.isLoaded("furniture")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture.FurnitureCompat.register(bus);
        if (mods.isLoaded("hearth_and_timber")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.hearth_and_timber.HearthAndTimberCompat.register(bus);
        if (mods.isLoaded("lilis_pottery")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.LilisPotteryCompat.register(bus);
    }
    private LetsDoCompat() {}
}
