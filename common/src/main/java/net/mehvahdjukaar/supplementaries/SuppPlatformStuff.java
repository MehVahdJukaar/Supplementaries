package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class SuppPlatformStuff {

    @ExpectPlatform
    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    @Contract
    public static <T> T getForgeCap(@Nonnull Object object, Class<T> capClass) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }
}
