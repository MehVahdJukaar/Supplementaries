package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.client.RopeSlideSoundInstance;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
//use event instead
    //TODO: use event instead 1.21
    @Inject(method = "addEntity", at = @At("RETURN"))
    private void supp$addRopeSoundInstance(Entity entity, CallbackInfo ci) {
        if (entity instanceof Player p) {
            if (CommonConfigs.Functional.ROPE_SLIDE.get())
                this.minecraft.getSoundManager().queueTickingSound(new RopeSlideSoundInstance(p));
        }
    }
}
