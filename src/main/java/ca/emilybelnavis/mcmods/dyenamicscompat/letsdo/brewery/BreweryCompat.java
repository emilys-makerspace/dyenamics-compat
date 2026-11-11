package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.brewery;

import ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.LetsDoModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.satisfy.brewery.core.block.PatternedCarpetBlock;
import net.satisfy.brewery.core.block.PatternedWoolBlock;
import net.satisfy.brewery.core.block.TableBlock;

public final class BreweryCompat {
    public static void register(IEventBus bus) {
        var module = new LetsDoModule("brewery", "brewery");
        module.family("patterned_wool", "patterned_wool", "white", true, (color, properties) -> new PatternedWoolBlock(properties));
        module.family("patterned_carpet", "patterned_carpet_block", "white", true, (color, properties) -> new PatternedCarpetBlock(properties));
        module.family("table", "table", "white", true, (color, properties) -> new TableBlock(properties) {
            { registerDefaultState(defaultBlockState().setValue(HAS_TABLECLOTH, true)); }
            @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
                if (!state.getValue(HAS_TABLECLOTH)) return super.useWithoutItem(state, level, pos, player, hit);
                if (!level.isClientSide) {
                    var original = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("brewery:table"));
                    level.setBlockAndUpdate(pos, original.withPropertiesOf(state).setValue(HAS_TABLECLOTH, false));
                    var carpet = new ItemStack(LetsDoModule.variant("brewery", "patterned_carpet", color).item().get());
                    if (!player.addItem(carpet)) player.drop(carpet, false);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        });
        module.register(bus);
    }
    private BreweryCompat() {}
}
