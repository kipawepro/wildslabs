package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.customslabs.soilslabs.PathSlab;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class CompatPathSlab extends PathSlab {
    private final CompatSlabs.Profile profile;
    public CompatPathSlab(Properties properties, CompatSlabs.Profile profile) {
        super(properties);
        this.profile = profile;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlockAndUpdate(pos, MixinHelper.withCopiedSlabProperties(state, CompatSlabs.baseSlab(profile)));
    }
}
