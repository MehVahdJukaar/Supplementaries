package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class LabelEntity extends HangingEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(LabelEntity.class, EntityDataSerializers.ITEM_STACK);
    public LabelEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }
    public LabelEntity(Level world) {
        this(ModRegistry.LABEL.get(), world);
    }

    public LabelEntity(FMLPlayMessages.SpawnEntity spawnEntity, Level world) {
        this(world);
    }


    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions size) {
        return 0.0F;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void dropItem(@Nullable Entity p_110128_1_) {

    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean setSlot(int slot, ItemStack stack) {
        if (slot == 0) {
            this.setItem(stack);
            return true;
        } else {
            return false;
        }
    }

    public void setItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
            stack.setEntityRepresentation(this);
        }
        this.getEntityData().set(DATA_ITEM, stack);
        if (!stack.isEmpty()) {
            this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
        }
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
        if (dataParameter.equals(DATA_ITEM)) {
            ItemStack itemstack = this.getItem();
            if (!itemstack.isEmpty() && itemstack.getEntityRepresentation() != this) {
                itemstack.setEntityRepresentation(this);
            }
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundTag com) {
        super.addAdditionalSaveData(com);
        if (!this.getItem().isEmpty()) {
            com.put("Item", this.getItem().save(new CompoundTag()));
        }

        com.putByte("Facing", (byte)this.direction.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_70037_1_) {
        super.readAdditionalSaveData(p_70037_1_);
        CompoundTag compoundnbt = p_70037_1_.getCompound("Item");
        if (compoundnbt != null && !compoundnbt.isEmpty()) {
            ItemStack itemstack = ItemStack.of(compoundnbt);
            if (itemstack.isEmpty()) {
                LOGGER.warn("Unable to load item from: {}", compoundnbt);
            }
            this.setItem(itemstack);
        }
        this.setDirection(Direction.from3DDataValue(p_70037_1_.getByte("Facing")));
        //this.setInvisible(p_70037_1_.getBoolean("Invisible"));
        //this.fixed = p_70037_1_.getBoolean("Fixed");
    }

    @Override
    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double d0 = (double)this.pos.getX() + 0.5D;
            double d1 = (double)this.pos.getY() + 0.5D;
            double d2 = (double)this.pos.getZ() + 0.5D;
            double d3 = 0.46875D;
            double d4 = this.offs(this.getWidth());
            double d5 = this.offs(this.getHeight());
            d0 = d0 - (double)this.direction.getStepX() * 0.46875D;
            d2 = d2 - (double)this.direction.getStepZ() * 0.46875D;
            d1 = d1 + d5;
            Direction direction = this.direction.getCounterClockWise();
            d0 = d0 + d4 * (double)direction.getStepX();
            d2 = d2 + d4 * (double)direction.getStepZ();
            this.setPosRaw(d0, d1, d2);
            double d6 = (double)this.getWidth();
            double d7 = (double)this.getHeight();
            double d8 = (double)this.getWidth();
            if (this.direction.getAxis() == Direction.Axis.Z) {
                d8 = 1.0D;
            } else {
                d6 = 1.0D;
            }

            d6 = d6 / 32.0D;
            d7 = d7 / 32.0D;
            d8 = d8 / 32.0D;
            this.setBoundingBox(new AABB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
        }
    }
    private double offs(int o) {
        return o % 32 == 0 ? 0.5D : 0.0D;
    }

    public enum AttachType{
        BLOCK(0),
        CHEST(-1/16f),
        JAR(-4/16f);
        public final float offset;

        AttachType(float offset){
            this.offset = offset;
        }

    }
}
