package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class SugarCubeItem extends BlockItem {
    public SugarCubeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var v = player.getVehicle();
        if (v instanceof Horse horse && CommonConfigs.Blocks.SUGAR_BLOCK_HORSE_SPEED_DURATION.get() != 0) {
            var stack = player.getItemInHand(usedHand);
            horse.fedFood(player, stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var v = context.getPlayer().getVehicle();
        if (v instanceof Horse && CommonConfigs.Blocks.SUGAR_BLOCK_HORSE_SPEED_DURATION.get() != 0) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
