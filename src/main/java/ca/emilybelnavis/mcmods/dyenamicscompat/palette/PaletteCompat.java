package ca.emilybelnavis.mcmods.dyenamicscompat.palette;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PaletteCompat {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DyenamicsCompat.MOD_ID);
    // Preserve the existing serializer ID for saved recipes and datapacks.
    public static final java.util.function.Supplier<RecipeSerializer<PaletteColoringRecipe>> COLORING = RECIPES.register("letsdo_coloring", PaletteColoringRecipe.Serializer::new);
    public static void register(IEventBus bus) {
        if (PaletteModule.VARIANTS.isEmpty()) return;
        RECIPES.register(bus);
        bus.addListener(PalettePack::findPacks);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, PaletteDyeInteractions::rightClick);
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT)
            ca.emilybelnavis.mcmods.dyenamicscompat.palette.client.PaletteClient.register(bus);
    }
    private PaletteCompat() {}
}
