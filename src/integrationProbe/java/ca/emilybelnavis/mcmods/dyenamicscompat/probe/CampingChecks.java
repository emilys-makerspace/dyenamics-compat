package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

final class CampingChecks {
    static void verify() {
        var variant = LetsDoModule.variant("camping", "tent", DyenamicColor.MINT);
        var tent = new CampingCompat.PaletteTentEntity(BlockPos.ZERO, variant.block().get().defaultBlockState());
        var bag = new ItemStack(LetsDoModule.variant("camping", "sleeping_bag", DyenamicColor.WINE).item().get());
        tent.setBag(bag);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var restored = (CampingCompat.PaletteTentEntity) BlockEntity.loadStatic(BlockPos.ZERO, tent.getBlockState(), tent.saveWithFullMetadata(registries), registries);
        RegistryProbe.check(restored != null && ItemStack.isSameItemSameComponents(bag, restored.getBag()), "Tent lost its sleeping bag during reload");
        RegistryProbe.check(restored.getBagBlock() != null, "Camping no longer recognizes the extra sleeping bag");
    }
}
