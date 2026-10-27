package net.countered.terrainslabs.block.customslabs.specialslabs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.function.Supplier;

public class HalfTransparentSlab extends CustomSlab {

    private final Supplier<Block> fullBlockEquivalent;
    private Block cachedFullBlock;
    private boolean fullBlockResolved;

    public HalfTransparentSlab(BlockBehaviour.Properties properties) {
        this(properties, null);
    }

    public HalfTransparentSlab(BlockBehaviour.Properties properties, Supplier<Block> fullBlockEquivalent) {
        super(properties);
        this.fullBlockEquivalent = fullBlockEquivalent;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (EnchantmentHelper.hasTag(tool, EnchantmentTags.PREVENTS_ICE_MELTING)) {
            return;
        }
        if (level.dimensionType().ultraWarm()) {
            level.removeBlock(pos, false);
            return;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.blocksMotion() || below.liquid()) {
            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        }
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        SlabType self = state.getValue(TYPE);

        if (isFullBlockEquivalent(neighborState)) {
            return faceIsSolid(self, direction);
        }

        if (neighborState.is(this)) {
            SlabType other = neighborState.getValue(TYPE);
            if (self == SlabType.DOUBLE) {
                return coversFace(other, direction.getOpposite());
            }
            switch (direction) {
                case UP:
                    return self == SlabType.TOP && coversFace(other, Direction.DOWN);
                case DOWN:
                    return self == SlabType.BOTTOM && coversFace(other, Direction.UP);
                default:
                    return other == SlabType.DOUBLE || self == other;
            }
        }

        return super.skipRendering(state, neighborState, direction);
    }

    public boolean isFullBlockEquivalent(BlockState neighborState) {
        if (fullBlockEquivalent == null) return false;
        if (!fullBlockResolved) {
            cachedFullBlock = fullBlockEquivalent.get();
            fullBlockResolved = true;
        }
        return cachedFullBlock != null && neighborState.is(cachedFullBlock);
    }

    private static boolean faceIsSolid(SlabType self, Direction direction) {
        return switch (direction) {
            case UP -> self == SlabType.TOP || self == SlabType.DOUBLE;
            case DOWN -> self == SlabType.BOTTOM || self == SlabType.DOUBLE;

            default -> true;
        };
    }

    private static boolean coversFace(SlabType t, Direction face) {
        if (t == SlabType.DOUBLE) return true;
        return switch (face) {
            case DOWN -> t == SlabType.BOTTOM;
            case UP -> t == SlabType.TOP;
            default -> false;
        };
    }
}
