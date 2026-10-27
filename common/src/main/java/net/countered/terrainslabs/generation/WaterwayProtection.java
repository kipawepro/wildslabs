package net.countered.terrainslabs.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public final class WaterwayProtection {
    private WaterwayProtection() {}

    public static boolean touchesFluid(BlockGetter level, BlockPos pos) {
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (!level.getFluidState(nearby).isEmpty()) return true;
        }
        return false;
    }
}
