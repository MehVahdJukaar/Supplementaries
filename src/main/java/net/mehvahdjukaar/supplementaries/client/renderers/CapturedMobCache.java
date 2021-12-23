package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CapturedMobCache {

    public static LoadingCache<UUID, Entity> cachedMobs = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, Entity>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    public static void addMob(Entity e) {
        if (e == null) e = defaultPig.get();
        cachedMobs.put(e.getUUID(), e);
    }

    public static final Lazy<EndCrystal> pedestalCrystal = Lazy.of(() -> {
        EndCrystal entity = new EndCrystal(EntityType.END_CRYSTAL, Minecraft.getInstance().level);
        entity.setShowBottom(false);
        return entity;
    });

    private static final Lazy<Entity> defaultPig = Lazy.of(() -> new Pig(EntityType.PIG, Minecraft.getInstance().level));

    @Nullable
    public static Entity getOrCreateCachedMob(UUID id, CompoundTag tag) {
        Entity e = cachedMobs.getIfPresent(id);
        if (e == null) {
            Level world = Minecraft.getInstance().level;
            if (world != null) {
                CompoundTag mobData = tag.getCompound("EntityData");

                e = MobContainer.createEntityFromNBT(mobData, id, world);
                addMob(e);
            }
        }
        return e;
    }


    public static void tickCrystal() {
        pedestalCrystal.get().time++;
    }
}
