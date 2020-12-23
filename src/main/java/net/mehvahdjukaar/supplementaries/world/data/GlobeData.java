package net.mehvahdjukaar.supplementaries.world.data;

import net.mehvahdjukaar.supplementaries.network.Networking;
import net.mehvahdjukaar.supplementaries.network.SyncGlobeDataPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;


public class GlobeData extends WorldSavedData {
    public static final String DATA_NAME = "supplementariesGlobeData";
    public static final GlobeDataGenerator gen = new GlobeDataGenerator();
    public byte[][] colors;
    public GlobeData() {
        super(DATA_NAME);
        this.colors= new byte[32][16];
    }

    public GlobeData(long seed){
        super(DATA_NAME);
        this.updateData(seed);
    }

    public void updateData(long seed){
        //even when data is recovered from disk this is called anyways
        this.colors = gen.generate(seed);
        //set and save data
        this.markDirty();
    }

    @Override
    public void read(CompoundNBT nbt) {
        for(int i = 0; i<colors.length; i++) {
            this.colors[i] = nbt.getByteArray("colors_"+i);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        for(int i = 0; i<colors.length; i++) {
            nbt.putByteArray("colors_"+i, this.colors[i]);
        }
        return nbt;
    }

    //call after you modify the data value
    public void syncData(World world) {
        this.markDirty();
        if (!world.isRemote)
            Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncGlobeDataPacket(this));
    }
    //data received from network is stored here
    public static GlobeData clientSide = new GlobeData();
    public static GlobeData get(World world) {
        if (world instanceof ServerWorld) {
            return world.getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(
                    ()->new GlobeData(((ServerWorld) world).getSeed()), DATA_NAME);
        } else {
            return clientSide;
        }
    }




    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        //TODO: might remove this on final release
        if(world instanceof ServerWorld && ((ServerWorld) world).getDimensionKey()==World.OVERWORLD){
            GlobeData.get((World) world).updateData(((ServerWorld) world).getSeed());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            GlobeData data = GlobeData.get(event.getPlayer().world);
            if (data != null)
                Networking.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                        new SyncGlobeDataPacket(data));
        }
    }





}



