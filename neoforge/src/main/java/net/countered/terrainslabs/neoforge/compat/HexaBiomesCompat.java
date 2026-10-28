package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class HexaBiomesCompat {
    public static void register(){
        if(ModList.get().isLoaded("hexabiomes"))NeoForge.EVENT_BUS.addListener(HexaBiomesCompat::onNeighborNotify);
    }

    private static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event){
        var level=event.getLevel();
        if(level.isClientSide())return;
        wet(level,event.getPos());
        if(event.getState().getFluidState().is(FluidTags.WATER))for(var direction:Direction.values())wet(level,event.getPos().relative(direction));
    }

    private static void wet(LevelAccessor level,BlockPos pos){
        var current=level.getBlockState(pos);
        if(!(current.getBlock() instanceof CustomSlab)||ModSlabsMap.getSourceForSlab(current.getBlock())!=Blocks.SAND)return;
        boolean water=current.getFluidState().is(FluidTags.WATER);
        for(var direction:Direction.values())water|=level.getFluidState(pos.relative(direction)).is(FluidTags.WATER);
        if(!water)return;
        var source=BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("hexabiomes","wet_sand"));
        Block slab=ModSlabsMap.getInteractionSlab(source);
        if(slab!=null)level.setBlock(pos,MixinHelper.withCopiedSlabProperties(current,slab),Block.UPDATE_ALL);
    }
}
