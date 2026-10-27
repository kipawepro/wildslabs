package net.countered.terrainslabs.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class PlatformConfigHooks {
    @ExpectPlatform
    public static boolean isWaterwayProtectionEnabled() { throw new AssertionError(); }
    @ExpectPlatform
    public static long getTerrainSeedOffset() { throw new AssertionError(); }
    @ExpectPlatform
    public static double getSlabPlacementChance() { throw new AssertionError(); }
    @ExpectPlatform
    public static boolean isAutomaticModVegetationEnabled() { throw new AssertionError(); }
    @ExpectPlatform
    public static net.minecraft.world.level.block.Block createFarmlandSlab() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isSlabGenerationEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isVegetationOnSlabsEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isSnowOnSlabsEnabled() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static float getAoStrength() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getSlabRunLength() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isCornerSlabsEnabled() {
        throw new AssertionError();
    }
}
