package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EnderPearlBehavior extends DefaultProjectileBehavior {

    public EnderPearlBehavior(Level level, ItemStack projectile) {
        super(level, projectile);
    }

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, BlockPos pos, Vec3 facing, int power, float drag, int inaccuracy, @Nullable Player owner) {
        BlockSource source = new BlockSourceImpl(level, pos);
        Projectile pearl = PearlMarker.getPearlToDispenseAndPlaceMarker(source);

        pearl.shoot(facing.x, facing.y, facing.z, -drag * power, inaccuracy);

        pearl.setPos(pos.getX() + 0.5 - facing.x, pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);
        level.addFreshEntity(pearl);
        return true;
    }
}
