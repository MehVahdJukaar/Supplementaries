package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
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

    public static void addMob(Entity e) {
        if (e != null) MOB_CACHE.put(e.getUUID(), e);
    }

    private static UUID crystalID = UUID.randomUUID();

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
        var e = MOB_CACHE.getIfPresent(crystalID);
        if (e instanceof EndCrystal c){
            c.time++;
            if(e.level != Minecraft.getInstance().level){
                //invalid. make new one
                crystalID = UUID.randomUUID();
            }
        }
    }

    public static EndCrystal getEndCrystal(Level level) {
        var e = MOB_CACHE.getIfPresent(crystalID);
        if (e instanceof EndCrystal c) return c;
        EndCrystal entity = new EndCrystal(EntityType.END_CRYSTAL, level);
        entity.setShowBottom(false);
        entity.setUUID(crystalID);
        addMob(entity);
        return entity;
    }

    //clears immediately so level itself can unload immediately too
    public static void clear(){
        MOB_CACHE.invalidateAll();
    }
}
