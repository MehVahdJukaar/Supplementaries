package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.ILavaAndWaterLoggable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NetheriteTrapdoorBlock extends TrapDoorBlock implements ILavaAndWaterLoggable, EntityBlock {
    public static final BooleanProperty LAVALOGGED = ModBlockProperties.LAVALOGGED;

    public NetheriteTrapdoorBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LAVALOGGED) ? 15 : 0), BlockSetType.IRON);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false).setValue(HALF, Half.BOTTOM).setValue(POWERED, false)
                .setValue(WATERLOGGED, false).setValue(LAVALOGGED, false));
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.NETHERITE_BLOCK;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {

        if (worldIn.getBlockEntity(pos) instanceof KeyLockableTile tile) {
            if (tile.handleAction(player, handIn, "trapdoor")) {
                state = state.cycle(OPEN);
                worldIn.setBlock(pos, state, 2);
                if (state.getValue(WATERLOGGED)) {
                    worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
                }

                //TODO: replace with proper sound event
                boolean open = state.getValue(OPEN);
                this.playSound(player, worldIn, pos, open);
                worldIn.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
        }

        return InteractionResult.sidedSuccess(worldIn.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        } else if (state.getValue(LAVALOGGED)) {
            worldIn.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        state = state.setValue(LAVALOGGED, fluidstate.getType() == Fluids.LAVA);
        return state.setValue(OPEN, false).setValue(POWERED, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new KeyLockableTile(pPos, pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction direction, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(LAVALOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(pLevel));
        }
        return super.updateShape(pState, direction, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAVALOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(LAVALOGGED) ? Fluids.LAVA.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter p_204510_1_, BlockPos p_204510_2_, BlockState p_204510_3_, Fluid p_204510_4_) {
        return ILavaAndWaterLoggable.super.canPlaceLiquid(p_204510_1_, p_204510_2_, p_204510_3_, p_204510_4_);
    }

    @Override
    public boolean placeLiquid(LevelAccessor p_204509_1_, BlockPos p_204509_2_, BlockState p_204509_3_, FluidState p_204509_4_) {
        return ILavaAndWaterLoggable.super.placeLiquid(p_204509_1_, p_204509_2_, p_204509_3_, p_204509_4_);
    }

    @Override
    public Fluid takeLiquid(LevelAccessor p_204508_1_, BlockPos p_204508_2_, BlockState p_204508_3_) {
        return ILavaAndWaterLoggable.super.takeLiquid(p_204508_1_, p_204508_2_, p_204508_3_);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.General.TOOLTIP_HINTS.get() || !flagIn.isAdvanced()) return;
        tooltip.add(Component.translatable("message.supplementaries.key.lockable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        return ILavaAndWaterLoggable.super.pickupBlock(pLevel, pPos, pState);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return super.getPickupSound();
    }
}
