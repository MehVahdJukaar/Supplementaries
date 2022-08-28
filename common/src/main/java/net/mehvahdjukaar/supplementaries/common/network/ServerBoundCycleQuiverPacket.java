package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ServerBoundCycleQuiverPacket implements Message {
    private final int amount;
    private final boolean mainHand;
    private final boolean setSlot;

    public ServerBoundCycleQuiverPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
        this.mainHand = buf.readBoolean();
        this.setSlot = buf.readBoolean();
    }

    public ServerBoundCycleQuiverPacket(int amount, boolean mainHand,   boolean setSlot) {
        this.amount = amount;
        this.mainHand = mainHand;
        this.setSlot = setSlot;
    }

    public ServerBoundCycleQuiverPacket(int amount, boolean mainHand) {
        this(amount, mainHand, false); //cycle
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
        buf.writeBoolean(this.mainHand);
        buf.writeBoolean(this.setSlot);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        // server world
        ServerPlayer player = (ServerPlayer) Objects.requireNonNull(context.getSender());
       if(player.getUsedItemHand()== InteractionHand.MAIN_HAND != this.mainHand){
           int aa = 1; //this should not happen
       }else{
           ItemStack stack = player.getUseItem();
           if(stack.getItem() != ModRegistry.QUIVER_ITEM.get()){
               int aaa = 1;
           }else{
               var data = QuiverItem.getQuiverData(stack);
               if(setSlot){
                   data.setSelectedSlot(amount);
               }else {
                   data.cycle(amount);
               }
           }
       }

    }
}