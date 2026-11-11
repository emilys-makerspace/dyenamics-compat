package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.brewery;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.satisfy.brewery.core.block.TableBlock;

public final class BreweryInteractions {
    public static boolean placeCloth(PlayerInteractEvent.RightClickBlock event, PaletteModule.Variant family) {
        if (!family.family().equals("table") || !(event.getItemStack().getItem() instanceof BlockItem item)) return false;
        var cloth = PaletteModule.variant(item.getBlock());
        if (cloth == null || !cloth.addon().equals("brewery") || !cloth.family().equals("patterned_carpet")) return false;
        var state = event.getLevel().getBlockState(event.getPos());
        if (state.getValue(TableBlock.HAS_TABLECLOTH)) return false;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!event.getLevel().isClientSide) {
            var block = PaletteModule.variant("brewery", "table", cloth.color()).block().get();
            event.getLevel().setBlockAndUpdate(event.getPos(), block.withPropertiesOf(state).setValue(TableBlock.HAS_TABLECLOTH, true));
            if (!event.getEntity().isCreative()) event.getItemStack().shrink(1);
        }
        return true;
    }
    private BreweryInteractions() {}
}
