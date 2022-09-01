package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.items.JarItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;

import javax.annotation.Nullable;
import java.util.Collection;

//this is still a mess
public class BucketHelper {
    //bucket item mob name (not id). Many mods don't extend the base BucketItem class nor the IBucketable interface... whyy
    private static final BiMap<Item, EntityType<?>> BUCKET_TO_MOB_MAP = HashBiMap.create();

    //only use this to access the map
    @Nullable
    public static EntityType<?> getEntityTypeFromBucket(Item bucket) {
        EntityType type = BUCKET_TO_MOB_MAP.get(bucket);
        if (type != null) {
            return type;
        } else if (bucket instanceof MobBucketItem bucketItem) {
            EntityType<?> en = ForgeHelper.getFishType(bucketItem);
            if (en != null) {
                BUCKET_TO_MOB_MAP.putIfAbsent(bucket, en);
                return en;
            }
        }
        //try parsing
        else {
            String mobId = null;
            String itemName = Utils.getID(bucket).toString();
            if (itemName.contains("_bucket")) {
                mobId = itemName.replace("_bucket", "");
            } else if (itemName.contains("bucket_of_")) {
                mobId = itemName.replace("_bucket", "");
            } else if (itemName.contains("bucket_")) {
                mobId = itemName.replace("bucket_", "");
            }
            if (mobId != null) {
                ResourceLocation res = new ResourceLocation(mobId);
                var opt = Registry.ENTITY_TYPE.getOptional(res);
                if (opt.isPresent()) {
                    EntityType<?> en = opt.get();
                    BUCKET_TO_MOB_MAP.putIfAbsent(bucket, en);
                    return en;
                }
            }
        }
        return null;
    }

    //TODO: rethink all this and remove this one
    public static void tryAddingFromEntityId(String id) {
        //EntityType<?> en = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(id));
        //  if (en != null && !BUCKET_TO_MOB_MAP.containsValue(en)) {
        //     BUCKET_TO_MOB_MAP.putIfAbsent()
        //  }
    }

    public static Collection<Item> getValidBuckets() {
        return BUCKET_TO_MOB_MAP.keySet();
    }

    //if it needs to have water
    public static boolean isFishBucket(Item item) {
        return getEntityTypeFromBucket(item) != null;
    }

    public static void associateMobToBucketIfAbsent(EntityType<?> entity, Item item) {
        if (!BUCKET_TO_MOB_MAP.containsKey(item)) {
            if (!BUCKET_TO_MOB_MAP.inverse().containsKey(entity)) {
                BUCKET_TO_MOB_MAP.putIfAbsent(item, entity);
            }
        }
    }

    @Nullable
    public static ItemStack getBucketFromEntity(Entity entity) {
        if (entity instanceof Bucketable bucketable) {
            return bucketable.getBucketItemStack();
        }
        //maybe remove. not needed with new bucketable interface. might improve compat
        else if (entity instanceof WaterAnimal) {
            return tryGettingFishBucketHackery(entity, (ServerLevel) entity.level);
        }
        return null;
    }

    /**
     * try catching a mob with a water or empty bucket to then store it in the mob holder
     *
     * @return filled bucket stack or empty stack
     */
    @ExpectPlatform
    private static ItemStack tryGettingFishBucketHackery(Entity entity, ServerLevel serverLevel) {
        throw new AssertionError();
    }

    public static boolean isModdedFish(Entity entity){
        return entity instanceof WaterAnimal || entity instanceof Bucketable;
    }

}
