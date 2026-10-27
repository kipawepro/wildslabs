package net.countered.terrainslabs.generation;

import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public final class VegetationTransfer {
    private VegetationTransfer() {}

    public static boolean canMove(BlockState state, WorldGenLevel level, BlockPos pos) {
        if(state.is(net.countered.terrainslabs.block.ModBlockTags.PRESERVED_BLOCKS))return false;
        if (state.getBlock() instanceof SnowLayerBlock) return PlatformConfigHooks.isSnowOnSlabsEnabled();
        return PlatformConfigHooks.isVegetationOnSlabsEnabled() && VegetationSupport.isCandidate(state)
                && state.getCollisionShape(level, pos).isEmpty();
    }

    public static boolean placeSlab(WorldGenLevel level, BlockPos pos, BlockState slab) {
        BlockState original = level.getBlockState(pos);

        if (!original.getFluidState().isEmpty() || original.liquid()) return false;
        if (original.isAir()) return level.setBlock(pos, slab, 3);
        List<BlockState> column = new ArrayList<>();
        BlockPos cursor = pos;
        while (canMove(level.getBlockState(cursor), level, cursor)) {
            if (column.size() == 16 || level.isOutsideBuildHeight(cursor.above())) return false;
            column.add(level.getBlockState(cursor));
            cursor = cursor.above();
        }
        if (column.isEmpty() || !level.isEmptyBlock(cursor)) return false;

        for (int i = column.size() - 1; i >= 0; i--) level.setBlock(pos.above(i + 1), column.get(i), 18);
        level.setBlock(pos, slab, 18);
        for (int i = 0; i < column.size(); i++) {
            if (!column.get(i).canSurvive(level, pos.above(i + 1))) {
                for (int j = 0; j < column.size(); j++) level.setBlock(pos.above(j), column.get(j), 18);
                level.setBlock(cursor, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 18);
                return false;
            }
        }
        level.blockUpdated(pos, slab.getBlock());
        slab.updateNeighbourShapes(level, pos, 3);
        return true;
    }
}
