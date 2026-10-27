package net.countered.terrainslabs.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class SlabAttachments {
    public static boolean isLight(BlockState state) {
        return state.getBlock() instanceof LanternBlock
                || state.getBlock() instanceof BaseTorchBlock && !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING);
    }

    public static boolean isHanging(BlockState state) {
        return state.getBlock() instanceof LanternBlock && state.getValue(LanternBlock.HANGING);
    }

    public static boolean canAttach(BlockState state, BlockGetter level, BlockPos pos) {
        if (!isLight(state)) return false;
        var supportPos = isHanging(state) ? pos.above() : pos.below();
        var support = level.getBlockState(supportPos);
        if (!(support.getBlock() instanceof SlabBlock)) return false;
        if (state.getBlock() instanceof BaseTorchBlock && support.hasProperty(SlabBlock.WATERLOGGED) && support.getValue(SlabBlock.WATERLOGGED)) return false;
        return support.getCollisionShape(level, supportPos).toAabbs().stream().anyMatch(box ->
                box.minX <= 0.5 && box.maxX >= 0.5 && box.minZ <= 0.5 && box.maxZ >= 0.5 && box.maxY > box.minY);
    }

    public static double offsetY(BlockState state, BlockGetter level, BlockPos pos) {
        if (!canAttach(state, level, pos)) return 0;
        var supportPos = isHanging(state) ? pos.above() : pos.below();
        var shape = level.getBlockState(supportPos).getCollisionShape(level, supportPos);
        return isHanging(state) ? shape.min(Direction.Axis.Y) : shape.max(Direction.Axis.Y) - 1;
    }
}
