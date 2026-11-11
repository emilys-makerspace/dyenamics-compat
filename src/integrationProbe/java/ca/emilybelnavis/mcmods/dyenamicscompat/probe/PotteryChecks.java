package ca.emilybelnavis.mcmods.dyenamicscompat.probe;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.lilis_pottery.LilisPotteryCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

final class PotteryChecks {
    static void verify() {
        var variant = LetsDoModule.variant("lilis_pottery", "planter", DyenamicColor.PEACH);
        var pot = new LilisPotteryCompat.PaletteStorageEntity(BlockPos.ZERO, variant.block().get().defaultBlockState());
        pot.setStack(0, new ItemStack(Items.DIAMOND, 37));
        pot.setSideColorRgb(0x123456); pot.setGlazed(true); pot.setGlazeColorRgb(0x654321); pot.setGlazeStrength(1.75F);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var saved = pot.saveWithFullMetadata(registries);
        var restored = (LilisPotteryCompat.PaletteStorageEntity) BlockEntity.loadStatic(BlockPos.ZERO, pot.getBlockState(), saved, registries);
        RegistryProbe.check(restored != null && restored.getInventory().getFirst().getCount() == 37 && restored.getInventory().getFirst().is(Items.DIAMOND), "Pottery inventory was lost");
        RegistryProbe.check(restored.getSideColorRgb() == 0x123456 && restored.isGlazed() && restored.getGlazeColorRgb() == 0x654321 && restored.getGlazeStrength() == 1.75F, "Pottery paint/glaze was lost");
    }
}
