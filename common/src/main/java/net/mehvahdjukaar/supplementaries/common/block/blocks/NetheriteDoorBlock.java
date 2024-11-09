package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetheriteDoorBlock extends DoorBlock implements EntityBlock {

    public NetheriteDoorBlock(Properties builder) {
        super(BlockSetType.IRON, builder);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.NETHERITE_BLOCK;
    }

    //overrides creative drop
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof KeyLockableTile tile) {
            //forge has a better override for this (no particles)
            if (PlatHelper.getPlatform().isFabric()) {
                if (CommonConfigs.Building.NETHERITE_DOOR_UNBREAKABLE.get()) {
                    if (!tile.getKeyInInventoryStatus(player).isCorrect()) return state;
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        BlockPos p = this.hasTileEntity(state) ? pos : pos.below();
        if (level.getBlockEntity(p) instanceof KeyLockableTile keyLockableTile) {
            if (keyLockableTile.handleAction(player, hand, stack, "door")) {

                GoldDoorBlock.tryOpenDoubleDoorKey(level, state, pos, player, hand, stack);

                state = state.cycle(OPEN);
                level.setBlock(pos, state, 10);
                boolean open = state.getValue(OPEN);
                this.playSound(player, level, pos, state.getValue(OPEN));
                level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(OPEN, false).setValue(POWERED, false);
    }

    public boolean hasTileEntity(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (this.hasTileEntity(pState)) {
            return new KeyLockableTile(pPos, pState);
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!MiscUtils.showsHints(tooltipFlag)) return;
        tooltipComponents.add(KeyLockableTile.KEY_LOCKABLE_TOOLTIP);
    }
}
