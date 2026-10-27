package net.countered.terrainslabs.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;
import net.countered.terrainslabs.TerrainSlabs;
import net.countered.terrainslabs.neoforge.client.NeoForgeBuiltinPacks;
import net.countered.terrainslabs.neoforge.client.TerrainSlabsNeoForgeClient;
import net.countered.terrainslabs.neoforge.feature.ModFeatures;
import net.countered.terrainslabs.platform.neoforge.PlatformConfigHooksImpl;
import net.countered.terrainslabs.registries.FlattenableBlockRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(TerrainSlabs.MOD_ID)
public final class TerrainSlabsNeoForge {

    public TerrainSlabsNeoForge(IEventBus modBus) {
        boolean migrateFullDensity = ConfigMigration.hasOldFullDensityDefault();
        MidnightConfig.init(TerrainSlabs.MOD_ID, PlatformConfigHooksImpl.class);
        if (migrateFullDensity) PlatformConfigHooksImpl.slabPlacementChance = 0.65;

        MidnightConfig.write(TerrainSlabs.MOD_ID);
        ModFeatures.FEATURES.register(modBus);
        net.countered.terrainslabs.neoforge.feature.LateTerrainModifier.SERIALIZERS.register(modBus);

        modBus.addListener(this::setupServer);
        modBus.addListener(this::setupClient);
        modBus.addListener(this::setupPack);

        TerrainSlabs.init();
        net.countered.terrainslabs.neoforge.compat.CompatSlabs.register(modBus);
        net.countered.terrainslabs.neoforge.test.WildSlabsGameTests.registerFixtures(modBus);
        net.countered.terrainslabs.neoforge.test.WorldgenSmokeTests.register();
        if(Boolean.getBoolean("wildslabs.integrationTests")&&net.neoforged.fml.loading.FMLEnvironment.production)
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post event)->net.minecraft.gametest.framework.GameTestTicker.SINGLETON.tick());
    }

    private void setupServer(FMLCommonSetupEvent event) {
        event.enqueueWork(FlattenableBlockRegistry::apply);
        event.enqueueWork(net.countered.terrainslabs.neoforge.compat.CompatSlabs::setup);
        if(Boolean.getBoolean("wildslabs.integrationTests"))event.enqueueWork(()->net.minecraft.gametest.framework.GameTestRegistry.register(net.countered.terrainslabs.neoforge.test.WildSlabsGameTests.class));
    }

    private void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TerrainSlabsNeoForgeClient.init(event);
        });
    }

    private void setupPack(AddPackFindersEvent event) {
        NeoForgeBuiltinPacks.addPack(event);
    }
}
