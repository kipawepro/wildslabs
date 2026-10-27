package net.countered.terrainslabs.generation;

import net.countered.terrainslabs.block.ModBlockTags;
import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class VegetationSupport {
    private record Probe(BlockGetter level, BlockPos pos, BlockState source) {}
    private static final ThreadLocal<Probe> PROBE = new ThreadLocal<>();

    public static boolean isRootedColumn(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.BambooStalkBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.BambooSaplingBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SugarCaneBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.CactusBlock;
    }

    public static boolean isCandidate(BlockState state) {
        if (state.isAir() || state.liquid() || state.hasBlockEntity() || !state.getFluidState().isEmpty() || isRootedColumn(state)
                || state.is(BlockTags.FIRE) || state.getBlock() instanceof SaplingBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.MultifaceBlock
                || state.getBlock() instanceof SnowLayerBlock) return false;
        return state.getBlock() instanceof BushBlock || state.is(BlockTags.FLOWERS)
                || state.is(ModBlockTags.ON_TOP_BLOCKS) || state.canBeReplaced();
    }

    public static BlockState sourceDuringProbe(BlockGetter level, BlockPos pos) {
        Probe probe = PROBE.get();
        return probe != null && probe.level() == level && probe.pos().equals(pos) ? probe.source() : null;
    }

    public static boolean canSurviveOnSource(BlockState plant, LevelReader level, BlockPos pos) {
        if (PROBE.get() != null || !PlatformConfigHooks.isVegetationOnSlabsEnabled()
                || !PlatformConfigHooks.isAutomaticModVegetationEnabled() || !isCandidate(plant)) return false;
        BlockPos soilPos = pos.below();
        BlockState soil = level.getBlockState(soilPos);
        if (soil.hasProperty(BlockStateProperties.WATERLOGGED) && soil.getValue(BlockStateProperties.WATERLOGGED)) return false;
        var source = ModSlabsMap.getSourceForSlab(soil.getBlock());
        if (source == null) return false;
        PROBE.set(new Probe(level, soilPos.immutable(), source.defaultBlockState()));
        try {
            return plant.canSurvive(level, pos);
        } finally {
            PROBE.remove();
        }
    }
}
