package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

//needed to suppress block actions, so we can always rotate a block even if for example it would open an inventory normally
class WrenchBehavior implements ItemUseOnBlockOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return RegistryConfigs.WRENCH_ENABLED.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        return item == ModRegistry.WRENCH.get();
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            var h = CommonConfigs.Items.WRENCH_BYPASS.get();
            if ((h == CommonConfigs.Hands.MAIN_HAND && hand == InteractionHand.MAIN_HAND) ||
                    (h == CommonConfigs.Hands.OFF_HAND && hand == InteractionHand.OFF_HAND) || h == CommonConfigs.Hands.BOTH) {

                return stack.useOn(new UseOnContext(player, hand, hit));
            }
        }
        return InteractionResult.PASS;
    }
}

