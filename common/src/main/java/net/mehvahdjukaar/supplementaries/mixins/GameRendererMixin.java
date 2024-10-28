package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow public abstract void loadEffect(ResourceLocation resourceLocation);

    //forge has an event for this but doing it this way is better as we can use instance check
    @Inject(method = "checkEntityPostEffect", at = @At("TAIL"))
    protected void checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if(entity != null && entity.getType() == EntityType.ENDER_DRAGON){
            this.loadEffect(ClientRegistry.FLARE_SHADER);
        }else if(entity instanceof AbstractSkeleton){
            this.loadEffect(ClientRegistry.BLACK_AND_WHITE_SHADER);
        }else if(entity instanceof Zombie){
            this.loadEffect(ClientRegistry.VANILLA_DESATURATE_SHADER);
        }else if(entity instanceof Rabbit e && e.getVariant() == Rabbit.Variant.EVIL){
            this.loadEffect(ClientRegistry.RAGE_SHADER);
        }else if(entity instanceof Piglin){
            this.loadEffect(ClientRegistry.GLITTER_SHADER);
        }
    }
}
