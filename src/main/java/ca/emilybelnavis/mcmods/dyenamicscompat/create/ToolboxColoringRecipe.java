package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/** Ordinary visible shapeless recipe that preserves all toolbox item components. */
public final class ToolboxColoringRecipe extends ShapelessRecipe {
    ToolboxColoringRecipe(ShapelessRecipe source) {
        super(source.getGroup(), source.category(), source.getResultItem(null), source.getIngredients());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack output = super.assemble(input, registries);
        for (ItemStack stack : input.items()) {
            if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ToolboxBlock) {
                output.applyComponents(stack.getComponentsPatch());
                break;
            }
        }
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return CreateCompat.TOOLBOX_COLORING.get(); }

    public static final class Serializer implements RecipeSerializer<ToolboxColoringRecipe> {
        private static final ShapelessRecipe.Serializer BASE = new ShapelessRecipe.Serializer();
        @Override
        public MapCodec<ToolboxColoringRecipe> codec() {
            return BASE.codec().xmap(ToolboxColoringRecipe::new, recipe -> recipe);
        }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ToolboxColoringRecipe> streamCodec() {
            return BASE.streamCodec().map(ToolboxColoringRecipe::new, recipe -> recipe);
        }
    }
}
