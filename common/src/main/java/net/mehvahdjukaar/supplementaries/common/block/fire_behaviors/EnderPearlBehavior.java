package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EnderPearlBehavior extends GenericProjectileBehavior {

    @Override
    public boolean fireInner(ItemStack stack, ServerLevel level, Vec3 pos, Vec3 facing,
                        float scaledPower, int inaccuracy, @Nullable Player owner) {
        BlockSource source = new BlockSource(level, BlockPos.containing(pos));
        Projectile pearl = owner != null ? new ThrownEnderpearl(level, owner) :
                PearlMarker.createPearlToDispenseAndPlaceMarker(source, pos);

        pearl.shoot(facing.x, facing.y, facing.z, scaledPower, inaccuracy);

        pearl.setPos(pos);
        level.addFreshEntity(pearl);
        return true;
    }
}
