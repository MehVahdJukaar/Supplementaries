package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.CreateBabyVillagerTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateBabyVillagerTask.class)
public abstract class CreateBabyVillagerTaskMixin {

    private boolean isNear = false;

    @Inject(method = {"tick"},
            at = {@At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/BrainUtil;lockGazeAndWalkToEachOther(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;F)V"
            )})
    private void tick1(ServerWorld p_212833_1_, VillagerEntity villager, long p_212833_3_, CallbackInfo ci) {
        if(!isNear){
            isNear = true;
            villager.level.broadcastEntityEvent(villager, (byte)15);
            //NetworkHandler.sendToAllTracking(villager, (ServerWorld) villager.level,new NosePacket(villager.getId(),true));
        }
    }

    @Redirect(method ="tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/merchant/villager/VillagerEntity;distanceToSqr(Lnet/minecraft/entity/Entity;)D"
                    ))
    private double tick3(VillagerEntity villagerEntity, Entity entity) {
        double dist = villagerEntity.distanceToSqr(entity);
        if(dist > 5.0D && this.isNear){
            this.isNear = false;
            villagerEntity.level.broadcastEntityEvent(villagerEntity, (byte)16);
            //NetworkHandler.sendToAllTracking(villagerEntity, (ServerWorld) villagerEntity.level,new NosePacket(villagerEntity.getId(),false));
        }
        return dist;
    }


    @Inject(method = {"stop"},
            at = {@At(value = "TAIL")})
    private void stop(ServerWorld p_212833_1_, VillagerEntity villager, long p_212833_3_, CallbackInfo ci) {
        villager.level.broadcastEntityEvent(villager, (byte)16);
        //NetworkHandler.sendToAllTracking(villager, (ServerWorld) villager.level,new NosePacket(villager.getId(),false));
        isNear = false;
    }



}
