package net.countered.terrainslabs.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IConditionalOffset {

    static <L extends BlockGetter> boolean allowOntop( L level, BlockPos pos, BlockState state ) {
        return !( state.getBlock() instanceof IConditionalOffset conditional && !conditional.couldBeOntop( level, pos, state ) );
    }
    static <L extends BlockGetter> boolean allowOnbottom( L level, BlockPos pos, BlockState state ) {
        return !( state.getBlock() instanceof IConditionalOffset conditional && !conditional.couldBeOnbottom( level, pos, state ) );
    }

    <L extends BlockGetter> boolean couldBeOntop(L level, BlockPos pos, BlockState state );

    <L extends BlockGetter> boolean couldBeOnbottom(L level, BlockPos pos, BlockState state );

}
