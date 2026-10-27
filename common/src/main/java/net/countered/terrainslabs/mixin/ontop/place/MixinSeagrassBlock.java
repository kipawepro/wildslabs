package net.countered.terrainslabs.mixin.ontop.place;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SeagrassBlock.class)
public class MixinSeagrassBlock {

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void allowPlacementOnSlabs(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(state.getBlock() instanceof net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab)) return;
        if (!net.countered.terrainslabs.platform.PlatformConfigHooks.isVegetationOnSlabsEnabled()) return;
        var source = net.countered.terrainslabs.block.ModSlabsMap.getSourceForSlab(state.getBlock());
        if (source == null) return;
        if (state.getValue(net.minecraft.world.level.block.SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM
                && !state.getValue(net.minecraft.world.level.block.SlabBlock.WATERLOGGED)) { cir.setReturnValue(false); return; }
        cir.setReturnValue(((BushBlockAccessor)(Object)this).wildslabs$mayPlaceOn(source.defaultBlockState(), level, pos));
    }
}
