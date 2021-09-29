package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld p_i50991_1_, GameProfile p_i50991_2_) {
        super(p_i50991_1_, p_i50991_2_);
    }

    /*

    //TODO: REMOVE
    //@OnlyIn(Dist.CLIENT)
    public void turn(double p_195049_1_, double p_195049_3_) {
        float minRot = -90;
        if (MathHelper.square(this.getFallFlyingTicks()) / 100 >= 1) {
            minRot = Math.min(-90, this.xRot - 1);
        }
        double d0 = p_195049_3_ * 0.15D;
        double d1 = p_195049_1_ * 0.15D;
        this.xRot = (float) ((double) this.xRot + d0);
        this.yRot = (float) ((double) this.yRot + d1);
        if (this.xRotO % 360 < -90 && this.xRotO % 360 > -270) {
            this.xRot = (float) Math.min(this.xRot, this.xRotO - 0.3 - 5 * MathHelper.square((this.xRotO + 90) / 180));
        }
        this.xRot = MathHelper.clamp(this.xRot, minRot, 90.0F);
        this.xRotO = (float) ((double) this.xRotO + d0);
        this.yRotO = (float) ((double) this.yRotO + d1);
        this.xRotO = MathHelper.clamp(this.xRotO, minRot, 90.0F);
        if (this.getVehicle() != null) {
            this.getVehicle().onPassengerTurned(this);
        }

    }

    */
}
