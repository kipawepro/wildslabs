package net.countered.terrainslabs.mixin.ontop.place;

import net.countered.terrainslabs.generation.VegetationSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class, WorldGenRegion.class})
public abstract class MixinSoilProbeLevel {
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void wildslabs$probeSource(BlockPos pos, CallbackInfoReturnable<BlockState> result) {
        BlockState source = VegetationSupport.sourceDuringProbe((BlockGetter)(Object)this, pos);
        if (source != null) result.setReturnValue(source);
    }
}
