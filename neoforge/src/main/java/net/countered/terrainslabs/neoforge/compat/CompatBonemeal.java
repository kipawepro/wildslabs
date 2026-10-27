package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.generation.VegetationSupport;
import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

final class CompatBonemeal {
    private CompatBonemeal() {}
    static boolean valid(LevelReader level,BlockPos pos,BlockState state){
        if(!PlatformConfigHooks.isVegetationOnSlabsEnabled()||state.getValue(SlabBlock.WATERLOGGED))return false;
        var source=VegetationSupport.sourceStateForSlab(state);
        return source!=null&&source.getBlock() instanceof BonemealableBlock growth
                &&VegetationSupport.withSourceView(level,pos,()->growth.isValidBonemealTarget(level,pos,source));
    }
    static boolean succeeds(Level level,RandomSource random,BlockPos pos,BlockState state){
        var source=VegetationSupport.sourceStateForSlab(state);
        return source!=null&&source.getBlock() instanceof BonemealableBlock growth
                &&VegetationSupport.withSourceView(level,pos,()->growth.isBonemealSuccess(level,random,pos,source));
    }
    static void grow(ServerLevel level,RandomSource random,BlockPos pos,BlockState state){
        var source=VegetationSupport.sourceStateForSlab(state);
        if(source!=null&&source.getBlock() instanceof BonemealableBlock growth)VegetationSupport.withSourceView(level,pos,()->{
            growth.performBonemeal(level,random,pos,source);
            return null;
        });
    }
}
