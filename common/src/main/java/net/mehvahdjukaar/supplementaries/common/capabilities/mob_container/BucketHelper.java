package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.Level;

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
            EntityType<?> en = SuppPlatformStuff.getFishType(bucketItem);
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
            return tryGettingFishBucketHackery(entity, entity.level);
        }
        return null;
    }

    /**
     * try catching a mob with a water or empty bucket and then return filled bucket
     *
     * @return filled bucket stack or empty stack
     */
    private static ItemStack tryGettingFishBucketHackery(Entity entity, Level level) {

        ItemStack bucket = ItemStack.EMPTY;
        Player player = CommonUtil.getFakePlayer(level);
        if (player != null) {
            //hax incoming
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            InteractionResult result = entity.interact(player, InteractionHand.MAIN_HAND);
            if (!result.consumesAction()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                result = entity.interact(player, InteractionHand.MAIN_HAND);
            }

            if (result.consumesAction()) {
                ItemStack filledBucket = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (!filledBucket.isEmpty() && !entity.isAlive()) {
                    bucket = filledBucket;
                }
            }
        }
        return bucket;
    }

    public static boolean isModdedFish(Entity entity) {
        return entity instanceof WaterAnimal || entity instanceof Bucketable;
    }

}
