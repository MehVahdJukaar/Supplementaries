package net.mehvahdjukaar.supplementaries.common.entities;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.MovingBlockSource;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PearlMarker extends Entity {

    private Pair<ThrownEnderpearl, HitResult> event = null;
    private final List<ThrownEnderpearl> pearls = new ArrayList<>();

    private static final EntityDataAccessor<BlockPos> TELEPORT_POS = SynchedEntityData.defineId(PearlMarker.class, EntityDataSerializers.BLOCK_POS);


    public PearlMarker(Level worldIn) {
        super(ModEntities.PEARL_MARKER.get(), worldIn);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public PearlMarker(EntityType<PearlMarker> type, Level level) {
        super(type, level);
        this.setInvisible(true);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TELEPORT_POS, this.blockPosition());
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public void kill() {
        this.discard();
    }

    @Override
    public void tick() {
        super.baseTick();
        // super.tick();

        pearls.removeIf(Entity::isRemoved);
        boolean dead = pearls.isEmpty();
        Level level = level();

        if (!dead) {
            BlockPos pos = blockPosition();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof DispenserBlock || state.getBlock() instanceof CannonBlock)) {
                PistonMovingBlockEntity piston = null;
                boolean didOffset = false;

                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof PistonMovingBlockEntity p && isValidBlock(p)) {
                    piston = p;
                } else for (Direction d : Direction.values()) {
                    BlockPos offPos = pos.relative(d);
                    tile = level.getBlockEntity(offPos);

                    if (tile instanceof PistonMovingBlockEntity p && isValidBlock(p)) {
                        piston = p;
                        break;
                    }
                }

                if (piston != null) {
                    Direction dir = piston.getMovementDirection();
                    move(MoverType.PISTON, new Vec3(dir.getStepX() * 0.33, dir.getStepY() * 0.33, dir.getStepZ() * 0.33));

                    didOffset = true;
                }

                dead = !didOffset;
            }
        }

        if (dead && !level.isClientSide) {
            discard();
        }
    }

    @NotNull
    private static boolean isValidBlock(PistonMovingBlockEntity p) {
        Block b = p.getMovedState().getBlock();
        return b instanceof DispenserBlock || b instanceof CannonBlock;
    }


    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return PlatHelper.getEntitySpawnPacket(this);
    }


    private void removePearl(ThrownEnderpearl pearl) {
        this.pearls.remove(pearl);
    }

    public void addPearl(ThrownEnderpearl pearl) {
        this.pearls.add(pearl);
    }

    @Override
    public void teleportTo(double pX, double pY, double pZ) {

        if (event != null) {
            var trace = event.getSecond();
            var pearl = event.getFirst();
            if (trace instanceof BlockHitResult hitResult) {
                Level level = level();
                BlockPos fromPos = this.blockPosition();
                BlockState state = level.getBlockState(fromPos);
                BlockEntity blockEntity = level.getBlockEntity(fromPos);
                if (isValidBlockEntity(blockEntity)) {
                    BlockPos toPos = hitResult.getBlockPos().relative(hitResult.getDirection());
                    if (level.getBlockState(toPos).canBeReplaced()) {
                        CompoundTag nbt = blockEntity.saveWithoutMetadata();
                        blockEntity.setRemoved();

                        if (level.setBlockAndUpdate(fromPos, Blocks.AIR.defaultBlockState()) &&
                                level.setBlockAndUpdate(toPos, setLandingState(hitResult, state))) {

                            BlockEntity dstEntity = level.getBlockEntity(toPos);
                            if (isValidBlockEntity(dstEntity)) {
                                dstEntity.load(nbt);
                            }
                            SoundType type = state.getSoundType();
                            level.playSound(null, toPos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                        }

                    }
                    this.setTeleportPos(toPos);
                    level.broadcastEntityEvent(this, (byte) 92);
                    //level.broadcastEntityEvent(this, (byte) 46);
                    super.teleportTo(toPos.getX() + 0.5, toPos.getY() + 0.5 - this.getBbHeight() / 2f, toPos.getZ() + 0.5);

                }
            }

            this.removePearl(pearl);
            pearl.discard();
            event = null;
        } else {
            super.teleportTo(pX, pY, pZ);
        }
    }

    @NotNull
    private static BlockState setLandingState(BlockHitResult hitResult, BlockState state) {
        Direction dir = hitResult.getDirection();
        return state.setValue(DispenserBlock.FACING, dir);
    }

    private static boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof CannonBlockTile || blockEntity instanceof DispenserBlockEntity;
    }

    public BlockPos getTeleportPos() {
        return this.entityData.get(TELEPORT_POS);
    }

    public void setTeleportPos(BlockPos pos) {
        this.entityData.set(TELEPORT_POS, pos);
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 92) {
            Level level = level();
            if (level.isClientSide) {
                RandomSource random = level.random;
                //smort
                BlockPos end = this.getTeleportPos();
                BlockPos start = this.blockPosition();
                for (int j = 0; j < 64; ++j) {
                    double d0 = random.nextDouble();
                    float f = (random.nextFloat() - 0.5F) * 0.2F;
                    float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                    float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                    double d1 = Mth.lerp(d0, end.getX(), start.getX()) + (random.nextDouble() - 0.5D) + 0.5D;
                    double d2 = Mth.lerp(d0, end.getY(), start.getY()) + random.nextDouble() - 0.5D;
                    double d3 = Mth.lerp(d0, end.getZ(), start.getZ()) + (random.nextDouble() - 0.5D) + 0.5D;
                    level.addParticle(ParticleTypes.PORTAL, d1, d2, d3, f, f1, f2);
                }
            }
        }
        super.handleEntityEvent(pId);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
        super.lerpTo(pX, pY, pZ, pYaw, pPitch, pPosRotationIncrements, pTeleport);
        this.setPos(pX, pY, pZ);
    }

    public static void onProjectileImpact(Projectile projectile, HitResult hitResult) {
        Level level = projectile.level();
        if (!level.isClientSide && projectile instanceof ThrownEnderpearl pearl &&
                projectile.getOwner() instanceof PearlMarker markerEntity && projectile.removeTag("dispensed")) {

            markerEntity.event = Pair.of(pearl, hitResult);
        }
    }


    public static ThrownEnderpearl createPearlToDispenseAndPlaceMarker(BlockSource source, Position pearlPos) {
        Level level = source.getLevel();
        BlockPos pos = source.getPos();
        ThrownEnderpearl pearl = new ThrownEnderpearl(EntityType.ENDER_PEARL, level);
        pearl.setPos(pearlPos.x(), pearlPos.y(), pearlPos.z());

        if (source instanceof MovingBlockSource<?> movingBlockSource) {
            pearl.setOwner(movingBlockSource.getMinecartEntity());
        } else {
            var entity = level.getEntitiesOfClass(PearlMarker.class, new AABB(pos), (e) -> e.blockPosition().equals(pos)).stream().findAny();
            PearlMarker marker;
            if (entity.isEmpty()) {
                marker = new PearlMarker(level);
                marker.setPos(pos.getX() + 0.5D,
                        pos.getY() + 0.5 - marker.getBbHeight() / 2f,
                        pos.getZ() + 0.5D);
                level.addFreshEntity(marker);
            } else marker = entity.get();

            marker.addPearl(pearl);
            pearl.setOwner(marker);
        }
        pearl.addTag("dispensed");
        return pearl;
    }

}
