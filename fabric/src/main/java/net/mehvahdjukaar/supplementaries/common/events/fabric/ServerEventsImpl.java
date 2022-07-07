package net.mehvahdjukaar.supplementaries.common.events.fabric;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ServerEventsImpl {

    public static void init(){
        UseBlockCallback.EVENT.register(ServerEvents::onRightClickBlockHP);
        UseBlockCallback.EVENT.register(ServerEvents::onRightClickBlock);

    }


}
