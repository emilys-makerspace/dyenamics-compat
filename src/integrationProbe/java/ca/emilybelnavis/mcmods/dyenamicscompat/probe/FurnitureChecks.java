package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import net.neoforged.fml.ModList;
import net.satisfy.furniture.core.registry.ObjectRegistry;

final class FurnitureChecks {
    static int expectedFamilies() {
        int count = 5;
        for (String fabric : new String[]{"rustic", "linen", "jacquard", "plaid", "chambray", "tweed", "warped", "homespun"}) {
            String owner = fabric.equals("homespun") ? "alpinewhispers" : "meadow";
            if (!ModList.get().isLoaded(owner)) continue;
            String key = owner + "_" + fabric;
            if (ObjectRegistry.CURTAINS.containsKey(key)) count++;
            if (ObjectRegistry.POUFFE.containsKey(key)) count++;
            if (ObjectRegistry.SOFAS.containsKey(key)) count++;
            if (ObjectRegistry.LAMP_ITEMS.containsKey(key)) count += 2;
        }
        return count;
    }
}
