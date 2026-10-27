package net.countered.terrainslabs.mixin.ontop.place;

import net.countered.terrainslabs.api.SlabAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BaseTorchBlock.class, LanternBlock.class})
public abstract class MixinSlabLightSupport {
    @Inject(method="canSurvive",at=@At("RETURN"),cancellable=true)
    private void wildslabs$slabSupport(BlockState state,LevelReader level,BlockPos pos,CallbackInfoReturnable<Boolean> result){
        if(!result.getReturnValue()&&SlabAttachments.canAttach(state,level,pos))result.setReturnValue(true);
    }
}
