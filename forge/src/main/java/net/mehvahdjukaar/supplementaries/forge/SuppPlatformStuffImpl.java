package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.mixins.forge.MobBucketItemAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;

public class SuppPlatformStuffImpl {

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).invokeGetFishType();
    }

}
