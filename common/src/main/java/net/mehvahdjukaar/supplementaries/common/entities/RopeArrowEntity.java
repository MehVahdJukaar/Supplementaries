package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeKnotBlock;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.utils.RopeHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class RopeArrowEntity extends AbstractArrow {
    private BlockPos prevPlacedPos = null;

    public RopeArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RopeArrowEntity(double x, double y, double z, Level level, ItemStack projectile, @Nullable ItemStack weapon) {
        super(ModEntities.ROPE_ARROW.get(), x, y, z, level, projectile, weapon);
    }

    public RopeArrowEntity(LivingEntity livingEntity, Level level, ItemStack projectile, @Nullable ItemStack weapon) {
        super(ModEntities.ROPE_ARROW.get(), livingEntity, level, projectile, weapon);
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return PlatHelper.getEntitySpawnPacket(this, serverEntity);
    }

    public int getCharges() {
        return getPickupItemStackOrigin().getOrDefault(ModComponents.CHARGES.get(), 0);
    }

    private void removeCharges(int amount) {
        int newCharges = this.getCharges() - amount;
        getPickupItemStackOrigin().set(ModComponents.CHARGES.get(), newCharges);
        //TODO: actual sound event here
        this.level().playSound(null, this.prevPlacedPos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 0.2f, 1.7f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.prevPlacedPos != null) {
            compound.put("PrevPlacedPos", NbtUtils.writeBlockPos(this.prevPlacedPos));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("PrevPlacedPos")) {
            this.prevPlacedPos = NbtUtils.readBlockPos(compound, "PrevPlacedPos").orElse(null);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModRegistry.ROPE_ARROW_ITEM.get().getDefaultInstance();
    }

    //TODO: add sound
    //on block hit.
    @Override
    protected void onHitBlock(BlockHitResult rayTraceResult) {
        super.onHitBlock(rayTraceResult);
        Block ropeBlock = CommonConfigs.getSelectedRope();
        if (ropeBlock == null) return;

        if (this.getCharges() <= 0) return;
        Level level = this.level();
        if (!level.isClientSide) {
            this.prevPlacedPos = null;
            Entity entity = this.getOwner();
            Player player = null;
            if (!(entity instanceof Mob) || PlatHelper.isMobGriefingOn(level, this)) {
                BlockPos hitPos = rayTraceResult.getBlockPos();

                if (entity instanceof Player pl) {
                    //TODO: i might just give null here since player isn't actually placing these blocks
                    player = pl;

                    if (CompatHandler.FLAN && !FlanCompat.canPlace(pl, hitPos)) {
                        return;
                    }
                }
                //Ugly but works
                //try finding existing ropes
                BlockState hitState = level.getBlockState(hitPos);
                Block hitBlock = hitState.getBlock();

                //knot blocks
                if (ropeBlock == ModRegistry.ROPE.get()) {
                    ModBlockProperties.PostType knotType = ModBlockProperties.PostType.get(hitState);
                    if (knotType != null) {
                        BlockState knotState = AbstractRopeKnotBlock.convertToRopeKnot(knotType, hitState, level, hitPos);
                        if (knotState != null) {
                            if (knotState.getValue(AbstractRopeKnotBlock.AXIS).isVertical()) {
                                this.prevPlacedPos = hitPos.relative(rayTraceResult.getDirection()).above();
                            } else {
                                this.prevPlacedPos = hitPos;
                            }
                            this.removeCharges(1);
                            return;
                        }
                    }
                }


                //try adding rope down
                if (hitBlock == ropeBlock && RopeHelper.addRopeDown(hitPos, level, player, InteractionHand.MAIN_HAND, ropeBlock)) {
                    this.prevPlacedPos = hitPos;
                    this.removeCharges(1);
                    return;
                }
                hitPos = hitPos.relative(rayTraceResult.getDirection());
                hitBlock = level.getBlockState(hitPos).getBlock();
                //rope to the side
                if (hitBlock == ropeBlock && RopeHelper.addRopeDown(hitPos, level, player, InteractionHand.MAIN_HAND, ropeBlock)) {
                    this.prevPlacedPos = hitPos;
                    this.removeCharges(1);
                    return;
                }

                //try placing it normally
                ItemStack ropes = new ItemStack(ropeBlock);
                BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, ropes, rayTraceResult);
                if (context.canPlace()) {
                    BlockState state = ItemsUtil.getPlacementState(context, ropeBlock);
                    if (state != null) {
                        level.setBlock(context.getClickedPos(), state, 11);
                        this.prevPlacedPos = context.getClickedPos();
                        this.removeCharges(1);
                    }
                }
            }
        }
    }


    private void continueUnwindingRope() {
        Block ropeBlock = CommonConfigs.getSelectedRope();
        if (ropeBlock == null) return;
        //no need to do other checks since this only happens after a onBlockCollision()
        Player player = null;
        Entity entity = this.getOwner();
        if (entity instanceof Player player1 && player1.mayBuild()) {
            player = player1;
        }
        BlockPos hitPos = this.prevPlacedPos;
        //Block hitBlock = this.level.getBlockState(hitPos).getBlock();
        //try adding rope down
        //hitBlock == ropeBlock &&
        if (RopeHelper.addRopeDown(hitPos.below(), level(), player, InteractionHand.MAIN_HAND, ropeBlock)) {
            this.prevPlacedPos = hitPos.below();
            this.removeCharges(1);
        } else {
            this.prevPlacedPos = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.getCharges() != 0 && this.prevPlacedPos != null) {
                this.continueUnwindingRope();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && entity.getType() != EntityType.ENDERMAN) {
            entity.setRemainingFireTicks(5);
        }
        entity.setRemainingFireTicks(k);

        this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
        this.setYRot(this.getYRot() + 180.0F);
        this.yRotO += 180.0F;
        if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
            if (this.pickup == Pickup.ALLOWED) {
                this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.discard();
        }
    }

    @Override
    public void playerTouch(Player entityIn) {
        if (!this.level().isClientSide) {
            if (entityIn.getInventory().add(this.getPickupItem())) {
                entityIn.take(this, 1);
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }
}
