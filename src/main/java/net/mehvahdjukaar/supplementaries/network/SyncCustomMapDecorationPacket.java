package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecorationType;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapDecorationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.item.FilledMapItem;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


public class SyncCustomMapDecorationPacket {
    private final int mapId;
    private final CustomDecoration[] customDecoration;


    public SyncCustomMapDecorationPacket(int mapId, CustomDecoration[] customDecoration) {
        this.mapId = mapId;
        this.customDecoration = customDecoration;
    }

    public SyncCustomMapDecorationPacket(PacketBuffer buffer) {
        this.mapId = buffer.readVarInt();
        this.customDecoration = new CustomDecoration[buffer.readVarInt()];

        for(int i = 0; i < this.customDecoration.length; ++i) {
            CustomDecorationType<?,?> type = MapDecorationHandler.get(buffer.readResourceLocation());
            this.customDecoration[i] = type.loadDecorationFromBuffer(buffer);
        }
    }

    public static void buffer(SyncCustomMapDecorationPacket message, PacketBuffer buffer) {
        buffer.writeVarInt(message.mapId);
        buffer.writeVarInt(message.customDecoration.length);

        for(CustomDecoration decoration : message.customDecoration) {
            buffer.writeResourceLocation(decoration.getType().getId());
            decoration.saveToBuffer(buffer);
        }
    }


    public static void handler(SyncCustomMapDecorationPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {

                Minecraft mc = Minecraft.getInstance();
                MapItemRenderer mapitemrenderer = mc.gameRenderer.getMapRenderer();
                String s = FilledMapItem.makeKey(message.getMapId());
                MapData mapdata = mc.level.getMapData(s);
                if (mapdata == null) {
                    mapdata = new MapData(s);
                    if (mapitemrenderer.getMapInstanceIfExists(s) != null) {
                        MapData mapdata1 = mapitemrenderer.getData(mapitemrenderer.getMapInstanceIfExists(s));
                        if (mapdata1 != null) {
                            mapdata = mapdata1;
                        }
                    }
                    mc.level.setMapData(mapdata);
                }

                message.applyToMap(mapdata);
                mapitemrenderer.update(mapdata);
            }
        });
        context.setPacketHandled(true);
    }



    @OnlyIn(Dist.CLIENT)
    public int getMapId() {
        return this.mapId;
    }

    @OnlyIn(Dist.CLIENT)
    public void applyToMap(MapData data) {
        if(data instanceof CustomDecorationHolder){
            Map<String, CustomDecoration> decorations = ((CustomDecorationHolder) data).getCustomDecorations();
            decorations.clear();
            for(int i = 0; i < this.customDecoration.length; ++i) {
                CustomDecoration mapdecoration = this.customDecoration[i];
                decorations.put("icon-" + i, mapdecoration);
            }
        }
    }
}