package net.mehvahdjukaar.supplementaries.common.entities;

import com.google.common.math.DoubleMath;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LabelEntity extends HangingEntity {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(LabelEntity.class,
            EntityDataSerializers.ITEM_STACK);

    public LabelEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }

    public LabelEntity(Level level, BlockPos pos, Direction direction) {
        super(ModRegistry.LABEL.get(), level, pos);
        this.setDirection(direction);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    //might as well use this
    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, this.direction.get2DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.setDirection(Direction.from2DDataValue(pPacket.getData()));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0;
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
    public void dropItem(@Nullable Entity entity) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof Player player) {
                if (player.getAbilities().instabuild) {
                    return;
                }
            }
            //this.spawnAtLocation(ModRegistry.LABEL_ITEM.get());
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (pKey.equals(DATA_ITEM)) {
            ItemStack itemstack = this.getItem();
            if (!itemstack.isEmpty() && itemstack.getEntityRepresentation() != this) {
                itemstack.setEntityRepresentation(this);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getItem().isEmpty()) {
            tag.put("Item", this.getItem().save(new CompoundTag()));
        }
        tag.putByte("Facing", (byte) this.direction.get2DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        CompoundTag compound = tag.getCompound("Item");
        if (!compound.isEmpty()) {
            ItemStack itemstack = ItemStack.of(compound);
            if (itemstack.isEmpty()) {
                Supplementaries.LOGGER.warn("Unable to load item from: {}", compound);
            }
            this.setItem(itemstack);
        }
        this.setDirection(Direction.from2DDataValue(tag.getByte("Facing")));
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (direction != null) {

        }
        super.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
    }

    //just updates bounding box based off current pos
    @Override
    protected void recalculateBoundingBox() {
        if (this.direction != null) {

            BlockPos pos = this.pos;
            var shape = level.getBlockState(pos).getBlockSupportShape(level, pos);
            if (shape.isEmpty()){
                return; //wait for survives to be called so this will be removed
            }
            double offset;
            if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                offset = -0.5 + shape.max(direction.getAxis());
            } else {
                offset = 0.5 - shape.min(direction.getAxis());
            }
            Vec3 v = Vec3.atCenterOf(pos);
            offset += 1 / 32f;

            v = v.add(direction.getStepX() * offset, direction.getStepY() * offset, direction.getStepZ() * offset);

            this.setPosRaw(v.x, v.y, v.z);

            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            double width = this.getWidth();
            double height = this.getHeight();
            double zWidth = this.getWidth();
            Direction.Axis axis = this.direction.getAxis();
            switch (axis) {
                case X -> width = 1.0D;
                case Y -> height = 1.0D;
                case Z -> zWidth = 1.0D;
            }
            width /= 32;
            height /= 32;
            zWidth /= 32;
            this.setBoundingBox(new AABB(x - width, y - height, z - zWidth, x + width, y + height, z + zWidth));
            //this.pos = new BlockPos(this.getX(), this.getY(), this.getZ());
        }
    }

    public BlockPos getSupportingBlockPos() {
        return switch (this.getDirection()) {
            default -> new BlockPos(this.position().add(0, 0, 0.05));
            case SOUTH -> new BlockPos(this.position().add(0, 0, -0.05));
            case WEST -> new BlockPos(this.position().add(0.05, 0, 0));
            case EAST -> new BlockPos(this.position().add(-0.05, 0, 0));
        };
    }


    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!itemstack.isEmpty() && !this.isRemoved()) {
            if (!this.level.isClientSide) {
                this.setItem(itemstack);
            }
            return InteractionResult.sidedSuccess(pPlayer.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean survives() {
        if (!this.level.noCollision(this)) {
            return false;
        }
        BlockPos pos = getSupportingBlockPos();
        Direction dir = this.getDirection();
        BlockState state = this.level.getBlockState(pos);
        var blockShape = state.getBlockSupportShape(level, pos);
        if (blockShape.isEmpty() || !state.getMaterial().isSolid()) {
            return false;
        }
        var bbShape = this.getBoundingBox().move(-Mth.floor(this.getX()), -Mth.floor(this.getY()), -Mth.floor(this.getZ()));

        var blockBounds = blockShape.bounds();
        if (dir.getAxisDirection() != Direction.AxisDirection.POSITIVE) {
            if (!DoubleMath.fuzzyEquals(bbShape.max(dir.getAxis()), bbShape.max(dir.getAxis()), 1.0E-7)) return false;
        } else {
            if (!DoubleMath.fuzzyEquals(bbShape.min(dir.getAxis()), bbShape.min(dir.getAxis()), 1.0E-7)) return false;
        }

        return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();

    }
/*
    public VoxelShape getFaceShape(VoxelShape shape, Direction side) {
        shape.

        VoxelShape voxelShape;
        if (shape.isEmpty() || shape == Shapes.block()) {
            return shape;
        }
        if (shape.faces != null) {
            voxelShape = shape.faces[side.ordinal()];
            if (voxelShape != null) {
                return voxelShape;
            }
        } else {
            shape.faces = new VoxelShape[6];
        }
        shape.faces[side.ordinal()] = voxelShape = calculateFace(shape,side);
        return voxelShape;
    }

    private static VoxelShape calculateFace(VoxelShape shape,Direction side) {
        Direction.Axis axis = side.getAxis();
        DoubleList doubleList = shape.getCoords(axis);
        if (doubleList.size() == 2 && DoubleMath.fuzzyEquals(doubleList.getDouble(0), 0.0, 1.0E-7) && DoubleMath.fuzzyEquals(doubleList.getDouble(1), 1.0, 1.0E-7)) {
            return shape;
        }
        Direction.AxisDirection axisDirection = side.getAxisDirection();
        int i = shape.findIndex(axis, axisDirection == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
        return Mth.binarySearch(0, shape.shape.getSize(axis) + 1, i -> pp < shape.get(axis, i)) - 1;
        return new SliceShape(shape, axis, i);
    }
    */

}
