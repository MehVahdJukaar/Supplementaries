package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.common.block.blocks.DirectionalCakeBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

class DoubleCakeBehavior implements ItemUseOnBlockOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Nullable
    @Override
    public MutableComponent getTooltip() {
        return Component.translatable("message.supplementaries.double_cake");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.CAKE;
    }

    private InteractionResult placeDoubleCake(Player player, ItemStack stack, BlockPos pos, Level world, BlockState state) {
        boolean isDirectional = state.getBlock() == ModRegistry.DIRECTIONAL_CAKE.get();

        if ((isDirectional && state.getValue(DirectionalCakeBlock.BITES) == 0) || state == net.minecraft.world.level.block.Blocks.CAKE.defaultBlockState()) {

            return InteractEventOverrideHandler.replaceSimilarBlock(ModRegistry.DOUBLE_CAKE.get(),
                    player, stack, pos, world, state, null, DoubleCakeBlock.FACING);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block b = state.getBlock();
            if (b == net.minecraft.world.level.block.Blocks.CAKE || b == ModRegistry.DIRECTIONAL_CAKE.get()) {
                InteractionResult result = InteractionResult.FAIL;

                if (CommonConfigs.Tweaks.DOUBLE_CAKE_PLACEMENT.get()) {
                    result = placeDoubleCake(player, stack, pos, world, state);
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }
}

