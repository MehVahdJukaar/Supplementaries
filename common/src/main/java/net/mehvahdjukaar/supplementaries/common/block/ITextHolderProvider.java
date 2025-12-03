package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.block.IOneUserInteractable;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.block.IWaxable;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;


//replicates what SignBlock does + more
public interface ITextHolderProvider extends IOneUserInteractable, IWashable, IWaxable {

    TextHolder getTextHolder(int ind);

    default TextHolder getTextHolder() {
        return getTextHolder(0);
    }

    default TextHolder getTextHolderAt(Vec3 hit) {
        return getTextHolder();
    }

    default int textHoldersCount() {
        return 1;
    }

    @Override
    default boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
        if (this.isWaxed()) {
            this.setWaxed(false);
            return true;
        }
        boolean success = false;
        var text = getTextHolderAt(hitVec);

        if (!text.isEmpty(null)) {
            text.clear();
            success = true;
        }
        if (success) {
            if (this instanceof BlockEntity be) {
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return true;
        }
        return false;
    }

    default boolean tryAcceptingClientText(BlockPos pos, ServerPlayer player, List<List<FilteredText>> filteredText) {
        if (!this.isWaxed() && this.canBeUsedBy(pos, player)) {
            for (int i = 0; i < filteredText.size(); i++) {
                var holder = this.getTextHolder(i);
                holder.acceptClientMessages(player, filteredText.get(i));
            }
            this.setCurrentUser(null);
            return true;
        } else {
            Supplementaries.LOGGER.warn("Player {} just tried to change non-editable sign",
                    player.getName().getString());
        }
        return false;
    }

    default boolean tryOpeningTextEditGui(BlockEntity be, ServerPlayer player, ItemStack stack, Direction face) {
        if (this.isWaxed()) return false;
        boolean filtering = player.isTextFilteringEnabled();
        for (int i = 0; i < this.textHoldersCount(); i++) {
            if (!this.getTextHolder(i).hasEditableText(filtering)) {
                return false;
            }
        }
        return Utils.openGuiIfPossible(be, player, stack, face);
    }

    //calls all interfaces methods
    default ItemInteractionResult textHolderInteract(BlockEntity be, int index,
                                                     Player player, InteractionHand hand, ItemStack stack, Direction face) {
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        BlockState state = be.getBlockState();

        ItemInteractionResult result = this.getTextHolder(index).playerInteract(level, pos, player, hand, stack);
        if (result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            result = this.tryWaxingWithItem(level, pos, player, stack);
        }
        if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
            if (!level.isClientSide && this instanceof BlockEntity te) {
                te.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));

            return result;
        }
        if (player instanceof ServerPlayer serverPlayer &&
                this.tryOpeningTextEditGui(be, serverPlayer, stack, face)) {
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.SUCCESS;
        // return InteractionResult.PASS;
    }

}
