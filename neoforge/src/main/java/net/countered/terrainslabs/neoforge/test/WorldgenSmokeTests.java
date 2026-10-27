package net.countered.terrainslabs.neoforge.test;

import com.google.gson.GsonBuilder;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class WorldgenSmokeTests {
    public static void register() {
        if (!Boolean.getBoolean("wildslabs.worldgenSmoke")) return;
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> {
            event.getServer().getWorldData().getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
            event.getServer().getWorldData().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, null);
        });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> run(event.getServer()));
    }

    private static void run(net.minecraft.server.MinecraftServer server) {
        var report = new java.util.TreeMap<String, Object>();
        try {
            for (var level : server.getAllLevels()) {
                long slabs = 0;
                long fluids = 0;
                long plants = 0;
                var materials = new java.util.TreeMap<String, Integer>();
                var biomes = new java.util.TreeSet<String>();
                for (int coordinate : new int[]{0, 16}) {
                    var chunk = level.getChunk(coordinate, coordinate);
                    var pos = new BlockPos.MutableBlockPos();
                    int minX = chunk.getPos().getMinBlockX();
                    int minZ = chunk.getPos().getMinBlockZ();
                    for (int x = minX; x < minX + 16; x++) {
                        for (int z = minZ; z < minZ + 16; z++) {
                            biomes.add(level.getBiome(pos.set(x, level.getSeaLevel(), z)).unwrapKey().orElseThrow().location().toString());
                            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                                var state = chunk.getBlockState(pos.set(x, y, z));
                                if (state.hasProperty(CustomSlab.GENERATED) && state.getValue(CustomSlab.GENERATED)) {
                                    slabs++;
                                    materials.merge(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), 1, Integer::sum);
                                }
                                if (!state.getFluidState().isEmpty()) fluids++;
                                if (state.getBlock() instanceof net.minecraft.world.level.block.BushBlock) plants++;
                            }
                        }
                    }
                }
                report.put(level.dimension().location().toString(), java.util.Map.of("chunks", 2, "generatedSlabs", slabs, "fluidBlocks", fluids, "plantBlocks", plants, "materials", materials, "biomes", biomes));
                org.slf4j.LoggerFactory.getLogger("WildSlabsTests").info("Worldgen smoke {}: {} slabs, {} fluid blocks, {} plant blocks", level.dimension().location(), slabs, fluids, plants);
            }
            for (var dimension : java.util.Map.of("aether", "aether:the_aether", "twilightforest", "twilightforest:twilight_forest").entrySet()) {
                if (net.neoforged.fml.ModList.get().isLoaded(dimension.getKey()) && !report.containsKey(dimension.getValue())) throw new IllegalStateException("Missing dimension: " + dimension.getValue());
            }
            java.nio.file.Files.writeString(java.nio.file.Path.of("worldgen-smoke.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
            org.slf4j.LoggerFactory.getLogger("WildSlabsTests").info("Worldgen smoke completed in {} dimensions", report.size());
        } catch (Exception exception) {
            throw new IllegalStateException("Worldgen smoke test failed", exception);
        } finally {
            server.halt(false);
        }
    }
}
