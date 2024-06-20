package net.mehvahdjukaar.supplementaries.common.events.overrides;


import net.mehvahdjukaar.supplementaries.common.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

class ClockItemBehavior implements ItemUseBehavior {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.CLOCK;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (world.isClientSide && ClientConfigs.Tweaks.CLOCK_CLICK.get()) {
            ClockBlock.displayCurrentHour(world, player);
            player.swing(hand);
        }
        return InteractionResult.PASS;
    }
}
