package net.mehvahdjukaar.supplementaries.block.util;

import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.mehvahdjukaar.supplementaries.network.ClientBoundOpenScreenPacket;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

@OnlyIn(
        value = Dist.CLIENT,
        _interface = IScreenProvider.class
)
public interface ITextHolderProvider extends IScreenProvider {
    TextHolder getTextHolder();

    default void sendOpenTextEditScreenPacket(World level, BlockPos pos, ServerPlayerEntity player){
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientBoundOpenScreenPacket(pos));
    }
}
