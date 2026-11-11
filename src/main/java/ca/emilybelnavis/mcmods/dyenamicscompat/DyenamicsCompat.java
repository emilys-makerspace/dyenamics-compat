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
        }
        // The JVM never loads the Create implementation unless both optional mods exist.
        if (ModList.get().isLoaded("create") && ModList.get().isLoaded("dyenamics")) {
            CreateCompat.register(modBus);
        }
    }
}
