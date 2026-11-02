package net.countered.terrainslabs.neoforge.test;

import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.generation.SlabFeature;
import net.countered.terrainslabs.platform.neoforge.PlatformConfigHooksImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("wildslabs")
@PrefixGameTestTemplate(false)
public final class TerrainSafetyGameTests {
    @GameTest(template="test_empty",batch="wildslabs_soil_exceptions")
    public static void cultivatedAndSoftTerrainGeneratesNativeSlabs(GameTestHelper helper){
        var level=helper.getLevel();var base=base(helper);
        var sources=new java.util.ArrayList<net.minecraft.world.level.block.Block>(java.util.List.of(Blocks.FARMLAND,Blocks.DIRT_PATH,Blocks.SOUL_SAND,Blocks.MUD));
        for(var entry:net.countered.terrainslabs.neoforge.compat.CompatSlabs.entries())if(java.util.List.of("farmland","path").contains(entry.kind()))sources.add(BuiltInRegistries.BLOCK.get(entry.source()));
        for(var source:sources){
            terrain(level,base,true);
            var state=source.defaultBlockState();
            if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE))state=state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE,5);
            for(int x=-2;x<=17;x++)for(int z=-2;z<=17;z++)level.setBlock(base.offset(x,x>=8?0:-1,z),state,18);
            smooth(level,base);
            var expected=net.countered.terrainslabs.block.ModSlabsMap.getSlabForBlock(source);
            int generated=0;
            for(var pos:BlockPos.betweenClosed(base,base.offset(15,1,15))){
                var actual=level.getBlockState(pos);
                if(!actual.is(expected)||!actual.getValue(CustomSlab.GENERATED))continue;
                generated++;
                if(state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE))helper.assertTrue(actual.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.MOISTURE)==5,"Generation lost native farmland moisture");
                if(expected instanceof net.countered.terrainslabs.block.customslabs.soilslabs.ISoilSlab soil)helper.assertTrue(level.getBlockState(pos.below()).is(soil.baseFullBlock().getBlock()),"Cultivated slab lost its native base dirt");
            }
            helper.assertTrue(generated>0,"Terrain exception produced no slabs: "+source);
        }
        helper.succeed();
    }
    private static BlockPos base(GameTestHelper helper){
        var pos=helper.absolutePos(new BlockPos(2,5,2));
        return new BlockPos(pos.getX()&~15,pos.getY(),pos.getZ()&~15);
    }

    private static void terrain(ServerLevel level,BlockPos base,boolean step){
        for(int x=-2;x<=17;x++)for(int z=-2;z<=17;z++)for(int y=-6;y<=12;y++){
            var state=y<-1?Blocks.STONE.defaultBlockState():y==-1?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState();
            if(step&&x>=8&&y==0)state=Blocks.GRASS_BLOCK.defaultBlockState();
            level.setBlock(base.offset(x,y,z),state,18);
        }
    }

    private static void smooth(ServerLevel level,BlockPos base){
        double chance=PlatformConfigHooksImpl.slabPlacementChance;
        boolean waterways=PlatformConfigHooksImpl.preserveWaterways;
        try{
            PlatformConfigHooksImpl.slabPlacementChance=1;
            PlatformConfigHooksImpl.preserveWaterways=true;
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)Heightmap.primeHeightmaps(level.getChunk(new ChunkPos(base).x+x,new ChunkPos(base).z+z),java.util.EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG));
            new SlabFeature(NoneFeatureConfiguration.CODEC).place(new FeaturePlaceContext<>(java.util.Optional.empty(),level,level.getChunkSource().getGenerator(),RandomSource.create(4),base,NoneFeatureConfiguration.INSTANCE));
        }finally{
            PlatformConfigHooksImpl.slabPlacementChance=chance;
            PlatformConfigHooksImpl.preserveWaterways=waterways;
        }
    }

    private static int slabs(ServerLevel level,BlockPos base){
        int count=0;
        for(var pos:BlockPos.betweenClosed(base.offset(0,-6,0),base.offset(15,12,15))){
            var state=level.getBlockState(pos);
            if(state.hasProperty(CustomSlab.GENERATED)&&state.getValue(CustomSlab.GENERATED))count++;
        }
        return count;
    }

    private static java.util.Map<BlockPos,BlockState> snapshot(ServerLevel level,BlockPos base){
        var result=new java.util.HashMap<BlockPos,BlockState>();
        for(var pos:BlockPos.betweenClosed(base.offset(0,-6,0),base.offset(15,12,15)))result.put(pos.immutable(),level.getBlockState(pos));
        return result;
    }

    @GameTest(template="test_empty",batch="wildslabs_chunk_boundary")
    public static void smoothingDoesNotWriteNeighborChunks(GameTestHelper helper){
        var level=helper.getLevel();var base=base(helper);terrain(level,base,true);
        var border=new java.util.HashMap<BlockPos,BlockState>();
        for(var pos:BlockPos.betweenClosed(base.offset(-1,-3,-1),base.offset(16,4,16))){
            int x=pos.getX()-base.getX(),z=pos.getZ()-base.getZ();
            if(x<0||x>15||z<0||z>15)border.put(pos.immutable(),level.getBlockState(pos));
        }
        smooth(level,base);
        helper.assertTrue(slabs(level,base)>0,"Chunk clipping disabled ordinary terrain generation");
        for(var entry:border.entrySet())helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),"Terrain feature wrote into a neighboring chunk");
        helper.succeed();
    }

    @GameTest(template="test_empty",batch="wildslabs_partial_shapes")
    public static void decorationsDoNotCreateTerrainSteps(GameTestHelper helper){
        var level=helper.getLevel();var base=base(helper);
        terrain(level,base,false);
        var decorations=new java.util.HashMap<BlockPos,BlockState>();
        var ids=java.util.List.of("minecraft:brown_mushroom","minecraft:poppy","minecraft:cobblestone_wall","minecraft:oak_fence","hexabiomes:ground_rock","hexabiomes:fallen_branch","hexabiomes:oak_twig","hexabiomes:trunk_mushrooms","hexabiomes:shelf_mushroom","hexabiomes:dead_copper_golem","hexabiomes:ancient_golem","hexabiomes:ancient_golem_hitbox");
        int i=0;
        for(var text:ids){
            var id=ResourceLocation.parse(text);if(!BuiltInRegistries.BLOCK.containsKey(id))continue;
            var pos=base.offset(3+(i%4)*3,0,3+(i/4)*3);i++;
            var state=BuiltInRegistries.BLOCK.get(id).defaultBlockState();level.setBlock(pos,state,18);decorations.put(pos,state);
        }
        decorations.replaceAll((pos,state)->level.getBlockState(pos));
        smooth(level,base);
        helper.assertTrue(slabs(level,base)==0,"Partial blocks, plants, and golems must not create steps on flat terrain");
        for(var entry:decorations.entrySet())helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),"Terrain smoothing replaced a decoration or boss block: "+entry.getValue()+" at "+entry.getKey()+" became "+level.getBlockState(entry.getKey()));
        helper.succeed();
    }

    @GameTest(template="test_empty",batch="wildslabs_cliff_safety")
    public static void steepCliffsRetainTheirBands(GameTestHelper helper){
        var level=helper.getLevel();var base=base(helper);
        terrain(level,base,false);
        for(int x=8;x<=17;x++)for(int z=-2;z<=17;z++)for(int y=0;y<8;y++){
            var state=(y%3==0?Blocks.GRANITE:y%3==1?Blocks.ANDESITE:Blocks.STONE).defaultBlockState();
            if(x==8&&y%3==1)state=Blocks.AIR.defaultBlockState();
            level.setBlock(base.offset(x,y,z),state,18);
        }
        var before=snapshot(level,base);smooth(level,base);
        helper.assertTrue(snapshot(level,base).equals(before),"Smoothing changed a steep layered cliff or its recesses");
        terrain(level,base,true);smooth(level,base);
        helper.assertTrue(slabs(level,base)>0,"Gentle natural terrain must still receive slabs");
        helper.succeed();
    }

    @GameTest(template="test_empty",batch="wildslabs_water_safety")
    public static void narrowRiversAndOceanBedsRemainIntact(GameTestHelper helper){
        var level=helper.getLevel();var base=base(helper);
        for(boolean ocean:new boolean[]{false,true}){
            terrain(level,base,true);
            var water=new java.util.HashMap<BlockPos,BlockState>();
            for(int x=ocean?0:7;x<=7;x++)for(int z=-2;z<=17;z++){
                level.setBlock(base.offset(x,-3,z),Blocks.GRAVEL.defaultBlockState(),18);
                for(int y=-2;y<=0;y++){
                    var state=Blocks.WATER.defaultBlockState();
                    if(!ocean&&y==0)state=state.setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,3);
                    var pos=base.offset(x,y,z);level.setBlock(pos,state,18);water.put(pos,state);
                }
            }
            var plant=base.offset(7,-2,7);level.setBlock(plant,Blocks.SEAGRASS.defaultBlockState(),18);water.put(plant,Blocks.SEAGRASS.defaultBlockState());
            smooth(level,base);
            for(var entry:water.entrySet())helper.assertTrue(level.getBlockState(entry.getKey()).equals(entry.getValue()),"River/ocean water or aquatic vegetation changed");
            for(int x=ocean?0:7;x<=7;x++)for(int z=0;z<16;z++)helper.assertTrue(level.getBlockState(base.offset(x,-3,z)).is(Blocks.GRAVEL),"Underwater bed was changed");
        }
        helper.succeed();
    }

    @GameTest(template="test_empty",batch="wildslabs_arena_safety")
    public static void hexaArenaReferencesProtectTheEntireFootprint(GameTestHelper helper) throws ReflectiveOperationException {
        var level=helper.getLevel();var base=base(helper);
        var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var structure=registry.get(ResourceLocation.parse("hexabiomes:ancient_golem"));
        if(structure==null){helper.succeed();return;}
        terrain(level,base,true);
        var center=base.offset(64,0,8);
        var piece=(net.minecraft.world.level.levelgen.structure.StructurePiece)Class.forName("fr.hexa.biomes.guardian.AncientGolemSitePiece").getConstructor(BlockPos.class,int.class).newInstance(center,0);
        var origin=level.getChunkAt(center);var local=level.getChunkAt(base);
        var starts=new java.util.HashMap<>(origin.getAllStarts());
        var refs=new java.util.HashMap<net.minecraft.world.level.levelgen.structure.Structure,it.unimi.dsi.fastutil.longs.LongSet>();
        local.getAllReferences().forEach((key,value)->refs.put(key,new it.unimi.dsi.fastutil.longs.LongOpenHashSet(value)));
        try{
            var start=new net.minecraft.world.level.levelgen.structure.StructureStart(structure,origin.getPos(),0,new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece)));
            origin.setStartForStructure(structure,start);local.addReferenceForStructure(structure,origin.getPos().toLong());
            var before=snapshot(level,base);smooth(level,base);
            helper.assertTrue(snapshot(level,base).equals(before),"Referenced arena floor was smoothed away from its center chunk");
        }finally{origin.setAllStarts(starts);local.setAllReferences(refs);}
        smooth(level,base);
        helper.assertTrue(slabs(level,base)>0,"Structure exclusion must not disable ordinary terrain globally");
        helper.succeed();
    }
}
