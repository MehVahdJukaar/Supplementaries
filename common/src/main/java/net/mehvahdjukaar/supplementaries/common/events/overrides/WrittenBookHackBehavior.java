package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

class WrittenBookHackBehavior implements ItemUseOnBlockBehavior {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.WRITTEN_BOOKS.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.WRITTEN_BOOK || item == Items.WRITABLE_BOOK;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            //calls the placement logic that the item already has, skipping the open book stuff
            var r = AdditionalItemPlacementsAPI.getBehavior(stack.getItem())
                    .overrideUseOn(new UseOnContext(player, hand, hit), null);
            if (r.consumesAction()) return r;
        }
        return InteractionResult.PASS;
    }
}
