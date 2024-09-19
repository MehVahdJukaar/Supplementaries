package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

class AntiqueInkBehavior implements ItemUseOnBlockBehavior {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return AntiqueInkItem.isEnabled();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.INK_SAC || item == ModRegistry.ANTIQUE_INK.get();
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        boolean newState = !stack.is(Items.INK_SAC);
        BlockPos pos = hit.getBlockPos();
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null && (!(tile instanceof IOwnerProtected op) || op.isAccessibleBy(player)) &&
                !(tile instanceof SignBlockEntity)) { //taken care by sign applicator
            if (AntiqueInkItem.toggleAntiqueInkOnSigns(world, player, pos, tile, newState)) {
                stack.consume(1, player);

                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}

