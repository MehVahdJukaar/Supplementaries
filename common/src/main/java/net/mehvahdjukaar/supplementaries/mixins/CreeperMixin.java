package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.entities.IPartyCreeper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.SyncPartyCreeperPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements IPartyCreeper {

    @Unique
    private boolean supplementaries$festive = false;

    @Override
    public boolean supplementaries$isFestive() {
        return this.supplementaries$festive;
    }

    @Override
    public void supplementaries$setFestive(boolean festive) {
        this.supplementaries$festive = festive;
        if (!level().isClientSide) {
            //only needed when entity is alraedy spawned
            NetworkHelper.sendToAllClientPlayersTrackingEntity(this,
                    new SyncPartyCreeperPacket(this));
        }
    }

    protected CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @WrapWithCondition(method = "explodeCreeper", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    public boolean supp$checkPartyCreeper(Level instance, Entity source, double x, double y, double z, float radius, Level.ExplosionInteraction explosionInteraction) {
        if (supplementaries$festive) {
            ClientBoundParticlePacket packet = new ClientBoundParticlePacket(new Vec3(x, y + this.getBbHeight() / 2, z), ClientBoundParticlePacket.Kind.CONFETTI_EXPLOSION,
                    (int) radius, null);

            NetworkHelper.sendToAllClientPlayersTrackingEntity(this, packet);
            return false;
        }
        return true;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void supp$addPartyCreeperData(CompoundTag compound, CallbackInfo ci) {
        if (this.supplementaries$festive) {
            compound.putBoolean("Party", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void supp$readPartyCreeperData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Party")) {
            this.supplementaries$setFestive(compound.getBoolean("Party"));
        }
    }
}
