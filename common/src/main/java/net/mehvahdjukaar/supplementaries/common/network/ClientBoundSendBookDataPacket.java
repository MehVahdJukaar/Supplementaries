package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.BookType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ClientBoundSendBookDataPacket implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ClientBoundSendBookDataPacket> CODEC = Message.makeType(
            Supplementaries.res("sync_books"),
            ClientBoundSendBookDataPacket::new);

    protected final Map<ResourceLocation, BookType> bookTypes;

    public ClientBoundSendBookDataPacket(final Set<Map.Entry<ResourceLocation, BookType>> data) {
        this.bookTypes = new HashMap<>();
        for (var entry : data) {
            this.bookTypes.put(entry.getKey(), entry.getValue());
        }
    }

    public ClientBoundSendBookDataPacket(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        this.bookTypes = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = ResourceLocation.STREAM_CODEC.decode(buf);
            BookType value = BookType.STREAM_CODEC.decode(buf);
            this.bookTypes.put(key, value);
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.bookTypes.size());
        for (var entry : this.bookTypes.entrySet()) {
            ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
            BookType.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    @Override
    public void handle(Context context) {
        ClientReceivers.handleSyncBookTypes(this);
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
