package net.countered.terrainslabs.neoforge.compat;

import net.countered.terrainslabs.block.customslabs.soilslabs.FarmlandSlab;
import net.countered.terrainslabs.platform.neoforge.PlatformConfigHooksImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class NeoForgeFarmlandSlab extends FarmlandSlab {
    public NeoForgeFarmlandSlab() { super(Properties.ofFullCopy(Blocks.FARMLAND)); }
    public NeoForgeFarmlandSlab(Properties properties) { super(properties); }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        if (facing != Direction.UP || !PlatformConfigHooksImpl.enableVegetationOnSlabs || state.getValue(WATERLOGGED)) return TriState.DEFAULT;
        return plant.getBlock() instanceof CropBlock || plant.getBlock() instanceof StemBlock
                || plant.getBlock() instanceof AttachedStemBlock ? TriState.TRUE : TriState.DEFAULT;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }
}
