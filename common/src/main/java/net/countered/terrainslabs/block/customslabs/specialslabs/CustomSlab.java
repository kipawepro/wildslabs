package net.countered.terrainslabs.block.customslabs.specialslabs;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

public class CustomSlab extends SlabBlock {
    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,net.minecraft.world.level.storage.loot.LootParams.Builder parameters){
        var source=net.countered.terrainslabs.generation.VegetationSupport.sourceStateForSlab(state);
        return source==null?super.getDrops(state,parameters):source.getDrops(parameters.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,source));
    }
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,net.minecraft.world.level.Level level,net.minecraft.core.BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        return net.countered.terrainslabs.platform.PlatformConfigHooks.useSourceItem(stack,state,level,pos,player,hand,hit);
    }
    public static final BooleanProperty GENERATED;

    static {
        GENERATED = BooleanProperty.create("generated");
    }

    public CustomSlab(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, false)
                .setValue(GENERATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, GENERATED);
    }
}
