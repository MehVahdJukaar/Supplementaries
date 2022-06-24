package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;

public class LabelEntity extends HangingEntity {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(LabelEntity.class,
            EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> ATTACHMENT = SynchedEntityData.defineId(LabelEntity.class,
            EntityDataSerializers.INT);

    public LabelEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
    }

    public LabelEntity(Level world) {
        this((EntityType<? extends HangingEntity>) null, world);
    }

    public LabelEntity(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(world);
    }

    public LabelEntity(Level level, BlockPos pos, Direction direction) {
        super(ModRegistry.LABEL.get(), level, pos);
        this.setDirection(direction);

    }

    //might aswell use this
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
        this.entityData.define(ATTACHMENT, 0);
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
        }else if(pKey.equals(ATTACHMENT)){
            this.recalculateBoundingBox();
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


    protected void recalculateBoundingBox() {
        if (this.direction != null) {
            double offsetFromCenter = (0.5 - 1 / 32f) + this.getAttachmentType().offset;
            double d1 = (double) this.pos.getX() + 0.5D - (double) this.direction.getStepX() * offsetFromCenter;
            double d2 = (double) this.pos.getY() + 0.5D - (double) this.direction.getStepY() * offsetFromCenter;
            double d3 = (double) this.pos.getZ() + 0.5D - (double) this.direction.getStepZ() * offsetFromCenter;
            this.setPosRaw(d1, d2, d3);
            double d4 = this.getWidth();
            double d5 = this.getHeight();
            double d6 = this.getWidth();
            Direction.Axis axis = this.direction.getAxis();
            switch (axis) {
                case X -> d4 = 1.0D;
                case Y -> d5 = 1.0D;
                case Z -> d6 = 1.0D;
            }
            d4 /= 32.0D;
            d5 /= 32.0D;
            d6 /= 32.0D;
            this.setBoundingBox(new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
            //this.pos = new BlockPos(this.getX(), this.getY(), this.getZ());
        }
    }

    public AttachType getAttachmentType() {
        return AttachType.values()[this.entityData.get(ATTACHMENT)];
    }

    public void setAttachmentType(AttachType type) {
        this.entityData.set(ATTACHMENT, type.ordinal());
        this.recalculateBoundingBox();
    }

    public enum AttachType {
        BLOCK(0),
        CHEST(1 / 16f),
        JAR(-3 / 16f);
        public final float offset;

        AttachType(float offset) {
            this.offset = offset;
        }

        public static AttachType get(BlockState state) {
            Block b = state.getBlock();
            if (b instanceof ChestBlock) return AttachType.CHEST;
            else if (b instanceof JarBlock) return AttachType.JAR;
            else return AttachType.BLOCK;
        }

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

        AttachType currentAttachment = this.getAttachmentType();
        BlockPos blockpos = this.pos.relative(this.direction.getOpposite());

        BlockState blockstate = this.level.getBlockState(blockpos);
        AttachType type = AttachType.get(blockstate);
        if (currentAttachment != type) {
            this.setAttachmentType(type);
        }
        if (type == AttachType.BLOCK && !blockstate.isFaceSturdy(level, pos, this.direction, SupportType.CENTER)) {
            return false;
        }
        return this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();

    }

}
