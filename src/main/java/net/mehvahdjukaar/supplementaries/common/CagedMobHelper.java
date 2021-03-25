package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class CagedMobHelper {
    public static Map<UUID, Entity> cachedMobs = new HashMap<>();

    //we may have a memory leak here... too bad
    public static boolean addMob(Entity e){
        if(e==null) e=defaultPig;
        if(!cachedMobs.containsKey(e.getUUID())) {
            cachedMobs.put(e.getUUID(), e);
            return true;
        }
        return false;
    }

    private static Entity defaultPig;
    @Nullable
    public static Entity getCachedMob(UUID id){
        return cachedMobs.getOrDefault(id, null);
    }

    //TODO: do some memory cleanups here

    //use better event
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof World && ((World) world).dimension()==World.OVERWORLD){
            cachedMobs = new HashMap<>();
            defaultPig = new PigEntity(EntityType.PIG, (World) world);
        }
    }


}
