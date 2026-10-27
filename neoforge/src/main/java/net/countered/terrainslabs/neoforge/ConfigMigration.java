package net.countered.terrainslabs.neoforge;

import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;

final class ConfigMigration {
    private ConfigMigration() {}
    static boolean hasOldFullDensityDefault() {
        var path = FMLPaths.CONFIGDIR.get().resolve("wildslabs.json");
        if (!Files.isRegularFile(path)) return false;
        try (var reader = Files.newBufferedReader(path)) {
            var config = JsonParser.parseReader(reader).getAsJsonObject();
            return !config.has("preserveWaterways") && config.has("slabPlacementChance")
                    && config.get("slabPlacementChance").getAsDouble() == 1.0;
        } catch (java.io.IOException | RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger("WildSlabs").warn("Cannot inspect legacy worldgen settings; leaving migration to config loader", exception);
            return false;
        }
    }
}
