package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
    public static Entity getCachedMob(UUID id) {
        return cachedMobs.getIfPresent(id);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) pedestalCrystal.get().time++;
    }

}
