package net.mehvahdjukaar.supplementaries.common.events.overrides;


import net.mehvahdjukaar.supplementaries.common.block.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

class CompassItemBehavior implements ItemUseBehavior {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == Items.COMPASS;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (!world.isClientSide && CommonConfigs.Tweaks.COMPASS_CLICK.get()) {
            LodestoneTracker lodestone = stack.get(DataComponents.LODESTONE_TRACKER);
            if (lodestone != null) {
                GlobalPos target = lodestone.target().orElse(null);
                if (target == null || target.dimension() != world.dimension()) {
                    GlobeBlock.displayCoordinates(world, player, player.blockPosition(), true);
                } else GlobeBlock.displayCoordinates(world, player, target.pos(), false);
            } else {
                GlobeBlock.displayCurrentCoordinates(world, player, player.blockPosition());
            }
            player.swing(hand);
        }
        return InteractionResult.PASS;
    }
}
