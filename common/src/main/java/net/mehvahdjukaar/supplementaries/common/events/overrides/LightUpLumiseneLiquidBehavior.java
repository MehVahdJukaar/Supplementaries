package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.fluids.FlammableLiquidBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// needed as normal behavior won't scan for liquids
class LightUpLumiseneLiquidBehavior implements ItemUseOnBlockBehavior {

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Functional.LUMISENE_ENABLED.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        // same as ILightable ones
        return item instanceof FlintAndSteelItem || item.builtInRegistryHolder().is(ILightable.FLINT_AND_STEELS) || item instanceof FireChargeItem;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        double blockReach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        BlockHitResult blockHitResult = (BlockHitResult) player.pick(blockReach, 1, true);
        BlockState state = world.getBlockState(blockHitResult.getBlockPos());
        if (state.getBlock() instanceof FlammableLiquidBlock) {
            //super hack
            return state.useItemOn(player.getItemInHand(hand), world, player, hand, blockHitResult).result();
        }

        return InteractionResult.PASS;
    }
}

