package net.countered.terrainslabs.neoforge.test;

import net.countered.terrainslabs.block.customslabs.soilslabs.FarmlandSlab;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildslabs")
@PrefixGameTestTemplate(false)
public final class WildSlabsGameTests {
    private static net.minecraft.world.level.BlockGetter renderingView(net.minecraft.server.level.ServerLevel level) {
        return new net.minecraft.world.level.BlockGetter() {
            public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos pos) { return level.getBlockEntity(pos); }
            public net.minecraft.world.level.block.state.BlockState getBlockState(BlockPos pos) { return level.getBlockState(pos); }
            public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos) { return level.getFluidState(pos); }
            public int getHeight() { return level.getHeight(); }
            public int getMinBuildHeight() { return level.getMinBuildHeight(); }
        };
    }
    private static void runTerrainFeature(net.minecraft.server.level.ServerLevel level, BlockPos origin) {
        net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(level.getChunkAt(origin),
                java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
        var feature = new net.countered.terrainslabs.generation.SlabFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC);
        feature.place(new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(java.util.Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(123), origin,
                net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE));
    }

    @GameTest(template = "test_empty")
    public static void plantsOnlyOffsetOnValidSoil(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var flower = Blocks.POPPY.defaultBlockState();
        level.setBlock(pos, ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState(), 18);
        helper.assertTrue(net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), flower), "Flower must sit flush with a grass slab");
        helper.assertTrue(flower.getOffset(renderingView(level), pos.above()).y == -0.5, "Actual flower model offset must reach the grass slab");
        level.setBlock(pos, ModBlocksRegistry.CUSTOM_STONE_SLAB.get().defaultBlockState(), 18);
        helper.assertTrue(!flower.canSurvive(level, pos.above()), "Flowers must not acquire stone as an allowed soil");
        helper.assertTrue(!net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), flower), "Stone must not cause a false visual plant offset");
        helper.assertTrue(flower.getOffset(renderingView(level), pos.above()).y == 0, "Ineligible stone must not move a flower's model");
        level.setBlock(pos, Blocks.OAK_SLAB.defaultBlockState(), 18);
        helper.assertTrue(!net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), flower), "Unrelated mod/wood slabs must not offset vegetation");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void rootedColumnsAreNotLifted(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
        for (var plant : new net.minecraft.world.level.block.Block[]{Blocks.BAMBOO_SAPLING, Blocks.BAMBOO, Blocks.SUGAR_CANE, Blocks.CACTUS}) {
            var state = plant.defaultBlockState();
            level.setBlock(pos, state, 18);
            helper.assertTrue(!net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos,
                    ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState()), "Rooted column was lifted: " + plant);
            helper.assertTrue(level.getBlockState(pos).equals(state), "Rooted plant state changed");
            helper.assertTrue(!net.countered.terrainslabs.api.SlabHelper.isValidOntop(state), "Rooted column renderer must retain vanilla alignment");
        }
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void submergedSeagrassOffsetMatchesWaterloggedSoil(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var slab = ModBlocksRegistry.DIRT_SLAB.get().defaultBlockState().setValue(CustomSlab.WATERLOGGED, true);
        var plant = Blocks.SEAGRASS.defaultBlockState();
        level.setBlock(pos, slab, 18);
        level.setBlock(pos.above(), plant, 18);
        helper.assertTrue(plant.canSurvive(level, pos.above()), "Seagrass should keep vanilla soil eligibility on a submerged terrain slab");
        helper.assertTrue(net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), plant), "Seagrass must meet the submerged slab surface");
        var render = renderingView(level);
        helper.assertTrue(plant.getOffset(render, pos.above()).y == -0.5, "Underwater model must not float half a block above the slab");
        helper.assertTrue(plant.getShape(render, pos.above()).min(Direction.Axis.Y) == -0.5, "Selection shape must follow the submerged plant");
        helper.assertTrue(!net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), Blocks.POPPY.defaultBlockState()), "Terrestrial flowers must not sink into waterlogged slabs");
        level.setBlock(pos, slab.setValue(CustomSlab.WATERLOGGED, false), 18);
        helper.assertTrue(!net.countered.terrainslabs.api.SlabHelper.isOffsetOntop(level, pos.above(), plant), "Aquatic offset requires water in the bottom slab");
        helper.succeed();
    }

    @GameTest(template = "test_empty", batch = "wildslabs_river")
    public static void riverVolumeAndAquaticPlantsSurviveWorldgen(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(2, 2, 2));
        var base = new BlockPos((origin.getX() & ~15) + 6, origin.getY(), (origin.getZ() & ~15) + 2);
        var before = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
        for (int z = 0; z < 12; z++) {
            for (int x = -1; x <= 2; x++) {
                var pos = base.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.SAND.defaultBlockState(), 18);
                level.setBlock(pos, (x == -1 || x == 2 ? Blocks.GRASS_BLOCK : Blocks.WATER).defaultBlockState(), 18);
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
            }
        }
        level.setBlock(base.offset(0,0,2), Blocks.SEAGRASS.defaultBlockState(), 18);
        level.setBlock(base.offset(1,0,5), Blocks.KELP.defaultBlockState(), 18);
        level.setBlock(base.offset(0,0,8), Blocks.TALL_SEAGRASS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER),18);
        level.setBlock(base.offset(0,1,8), Blocks.TALL_SEAGRASS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),18);
        level.setBlock(base.offset(1,0,10),Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,4),18);
        for (int z = 0; z < 12; z++) for (int x = -1; x <= 2; x++) {
            var pos = base.offset(x,0,z);
            before.put(pos,level.getBlockState(pos));
            before.put(pos.below(),level.getBlockState(pos.below()));
            before.put(pos.above(),level.getBlockState(pos.above()));
        }
        runTerrainFeature(level, base);
        for (var entry : before.entrySet()) helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),
                "River, aquatic plant, bed or shoreline changed at " + entry.getKey());
        helper.succeed();
    }

    @GameTest(template = "test_empty", batch = "wildslabs_bamboo")
    public static void actualWorldgenLeavesBambooOnFullSoil(GameTestHelper helper) {
        var level=helper.getLevel();
        var origin=helper.absolutePos(new BlockPos(2,2,2));
        var base=new BlockPos((origin.getX() & ~15)+6,origin.getY(),(origin.getZ() & ~15)+6);
        for(int z=-1; z<=2; z++) for(int x=-1; x<=1; x++) {
            level.setBlock(base.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),18);
            level.setBlock(base.offset(x,0,z),Blocks.AIR.defaultBlockState(),18);
            level.setBlock(base.offset(x,1,z),Blocks.AIR.defaultBlockState(),18);
        }
        level.setBlock(base,Blocks.BAMBOO_SAPLING.defaultBlockState(),18);
        level.setBlock(base.south(),Blocks.BAMBOO.defaultBlockState(),18);
        level.setBlock(base.south().above(),Blocks.BAMBOO.defaultBlockState(),18);
        level.setBlock(base.east(),Blocks.GRASS_BLOCK.defaultBlockState(),18);
        level.setBlock(base.east().south(),Blocks.GRASS_BLOCK.defaultBlockState(),18);
        runTerrainFeature(level,base);
        helper.assertTrue(level.getBlockState(base).is(Blocks.BAMBOO_SAPLING) && level.getBlockState(base.below()).is(Blocks.GRASS_BLOCK),"Bamboo shoot must retain its full original soil");
        helper.assertTrue(level.getBlockState(base.south()).is(Blocks.BAMBOO) && level.getBlockState(base.south().above()).is(Blocks.BAMBOO)
                && level.getBlockState(base.south().below()).is(Blocks.GRASS_BLOCK),"Bamboo column was lifted, truncated or lost its soil");
        helper.assertTrue(level.getBlockState(base).canSurvive(level,base) && level.getBlockState(base.south()).canSurvive(level,base.south()),"Bamboo must still satisfy its native growth support rules");
        helper.succeed();
    }

    @GameTest(template = "test_empty", batch = "wildslabs_sparse")
    public static void naturalSlopesKeepGapsByDefault(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(2,2,2));
        var base = new BlockPos(origin.getX() & ~15, origin.getY(), origin.getZ() & ~15);
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            level.setBlock(base.offset(x,-1,z), Blocks.GRASS_BLOCK.defaultBlockState(),18);
            level.setBlock(base.offset(x,0,z), Blocks.AIR.defaultBlockState(),18);
            level.setBlock(base.offset(x,1,z), Blocks.AIR.defaultBlockState(),18);
        }
        for (int x : new int[]{3,6,9,12}) for (int z = 1; z < 15; z++) level.setBlock(base.offset(x,0,z),Blocks.GRASS_BLOCK.defaultBlockState(),18);
        runTerrainFeature(level,base);
        int slabs=0, gaps=0;
        for (int x : new int[]{2,5,8,11}) for (int z=1; z<15; z++) {
            var state=level.getBlockState(base.offset(x,0,z));
            if(state.is(ModBlocksRegistry.GRASS_SLAB.get())) slabs++;
            else if(state.isAir()) gaps++;
        }
        helper.assertTrue(slabs>0 && gaps>0 && slabs+gaps==56,"Default smoothing must keep both steps and unsmoothed gaps");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void optionalModFoliageRetainsAllStates(GameTestHelper helper) {
        if (!net.neoforged.fml.ModList.get().isLoaded("biomesoplenty")) { helper.succeed(); return; }
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        int moved = 0;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals("biomesoplenty") || block instanceof net.minecraft.world.level.block.DoublePlantBlock) continue;
            for (var state : block.getStateDefinition().getPossibleStates()) {
                if (!net.countered.terrainslabs.generation.VegetationSupport.isCandidate(state)) continue;
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
                if (!state.getCollisionShape(level, pos).isEmpty() || !state.canSurvive(level, pos)) continue;
                level.setBlock(pos, state, 18);
                helper.assertTrue(net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos,
                        ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState()), "BOP foliage transfer failed: " + state);
                helper.assertTrue(level.getBlockState(pos.above()).equals(state), "BOP flower properties changed: " + state);
                moved++;
            }
        }
        helper.assertTrue(moved > 5, "Expected multiple real BOP flower states to be exercised");
        org.slf4j.LoggerFactory.getLogger("WildSlabsTests").info("Preserved {} real Biomes O' Plenty vegetation states", moved);
        helper.succeed();
    }

    private static final net.neoforged.neoforge.registries.DeferredRegister.Blocks FIXTURES =
            net.neoforged.neoforge.registries.DeferredRegister.createBlocks("wildslabs");
    private static final java.util.function.Supplier<net.minecraft.world.level.block.Block> STRICT_FLOWER = FIXTURES.register("test/strict_source_flower", () ->
            new net.minecraft.world.level.block.Block(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().replaceable()) {
                @Override
                public boolean canSurvive(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
                    return level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
                }
            });

    public static void registerFixtures(net.neoforged.bus.api.IEventBus bus) {
        if (Boolean.getBoolean("neoforge.enableGameTest")) FIXTURES.register(bus);
    }

    @GameTest(template = "test_empty")
    public static void arbitraryModPlantUsesOriginalSoil(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var custom = STRICT_FLOWER.get().defaultBlockState();
        var slab = ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState();
        level.setBlock(pos, slab, 18);
        helper.assertTrue(custom.canSurvive(level, pos.above()), "Non-BushBlock plants with strict source-block checks must work");
        helper.assertTrue(level.getBlockState(pos).equals(slab), "Soil probe must not mutate the world");
        level.setBlock(pos, ModBlocksRegistry.DIRT_SLAB.get().defaultBlockState(), 18);
        helper.assertTrue(!custom.canSurvive(level, pos.above()), "Compatibility must preserve a plant's original soil restriction");
        level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
        level.setBlock(pos, custom, 18);
        helper.assertTrue(net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos, slab), "Custom mod vegetation must transfer without an explicit profile");
        helper.assertTrue(level.getBlockState(pos.above()).equals(custom), "Custom mod flower was lost");
        helper.succeed();
    }

    @GameTest(template = "test_empty", batch = "wildslabs_worldgen")
    public static void actualWorldgenPreservesPetalDensity(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos base = new BlockPos((origin.getX() & ~15) + 6, origin.getY(), (origin.getZ() & ~15) + 6);
        for (int z = -1; z <= 4; z++) {
            for (int x = -1; x <= 1; x++) {
                var at = base.offset(x, -1, z);
                level.setBlock(at, Blocks.GRASS_BLOCK.defaultBlockState(), 18);
                level.setBlock(at.above(), Blocks.AIR.defaultBlockState(), 18);
                level.setBlock(at.above(2), Blocks.AIR.defaultBlockState(), 18);
            }
        }
        for (int i = 0; i < 4; i++) {
            level.setBlock(base.offset(1, 0, i), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
            level.setBlock(base.offset(0, 0, i), Blocks.PINK_PETALS.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT, i + 1), 18);
        }
        var chunk = level.getChunkAt(base);
        net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(chunk,
                java.util.EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG));
        var feature = new net.countered.terrainslabs.generation.SlabFeature(net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC);
        feature.place(new net.minecraft.world.level.levelgen.feature.FeaturePlaceContext<>(java.util.Optional.empty(), level,
                level.getChunkSource().getGenerator(), RandomSource.create(123), base,
                net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.INSTANCE));
        for (int i = 0; i < 4; i++) {
            var pos = base.offset(0, 0, i);
            boolean smoothed = level.getBlockState(pos).is(ModBlocksRegistry.GRASS_SLAB.get());
            var flower = level.getBlockState(smoothed ? pos.above() : pos);
            helper.assertTrue(flower.is(Blocks.PINK_PETALS) && flower.getValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT) == i + 1,
                    "Worldgen changed original flower density");
        }
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void petalCountsAndTallFoliage(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var slab = ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState().setValue(CustomSlab.GENERATED, true);
        for (int count = 1; count <= 4; count++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
                level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
                var petals = Blocks.PINK_PETALS.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT, count)
                        .setValue(net.minecraft.world.level.block.PinkPetalsBlock.FACING, direction);
                level.setBlock(pos, petals, 18);
                helper.assertTrue(net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos, slab), "Petal transfer failed");
                helper.assertTrue(level.getBlockState(pos.above()).equals(petals), "Petal count or orientation was reset");
            }
        }
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 18);
        level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
        var lower = Blocks.TALL_GRASS.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER);
        var upper = lower.setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        level.setBlock(pos, lower, 18);
        level.setBlock(pos.above(), upper, 18);
        helper.assertTrue(net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos, slab), "Tall vegetation transfer failed");
        helper.assertTrue(level.getBlockState(pos.above()).equals(lower) && level.getBlockState(pos.above(2)).equals(upper), "Tall plant lost a half");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void blockedFoliageIsNotDeleted(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
        var flower = Blocks.POPPY.defaultBlockState();
        level.setBlock(pos, flower, 18);
        level.setBlock(pos.above(), Blocks.STONE.defaultBlockState(), 18);
        boolean moved = net.countered.terrainslabs.generation.VegetationTransfer.placeSlab(level, pos,
                ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState());
        helper.assertTrue(!moved && level.getBlockState(pos).equals(flower) && level.getBlockState(pos.above()).is(Blocks.STONE),
                "Blocked transfers must preserve the original decoration");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void seededTerrainSampling(GameTestHelper helper) {
        int accepted = 0;
        int changed = 0;
        for (int i = 0; i < 10000; i++) {
            var pos = new BlockPos(i % 100, 70, i / 100);
            boolean first = net.countered.terrainslabs.generation.TerrainRandom.accepts(12345, 42, pos, 0.5);
            boolean repeat = net.countered.terrainslabs.generation.TerrainRandom.accepts(12345, 42, pos, 0.5);
            boolean other = net.countered.terrainslabs.generation.TerrainRandom.accepts(12345, 43, pos, 0.5);
            helper.assertTrue(first == repeat, "Same seed and position must reproduce placement");
            helper.assertTrue(net.countered.terrainslabs.generation.TerrainRandom.accepts(12345, 42, pos, 1), "Default density must accept all eligible terrain");
            if (first) accepted++;
            if (first != other) changed++;
        }
        helper.assertTrue(accepted > 4700 && accepted < 5300 && changed > 4000, "Seed offset or placement probability is ineffective");
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void optionalBiomeMappings(GameTestHelper helper) {
        var mappings = net.countered.terrainslabs.block.ModSlabsMap.getMappings();
        int count = 0;
        for (var entry : mappings.entrySet()) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(entry.getKey());
            if (!id.getNamespace().equals("biomesoplenty")) continue;
            count++;
            var state = entry.getValue().defaultBlockState();
            helper.assertTrue(state.hasProperty(CustomSlab.GENERATED), "Optional slab lacks worldgen property");
            var recipe = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("wildslabs", "biomesoplenty/" + id.getPath() + "_slab");
            helper.assertTrue(helper.getLevel().getRecipeManager().byKey(recipe).isPresent(), "Optional slab recipe missing: " + recipe);
        }
        boolean loaded = net.neoforged.fml.ModList.get().isLoaded("biomesoplenty");
        helper.assertTrue(count == (loaded ? 13 : 0), "Expected 13 BOP terrain mappings when installed, zero otherwise; got " + count);
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void cropsAndHydration(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        for (SlabType type : SlabType.values()) {
            var soil = ModBlocksRegistry.FARMLAND_SLAB.get().defaultBlockState().setValue(CustomSlab.TYPE, type);
            level.setBlockAndUpdate(pos, soil);
            level.setBlockAndUpdate(pos.east(), Blocks.WATER.defaultBlockState());
            ((FarmlandSlab) soil.getBlock()).randomTick(soil, level, pos, RandomSource.create(42));
            var hydrated = level.getBlockState(pos);
            helper.assertTrue(hydrated.getValue(FarmlandSlab.MOISTURE) == 7, "Water must hydrate every slab orientation");
            helper.assertTrue(hydrated.isFertile(level, pos), "Hydration must affect NeoForge crop growth");
            for (var crop : new net.minecraft.world.level.block.Block[]{Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.MELON_STEM}) {
                helper.assertTrue(crop.defaultBlockState().canSurvive(level, pos.above()), "Crop cannot survive on " + type + ": " + crop);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void hoeAndDrying(GameTestHelper helper) {
        var level = helper.getLevel();
        level.setWeatherParameters(10000, 0, false, false);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var player = FakePlayerFactory.getMinecraft(level);
        for (SlabType type : SlabType.values()) {
            var stack = new ItemStack(Items.IRON_HOE);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, ModBlocksRegistry.DIRT_SLAB.get().defaultBlockState()
                    .setValue(CustomSlab.TYPE, type).setValue(CustomSlab.GENERATED, true));
            var context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
            Items.IRON_HOE.useOn(context);
            var soil = level.getBlockState(pos);
            helper.assertTrue(soil.getBlock() instanceof FarmlandSlab, "Hoe did not create farmland");
            helper.assertTrue(soil.getValue(CustomSlab.TYPE) == type && soil.getValue(CustomSlab.GENERATED), "Hoe lost slab state");
            ((FarmlandSlab) soil.getBlock()).randomTick(soil, level, pos, RandomSource.create(1));
            helper.assertTrue(level.getBlockState(pos).is(ModBlocksRegistry.DIRT_SLAB.get()), "Dry empty soil must return to dirt");
        }
        helper.succeed();
    }

    @GameTest(template = "test_empty")
    public static void flowersAndGrass(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var grass = ModBlocksRegistry.GRASS_SLAB.get().defaultBlockState();
        level.setBlockAndUpdate(pos, grass);
        helper.assertTrue(Blocks.POPPY.defaultBlockState().canSurvive(level, pos.above()), "Flower must survive on grass slab");
        ((net.minecraft.world.level.block.BonemealableBlock) grass.getBlock()).performBonemeal(level, RandomSource.create(3), pos, grass);
        helper.assertTrue(!level.isEmptyBlock(pos.above()), "Bonemeal must plant vegetation");
        helper.succeed();
    }
}
