package net.countered.terrainslabs.neoforge.client;

import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class TerrainSlabsNeoForgeClient {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        registerRenderLayers();
        registerBlockColorProviders();
        registerItemColorProviders();
    }

    @SuppressWarnings("deprecation")
    public static void registerRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocksRegistry.ICE_SLAB.get(), RenderType.translucent()
        );
        ItemBlockRenderTypes.setRenderLayer(
                ModBlocksRegistry.GRASS_SLAB.get(), RenderType.cutoutMipped()
        );
    }

    @SuppressWarnings("deprecation")
    public static void registerBlockColorProviders() {
        for (var entry : net.countered.terrainslabs.neoforge.compat.CompatSlabs.entries()) {
            var source = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(entry.source());
            Minecraft.getInstance().getBlockColors().register(
                    (state, world, pos, tintIndex) -> Minecraft.getInstance().getBlockColors()
                            .getColor(source.defaultBlockState(), world, pos, tintIndex), entry.slab().get());
        }
        Minecraft.getInstance().getBlockColors().register(
                (state, world, pos, tintIndex) ->
                        world != null && pos != null
                                ? Minecraft.getInstance().getBlockColors().getColor(
                                Blocks.GRASS_BLOCK.defaultBlockState(), world, pos, tintIndex)
                                : GrassColor.getDefaultColor(),
                ModBlocksRegistry.GRASS_SLAB.get()
        );
    }

    public static void registerItemColorProviders() {
        for (var entry : net.countered.terrainslabs.neoforge.compat.CompatSlabs.entries()) {
            var source = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(entry.source());
            Minecraft.getInstance().getItemColors().register(
                    (stack, tintIndex) -> Minecraft.getInstance().getBlockColors()
                            .getColor(source.defaultBlockState(), null, null, tintIndex), entry.item().get());
        }
        Minecraft.getInstance().getItemColors().register(
                (stack, tintIndex) ->
                        Minecraft.getInstance().getBlockColors()
                                .getColor(Blocks.GRASS_BLOCK.defaultBlockState(), null, null, tintIndex),
                ModBlocksRegistry.GRASS_SLAB.get()
        );
    }
}
