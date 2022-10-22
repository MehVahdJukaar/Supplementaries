package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//why can't they open them already shm
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {

    protected WanderingTraderMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"registerGoals"},
            at = {@At("RETURN")}
    )
    public void registerGoals(CallbackInfo ci) {
        if(CommonConfigs.Tweaks.WANDERING_TRADER_DOORS.get()) {
            this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        }
    }
}
