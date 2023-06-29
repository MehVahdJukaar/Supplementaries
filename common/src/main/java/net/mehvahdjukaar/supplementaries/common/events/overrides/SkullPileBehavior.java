package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

class SkullPileBehavior implements ItemUseOnBlockOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean placesBlock() {
        return true;
    }

    @Nullable
    @Override
    public MutableComponent getTooltip() {
        return Component.translatable("message.supplementaries.double_cake");
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.SKULL_PILES.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock skull &&
                skull.getType() != SkullBlock.Types.DRAGON && skull.getType() != EndermanSkullBlock.TYPE;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();

        if (world.getBlockEntity(pos) instanceof SkullBlockEntity oldTile) {
            BlockState state = oldTile.getBlockState();
            if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                ItemStack copy = stack.copy();

                InteractionResult result = InteractEventOverrideHandler.replaceSimilarBlock(ModRegistry.SKULL_PILE.get(),
                        player, stack, pos, world, state, null, SkullBlock.ROTATION);

                if (result.consumesAction()) {
                    if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {
                        tile.initialize(oldTile, copy, player, hand);
                    }
                }
                return result;
            }
        }
        return InteractionResult.PASS;
    }
}

