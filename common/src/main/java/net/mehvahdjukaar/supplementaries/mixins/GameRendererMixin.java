package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow public abstract void loadEffect(ResourceLocation resourceLocation);

    @Inject(method = "checkEntityPostEffect", at = @At("TAIL"))
    protected void checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if(entity != null && entity.getType() == EntityType.ENDER_DRAGON){
            this.loadEffect(new ResourceLocation("supplementaries:shaders/post/flare.json"));
        }else if(entity instanceof AbstractSkeleton){
            this.loadEffect(new ResourceLocation("supplementaries:shaders/post/black_and_white.json"));
        }else if(entity instanceof Zombie){
            this.loadEffect(new ResourceLocation("shaders/post/desaturate.json"));
        }
    }
}
