package net.countered.terrainslabs.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface ICustomOffsetConversion {

    <L extends LevelAccessor> @NotNull BlockState onSetOntop(L level, BlockPos pos, BlockState state);

    <L extends LevelAccessor> @NotNull BlockState onSetOnbottom( L level, BlockPos pos, BlockState state);

}
