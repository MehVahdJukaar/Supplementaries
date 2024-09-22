package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.api.util.FakePlayerManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

class KeyBehavior extends DispenserHelper.AdditionalDispenserBehavior {
    protected KeyBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Direction dir = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(dir);

        var tile = level.getBlockEntity(pos);
        if (tile instanceof KeyLockableTile kt) {
            Player fp = FakePlayerManager.getDefault(level);
            fp.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockHitResult blockHitResult = new BlockHitResult(Vec3.atCenterOf(pos), dir, pos, false);

            kt.getBlockState().useItemOn(stack, level, fp, InteractionHand.MAIN_HAND, blockHitResult);
            //var v = stack.useOn(context);
            //doesnt consume key
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
