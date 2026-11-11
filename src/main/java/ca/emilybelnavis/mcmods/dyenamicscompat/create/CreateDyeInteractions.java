package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.bearing.SailBlock;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlock;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.kinetics.crank.ValveHandleBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Public interaction event; no enum injection, mixins, remapping, or missing-entry placeholders. */
public final class CreateDyeInteractions {
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isSpectator() || !event.getEntity().mayBuild() || event.getEntity().isShiftKeyDown()) return;
        Level level = event.getLevel();
        BlockState oldState = level.getBlockState(event.getPos());
        String family = family(oldState.getBlock());
        if (family == null) return;
        CompatColor custom = customDye(event.getItemStack());
        CreateCompat.Variant oldVariant = CreateCompat.variant(oldState.getBlock());
        // Vanilla-to-vanilla coloring remains entirely Create's responsibility.
        if (custom == null && oldVariant == null) return;
        Block target = custom != null ? CreateCompat.block(custom, family)
                : vanillaBlock(DyeColor.getColor(event.getItemStack()), family);
        if (target == null) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide || target == oldState.getBlock()) return;

        BlockEntity oldEntity = level.getBlockEntity(event.getPos());
        // Unequipping can return hotbar stacks to the toolbox; snapshot only after that transfer.
        if (oldEntity instanceof ToolboxBlockEntity toolbox) toolbox.unequipTracked();
        CompoundTag saved = oldEntity == null ? null : oldEntity.saveWithFullMetadata(level.registryAccess());
        // Different registry types must never retain the previous block entity instance.
        if (oldEntity != null) level.removeBlockEntity(event.getPos());
        BlockState newState = BlockHelper.copyProperties(oldState, target.defaultBlockState());
        level.setBlockAndUpdate(event.getPos(), newState);
        BlockEntity replacement = level.getBlockEntity(event.getPos());
        if (saved != null && replacement != null) {
            replacement.loadWithComponents(saved, level.registryAccess());
            replacement.setChanged();
            level.sendBlockUpdated(event.getPos(), newState, newState, Block.UPDATE_CLIENTS);
        }
        // Match Create: coloring by right-click does not consume the dye.
        level.playSound(null, event.getPos(), SoundEvents.DYE_USE, SoundSource.BLOCKS, 1, 1);
    }

    static CompatColor customDye(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (CompatColor color : CompatColor.values()) {
            if (itemId.equals(ResourceLocation.fromNamespaceAndPath("dyenamics", color.id + "_dye"))
                    || stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dyes/" + color.id)))) return color;
        }
        return null;
    }

    static String family(Block block) {
        String namespace = BuiltInRegistries.BLOCK.getKey(block).getNamespace();
        if (!namespace.equals("create") && !namespace.equals("dyenamicscompat")) return null;
        if (block instanceof SeatBlock) return "seat";
        if (block instanceof SailBlock sail && !sail.isFrame()) return "sail";
        if (block instanceof ValveHandleBlock || block instanceof CompatValveBlock) return "valve_handle";
        if (block instanceof ToolboxBlock) return "toolbox";
        return null;
    }

    static Block vanillaBlock(DyeColor color, String family) {
        if (color == null) return null;
        return switch (family) {
            case "seat" -> AllBlocks.SEATS.get(color).get();
            case "sail" -> AllBlocks.DYED_SAILS.get(color).get();
            case "valve_handle" -> AllBlocks.DYED_VALVE_HANDLES.get(color).get();
            case "toolbox" -> AllBlocks.TOOLBOXES.get(color).get();
            default -> null;
        };
    }

    private CreateDyeInteractions() {}
}
