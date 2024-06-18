package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSlimedMessage;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SlimeBallEntity extends ImprovedProjectileEntity {

    protected SlimeBallEntity(double x, double y, double z, Level world) {
        super(ModEntities.THROWABLE_SLIMEBALL.get(), x, y, z, world);
    }

    protected SlimeBallEntity(LivingEntity thrower, Level world) {
        super(ModEntities.THROWABLE_SLIMEBALL.get(), thrower, world);
    }

    public SlimeBallEntity(EntityType<SlimeBallEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Component getTypeName() {
        return this.getItem().getDisplayName();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Direction hitDirection = result.getDirection();
        BlockPos pos = result.getBlockPos();

        Vec3 velocity = this.getDeltaMovement();
        Vector3f surfaceNormal = hitDirection.step();

        BlockState hitBlock = level().getBlockState(pos);

        Vec3 newVel = new Vec3(velocity.toVector3f().reflect(surfaceNormal));
        this.setDeltaMovement(newVel);
        SoundType soundType = hitBlock.getSoundType();
        this.playSound(soundType.getFallSound(), soundType.volume * 1.5f, soundType.getPitch());
        //bounce sound here
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof ISlimeable s) {
            //sets on both but also sends packet just because lmao
            s.supp$setSlimedTicks(300);

            if (!level().isClientSide) {
                ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntity(entity,
                        new ClientBoundSyncSlimedMessage(entity.getId(), s.supp$getSlimedTicks()));
            }
        }
        this.discard();
    }
}
