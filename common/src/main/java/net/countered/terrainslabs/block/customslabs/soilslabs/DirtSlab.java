package net.countered.terrainslabs.block.customslabs.soilslabs;

import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class DirtSlab extends CustomSlab {
    public DirtSlab(Properties properties) { super(properties.randomTicks()); }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WATERLOGGED) || level.getMaxLocalRawBrightness(pos.above()) < 9
                || level.getBlockState(pos.above()).canOcclude() || !level.getFluidState(pos.above()).isEmpty()) return;

        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1,-1,-1),pos.offset(1,1,1))) {
            BlockState neighbor = level.getBlockState(nearby);
            if (neighbor.is(Blocks.GRASS_BLOCK) || neighbor.is(ModBlocksRegistry.GRASS_SLAB.get())) {
                level.setBlockAndUpdate(pos, MixinHelper.withCopiedSlabProperties(state, ModBlocksRegistry.GRASS_SLAB.get()));
                return;
            }
        }
    }
}
