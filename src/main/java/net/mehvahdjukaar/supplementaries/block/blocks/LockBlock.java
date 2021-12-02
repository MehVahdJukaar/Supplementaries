package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class LockBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (state.getValue(POWERED)) return InteractionResult.PASS;
        if (worldIn.getBlockEntity(pos) instanceof KeyLockableTile tile) {
            if (tile.handleAction(player, handIn, "lock_block")) {
                if (!worldIn.isClientSide) {
                    worldIn.setBlock(pos, state.setValue(POWERED, true), 2 | 4 | 3);
                    worldIn.getBlockTicks().scheduleTick(pos, this, 20);
                }

                worldIn.levelEvent(player, 1037, pos, 0);
                //this.playSound(player, worldIn, pos, true);
            }
        }

        return InteractionResult.sidedSuccess(worldIn.isClientSide);
    }

    protected void playSound(@Nullable Player player, Level worldIn, BlockPos pos, boolean isOpened) {
        if (isOpened) {
            int i = this.material == Material.METAL ? 1037 : 1007;
            worldIn.levelEvent(player, i, pos, 0);
        } else {
            int j = this.material == Material.METAL ? 1036 : 1013;
            worldIn.levelEvent(player, j, pos, 0);
        }

    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        if (!worldIn.isClientSide && state.getValue(POWERED)) {
            worldIn.setBlock(pos, state.setValue(POWERED, false), 2 | 4 | 3);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new KeyLockableTile(pPos, pState);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.key.lockable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }
}
