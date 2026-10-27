package net.countered.terrainslabs.mixin.ontop.render;

import net.countered.terrainslabs.api.SlabHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase {

    @Inject(method = "getOffset", at = @At("RETURN"), cancellable = true)
    private void wildslabs$getOffset(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        BlockState state = (BlockState) (Object) this;
        if (net.countered.terrainslabs.api.SlabAttachments.isLight(state)) {
            Vec3 offset = cir.getReturnValue();
            cir.setReturnValue(new Vec3(offset.x,offset.y+net.countered.terrainslabs.api.SlabAttachments.offsetY(state,level,pos),offset.z));
            return;
        }
        if (level instanceof ServerLevel) {
            return;
        }

        if (!SlabHelper.isValidOntop(level, pos, state)) return;

        BlockPos belowPos = pos.below();
        if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            belowPos = pos.below(2);
        }

        BlockState belowState = level.getBlockState(belowPos);

        if (SlabHelper.isOffsetBase(state, belowState)) {
            Vec3 currentOffset = cir.getReturnValue();
            cir.setReturnValue(new Vec3(currentOffset.x, -0.5, currentOffset.z));
        }
    }

    @Inject(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true)
    private void wildslabs$smartShapeOffset(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = (BlockState) (Object) this;
        if (net.countered.terrainslabs.api.SlabAttachments.isLight(state)) {
            cir.setReturnValue(cir.getReturnValue().move(0,net.countered.terrainslabs.api.SlabAttachments.offsetY(state,level,pos),0));
            return;
        }
        if (level instanceof ServerLevel) {
            return;
        }

        if (!SlabHelper.isValidOntop(level, pos, state)) return;

        BlockPos belowPos = pos.below();
        if (state.hasProperty(DoublePlantBlock.HALF) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            belowPos = pos.below(2);
        }

        BlockState belowState = level.getBlockState(belowPos);

        if (SlabHelper.isOffsetBase(state, belowState)) {
            Vec3 offset = state.getOffset(level, pos);

            if (offset.y < 0) {
                VoxelShape currentShape = cir.getReturnValue();
                if (!currentShape.isEmpty() && currentShape.min(Direction.Axis.Y) >= 0) {
                    cir.setReturnValue(currentShape.move(offset.x, offset.y, offset.z));
                }
            }
        }
    }

    @Inject(method="getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",at=@At("HEAD"),cancellable=true)
    private void wildslabs$lightCollision(BlockGetter level,BlockPos pos,CallbackInfoReturnable<VoxelShape> result){
        BlockState state=(BlockState)(Object)this;
        if(net.countered.terrainslabs.api.SlabAttachments.isLight(state))result.setReturnValue(state.getCollisionShape(level,pos,CollisionContext.empty()));
    }

    @Inject(method = "getFaceOcclusionShape", at = @At("RETURN"), cancellable = true)
    private void wildslabs$reduceOcclusion(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = (BlockState) (Object) this;
        if (state.getBlock() instanceof SnowLayerBlock) {
            if (direction.getAxis().isHorizontal()) {
                cir.setReturnValue(Shapes.empty());
            }
        }
    }
}
