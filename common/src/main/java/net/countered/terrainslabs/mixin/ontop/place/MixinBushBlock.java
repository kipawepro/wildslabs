package net.countered.terrainslabs.mixin.ontop.place;

import net.countered.terrainslabs.platform.PlatformConfigHooks;
import net.countered.terrainslabs.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BushBlock.class)
public class MixinBushBlock {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void wildslabs$mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(state.getBlock() instanceof net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab)) return;
        if (!PlatformConfigHooks.isVegetationOnSlabsEnabled() || (Object)this instanceof SaplingBlock
                || (Object)this instanceof net.minecraft.world.level.block.BambooSaplingBlock) return;
        var source = net.countered.terrainslabs.block.ModSlabsMap.getSourceForSlab(state.getBlock());
        if (source == null || state.getValue(BlockStateProperties.WATERLOGGED)) return;
        cir.setReturnValue(((BushBlockAccessor)(Object)this).wildslabs$mayPlaceOn(source.defaultBlockState(), level, pos));
    }
}
