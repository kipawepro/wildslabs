package net.countered.terrainslabs.platform.neoforge;

import eu.midnightdust.lib.config.MidnightConfig;

public class PlatformConfigHooksImpl extends MidnightConfig {
    public static net.minecraft.world.ItemInteractionResult useSourceItem(net.minecraft.world.item.ItemStack stack,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        var source=net.countered.terrainslabs.generation.VegetationSupport.sourceStateForSlab(state);
        if(source==null)return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return net.countered.terrainslabs.generation.VegetationSupport.withSourceView(level,pos,()->source.useItemOn(stack,level,player,hand,hit));
    }
    public static net.minecraft.world.level.block.Block getToolTarget(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.item.context.UseOnContext context, boolean hoe) {
        var source=net.countered.terrainslabs.block.ModSlabsMap.getSourceForSlab(state.getBlock());
        if(source==null)return null;
        var result=net.countered.terrainslabs.generation.VegetationSupport.withSourceView(context.getLevel(),context.getClickedPos(),()->source.defaultBlockState().getToolModifiedState(context,hoe?net.neoforged.neoforge.common.ItemAbilities.HOE_TILL:net.neoforged.neoforge.common.ItemAbilities.SHOVEL_FLATTEN,false));
        return result==null?null:net.countered.terrainslabs.block.ModSlabsMap.getInteractionSlab(result.getBlock());
    }
    public static net.minecraft.world.level.block.Block createFarmlandSlab() {
        return new net.countered.terrainslabs.neoforge.compat.NeoForgeFarmlandSlab();
    }

    public static final String GENERATION = "generation";
    public static final String RENDERING = "rendering";

    @Entry(category = GENERATION)
    public static long terrainSeedOffset = 0L;
    public static long getTerrainSeedOffset() { return terrainSeedOffset; }

    @Entry(category = GENERATION, min = 0, max = 1)
    public static double slabPlacementChance = 0.65;
    public static double getSlabPlacementChance() { return slabPlacementChance; }

    @Entry(category = GENERATION)
    public static boolean preserveWaterways = true;
    public static boolean isWaterwayProtectionEnabled() { return preserveWaterways; }

    @Entry(category = GENERATION)
    public static boolean automaticModVegetationCompatibility = true;
    public static boolean isAutomaticModVegetationEnabled() { return automaticModVegetationCompatibility; }

    @Entry(category = RENDERING)
    public static boolean enableBetterGrassCompatibility = false;

    @Entry(category = GENERATION)
    public static boolean enableSlabGeneration = true;
    public static boolean isSlabGenerationEnabled() {
        return enableSlabGeneration;
    }

    @Entry(category = GENERATION)
    public static boolean enableVegetationOnSlabs = true;
    public static boolean isVegetationOnSlabsEnabled() {
        return enableVegetationOnSlabs;
    }

    @Entry(category = GENERATION)
    public static boolean enableSnowOnSlabs = true;
    public static boolean isSnowOnSlabsEnabled() {
        return enableSnowOnSlabs;
    }

    @Entry(category = GENERATION)
    public static boolean enableExperimentalFeatures = false;

    @Comment(category = GENERATION, name = "Experimental Features:")
    public static Comment needsExperimentalEnabled;

    @Condition(requiredOption = "enableExperimentalFeatures", visibleButLocked = true)
    @Entry(category = GENERATION)
    public static boolean enableCornerSlabs = false;
    public static boolean isCornerSlabsEnabled() {
        return enableCornerSlabs;
    }

    @Condition(requiredOption = "enableExperimentalFeatures", visibleButLocked = true)
    @Entry(category = GENERATION, isSlider = true, min = 1, max = 8)
    public static int slabRunLength = 1;
    public static int getSlabRunLength() {
        return slabRunLength;
    }

    @Entry(category = RENDERING, isSlider = true, min = 0, max = 1f)
    public static float adjustSlabAo = 0.5f;
    public static float getAoStrength() {
        return adjustSlabAo;
    }
}
