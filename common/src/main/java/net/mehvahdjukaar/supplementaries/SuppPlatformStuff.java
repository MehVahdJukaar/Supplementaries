package net.mehvahdjukaar.supplementaries;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.block.NoteBlock;


public class SuppPlatformStuff {

    @ExpectPlatform
    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        throw new AssertionError();
    }

}
