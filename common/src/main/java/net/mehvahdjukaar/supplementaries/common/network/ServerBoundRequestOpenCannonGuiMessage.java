package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ServerBoundRequestOpenCannonGuiMessage(TileOrEntityTarget target) implements Message {

    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundRequestOpenCannonGuiMessage> CODEC = Message.makeType(
            Supplementaries.res("c2s_request_open_cannon_gui"), ServerBoundRequestOpenCannonGuiMessage::new);

    public ServerBoundRequestOpenCannonGuiMessage(RegistryFriendlyByteBuf buf) {
        this(TileOrEntityTarget.read(buf));
    }

    @Override
    public void write(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.target.write(friendlyByteBuf);
    }

    @Override
    public void handle(Context context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            Level level = player.level();

            if ( target.getTarget(level) instanceof MenuProvider mp) {
                Utils.openGuiIfPossible().openCustomMenu(mp);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return CODEC.type();
    }
}
