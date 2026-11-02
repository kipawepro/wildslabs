package net.countered.terrainslabs.mixin.ontop.render.target;

import net.countered.terrainslabs.api.SlabAttachments;
import net.countered.terrainslabs.api.SlabHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=BlockGetter.class,priority=900)
public interface MixinRaytrace {
    @Inject(method="clip",at=@At("RETURN"),cancellable=true)
    private void wildslabs$includeShiftedShapes(ClipContext context,CallbackInfoReturnable<BlockHitResult> result){
        BlockGetter level=(BlockGetter)this;
        var shifted=BlockGetter.traverseBlocks(context.getFrom(),context.getTo(),context,(clip,pos)->{
            BlockHitResult nearest=null;
            for(BlockPos candidate:new BlockPos[]{pos.above(),pos.below()}){
                var state=level.getBlockState(candidate);
                if(!SlabAttachments.isLight(state)&&!SlabHelper.isValidOntop(level,candidate,state))continue;
                var shape=clip.getBlockShape(state,level,candidate);
                if(shape.isEmpty())continue;
                var hit=shape.clip(clip.getFrom(),clip.getTo(),candidate);
                if(hit!=null&&(nearest==null||clip.getFrom().distanceToSqr(hit.getLocation())<clip.getFrom().distanceToSqr(nearest.getLocation())))nearest=hit;
            }
            return nearest;
        },clip->null);
        var existing=result.getReturnValue();
        if(shifted!=null&&(existing==null||existing.getType()==HitResult.Type.MISS||context.getFrom().distanceToSqr(shifted.getLocation())<context.getFrom().distanceToSqr(existing.getLocation())))result.setReturnValue(shifted);
    }
}
