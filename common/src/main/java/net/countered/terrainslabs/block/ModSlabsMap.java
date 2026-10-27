package net.countered.terrainslabs.block;

import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModSlabsMap {

    private static final Map<Block, Block> SLAB_MAP = new HashMap<>();
    private static final Map<Block, Block> SOURCE_MAP = new HashMap<>();
    private static final Map<Block, Block> INTERACTION_MAP = new HashMap<>();

    private static final java.util.Set<Block> ADDON_SOIL_SLABS = new java.util.HashSet<>();

    static {
        SLAB_MAP.put(Blocks.STONE, ModBlocksRegistry.CUSTOM_STONE_SLAB.get());
        SLAB_MAP.put(Blocks.TUFF, ModBlocksRegistry.CUSTOM_TUFF_SLAB.get());
        SLAB_MAP.put(Blocks.SANDSTONE, ModBlocksRegistry.CUSTOM_SANDSTONE_SLAB.get());
        SLAB_MAP.put(Blocks.RED_SANDSTONE, ModBlocksRegistry.CUSTOM_RED_SANDSTONE_SLAB.get());
        SLAB_MAP.put(Blocks.ANDESITE, ModBlocksRegistry.CUSTOM_ANDESITE_SLAB.get());
        SLAB_MAP.put(Blocks.DIORITE, ModBlocksRegistry.CUSTOM_DIORITE_SLAB.get());
        SLAB_MAP.put(Blocks.GRANITE, ModBlocksRegistry.CUSTOM_GRANITE_SLAB.get());

        SLAB_MAP.put(Blocks.GRASS_BLOCK, ModBlocksRegistry.GRASS_SLAB.get());
        SLAB_MAP.put(Blocks.MYCELIUM, ModBlocksRegistry.MYCELIUM_SLAB.get());
        SLAB_MAP.put(Blocks.PODZOL, ModBlocksRegistry.PODZOL_SLAB.get());
        SLAB_MAP.put(Blocks.DIRT_PATH, ModBlocksRegistry.PATH_SLAB.get());

        SLAB_MAP.put(Blocks.GRAVEL, ModBlocksRegistry.GRAVEL_SLAB.get());
        SLAB_MAP.put(Blocks.SAND, ModBlocksRegistry.SAND_SLAB.get());
        SLAB_MAP.put(Blocks.RED_SAND, ModBlocksRegistry.RED_SAND_SLAB.get());

        SLAB_MAP.put(Blocks.DIRT, ModBlocksRegistry.DIRT_SLAB.get());
        SLAB_MAP.put(Blocks.MUD, ModBlocksRegistry.MUD_SLAB.get());
        SLAB_MAP.put(Blocks.COARSE_DIRT, ModBlocksRegistry.COARSE_SLAB.get());
        SLAB_MAP.put(Blocks.SNOW_BLOCK, ModBlocksRegistry.SNOW_SLAB.get());
        SLAB_MAP.put(Blocks.PACKED_ICE, ModBlocksRegistry.PACKED_ICE_SLAB.get());
        SLAB_MAP.put(Blocks.CLAY, ModBlocksRegistry.CLAY_SLAB.get());
        SLAB_MAP.put(Blocks.DEEPSLATE, ModBlocksRegistry.DEEPSLATE_SLAB.get());
        SLAB_MAP.put(Blocks.MOSS_BLOCK, ModBlocksRegistry.MOSS_SLAB.get());

        SLAB_MAP.put(Blocks.CALCITE, ModBlocksRegistry.CALCITE_SLAB.get());
        SLAB_MAP.put(Blocks.SMOOTH_BASALT, ModBlocksRegistry.SMOOTH_BASALT_SLAB.get());
        SLAB_MAP.put(Blocks.LIGHT_BLUE_TERRACOTTA, ModBlocksRegistry.LIGHT_BLUE_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.CYAN_TERRACOTTA, ModBlocksRegistry.CYAN_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.COBBLESTONE, ModBlocksRegistry.CUSTOM_COBBLESTONE_SLAB.get());
        SLAB_MAP.put(Blocks.MOSSY_COBBLESTONE, ModBlocksRegistry.CUSTOM_MOSSY_COBBLESTONE_SLAB.get());
        SLAB_MAP.put(Blocks.COBBLED_DEEPSLATE, ModBlocksRegistry.CUSTOM_COBBLED_DEEPSLATE_SLAB.get());
        SLAB_MAP.put(Blocks.ICE, ModBlocksRegistry.ICE_SLAB.get());
        SLAB_MAP.put(Blocks.ROOTED_DIRT, ModBlocksRegistry.ROOTED_DIRT_SLAB.get());
        SLAB_MAP.put(Blocks.PACKED_MUD, ModBlocksRegistry.PACKED_MUD_SLAB.get());
        SLAB_MAP.put(Blocks.BLUE_ICE, ModBlocksRegistry.BLUE_ICE_SLAB.get());
        SLAB_MAP.put(Blocks.BLACK_TERRACOTTA, ModBlocksRegistry.BLACK_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.PRISMARINE, ModBlocksRegistry.CUSTOM_PRISMARINE_SLAB.get());

        SLAB_MAP.put(Blocks.TERRACOTTA, ModBlocksRegistry.TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.RED_TERRACOTTA, ModBlocksRegistry.RED_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.ORANGE_TERRACOTTA, ModBlocksRegistry.ORANGE_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.LIGHT_GRAY_TERRACOTTA, ModBlocksRegistry.LIGHT_GRAY_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.WHITE_TERRACOTTA, ModBlocksRegistry.WHITE_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.BROWN_TERRACOTTA, ModBlocksRegistry.BROWN_TERRACOTTA_SLAB.get());
        SLAB_MAP.put(Blocks.YELLOW_TERRACOTTA, ModBlocksRegistry.YELLOW_TERRACOTTA_SLAB.get());

        SLAB_MAP.put(Blocks.SOUL_SAND, ModBlocksRegistry.SOUL_SAND_SLAB.get());
        SLAB_MAP.put(Blocks.SOUL_SOIL, ModBlocksRegistry.SOUL_SOIL_SLAB.get());
        SLAB_MAP.put(Blocks.NETHERRACK, ModBlocksRegistry.NETHERRACK_SLAB.get());
        SLAB_MAP.put(Blocks.WARPED_NYLIUM, ModBlocksRegistry.WARPED_NYLIUM_SLAB.get());
        SLAB_MAP.put(Blocks.CRIMSON_NYLIUM, ModBlocksRegistry.CRIMSON_NYLIUM_SLAB.get());
        SLAB_MAP.put(Blocks.BASALT, ModBlocksRegistry.BASALT_SLAB.get());
        SLAB_MAP.put(Blocks.BLACKSTONE, ModBlocksRegistry.CUSTOM_BLACKSTONE_SLAB.get());
        SLAB_MAP.put(Blocks.END_STONE, ModBlocksRegistry.ENDSTONE_SLAB.get());
        SLAB_MAP.forEach((source, slab) -> SOURCE_MAP.put(slab, source));
        SOURCE_MAP.put(ModBlocksRegistry.FARMLAND_SLAB.get(), Blocks.FARMLAND);
        INTERACTION_MAP.putAll(SLAB_MAP);
        INTERACTION_MAP.put(Blocks.FARMLAND, ModBlocksRegistry.FARMLAND_SLAB.get());
    }

    public static @Nullable Block getSlabForBlock(Block block) {
        return SLAB_MAP.get(block);
    }

    public static @Nullable Block getSourceForSlab(Block slab) { return SOURCE_MAP.get(slab); }
    public static void registerSourceAlias(Block slab,Block source) { SOURCE_MAP.putIfAbsent(slab,source); }

    public static @Nullable Block getInteractionSlab(Block source) { return INTERACTION_MAP.get(source); }

    public static void registerInteraction(Block source, Block slab) {
        SOURCE_MAP.put(slab, source);
        INTERACTION_MAP.put(source, slab);
    }

    public static boolean register(Block source, Block slab) {
        INTERACTION_MAP.put(source, slab);
        SOURCE_MAP.put(slab, source);
        return SLAB_MAP.put(source, slab) == null;
    }

    public static boolean registerSoilSlab(Block source, Block slab) {
        INTERACTION_MAP.put(source, slab);
        SOURCE_MAP.put(slab, source);
        boolean isNew = SLAB_MAP.put(source, slab) == null;
        ADDON_SOIL_SLABS.add(slab);
        return isNew;
    }

    public static boolean isSoilSlab(Block slab) {
        return SOIL_SLAB_BLOCKS.contains(slab) || ADDON_SOIL_SLABS.contains(slab);
    }

    public static Map<Block, Block> getMappings() {
        return java.util.Collections.unmodifiableMap(SLAB_MAP);
    }

    public static final Set<Block> SOIL_SLAB_BLOCKS = Set.of(
            ModBlocksRegistry.GRASS_SLAB.get(),
            ModBlocksRegistry.PODZOL_SLAB.get(),
            ModBlocksRegistry.MYCELIUM_SLAB.get(),
            ModBlocksRegistry.PATH_SLAB.get()
    );
}
