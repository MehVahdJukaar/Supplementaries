package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.supplementaries.common.block.util.MobContainer.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CapturedMobCache {

    public static LoadingCache<UUID, Entity> MOB_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    public static void addMob(Entity e) {
        if (e == null) e = DEFAULT_PIG.get();
        MOB_CACHE.put(e.getUUID(), e);
    }

    public static final Supplier<EndCrystal> PEDESTAL_CRYSTAL = Suppliers.memoize(() -> {
        EndCrystal entity = new EndCrystal(EntityType.END_CRYSTAL, Minecraft.getInstance().level);
        entity.setShowBottom(false);
        return entity;
    });

    private static final Supplier<Entity> DEFAULT_PIG = Suppliers.memoize(() -> new Pig(EntityType.PIG, Minecraft.getInstance().level));

    @Nullable
    public static Entity getOrCreateCachedMob(UUID id, CompoundTag tag) {
        Entity e = MOB_CACHE.getIfPresent(id);
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
        PEDESTAL_CRYSTAL.get().time++;
    }
}
