package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BlockUtils {

    public static void addOptionalOwnership(LivingEntity placer, BlockEntity tileEntity){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            ((IOwnerProtected) tileEntity).setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, Level world, BlockPos pos){
        if(ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IOwnerProtected) {
                ((IOwnerProtected) tile).setOwner(placer.getUUID());
            }
        }
    }
}
