package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @ModifyExpressionValue(method = "handleSetEntityPassengersPacket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent supp$addCannonMessage(MutableComponent original,
                                                   @Local(ordinal = 0) Entity vehicle) {
      if(vehicle instanceof CannonBoatEntity) {
          Minecraft mc = Minecraft.getInstance();
          return Component.translatable("message.supplementaries.cannon_boat",
                  mc.options.keyShift.getTranslatedKeyMessage(),
                  mc.options.keySprint.getTranslatedKeyMessage(),
                  mc.options.keyJump.getTranslatedKeyMessage());
      }
        return original;
    }
}
