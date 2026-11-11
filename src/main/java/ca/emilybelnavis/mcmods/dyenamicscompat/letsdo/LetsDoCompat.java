package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LetsDoCompat {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DyenamicsCompat.MOD_ID);
    public static final java.util.function.Supplier<RecipeSerializer<LetsDoColoringRecipe>> COLORING = RECIPES.register("letsdo_coloring", LetsDoColoringRecipe.Serializer::new);

    public static void register(IEventBus bus) {
        ModList mods = ModList.get();
        if (mods.isLoaded("brewery")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.brewery.BreweryCompat.register(bus);
        if (mods.isLoaded("camping")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingCompat.register(bus);
        if (mods.isLoaded("furniture")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture.FurnitureCompat.register(bus);
        if (mods.isLoaded("hearth_and_timber")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.hearth_and_timber.HearthAndTimberCompat.register(bus);
        if (mods.isLoaded("lilis_pottery")) ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.LilisPotteryCompat.register(bus);
        if (LetsDoModule.VARIANTS.isEmpty()) return;
        RECIPES.register(bus);
        bus.addListener(LetsDoPack::findPacks);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, LetsDoDyeInteractions::rightClick);
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.client.LetsDoClient.register(bus);
        }
    }
    private LetsDoCompat() {}
}
