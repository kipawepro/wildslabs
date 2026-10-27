package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.customslabs.soilslabs.ISoilSlab;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.minecraft.world.level.block.state.BlockState;

public final class CompatSurfaceSlab extends CustomSlab implements ISoilSlab,net.minecraft.world.level.block.BonemealableBlock {
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder parameters){
        var nativeDrops=CompatDrops.nativeDrops(state,parameters);
        return nativeDrops==null?super.getDrops(state,parameters):nativeDrops;
    }

    private final CompatSlabs.Profile profile;
    public CompatSurfaceSlab(Properties properties, CompatSlabs.Profile profile) { super(properties); this.profile = profile; }
    @Override
    public BlockState baseFullBlock() { return CompatSlabs.baseBlock(profile).defaultBlockState(); }
    @Override
    public BlockState baseSlabBlock() { return CompatSlabs.baseSlab(profile).defaultBlockState(); }
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,net.minecraft.core.BlockPos pos,BlockState state){return CompatBonemeal.valid(level,pos,state);}
    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level,net.minecraft.util.RandomSource random,net.minecraft.core.BlockPos pos,BlockState state){return CompatBonemeal.succeeds(level,random,pos,state);}
    @Override
    public void performBonemeal(net.minecraft.server.level.ServerLevel level,net.minecraft.util.RandomSource random,net.minecraft.core.BlockPos pos,BlockState state){CompatBonemeal.grow(level,random,pos,state);}
}
