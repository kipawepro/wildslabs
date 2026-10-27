package net.countered.terrainslabs.mixin.ontop.render;

import net.countered.terrainslabs.api.SlabAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({TorchBlock.class, RedstoneTorchBlock.class})
public abstract class MixinTorchParticles {
    @ModifyArgs(method="animateTick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void wildslabs$lowerFlame(Args arguments,BlockState state,Level level,BlockPos pos,RandomSource random){
        arguments.set(2,(double)arguments.get(2)+SlabAttachments.offsetY(state,level,pos));
    }
}
