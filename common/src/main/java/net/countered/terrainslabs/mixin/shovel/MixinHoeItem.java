package net.countered.terrainslabs.mixin.shovel;

import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.countered.terrainslabs.util.MixinHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void wildslabs$till(UseOnContext context, CallbackInfoReturnable<InteractionResult> result) {
        var level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (context.getClickedFace() == Direction.DOWN || !level.isEmptyBlock(pos.above())
                || !(state.getBlock() instanceof CustomSlab) || state.getValue(CustomSlab.WATERLOGGED)) return;

        Block target=net.countered.terrainslabs.platform.PlatformConfigHooks.getToolTarget(state,context,true);
        if (target==null && (state.is(ModBlocksRegistry.COARSE_SLAB.get()) || state.is(ModBlocksRegistry.ROOTED_DIRT_SLAB.get()))) {
            target = ModBlocksRegistry.DIRT_SLAB.get();
        } else if (target==null && (state.is(ModBlocksRegistry.DIRT_SLAB.get()) || state.is(ModBlocksRegistry.GRASS_SLAB.get())
                || state.is(ModBlocksRegistry.PATH_SLAB.get()) || state.is(net.countered.terrainslabs.block.ModBlockTags.TILLABLE_SLABS))) {
            target = ModBlocksRegistry.FARMLAND_SLAB.get();
        } else if(target==null) return;

        level.playSound(context.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
        if (!level.isClientSide) {
            BlockState tilled = MixinHelper.withCopiedSlabProperties(state, target);
            level.setBlock(pos, tilled, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), tilled));
            if (context.getPlayer() != null) context.getItemInHand().hurtAndBreak(1, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
        }
        result.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
