package ca.emilybelnavis.mcmods.dyenamicscompat.palette;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/** Keeps custom names, pottery contents/painting, and other components during recoloring. */
public final class PaletteColoringRecipe extends ShapelessRecipe {
    public PaletteColoringRecipe(ShapelessRecipe source) {
        super(source.getGroup(), source.category(), source.getResultItem(null), source.getIngredients());
    }
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        var resultFamily = result.getItem() instanceof net.minecraft.world.item.BlockItem item
                ? PaletteDyeInteractions.family(item.getBlock()) : null;
        for (ItemStack stack : input.items()) {
            if (resultFamily != null) {
                if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem item)) continue;
                var sourceFamily = PaletteDyeInteractions.family(item.getBlock());
                if (sourceFamily == null || !sourceFamily.addon().equals(resultFamily.addon()) || !sourceFamily.family().equals(resultFamily.family())) continue;
            }
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!id.getNamespace().equals("dyenamics") && DyeColor.getColor(stack) == null) {
                result.applyComponents(stack.getComponentsPatch());
                break;
            }
        }
        if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(result.getItem()).getNamespace().equals("brewery")) {
            for (ItemStack stack : input.items()) {
                DyeColor dye = DyeColor.getColor(stack);
                if (dye != null) {
                    var properties = new java.util.HashMap<>(result.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).properties());
                    properties.put("color", dye.getName());
                    result.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(properties));
                }
            }
        }
        return result;
    }
    @Override public RecipeSerializer<?> getSerializer() { return ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteCompat.COLORING.get(); }
    public static final class Serializer implements RecipeSerializer<PaletteColoringRecipe> {
        private static final ShapelessRecipe.Serializer BASE = new ShapelessRecipe.Serializer();
        @Override public MapCodec<PaletteColoringRecipe> codec() { return BASE.codec().xmap(PaletteColoringRecipe::new, r -> r); }
        @Override public StreamCodec<RegistryFriendlyByteBuf, PaletteColoringRecipe> streamCodec() { return BASE.streamCodec().map(PaletteColoringRecipe::new, r -> r); }
    }
}
