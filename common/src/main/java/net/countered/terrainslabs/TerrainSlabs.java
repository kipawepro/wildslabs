package net.countered.terrainslabs;

import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.countered.terrainslabs.registries.data.FlattenableData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TerrainSlabs {

    public static final String MOD_ID = "wildslabs";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Initializing WildSlabs...");
        ModBlocksRegistry.registerModBlocks();
        FlattenableData.init();
    }
}
