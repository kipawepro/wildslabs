package net.countered.terrainslabs.generation;

import net.minecraft.core.BlockPos;

public final class TerrainRandom {
    private TerrainRandom() {}

    public static long value(long worldSeed, long seedOffset, BlockPos pos) {
        long value = worldSeed ^ Long.rotateLeft(seedOffset, 23) ^ pos.asLong() ^ 0x57494c44534c4142L;
        value = (value ^ value >>> 30) * 0xbf58476d1ce4e5b9L;
        value = (value ^ value >>> 27) * 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    public static boolean accepts(long worldSeed, long seedOffset, BlockPos pos, double chance) {
        if (chance >= 1) return true;
        if (chance <= 0) return false;
        return (value(worldSeed, seedOffset, pos) >>> 11) * 0x1.0p-53 < chance;
    }
}
