package net.mehvahdjukaar.supplementaries.world.data;

import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.SyncGlobeDataPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class GlobeData extends WorldSavedData {
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
    public void load(CompoundNBT nbt) {
        for(int i = 0; i< globePixels.length; i++) {
            this.globePixels[i] = nbt.getByteArray("colors_"+i);
        }
        this.seed = nbt.getLong("seed");
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        for(int i = 0; i< globePixels.length; i++) {
            nbt.putByteArray("colors_"+i, this.globePixels[i]);
        }
        nbt.putLong("seed",this.seed);
        return nbt;
    }

    //call after you modify the data value
    public void syncData(World world) {
        this.setDirty();
        if (!world.isClientSide)
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncGlobeDataPacket(this));
    }
    //data received from network is stored here
    public static GlobeData clientSide = new GlobeData();
    public static GlobeData get(World world) {
        if (world instanceof ServerWorld) {
            return world.getServer().getLevel(World.OVERWORLD).getDataStorage().computeIfAbsent(
                    ()->new GlobeData(((ISeedReader) world).getSeed()), DATA_NAME);
        } else {
            return clientSide;
        }
    }


    //i have no idea of what this does anymore

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof ServerWorld && ((World) world).dimension()==World.OVERWORLD){
            GlobeData.get((World) world).updateData();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            GlobeData data = GlobeData.get(event.getPlayer().level);
            if (data != null)
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                        new SyncGlobeDataPacket(data));
        }
    }





}



