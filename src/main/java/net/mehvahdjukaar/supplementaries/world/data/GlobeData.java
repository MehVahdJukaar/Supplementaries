package net.mehvahdjukaar.supplementaries.world.data;

import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SyncGlobeDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class GlobeData extends SavedData {
    public static final String DATA_NAME = "supplementariesGlobeData";
    public byte[][] globePixels;
    public long seed;
    public GlobeData() {
        super(DATA_NAME);
        this.globePixels = new byte[32][16];
    }

    public GlobeData(long seed){
        super(DATA_NAME);
        this.seed = seed;
        this.updateData();
    }

    public void updateData(){
        //even when data is recovered from disk this is called anyways
        this.globePixels = GlobeDataGenerator.generate(this.seed);
        //set and save data
        this.setDirty();
    }

    @Override
    public void load(CompoundTag nbt) {
        for(int i = 0; i< globePixels.length; i++) {
            this.globePixels[i] = nbt.getByteArray("colors_"+i);
        }
        this.seed = nbt.getLong("seed");
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for(int i = 0; i< globePixels.length; i++) {
            nbt.putByteArray("colors_"+i, this.globePixels[i]);
        }
        nbt.putLong("seed",this.seed);
        return nbt;
    }

    //call after you modify the data value
    public void syncData(Level world) {
        this.setDirty();
        if (!world.isClientSide)
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncGlobeDataPacket(this));
    }
    //data received from network is stored here
    private static GlobeData clientSide = new GlobeData();
    public static GlobeData get(Level world) {
        if (world instanceof ServerLevel) {
            return world.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                    ()->new GlobeData(((WorldGenLevel) world).getSeed()), DATA_NAME);
        } else {
            return clientSide;
        }
    }
    public static void setClientData(GlobeData data){
        clientSide = data;
        GlobeTextureManager.INSTANCE.update();
    }


    //i have no idea of what this does anymore

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        LevelAccessor world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof ServerLevel && ((Level) world).dimension()==Level.OVERWORLD){
            GlobeData.get((Level) world).updateData();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            GlobeData data = GlobeData.get(event.getPlayer().level);
            if (data != null)
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()),
                        new SyncGlobeDataPacket(data));
        }
    }





}



