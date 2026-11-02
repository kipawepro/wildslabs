package net.countered.terrainslabs.generation;

import com.mojang.serialization.Codec;
import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.block.customslabs.soilslabs.ISoilSlab;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.*;

public class SlabFeature extends Feature<NoneFeatureConfiguration> {

    public SlabFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (PlatformConfigHooks.isSlabGenerationEnabled()) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            try(var protection=TerrainProtection.begin(level,origin)){generateSlabs(level, origin);}
            return true;
        }
        return false;
    }

    private void generateSlabs(WorldGenLevel level, BlockPos origin) {
        Set<BlockPos> botSlabPositions = new HashSet<>();
        Set<BlockPos> topSlabPositions = new HashSet<>();
        Set<BlockPos> extendedPositions = new HashSet<>();

        ChunkPos chunkPos = new ChunkPos(origin);
        int minY = level.getMinBuildHeight();
        int offsetXZ = PlatformConfigHooks.isCornerSlabsEnabled() ? 1 : 0;
        for (int x = -offsetXZ; x < 16 + offsetXZ ; x++) {
            for (int z = -offsetXZ; z < 16 + offsetXZ; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                int maxY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ);
                for (int y = maxY; y >= minY; y--) {
                    BlockPos currentPos = new BlockPos(worldX, y, worldZ);
                    if (shouldPlaceBottomSlab(level, currentPos, y == maxY-1, false)) {
                        botSlabPositions.add(currentPos);
                    } else if (shouldPlaceTopSlab(level, currentPos)) {
                        topSlabPositions.add(currentPos);
                    }
                }
            }
        }
        placeBottomSlabs(level, botSlabPositions);
        placeTopSlabs(level, topSlabPositions);
        if (PlatformConfigHooks.getSlabRunLength() > 1) {
            generateExtended(level, botSlabPositions, extendedPositions);
        }
        else if (PlatformConfigHooks.isCornerSlabsEnabled()) {
            calculateCornerPositions(level, botSlabPositions, extendedPositions);
            placeBottomSlabs(level, extendedPositions);
        }
    }

    private void placeBottomSlabs(WorldGenLevel level, Set<BlockPos> slabPositions) {
        for (BlockPos pos : slabPositions.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList()) {
            placeBottomSlab(level, pos);
        }
    }

    private void placeTopSlabs(WorldGenLevel level, Set<BlockPos> topSlabPositions) {
        for (BlockPos pos : topSlabPositions.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList()) {
            placeTopSlab(level, pos);
        }
    }

    private void generateExtended(WorldGenLevel level, Set<BlockPos> botSlabPositions, Set<BlockPos> extendedPositions) {
        for (int i = 1; i < PlatformConfigHooks.getSlabRunLength(); i++) {
            extendedPositions.clear();
            calculateExtendedPositions(level, botSlabPositions, extendedPositions);
            placeBottomSlabs(level, extendedPositions);
            botSlabPositions = Set.copyOf(extendedPositions);
        }
    }

    private void calculateExtendedPositions(WorldGenLevel level, Set<BlockPos> botSlabPositions, Set<BlockPos> extendedPositions) {
        for (BlockPos pos : botSlabPositions) {
            for (Direction direction : Direction.values()) {
                BlockPos extendedPos = pos.relative(direction);
                if (shouldPlaceBottomSlab(level, extendedPos, false, true)) {
                    extendedPositions.add(extendedPos);
                }
            }
        }
    }

    private void calculateCornerPositions(WorldGenLevel level, Set<BlockPos> botSlabPositions, Set<BlockPos> cornerPositions) {
        int[][] diagonals = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (BlockPos currentPos : botSlabPositions) {
            for (int[] d : diagonals) {
                int nx = currentPos.getX() + d[0];
                int nz = currentPos.getZ() + d[1];
                int y = currentPos.getY();

                if (!botSlabPositions.contains(new BlockPos(nx, y, nz))) continue;

                BlockPos corner1 = new BlockPos(currentPos.getX(), y, nz);
                BlockPos corner2 = new BlockPos(nx, y, currentPos.getZ());

                if (shouldPlaceBottomSlab(level, corner1, false, true)) cornerPositions.add(corner1);
                if (shouldPlaceBottomSlab(level, corner2, false, true)) cornerPositions.add(corner2);
            }
        }
    }

    private boolean shouldPlaceBottomSlab(WorldGenLevel level, BlockPos currentPos, boolean isMaxY, boolean neighbourCanBeSlab) {
        if(TerrainProtection.isProtected(level,currentPos))return false;
        if (!TerrainRandom.accepts(level.getSeed(), PlatformConfigHooks.getTerrainSeedOffset(), currentPos,
                PlatformConfigHooks.getSlabPlacementChance())) return false;
        if (!isPosUpDownValid(level, currentPos)) return false;
        if(TerrainProtection.hasFixedAttachment(level,currentPos))return false;
        if(TerrainProtection.hasSteepDrop(level,currentPos))return false;
        if (PlatformConfigHooks.isWaterwayProtectionEnabled() && WaterwayProtection.touchesFluid(level, currentPos)) return false;

        Biome biome = level.getBiome(currentPos).value();
        if (isMaxY && biome.shouldFreeze(level, currentPos, false)) return false;

        if (!validSurroundingBottom(level, currentPos, neighbourCanBeSlab)) return false;

        return true;
    }

    private static boolean isPosUpDownValid(WorldGenLevel level, BlockPos currentPos) {
        BlockState currentBlockState = level.getBlockState(currentPos);
        if(currentBlockState.is(net.countered.terrainslabs.block.ModBlockTags.PRESERVED_BLOCKS))return false;
        if (!currentBlockState.isAir()&&!VegetationTransfer.canMove(currentBlockState, level, currentPos)) return false;
        BlockState blockAboveState = level.getBlockState(currentPos.above());
        if (!blockAboveState.getCollisionShape(level, currentPos.above()).isEmpty()) return false;
        BlockState blockBelowState = level.getBlockState(currentPos.below());
        if (!TerrainProtection.isFullTerrain(level,currentPos.below())) return false;
        return true;
    }

    private boolean validSurroundingBottom(WorldGenLevel level, BlockPos currentPos, boolean neighbourCanBeSlab) {
        boolean validNeighbors = false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = currentPos.relative(direction);
            BlockPos oppositePos = currentPos.relative(direction.getOpposite());
            BlockPos belowOppositePos = oppositePos.below();
            BlockState neighborState = level.getBlockState(neighborPos);
            BlockState oppositeState = level.getBlockState(oppositePos);
            BlockState belowOppositeState = level.getBlockState(belowOppositePos);

            if (neighborState.is(Blocks.LAVA)) return false;

            if (((TerrainProtection.isFullTerrain(level,neighborPos))
                    || (neighborState.hasProperty(CustomSlab.GENERATED)&&neighborState.getValue(CustomSlab.GENERATED)&&neighbourCanBeSlab))
                    && TerrainProtection.isOpen(level,oppositePos)
                    && TerrainProtection.isFullTerrain(level,belowOppositePos)
                    && (level.getBlockState(neighborPos.above()).getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()))
            {
                validNeighbors = true;
            }
        }
        return validNeighbors;
    }

    private void placeBottomSlab(WorldGenLevel level, BlockPos pos) {
        if(TerrainProtection.isProtected(level,pos)||!isPosUpDownValid(level,pos)||TerrainProtection.hasFixedAttachment(level,pos)||TerrainProtection.hasSteepDrop(level,pos))return;
        if (PlatformConfigHooks.isWaterwayProtectionEnabled() && WaterwayProtection.touchesFluid(level, pos)) return;
        BlockPos blockBelowPos = pos.below();
        BlockPos blockAbovePos = pos.above();
        BlockState blockAboveState = level.getBlockState(blockAbovePos);
        BlockState currentBlockState = level.getBlockState(pos);
        BlockState blockBelowState = level.getBlockState(blockBelowPos);

        if (currentBlockState.getBlock() instanceof SlabBlock) return;

        Block slab = ModSlabsMap.getSlabForBlock(blockBelowState.getBlock());
        if (slab == null) return;

        BlockState slabState = slab.defaultBlockState();

        slabState = updateBottomWaterloggedState(currentBlockState, blockAboveState, slabState)
                .setValue(CustomSlab.GENERATED, true);
        if (!VegetationTransfer.placeSlab(level, pos, slabState)) return;

        if (ModSlabsMap.isSoilSlab(slabState.getBlock())) {
            BlockState belowState = slabState.getBlock() instanceof ISoilSlab soil
                    ? soil.baseFullBlock()
                    : Blocks.DIRT.defaultBlockState();
            setBlockState(level, blockBelowPos, belowState);
        }
        if (slabState.is(ModBlocksRegistry.WARPED_NYLIUM_SLAB.get()) || slabState.is(ModBlocksRegistry.CRIMSON_NYLIUM_SLAB.get())) {
            setBlockState(level,blockBelowPos, Blocks.NETHERRACK.defaultBlockState());
        }
    }

    private BlockState updateBottomWaterloggedState(BlockState currentBlockState, BlockState blockAboveState, BlockState slabState) {
        if (slabState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            if (currentBlockState.is(Blocks.WATER) || blockAboveState.is(Blocks.WATER) || currentBlockState.getFluidState().is(FluidTags.WATER))
            {
                return slabState.setValue(BlockStateProperties.WATERLOGGED, true);
            }
        }
        return slabState;
    }

    private boolean shouldPlaceTopSlab(WorldGenLevel level, BlockPos currentPos) {
        if(TerrainProtection.isProtected(level,currentPos))return false;
        if (!TerrainRandom.accepts(level.getSeed(), PlatformConfigHooks.getTerrainSeedOffset(), currentPos,
                PlatformConfigHooks.getSlabPlacementChance())) return false;
        BlockPos blockAbovePos = currentPos.above();
        BlockPos blockBelowPos = currentPos.below();
        BlockState currentBlockState = level.getBlockState(currentPos);

        if(ModSlabsMap.getSlabForBlock(currentBlockState.getBlock())==null
                ||currentBlockState.is(net.countered.terrainslabs.block.ModBlockTags.PRESERVED_BLOCKS))return false;
        if (!TerrainProtection.isFullTerrain(level,currentPos)) return false;

        BlockState blockBelowState = level.getBlockState(blockBelowPos);
        if(!TerrainProtection.isOpen(level,blockBelowPos)) return false;

        BlockState blockAboveState = level.getBlockState(blockAbovePos);
        if (ModSlabsMap.getSlabForBlock(blockAboveState.getBlock()) == null) return false;
        if(TerrainProtection.hasFixedAttachment(level,currentPos)||TerrainProtection.hasSteepDrop(level,currentPos))return false;
        if (PlatformConfigHooks.isWaterwayProtectionEnabled() && WaterwayProtection.touchesFluid(level, currentPos)) return false;

        if (!validSurroundingTop(level, currentPos)) return false;

        return true;
    }

    private boolean validSurroundingTop(WorldGenLevel level, BlockPos currentPos) {
        boolean validNeighbors = false;
        boolean hasWaterNeighbor = false;
        boolean hasAirNeighbor = false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = currentPos.relative(direction);
            BlockPos oppositePos = currentPos.relative(direction.getOpposite());
            BlockPos aboveOppositePos = oppositePos.above();
            BlockState neighborState = level.getBlockState(neighborPos);
            BlockState oppositeState = level.getBlockState(oppositePos);
            BlockState aboveOppositeState = level.getBlockState(aboveOppositePos);

            if (neighborState.is(Blocks.GLOW_LICHEN) || neighborState.is(Blocks.LAVA)) {
                return false;
            } else if (neighborState.is(Blocks.WATER)) {
                hasWaterNeighbor = true;
            } else if (neighborState.is(Blocks.AIR)) {
                hasAirNeighbor = true;
            }

            if (TerrainProtection.isFullTerrain(level,neighborPos) &&
                    TerrainProtection.isFullTerrain(level,aboveOppositePos) &&
                    TerrainProtection.isOpen(level,oppositePos) &&
                    TerrainProtection.isOpen(level,neighborPos.below())
            ) {
                validNeighbors = true;
            }
        }
        return validNeighbors && (!(hasWaterNeighbor && hasAirNeighbor));
    }

    private void placeTopSlab(WorldGenLevel level, BlockPos pos) {
        if(TerrainProtection.isProtected(level,pos)||TerrainProtection.hasFixedAttachment(level,pos)||TerrainProtection.hasSteepDrop(level,pos))return;
        if (PlatformConfigHooks.isWaterwayProtectionEnabled() && WaterwayProtection.touchesFluid(level, pos)) return;
        Boolean waterlogged = isTopStateWaterlogged(level, pos);
        BlockState original = level.getBlockState(pos);
        Block material = ModSlabsMap.getSlabForBlock(original.getBlock());
        if(material==null||original.is(net.countered.terrainslabs.block.ModBlockTags.PRESERVED_BLOCKS))return;
        BlockState slabState = material.defaultBlockState();

        if (slabState.getBlock().equals(Blocks.AIR)) {
            return;
        }
        if (ModSlabsMap.isSoilSlab(slabState.getBlock())) {
            slabState = slabState.getBlock() instanceof ISoilSlab soil
                    ? soil.baseSlabBlock()
                    : ModBlocksRegistry.DIRT_SLAB.get().defaultBlockState();
        }
        if (slabState.is(ModBlocksRegistry.WARPED_NYLIUM_SLAB.get()) || slabState.is(ModBlocksRegistry.CRIMSON_NYLIUM_SLAB.get())) {
            slabState = ModBlocksRegistry.NETHERRACK_SLAB.get().defaultBlockState();
        }
        setBlockState(level, pos, slabState.setValue(CustomSlab.GENERATED, true).setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP).setValue(BlockStateProperties.WATERLOGGED, waterlogged));
    }

    private boolean isTopStateWaterlogged(LevelAccessor levelAccessor, BlockPos currentPos) {
        if (!levelAccessor.getBlockState(currentPos.below()).is(Blocks.WATER)) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {

            if (levelAccessor.getBlockState(currentPos.relative(direction)).is(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void setBlockState(LevelAccessor world, BlockPos pos, BlockState state) {
        world.setBlock(pos, state, 3);
    }
}
