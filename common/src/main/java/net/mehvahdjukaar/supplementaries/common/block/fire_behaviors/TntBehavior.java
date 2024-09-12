package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.misc.explosion.GunpowderExplosion;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TntBehavior implements IFireItemBehavior {


    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
                        float power, int inaccuracy, @Nullable Player owner) {
        BlockPos blockpos = BlockPos.containing(firePos);

        if (stack.getItem() instanceof BlockItem bi) {
            Block tnt = bi.getBlock();
            if (tnt instanceof TntBlock) {
                Explosion dummyExplosion = new Explosion(level, null,
                        firePos.x, firePos.y, firePos.z, 0, false, Explosion.BlockInteraction.KEEP);
                tnt.wasExploded(level, blockpos, dummyExplosion);
            } else {
                GunpowderExplosion.igniteTntHack(level, blockpos, tnt);
            }

            var entities = level.getEntities((Entity) null, new AABB(blockpos).move(0, 0.5, 0),
                    entity -> (entity instanceof PrimedTnt) || entity.getType() == CompatObjects.ALEX_NUKE.get());
            for (var e : entities) {
                Vec3 p = e.position();
                e.setPos(new Vec3(p.x, blockpos.getY() + 10 / 16f, p.z));
            }
            level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);

            return true;
        }
        return false;
    }


}
