package net.mehvahdjukaar.supplementaries.world.data;

import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.SyncGlobeDataPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;


public class GlobeData {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            WorldSavedData mapdata = MapVariables.get(event.getPlayer().world);
            if (mapdata != null)
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                        new SyncGlobeDataPacket(mapdata));
        }
    }



    public static class MapVariables extends WorldSavedData {
        public static final String DATA_NAME = "supplementariesData";
        public String globeData = "";
        public MapVariables() {
            super(DATA_NAME);
        }

        public MapVariables(String s) {
            super(s);
        }

        @Override
        public void read(CompoundNBT nbt) {
            globeData = nbt.getString("globeData");
        }

        @Override
        public CompoundNBT write(CompoundNBT nbt) {
            nbt.putString("globeData", globeData);
            return nbt;
        }

        public void syncData(World world) {
            this.markDirty();
            if (!world.isRemote)
                Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncGlobeDataPacket(this));
        }
        //data received from network is stored here
        public static MapVariables clientSide = new MapVariables();
        public static MapVariables get(World world) {
            if (world instanceof ServerWorld) {
                return world.getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(MapVariables::new, DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

}

