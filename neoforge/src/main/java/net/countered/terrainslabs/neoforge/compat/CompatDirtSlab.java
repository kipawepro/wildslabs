package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public final class CompatDirtSlab extends CustomSlab implements net.minecraft.world.level.block.BonemealableBlock {
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder parameters){
        var nativeDrops=CompatDrops.nativeDrops(state,parameters);
        return nativeDrops==null?super.getDrops(state,parameters):nativeDrops;
    }

    private final List<CompatSlabs.Profile> grass;
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,BlockPos pos,BlockState state){return CompatBonemeal.valid(level,pos,state);}
    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level,RandomSource random,BlockPos pos,BlockState state){return CompatBonemeal.succeeds(level,random,pos,state);}
    @Override
    public void performBonemeal(ServerLevel level,RandomSource random,BlockPos pos,BlockState state){CompatBonemeal.grow(level,random,pos,state);}
    public CompatDirtSlab(Properties properties,CompatSlabs.Profile profile) {
        super(CompatSlabs.grassCovers(profile.source()).isEmpty()?properties:properties.randomTicks());
        grass=CompatSlabs.grassCovers(profile.source());
    }
    @Override
    public void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if(state.getValue(WATERLOGGED)||level.getMaxLocalRawBrightness(pos.above())<9
                ||level.getBlockState(pos.above()).canOcclude()||!level.getFluidState(pos.above()).isEmpty())return;
        for(var profile:grass){
            var source=BuiltInRegistries.BLOCK.get(ResourceLocation.parse(profile.source()));
            var slab=ModSlabsMap.getSlabForBlock(source);
            if(slab==null)continue;
            for(BlockPos nearby:BlockPos.betweenClosed(pos.offset(-1,-1,-1),pos.offset(1,1,1))){
                var neighbor=level.getBlockState(nearby);
                if(neighbor.is(source)||neighbor.is(slab)){
                    level.setBlockAndUpdate(pos,MixinHelper.withCopiedSlabProperties(state,slab));
                    return;
                }
            }
        }
    }
}
