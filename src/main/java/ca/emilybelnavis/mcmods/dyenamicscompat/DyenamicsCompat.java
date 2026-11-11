package ca.emilybelnavis.mcmods.dyenamicscompat;

import ca.emilybelnavis.mcmods.dyenamicscompat.create.CreateCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(DyenamicsCompat.MOD_ID)
public final class DyenamicsCompat {
    public static final String MOD_ID = "dyenamicscompat";

    public DyenamicsCompat(IEventBus modBus) {
        if (ModList.get().isLoaded("dyenamics")) {
            ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoCompat.register(modBus);
            if (ModList.get().isLoaded("supplementaries")) ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries.SupplementariesCompat.register(modBus);
            if (ModList.get().isLoaded("suppsquared")) ca.emilybelnavis.mcmods.dyenamicscompat.suppsquared.SuppSquaredCompat.register(modBus);
            if (ModList.get().isLoaded("quark")) ca.emilybelnavis.mcmods.dyenamicscompat.quark.QuarkCompat.register(modBus);
            ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteCompat.register(modBus);
        }
        // The JVM never loads the Create implementation unless both optional mods exist.
        if (ModList.get().isLoaded("create") && ModList.get().isLoaded("dyenamics")) {
            CreateCompat.register(modBus);
        }
    }
}
