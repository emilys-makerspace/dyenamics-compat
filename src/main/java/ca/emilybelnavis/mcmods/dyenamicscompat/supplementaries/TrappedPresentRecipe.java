package ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;

public final class TrappedPresentRecipe extends CustomRecipe {
    private final net.mehvahdjukaar.supplementaries.common.items.crafting.TrappedPresentRecipe original;
    private TrappedPresentRecipe(net.mehvahdjukaar.supplementaries.common.items.crafting.TrappedPresentRecipe original) { super(original.category()); this.original=original; }
    @Override public boolean matches(CraftingInput input,Level level) { return original.matches(input,level); }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        for(var stack:input.items()) if(stack.getItem() instanceof BlockItem item) {
            var v=PaletteModule.variant(item.getBlock());
            if(v!=null && v.addon().equals("supplementaries") && (v.family().equals("present") || v.family().equals("trapped_present"))) {
                var result=stack.transmuteCopy(PaletteModule.variant("supplementaries","trapped_present",v.color()).item().get(),1);
                result.remove(ModComponents.ADDRESS.get()); result.remove(DataComponents.MAX_STACK_SIZE);
                return result;
            }
        }
        return original.assemble(input,registries);
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return original.canCraftInDimensions(width,height); }
    @Override public RecipeSerializer<?> getSerializer() { return SupplementariesCompat.TRAPPING.get(); }
    public static final class Serializer implements RecipeSerializer<TrappedPresentRecipe> {
        private static final net.mehvahdjukaar.supplementaries.common.items.crafting.TrappedPresentRecipe.Serializer BASE=new net.mehvahdjukaar.supplementaries.common.items.crafting.TrappedPresentRecipe.Serializer();
        @Override public MapCodec<TrappedPresentRecipe> codec() { return BASE.codec().xmap(TrappedPresentRecipe::new,r -> r.original); }
        @Override public StreamCodec<RegistryFriendlyByteBuf,TrappedPresentRecipe> streamCodec() { return BASE.streamCodec().map(TrappedPresentRecipe::new,r -> r.original); }
    }
}
