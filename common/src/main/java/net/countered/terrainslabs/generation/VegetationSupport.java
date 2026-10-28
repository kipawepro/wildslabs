package net.countered.terrainslabs.generation;

import net.countered.terrainslabs.block.ModBlockTags;
import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class VegetationSupport {
    private record Probe(BlockGetter level, BlockPos pos, BlockState source, boolean surroundings) {}
    private static final ThreadLocal<Probe> PROBE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> READING = new ThreadLocal<>();

    public static boolean isRootedColumn(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.BambooStalkBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.BambooSaplingBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.SugarCaneBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.CactusBlock;
    }

    public static boolean isCandidate(BlockState state) {
        if (state.isAir() || state.liquid() || state.hasBlockEntity() || state.is(ModBlockTags.PRESERVED_BLOCKS) || !state.getFluidState().isEmpty() || isRootedColumn(state)
                || state.is(BlockTags.FIRE) || state.getBlock() instanceof SaplingBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.MultifaceBlock
                || state.getBlock() instanceof SnowLayerBlock) return false;
        return state.getBlock() instanceof BushBlock || state.is(BlockTags.FLOWERS) || state.is(ModBlockTags.SURFACE_ATTACHMENTS)
                || state.is(ModBlockTags.ON_TOP_BLOCKS) || state.canBeReplaced();
    }

    public static BlockState sourceDuringProbe(BlockGetter level, BlockPos pos) {
        Probe probe = PROBE.get();
        if(probe==null||probe.level()!=level||Boolean.TRUE.equals(READING.get()))return null;
        if(!probe.surroundings())return probe.pos().equals(pos)?probe.source():null;
        if(Math.abs(pos.getX()-probe.pos().getX())>16||Math.abs(pos.getY()-probe.pos().getY())>16||Math.abs(pos.getZ()-probe.pos().getZ())>16)return null;
        READING.set(true);
        try{return sourceStateForSlab(level.getBlockState(pos));}finally{READING.remove();}
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from,BlockState to,net.minecraft.world.level.block.state.properties.Property<T> property){
        return to.hasProperty(property)?to.setValue(property,from.getValue(property)):to;
    }

    public static BlockState sourceStateForSlab(BlockState slab){
        if(!(slab.getBlock() instanceof net.minecraft.world.level.block.SlabBlock))return null;
        var source=ModSlabsMap.getSourceForSlab(slab.getBlock());
        if(source==null)return null;
        var state=source.defaultBlockState();
        for(var property:slab.getProperties())state=copyProperty(slab,state,property);
        return state;
    }

    public static <T> T withSourceView(LevelReader level,BlockPos pos,java.util.function.Supplier<T> action){
        Probe previous=PROBE.get();
        PROBE.set(new Probe(level,pos.immutable(),null,true));
        try{return action.get();}finally{if(previous==null)PROBE.remove();else PROBE.set(previous);}
    }

    public static BlockState remapSourceWrite(BlockGetter level,BlockPos pos,BlockState requested){
        Probe probe=PROBE.get();
        if(probe==null||!probe.surroundings()||probe.level()!=level||Boolean.TRUE.equals(READING.get())
                ||requested.getBlock() instanceof net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab)return null;
        var target=ModSlabsMap.getInteractionSlab(requested.getBlock());
        if(target==null)return null;
        READING.set(true);
        BlockState current;
        try{current=level.getBlockState(pos);}finally{READING.remove();}
        if(!(current.getBlock() instanceof net.minecraft.world.level.block.SlabBlock)||ModSlabsMap.getSourceForSlab(current.getBlock())==null)return null;
        var result=net.countered.terrainslabs.util.MixinHelper.withCopiedSlabProperties(current,target);
        for(var property:requested.getProperties())result=copyProperty(requested,result,property);
        return result;
    }

    public static boolean canProbePlant(BlockState plant, LevelReader level, BlockPos pos) {
        if (PROBE.get() != null || !PlatformConfigHooks.isVegetationOnSlabsEnabled()
                || !PlatformConfigHooks.isAutomaticModVegetationEnabled() || !isCandidate(plant)) return false;
        BlockPos soilPos = pos.below();
        BlockState soil = level.getBlockState(soilPos);
        if (soil.hasProperty(BlockStateProperties.WATERLOGGED) && soil.getValue(BlockStateProperties.WATERLOGGED)) return false;
        return sourceStateForSlab(soil)!=null;
    }

    public static <T> T withPlantSoil(BlockState plant,LevelReader level,BlockPos pos,java.util.function.Supplier<T> action){
        BlockPos soilPos=pos.below();
        PROBE.set(new Probe(level,soilPos.immutable(),sourceStateForSlab(level.getBlockState(soilPos)),false));
        try {
            return action.get();
        } finally {
            PROBE.remove();
        }
    }

    public static boolean canSurviveOnSource(BlockState plant,LevelReader level,BlockPos pos){
        return canProbePlant(plant,level,pos)&&withPlantSoil(plant,level,pos,()->plant.canSurvive(level,pos));
    }
}
