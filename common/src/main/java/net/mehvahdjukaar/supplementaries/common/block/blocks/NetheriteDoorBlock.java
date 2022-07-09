package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetheriteDoorBlock extends DoorBlock implements EntityBlock {

    public NetheriteDoorBlock(Properties builder) {
        super(builder);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {

        BlockPos p = this.hasTileEntity(state) ? pos : pos.below();
        if (worldIn.getBlockEntity(p) instanceof KeyLockableTile keyLockableTile) {
            if (keyLockableTile.handleAction(player, handIn, "door")) {

                GoldDoorBlock.tryOpenDoubleDoorKey(worldIn, state, pos, player, handIn);

                state = state.cycle(OPEN);
                worldIn.setBlock(pos, state, 10);
                //TODO: replace with proper sound event
                boolean open = state.getValue(OPEN);
                worldIn.levelEvent(player, open ? this.getOpenSound() : this.getCloseSound(), pos, 0);
                worldIn.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
        }

        return InteractionResult.sidedSuccess(worldIn.isClientSide);
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

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
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
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(Component.translatable("message.supplementaries.key.lockable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }
}
