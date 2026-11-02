package net.countered.terrainslabs.neoforge;

import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;

public final class ConfigMigration {
    private ConfigMigration() {}
    public static boolean shouldUpgradeDensity(com.google.gson.JsonObject config){
        if(config.has("terrainDensityVersion")&&config.get("terrainDensityVersion").getAsInt()>=1)return false;
        if(!config.has("slabPlacementChance"))return false;
        double chance=config.get("slabPlacementChance").getAsDouble();
        return chance==0.65||chance==1.0&&!config.has("preserveWaterways");
    }
    static boolean hasOldFullDensityDefault() {
        var path = FMLPaths.CONFIGDIR.get().resolve("wildslabs.json");
        if (!Files.isRegularFile(path)) return false;
        try (var reader = Files.newBufferedReader(path)) {
            var config = JsonParser.parseReader(reader).getAsJsonObject();
            return shouldUpgradeDensity(config);
        } catch (java.io.IOException | RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger("WildSlabs").warn("Cannot inspect legacy worldgen settings; leaving migration to config loader", exception);
            return false;
        }
    }
}
