package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.supplementaries.mixins.fabric.MobBucketItemAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;

public class SuppPlatformStuffImpl {


    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).getType();
    }



}
