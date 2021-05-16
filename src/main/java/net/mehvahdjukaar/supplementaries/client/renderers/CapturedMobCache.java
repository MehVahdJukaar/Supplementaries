package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CapturedMobCache {

    public static LoadingCache<UUID, Entity> cachedMobs = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, Entity>() {
                @Override
                public Entity load(UUID key) {
                    return null;
                }
            });

    public static void addMob(Entity e){
        if(e==null) e=defaultPig;
        cachedMobs.put(e.getUUID(), e);
    }

    public static EnderCrystalEntity pedestalCrystal;

    private static Entity defaultPig;
    @Nullable
    public static Entity getCachedMob(UUID id){
        return cachedMobs.getIfPresent(id);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END && pedestalCrystal!=null)pedestalCrystal.time++;
    }

    //remove?
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof World && ((World) world).dimension()==World.OVERWORLD){
            defaultPig = new PigEntity(EntityType.PIG, (World) world);
            pedestalCrystal = new EnderCrystalEntity(EntityType.END_CRYSTAL,(World)world);
            pedestalCrystal.setShowBottom(false);
        }
    }
}
