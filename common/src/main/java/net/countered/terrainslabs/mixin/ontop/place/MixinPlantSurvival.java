package net.countered.terrainslabs.mixin.ontop.place;

import net.countered.terrainslabs.generation.VegetationSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinPlantSurvival {
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void wildslabs$originalSoilRules(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> result) {
        if (!result.getReturnValue() && VegetationSupport.canSurviveOnSource((BlockState)(Object)this, level, pos)) {
            result.setReturnValue(true);
        }
    }
}
