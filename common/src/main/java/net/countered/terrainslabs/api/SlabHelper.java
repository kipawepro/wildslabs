package net.countered.terrainslabs.api;

import net.countered.terrainslabs.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public class SlabHelper {

    public static boolean isValidOntop( BlockState state ) {
        if (net.countered.terrainslabs.generation.VegetationSupport.isRootedColumn(state)) return false;
        return state.is( ModBlockTags.ON_TOP_BLOCKS )
                || OffsetClasses.isDefaultOntop( state.getBlock() )
                || net.countered.terrainslabs.platform.PlatformConfigHooks.isAutomaticModVegetationEnabled()
                    && net.countered.terrainslabs.platform.PlatformConfigHooks.isVegetationOnSlabsEnabled()
                    && net.countered.terrainslabs.generation.VegetationSupport.isCandidate(state);
    }

    public static <L extends BlockGetter> boolean isValidOntop( L level, BlockPos pos, BlockState state ) {
        if (!isValidOntop(state) || !IConditionalOffset.allowOntop(level, pos, state)) return false;
        if (state.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock) {
            return net.countered.terrainslabs.platform.PlatformConfigHooks.isSnowOnSlabsEnabled();
        }
        BlockPos soilPos = pos.below();
        if (state.hasProperty(net.minecraft.world.level.block.DoublePlantBlock.HALF)
                && state.getValue(net.minecraft.world.level.block.DoublePlantBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) soilPos = pos.below(2);
        BlockState soil = level.getBlockState(soilPos);
        if (!(soil.getBlock() instanceof net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab)) return false;
        var source = net.countered.terrainslabs.block.ModSlabsMap.getSourceForSlab(soil.getBlock());
        if (source == null) return false;
        boolean aquatic = state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
        boolean wetSlab = soil.hasProperty(SlabBlock.WATERLOGGED) && soil.getValue(SlabBlock.WATERLOGGED);
        if (wetSlab != aquatic && !state.is(ModBlockTags.WATER_OFFSET_BLOCKS)) return false;
        if (state.getBlock() instanceof net.minecraft.world.level.block.BushBlock) {
            return ((net.countered.terrainslabs.mixin.ontop.place.BushBlockAccessor)state.getBlock())
                    .wildslabs$mayPlaceOn(source.defaultBlockState(), level, soilPos);
        }
        if (level instanceof net.minecraft.world.level.LevelReader reader) return state.canSurvive(reader, pos);
        return source.defaultBlockState().is(BlockTags.DIRT) || source == net.minecraft.world.level.block.Blocks.FARMLAND;
    }

    public static <L extends BlockGetter> boolean isOffsetOntop( L level, BlockPos pos, BlockState state ) {
        return isValidOntop( level, pos, state )
                && isOffsetBase( state, level.getBlockState( pos.below() ) );
    }

    public static boolean isOffsetBase( BlockState state, BlockState belowState ) {
        if ( !belowState.is( BlockTags.SLABS )
                || !belowState.hasProperty( SlabBlock.TYPE )
                || belowState.getValue( SlabBlock.TYPE ) != SlabType.BOTTOM ) {
            return false;
        }
        boolean waterlogged = belowState.hasProperty( BlockStateProperties.WATERLOGGED )
                && belowState.getValue( BlockStateProperties.WATERLOGGED );
        boolean aquatic = state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
        return waterlogged ? aquatic || state.is(ModBlockTags.WATER_OFFSET_BLOCKS) : !aquatic;
    }
}
