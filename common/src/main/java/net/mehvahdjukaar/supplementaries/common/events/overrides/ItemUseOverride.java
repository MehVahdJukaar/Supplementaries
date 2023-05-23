package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

interface ItemUseOverride {

    /**
     * Used for permission checks on flan compat
     */
    default boolean altersWorld() {
        return false;
    }

    boolean isEnabled();

    boolean appliesToItem(Item item);

    @Nullable
    default MutableComponent getTooltip() {
        return null;
    }

    InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                          ItemStack stack, BlockHitResult hit);
}
