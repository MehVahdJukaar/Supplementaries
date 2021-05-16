package net.mehvahdjukaar.supplementaries.entities;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class LabelEntity extends HangingEntity {
    private static final DataParameter<ItemStack> DATA_ITEM = EntityDataManager.defineId(LabelEntity.class, DataSerializers.ITEM_STACK);
    public LabelEntity(EntityType<? extends HangingEntity> entityType, World world) {
        super(entityType, world);
    }
    public LabelEntity(World world) {
        this(Registry.LABEL.get(), world);
    }

    public LabelEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world) {
        this(world);
    }


    @Override
    protected float getEyeHeight(Pose pose, EntitySize size) {
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
    public IPacket<?> getAddEntityPacket() {
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
    public void onSyncedDataUpdated(DataParameter<?> dataParameter) {
        if (dataParameter.equals(DATA_ITEM)) {
            ItemStack itemstack = this.getItem();
            if (!itemstack.isEmpty() && itemstack.getEntityRepresentation() != this) {
                itemstack.setEntityRepresentation(this);
            }
        }
    }


    @Override
    public void addAdditionalSaveData(CompoundNBT com) {
        super.addAdditionalSaveData(com);
        if (!this.getItem().isEmpty()) {
            com.put("Item", this.getItem().save(new CompoundNBT()));
        }

        com.putByte("Facing", (byte)this.direction.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT p_70037_1_) {
        super.readAdditionalSaveData(p_70037_1_);
        CompoundNBT compoundnbt = p_70037_1_.getCompound("Item");
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
            this.setBoundingBox(new AxisAlignedBB(d0 - d6, d1 - d7, d2 - d8, d0 + d6, d1 + d7, d2 + d8));
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
