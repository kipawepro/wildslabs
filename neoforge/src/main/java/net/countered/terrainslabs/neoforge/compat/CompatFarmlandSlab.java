package net.countered.terrainslabs.neoforge.compat;

import net.minecraft.world.level.block.Block;

public final class CompatFarmlandSlab extends NeoForgeFarmlandSlab {
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder parameters){
        var nativeDrops=CompatDrops.nativeDrops(state,parameters);
        return nativeDrops==null?super.getDrops(state,parameters):nativeDrops;
    }

    private final CompatSlabs.Profile profile;
    public CompatFarmlandSlab(Properties properties, CompatSlabs.Profile profile) { super(properties); this.profile = profile; }
    @Override
    protected Block dirtSlab() { return CompatSlabs.baseSlab(profile); }

    @Override
    public boolean isFertile(net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){
        var source=net.countered.terrainslabs.generation.VegetationSupport.sourceStateForSlab(state);
        return source!=null&&source.isFertile(level,pos);
    }
}
