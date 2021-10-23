package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class BlockUtils {

    public static <T extends BlockEntity & IOwnerProtected> void addOptionalOwnership(LivingEntity placer, T tileEntity) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            tileEntity.setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, Level world, BlockPos pos) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IOwnerProtected) {
                ((IOwnerProtected) tile).setOwner(placer.getUUID());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>)ticker : null;
    }

    public static class PlayerLessContext extends BlockPlaceContext {
        public PlayerLessContext(Level worldIn, @Nullable Player playerIn, InteractionHand handIn, ItemStack stackIn, BlockHitResult rayTraceResultIn) {
            super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
        }
    }
}
