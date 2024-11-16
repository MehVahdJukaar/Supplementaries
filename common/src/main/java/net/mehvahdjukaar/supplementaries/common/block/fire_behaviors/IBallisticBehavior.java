package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Provides stats for a ballistic trajectory
public interface IBallisticBehavior extends IFireItemBehavior {

    Data LINE = new Data(1, 0, 1);

    Data calculateData(ItemStack stack, Level level);

    record Data(float drag, float gravity, float initialSpeed) {

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("drag").forGetter(Data::drag),
                Codec.FLOAT.fieldOf("gravity").forGetter(Data::gravity),
                Codec.FLOAT.fieldOf("initialSpeed").forGetter(Data::initialSpeed)
        ).apply(instance, Data::new));
    }

    // dont override this
    @Override
    default boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Player owner) {
        var data = calculateData(stack, level);
        return fireInner(stack, level, firePos, direction,
                power * data.drag * data.initialSpeed,
                inaccuracy, owner);
    }

    boolean fireInner(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float scaledPower,
                      int inaccuracy, @Nullable Player owner);


}
