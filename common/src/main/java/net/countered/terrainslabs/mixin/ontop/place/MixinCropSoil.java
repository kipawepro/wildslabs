package net.countered.terrainslabs.mixin.ontop.place;

import net.countered.terrainslabs.block.customslabs.soilslabs.FarmlandSlab;
import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CropBlock.class, StemBlock.class, AttachedStemBlock.class})
public abstract class MixinCropSoil {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void wildslabs$cropSoil(BlockState soil, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> result) {
        if (PlatformConfigHooks.isVegetationOnSlabsEnabled() && soil.getBlock() instanceof FarmlandSlab
                && !soil.getValue(FarmlandSlab.WATERLOGGED)) {
            result.setReturnValue(true);
        }
    }
}
