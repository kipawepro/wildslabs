package net.countered.terrainslabs.block.customslabs.soilslabs;

import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmlandSlab extends CustomSlab {
    protected Block dirtSlab() { return ModBlocksRegistry.DIRT_SLAB.get(); }
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    private static final VoxelShape BOTTOM = Block.box(0, 0, 0, 16, 7, 16);
    private static final VoxelShape TOP = Block.box(0, 8, 0, 16, 15, 16);
    private static final VoxelShape DOUBLE = Block.box(0, 0, 0, 16, 15, 16);

    public FarmlandSlab(Properties properties) {
        super(properties.randomTicks().noOcclusion());
        registerDefaultState(defaultBlockState().setValue(MOISTURE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MOISTURE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(TYPE)) {
            case BOTTOM -> BOTTOM;
            case TOP -> TOP;
            case DOUBLE -> DOUBLE;
        };
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean wet = state.getValue(WATERLOGGED) || level.isRainingAt(pos.above());
        if (!wet) {
            for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
                if (level.getFluidState(nearby).is(FluidTags.WATER)) {
                    wet = true;
                    break;
                }
            }
        }
        int moisture = state.getValue(MOISTURE);
        if (wet && moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        } else if (!wet && moisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
        } else if (!wet && !level.getBlockState(pos.above()).is(BlockTags.CROPS)) {
            level.setBlockAndUpdate(pos, MixinHelper.withCopiedSlabProperties(state, dirtSlab()));
        }
    }
}
