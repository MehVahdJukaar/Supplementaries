package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SoapBlock;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSlimedMessage;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class SlimeBallEntity extends ImprovedProjectileEntity {

    private int hits = 0;

    public SlimeBallEntity(Level world, double x, double y, double z) {
        super(ModEntities.THROWABLE_SLIMEBALL.get(), x, y, z, world);
    }

    public SlimeBallEntity(LivingEntity thrower) {
        super(ModEntities.THROWABLE_SLIMEBALL.get(), thrower, thrower.level());
    }

    public SlimeBallEntity(EntityType<SlimeBallEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("hits", this.hits);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.hits = compound.getInt("hits");
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
        hits++;
        Direction hitDirection = result.getDirection();
        BlockPos pos = result.getBlockPos();

        Vec3 velocity = this.getDeltaMovement();
        Vector3f surfaceNormal = hitDirection.step();

        BlockState hitBlock = level().getBlockState(pos);

        Vec3 newVel = new Vec3(velocity.toVector3f().reflect(surfaceNormal));
        float conservedEnergy = 0.75f;
        newVel = newVel.scale(conservedEnergy);
        this.setDeltaMovement(newVel);
        this.setPos(this.position().add(surfaceNormal.x * 0.1f, surfaceNormal.y * 0.1f, surfaceNormal.z * 0.1f));
        SoundType soundType = hitBlock.getSoundType();
        this.playSound(soundType.getFallSound(), soundType.volume * 1.5f, soundType.getPitch());
        // bounce sound here and partiles

        if (hits > 3) {
            SoapBlock
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof ISlimeable s) {
            //sets on both but also sends packet just because lmao
            s.supp$setSlimedTicks(CommonConfigs.Tweaks.SLIME_DURATION.get(), true);
        }
        this.discard();
    }

    @Override
    public boolean collidesWithBlocks() {
        return true;
    }
}
