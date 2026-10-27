package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.generation.VegetationSupport;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import java.util.List;

final class CompatDrops {
    private CompatDrops() {}
    static List<ItemStack> nativeDrops(BlockState state,LootParams.Builder parameters){
        if(!state.getValue(CustomSlab.GENERATED))return null;
        var source=VegetationSupport.sourceStateForSlab(state);
        return source==null?null:source.getDrops(parameters.withParameter(LootContextParams.BLOCK_STATE,source));
    }
}
