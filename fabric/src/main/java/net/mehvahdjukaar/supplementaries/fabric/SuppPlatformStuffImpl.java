package net.mehvahdjukaar.supplementaries.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SuppPlatformStuffImpl {


    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }

    @Nullable
    public static <T> T getForgeCap(Object object, Class<T> capClass) {
        return null;
    }

    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        return null;
    }

}
