package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record AlternativeBehavior(IFireItemBehavior first,
                                  IFireItemBehavior second) implements IFireItemBehavior, IBallistic {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction,
                        float power, float drag, int inaccuracy, @Nullable Player owner) {
        return this.first.fire(stack, level, firePos, direction, power, drag, inaccuracy, owner) ||
                this.second.fire(stack, level, firePos, direction, power, drag, inaccuracy, owner);
    }

    @Override
    public Data calculateData(ItemStack stack, Level level) {
        if (this.first instanceof IBallistic b) {
            var d = b.calculateData(stack, level);
            if (d != IBallistic.LINE) return d;
        }
        if (this.second instanceof IBallistic b) {
            return b.calculateData(stack, level);
        }
        return IBallistic.LINE;
    }
}
