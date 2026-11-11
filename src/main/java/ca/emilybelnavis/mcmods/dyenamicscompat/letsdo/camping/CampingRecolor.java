package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.camping;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.satisfy.camping.core.world.block.SleepingBagBlock;
import net.satisfy.camping.core.world.block.TentBlock;

public final class CampingRecolor {
    public static List<BlockPos> positions(Level level, BlockPos pos, BlockState state) {
        List<BlockPos> result = new ArrayList<>();
        if (state.getBlock() instanceof SleepingBagBlock) {
            BlockPos other = pos.relative(state.getValue(BedBlock.PART) == BedPart.FOOT ? state.getValue(BedBlock.FACING) : state.getValue(BedBlock.FACING).getOpposite());
            result.add(pos); result.add(other);
        } else if (state.getBlock() instanceof TentBlock) {
            BlockPos main = TentBlock.getMainPos(state, pos);
            for (TentBlock.TentPart part : TentBlock.TentPart.values()) {
                BlockPos lower = part.fromMain(main, state.getValue(TentBlock.FACING));
                result.add(lower); result.add(lower.above());
            }
        }
        return result.stream().allMatch(p -> level.hasChunkAt(p) && level.getBlockState(p).is(state.getBlock())) ? result : List.of();
    }
    private CampingRecolor() {}
}
