package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CapturedMobCache {

    private static final LoadingCache<UUID, Entity> MOB_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    private static void addToCache(Entity e) {
        if (e != null) MOB_CACHE.put(e.getUUID(), e);
    }

    private static UUID crystalID = UUID.randomUUID();
    private static boolean updateCrystal = false;

    @Nullable
    public static Entity getOrCreateCachedMob(@NotNull Level level, UUID id, CompoundTag tag) {
        Entity e = MOB_CACHE.getIfPresent(id);
        if (e == null) {
            e = createEntityFromNBT(tag, id, level);
            addToCache(e);
        }
        return e;
    }


    @Nullable
    public static Entity createEntityFromNBT(CompoundTag tag, @Nullable UUID id, Level world) {
        if (tag != null && tag.contains("id")) {
            Entity entity = EntityType.loadEntityRecursive(tag, world, o -> o);
            if (id != null && entity != null) {
                entity.setUUID(id);
                if (entity.hasCustomName()) entity.setCustomName(entity.getCustomName());
            }
            return entity;
        }
        return null;
    }

    public static void tickCrystal() {
        if (!updateCrystal) return;
        var e = MOB_CACHE.getIfPresent(crystalID);
        if (e instanceof EndCrystal c) {
            c.time++;
            if (e.level() != Minecraft.getInstance().level) {
                //invalid. make new one
                crystalID = UUID.randomUUID();
            }
        }
        updateCrystal = false;
    }

    public static EndCrystal getEndCrystal(Level level) {
        updateCrystal = true;
        var e = MOB_CACHE.getIfPresent(crystalID);
        if (e instanceof EndCrystal c) return c;
        EndCrystal entity = new EndCrystal(EntityType.END_CRYSTAL, level);
        entity.setShowBottom(false);
        entity.setUUID(crystalID);
        addToCache(entity);
        return entity;
    }

    //clears immediately so level itself can unload immediately too
    public static void clear() {
        MOB_CACHE.invalidateAll();
    }
}
