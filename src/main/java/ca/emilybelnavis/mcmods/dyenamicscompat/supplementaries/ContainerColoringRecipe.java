package ca.emilybelnavis.mcmods.dyenamicscompat.supplementaries;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.*;
import ca.emilybelnavis.mcmods.dyenamicscompat.suppsquared.SuppSquaredCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Replaces native container dye recipes, whose vanilla-only lookup cannot resolve extra colors. */
public final class ContainerColoringRecipe extends CustomRecipe {
    private final boolean sack;
    public ContainerColoringRecipe(CraftingBookCategory category, boolean sack) { super(category); this.sack=sack; }
    private PaletteModule.Variant family(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        var family=PaletteDyeInteractions.family(item.getBlock());
        if (sack && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("supplementaries:sack"))
            return PaletteModule.VARIANTS.stream().filter(v -> v.addon().equals("suppsquared") && v.family().equals("sack")).findFirst().orElse(null);
        if (family==null) return null;
        return (sack ? family.addon().equals("suppsquared") && family.family().equals("sack")
                : family.addon().equals("supplementaries") && (family.family().equals("present") || family.family().equals("trapped_present"))) ? family : null;
    }
    @Override public boolean matches(CraftingInput input,Level level) {
        int containers=0,dyes=0;
        for(var stack:input.items()) if(!stack.isEmpty()) {
            if(family(stack)!=null) containers++;
            else if(PaletteDyeInteractions.dye(stack)!=null || DyeColor.getColor(stack)!=null) dyes++;
            else return false;
        }
        return containers==1 && dyes==1;
    }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) {
        ItemStack source=input.items().stream().filter(s -> family(s)!=null).findFirst().orElseThrow();
        var family=family(source);
        var pigment=input.items().stream().filter(s -> !s.isEmpty() && family(s)==null).findFirst().orElseThrow();
        var extra=PaletteDyeInteractions.dye(pigment);
        Item target=extra!=null ? PaletteModule.variant(family.addon(),family.family(),extra).item().get()
                : BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(family.addon(),family.nativePattern().formatted(DyeColor.getColor(pigment).getName())));
        return source.transmuteCopy(target,1);
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return width*height>=2; }
    @Override public RecipeSerializer<?> getSerializer() { return sack ? SuppSquaredCompat.COLORING.get() : SupplementariesCompat.COLORING.get(); }
}
