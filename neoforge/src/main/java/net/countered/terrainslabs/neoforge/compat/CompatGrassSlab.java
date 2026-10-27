package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.customslabs.soilslabs.GrassSlab;
import net.minecraft.world.level.block.state.BlockState;

public final class CompatGrassSlab extends GrassSlab {

    private final CompatSlabs.Profile profile;
    public CompatGrassSlab(Properties properties, CompatSlabs.Profile profile) { super(properties); this.profile = profile; }
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
