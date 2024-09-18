package net.mehvahdjukaar.supplementaries.common.entities;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.MovingBlockSource;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TELEPORT_POS, this.blockPosition());
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
            if (!(isValidBlock(state))) {
                PistonMovingBlockEntity piston = null;
                boolean didOffset = false;

                BlockEntity tile = level.getBlockEntity(pos);
                if (tile instanceof PistonMovingBlockEntity p && isValidBlock(p.getMovedState())) {
                    piston = p;
                } else for (Direction d : Direction.values()) {
                    BlockPos offPos = pos.relative(d);
                    tile = level.getBlockEntity(offPos);

                    if (tile instanceof PistonMovingBlockEntity p && isValidBlock(p.getMovedState())) {
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
    private static boolean isValidBlock(BlockState p) {
        Block b = p.getBlock();
        return b instanceof DispenserBlock || b instanceof CannonBlock || b instanceof TrappedPresentBlock;
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
                    Direction direction = hitResult.getDirection();
                    BlockPos toPos = hitResult.getBlockPos().relative(direction);
                    if (level.getBlockState(toPos).canBeReplaced()) {
                        CompoundTag nbt = blockEntity.saveWithoutMetadata();
                        blockEntity.setRemoved();

                        BlockState newState = getLandingState(state, toPos, direction, level);
                        if (level.setBlockAndUpdate(fromPos, Blocks.AIR.defaultBlockState()) &&
                                level.setBlockAndUpdate(toPos, newState)) {
                            // gets rid of triggered state of dispenser
                            newState.neighborChanged(level, toPos, level.getBlockState(toPos.below()).getBlock(), toPos.below(), true);

                            BlockEntity dstEntity = level.getBlockEntity(toPos);
                            if (isValidBlockEntity(dstEntity)) {
                                dstEntity.load(nbt);
                            }
                            SoundType type = state.getSoundType();
                            level.playSound(null, toPos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                        }

                    }
                    NetworkHelper.sentToAllClientPlayersTrackingEntity(this,
                            new ClientBoundParticlePacket(fromPos.getCenter(),
                                    ClientBoundParticlePacket.Kind.PEARL_TELEPORT,
                                    0, toPos.getCenter()));

                    super.teleportTo(toPos.getX() + 0.5, toPos.getY() + 0.5 - this.getBbHeight() / 2f, toPos.getZ() + 0.5);
                }
            }

            this.removePearl(pearl);
            pearl.discard();
            event = null;
        } else {
            super.teleportTo(pX, pY, pZ);
            Supplementaries.error();
        }
    }

    @NotNull
    private static BlockState getLandingState(BlockState state, BlockPos pos, Direction direction, Level level) {
        if(state.hasProperty(DispenserBlock.FACING)) {
            state = state.setValue(DispenserBlock.FACING, direction);
        }
        return Block.updateFromNeighbourShapes(state, level, pos);
    }

    private static boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof CannonBlockTile || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof TrappedPresentBlockTile;
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
        Level level = source.level();
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
