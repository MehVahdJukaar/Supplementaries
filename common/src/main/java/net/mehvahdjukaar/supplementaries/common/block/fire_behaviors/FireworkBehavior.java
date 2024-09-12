package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FireworkBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
                        float power, int inaccuracy, @Nullable Player owner) {

        FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, stack,
                firePos.x(), firePos.y(), firePos.z(), true);

        fireworkrocketentity.shoot(0, 1, 0, 0.5F, 1.0F);
        level.addFreshEntity(fireworkrocketentity);
        stack.shrink(1);

        level.levelEvent(LevelEvent.SOUND_FIREWORK_SHOOT, BlockPos.containing(firePos), 0);

        return true;
    }

}
