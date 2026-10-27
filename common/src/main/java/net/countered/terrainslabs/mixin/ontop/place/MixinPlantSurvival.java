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
    @Inject(method="updateShape",at=@At("HEAD"),cancellable=true)
    private void wildslabs$nativeNeighborSoil(net.minecraft.core.Direction direction,BlockState neighbor,net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockPos neighborPos,CallbackInfoReturnable<BlockState> result){
        BlockState state=(BlockState)(Object)this;
        if(!VegetationSupport.canProbePlant(state,level,pos))return;
        BlockState replacement=direction==net.minecraft.core.Direction.DOWN?VegetationSupport.sourceStateForSlab(neighbor):null;
        result.setReturnValue(VegetationSupport.withPlantSoil(state,level,pos,()->state.updateShape(direction,replacement==null?neighbor:replacement,level,pos,neighborPos)));
    }
    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private void wildslabs$originalSoilRules(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> result) {
        if (!result.getReturnValue() && VegetationSupport.canSurviveOnSource((BlockState)(Object)this, level, pos)) {
            result.setReturnValue(true);
        }
    }
}
