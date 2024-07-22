package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EnderPearlBehavior extends GenericProjectileBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 pos, Vec3 facing,
                        float power, float drag, int inaccuracy, @Nullable Player owner) {
        BlockSource source = new BlockSourceImpl(level, BlockPos.containing(pos));
        Projectile pearl = PearlMarker.createPearlToDispenseAndPlaceMarker(source, pos);

        pearl.shoot(facing.x, facing.y, facing.z, -drag * power, inaccuracy);

        pearl.setPos(pos);
        level.addFreshEntity(pearl);
        return true;
    }
}
