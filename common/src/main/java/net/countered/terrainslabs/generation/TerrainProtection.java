package net.countered.terrainslabs.generation;

import net.countered.terrainslabs.block.ModBlockTags;
import net.countered.terrainslabs.block.ModSlabsMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class TerrainProtection implements AutoCloseable {
    public static final TagKey<Structure> ALLOW_SMOOTHING=TagKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath("wildslabs","allow_terrain_smoothing"));
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHAPE_EXCEPTIONS=net.minecraft.tags.TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("wildslabs","terrain_shape_exceptions"));
    private static final ThreadLocal<TerrainProtection> CURRENT=new ThreadLocal<>();
    private final TerrainProtection previous;
    private final WorldGenLevel level;
    private final ChunkPos chunk;
    private final java.util.List<BoundingBox> boxes;
    private final java.util.Map<Long,Integer> heights=new java.util.HashMap<>();

    private TerrainProtection(WorldGenLevel level,BlockPos origin){
        this.level=level;
        chunk=new ChunkPos(origin);
        previous=CURRENT.get();
        var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var allowed=registry.getTag(ALLOW_SMOOTHING).map(set->set.stream().map(net.minecraft.core.Holder::value).collect(java.util.stream.Collectors.toSet())).orElse(java.util.Set.of());
        var manager=level.getLevel().structureManager();
        if(level instanceof net.minecraft.server.level.WorldGenRegion region)manager=manager.forWorldGenRegion(region);
        boxes=manager.startsForStructure(new ChunkPos(origin),structure->!allowed.contains(structure)).stream()
                .filter(net.minecraft.world.level.levelgen.structure.StructureStart::isValid).map(start->start.getBoundingBox().inflatedBy(2)).toList();
        CURRENT.set(this);
    }

    public static TerrainProtection begin(WorldGenLevel level,BlockPos origin){return new TerrainProtection(level,origin);}

    public static boolean isProtected(WorldGenLevel level,BlockPos pos){
        var scope=CURRENT.get();
        return scope!=null&&scope.level==level&&((pos.getX()>>4)!=scope.chunk.x||(pos.getZ()>>4)!=scope.chunk.z
                ||scope.boxes.stream().anyMatch(box->box.isInside(pos)));
    }

    public static boolean isFullTerrain(WorldGenLevel level,BlockPos pos){
        BlockState state=level.getBlockState(pos);
        return !isWood(state)&&ModSlabsMap.getSlabForBlock(state.getBlock())!=null&&isTerrainShape(state,level,pos);
    }

    public static boolean isTerrainShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos){
        if(isFullBlock(state,level,pos))return true;
        if(state.hasBlockEntity()||!state.getFluidState().isEmpty()||state.is(ModBlockTags.PRESERVED_BLOCKS))return false;
        boolean exception=state.getBlock() instanceof net.minecraft.world.level.block.FarmBlock
                ||state.getBlock() instanceof net.minecraft.world.level.block.DirtPathBlock
                ||state.getBlock() instanceof net.minecraft.world.level.block.SoulSandBlock
                ||state.getBlock() instanceof net.minecraft.world.level.block.MudBlock
                ||state.is(net.minecraft.tags.BlockTags.DIRT)||state.is(SHAPE_EXCEPTIONS)||state.is(net.minecraft.tags.BlockTags.SOUL_FIRE_BASE_BLOCKS);
        return exception&&isLowCube(state.getShape(level,pos))&&isLowCube(state.getCollisionShape(level,pos));
    }

    private static boolean isLowCube(net.minecraft.world.phys.shapes.VoxelShape shape){
        var boxes=shape.toAabbs();if(boxes.size()!=1)return false;var box=boxes.get(0);
        return box.minX==0&&box.minY==0&&box.minZ==0&&box.maxX==1&&box.maxZ==1&&box.maxY>=0.875&&box.maxY<=1;
    }

    public static boolean isFullBlock(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos){
        return !state.hasBlockEntity()&&state.getFluidState().isEmpty()&&!state.is(ModBlockTags.PRESERVED_BLOCKS)
                &&state.isCollisionShapeFullBlock(level,pos)&&net.minecraft.world.level.block.Block.isShapeFullBlock(state.getShape(level,pos));
    }

    public static boolean isOpen(WorldGenLevel level,BlockPos pos){
        var state=level.getBlockState(pos);
        return state.isAir()||!state.hasBlockEntity()&&state.getFluidState().isEmpty()&&!state.is(ModBlockTags.PRESERVED_BLOCKS)&&state.getCollisionShape(level,pos).isEmpty();
    }

    public static boolean isWood(BlockState state){
        return state.is(net.minecraft.tags.BlockTags.LOGS)||state.is(net.minecraft.tags.BlockTags.PLANKS)
                ||state.is(net.minecraft.tags.BlockTags.WOODEN_STAIRS)||state.is(net.minecraft.tags.BlockTags.WOODEN_SLABS)
                ||state.is(net.minecraft.tags.BlockTags.WOODEN_FENCES)||state.is(net.minecraft.tags.BlockTags.FENCE_GATES)
                ||state.is(net.minecraft.tags.BlockTags.WOODEN_DOORS)||state.is(net.minecraft.tags.BlockTags.WOODEN_TRAPDOORS);
    }

    public static boolean hasFixedAttachment(WorldGenLevel level,BlockPos pos){
        for(var direction:Direction.values()){
            var nearby=pos.relative(direction);var state=level.getBlockState(nearby);
            if(state.hasBlockEntity()||state.is(ModBlockTags.PRESERVED_BLOCKS)||isWood(state))return true;
            if(isFullTerrain(level,nearby))continue;
            if(state.hasProperty(net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab.GENERATED)
                    &&state.getValue(net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab.GENERATED))continue;
            if(!state.isAir()&&state.getFluidState().isEmpty()&&!state.isCollisionShapeFullBlock(level,nearby)
                    &&!state.getShape(level,nearby).isEmpty()&&!VegetationTransfer.canMove(state,level,nearby)
                    &&VegetationSupport.dependsOnSupport(state,level,nearby,pos))return true;
        }
        return false;
    }

    public static boolean hasSteepDrop(WorldGenLevel level,BlockPos pos){
        int height=terrainHeight(level,pos.getX(),pos.getZ());
        for(var direction:Direction.Plane.HORIZONTAL){
            var neighbor=pos.relative(direction);
            int other=terrainHeight(level,neighbor.getX(),neighbor.getZ());
            if(Math.abs(height-other)>=3&&pos.getY()>=Math.min(height,other)-1&&pos.getY()<=Math.max(height,other)+1)return true;
            boolean drop=true;
            for(int depth=1;depth<=3;depth++)if(!isOpen(level,pos.relative(direction).below(depth))){drop=false;break;}
            if(drop)return true;
        }
        return false;
    }

    private static int terrainHeight(WorldGenLevel level,int x,int z){
        var scope=CURRENT.get();
        long key=ChunkPos.asLong(x,z);
        if(scope!=null&&scope.level==level)return scope.heights.computeIfAbsent(key,ignored->findHeight(level,x,z));
        return findHeight(level,x,z);
    }

    private static int findHeight(WorldGenLevel level,int x,int z){
        int top=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,x,z)-1;
        for(int y=top;y>=level.getMinBuildHeight();y--)if(isFullTerrain(level,new BlockPos(x,y,z)))return y;
        return level.getMinBuildHeight()-1;
    }

    @Override
    public void close(){if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
}
