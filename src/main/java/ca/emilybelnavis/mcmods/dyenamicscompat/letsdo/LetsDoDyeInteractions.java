package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class LetsDoDyeInteractions {
    public static DyenamicColor dye(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (DyenamicColor color : DyenamicColor.values()) {
            if (id.equals(ResourceLocation.fromNamespaceAndPath("dyenamics", color.id + "_dye"))
                    || stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dyes/" + color.id)))) return color;
        }
        return null;
    }
    public static LetsDoModule.Variant family(Block block) {
        var own = LetsDoModule.variant(block);
        if (own != null) return own;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return LetsDoModule.VARIANTS.stream().filter(v -> v.addon().equals(id.getNamespace()) &&
                (v.source().equals(id) || java.util.Arrays.stream(DyeColor.values()).anyMatch(c -> v.nativePattern().formatted(c.getName()).equals(id.getPath()))))
                .findFirst().orElse(null);
    }
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        if (player.isSpectator() || player.isShiftKeyDown() || !player.mayBuild() || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, event.getFace(), event.getItemStack())) return;
        BlockState state = level.getBlockState(pos);
        var family = family(state.getBlock());
        if (family == null) return;
        if (family.addon().equals("brewery") && ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.brewery.BreweryInteractions.placeCloth(event, family)) return;
        DyenamicColor extra = dye(event.getItemStack());
        DyeColor vanilla = extra == null ? DyeColor.getColor(event.getItemStack()) : null;
        if (extra == null && (vanilla == null || LetsDoModule.variant(state.getBlock()) == null)) return;
        if (vanilla != null && family.addon().equals("furniture") && !family.nativePattern().contains("%s") && vanilla != DyeColor.WHITE) return;
        if (family.addon().equals("brewery") && family.family().equals("table") && !state.getValue(net.satisfy.brewery.core.block.TableBlock.HAS_TABLECLOTH)) return;
        Block target = extra != null ? LetsDoModule.variant(family.addon(), family.family(), extra).block().get()
                : BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(family.addon(), family.nativePattern().formatted(vanilla.getName())));
        if (target == Blocks.AIR) return;
        List<BlockPos> positions = family.addon().equals("camping")
                ? ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping.CampingRecolor.positions(level, pos, state)
                : List.of(pos);
        if (positions.isEmpty() || positions.stream().anyMatch(p -> !level.hasChunkAt(p) || !level.mayInteract(player, p)
                || !player.mayUseItemAt(p, event.getFace(), event.getItemStack())
                || level.getBlockState(p).getOptionalValue(BlockStateProperties.OCCUPIED).orElse(false))) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) return;
        if (positions.stream().allMatch(p -> level.getBlockState(p).getBlock() == target) && vanilla == null) return;
        replace(level, positions, old -> target.withPropertiesOf(old), vanilla, family.addon());
        if (!player.isCreative()) event.getItemStack().shrink(1);
        level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1, 1);
    }

    public static void replace(Level level, List<BlockPos> positions, java.util.function.Function<BlockState, BlockState> transform,
            DyeColor vanilla, String addon) {
        record Saved(BlockState state, CompoundTag entity) {}
        Map<BlockPos, Saved> snapshots = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            BlockEntity entity = level.getBlockEntity(pos);
            snapshots.put(pos, new Saved(level.getBlockState(pos), entity == null ? null : entity.saveWithFullMetadata(level.registryAccess())));
        }
        // Remove entities before changing identities so upstream onRemove cannot spill retained inventory.
        snapshots.forEach((pos, saved) -> { if (saved.entity() != null) level.removeBlockEntity(pos); });
        snapshots.forEach((pos, saved) -> {
            BlockState replacement = transform.apply(saved.state());
            if (vanilla != null && addon.equals("brewery")) replacement = replacement.setValue(net.satisfy.brewery.core.block.PatternedWoolBlock.COLOR, vanilla);
            level.setBlock(pos, replacement, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            BlockEntity entity = level.getBlockEntity(pos);
            if (saved.entity() != null && entity != null) {
                entity.loadWithComponents(saved.entity(), level.registryAccess());
                entity.setChanged();
                level.sendBlockUpdated(pos, replacement, replacement, Block.UPDATE_CLIENTS);
            }
        });
        // Notify only after every tent/bag part agrees on its block identity.
        snapshots.forEach((pos, saved) -> {
            BlockState state = level.getBlockState(pos);
            level.updateNeighborsAt(pos, state.getBlock());
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        });
    }
    private LetsDoDyeInteractions() {}
}
