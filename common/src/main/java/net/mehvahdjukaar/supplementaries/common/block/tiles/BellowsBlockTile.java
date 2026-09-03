package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.SableCompat;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

//TODO: this is a mess
public class BellowsBlockTile extends BlockEntity {

    private static final float MAX_COMPRESSION = 2 / 16f;//0.09375f;

    private float height = 0;
    private float prevHeight = 0;
    private int manualPress = 0;
    private long startTime = 0;
    private boolean isPressed = false;

    //for sounds
    private boolean lastBlowing = false;

    public BellowsBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BELLOWS_TILE.get(), pos, state);
    }

    //TODO: optimize this (also for flywheel)
    public static void tick(Level level, BlockPos pos, BlockState state, BellowsBlockTile tile) {
        int power = state.getValue(BellowsBlock.POWER);
        tile.prevHeight = tile.height;

        if (power != 0 && !(tile.startTime == 0 && tile.height != 0)) {
            long time = level.getGameTime();
            if (tile.startTime == 0) {
                tile.startTime = time;
            }

            float period = tile.getPeriodForPower(power);

            //slope of animation. for particles and pushing entities
            float arg = (float) Math.PI * 2 * (((time - tile.startTime) / period) % 1);
            float sin = Mth.sin(arg);
            float cos = Mth.cos(arg);

            float half = MAX_COMPRESSION / 2f;
            tile.height = half * cos - half;

            tile.pushAir(level, pos, state, power, time, period, sin);

            //sound
            boolean blowing = Mth.sin(arg - 0.8f) > 0;
            if (tile.lastBlowing != blowing) {
                level.playSound(null, pos,
                        blowing ? ModSounds.BELLOWS_BLOW.get() : ModSounds.BELLOWS_RETRACT.get(),
                        SoundSource.BLOCKS, 0.1f,
                        MthUtils.nextWeighted(level.random, 0.1f) + 0.85f + 0.6f * power / 15f);
            }

            tile.lastBlowing = blowing;

        } else if (tile.isPressed) {
            float minH = -MAX_COMPRESSION;
            tile.height = Math.max(tile.height - 0.01f, minH);

            if (tile.height > minH) {
                long time = level.getGameTime();
                //when operated by a mob it behaves like a constant with 7 power
                int p = 7;
                float period = tile.getPeriodForPower(p);

                tile.pushAir(level, pos, state, p, time, period, 0.8f);
            }
        }
        //resets counter when powered off
        else {
            tile.startTime = 0;
            if (tile.height < 0) {
                tile.height = Math.min(tile.height + 0.01f, 0);
            }
        }
        if (tile.prevHeight != 0 && tile.height != 0) {
            tile.moveCollidedEntities(level);
        }
        if (tile.manualPress > 0) {
            tile.manualPress--;
            tile.isPressed = true;
        } else tile.isPressed = false;
    }

    public float getHeight(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevHeight, this.height);
    }

    public void setManualPress() {
        manualPress = 10;
        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    //TODO: rewrite some of this

    private AABB getProgressDeltaAabb(Direction dir) {
        var bb = new AABB(BlockPos.ZERO);
        float max = Math.max(height, prevHeight);
        float min = Math.min(height, prevHeight);
        return (switch (dir) {
            case UP -> bb.setMaxY(1 + max).setMinY(1 + min);
            case DOWN -> bb.setMaxY(max).setMinY(min);
            case NORTH -> bb.setMaxZ(max).setMinZ(min);
            case SOUTH -> bb.setMaxZ(1 + max).setMinZ(1 + min);
            case EAST -> bb.setMaxX(1 + max).setMinX(1 + min);
            case WEST -> bb.setMaxX(max).setMinX(min);
        }).move(worldPosition);
    }

    //TODO: make this act on player on client side
    private void moveCollidedEntities(Level level) {
        Direction dir = this.getDirection().getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        for (int j = 0; j < 2; j++) {
            AABB progressDelta = this.getProgressDeltaAabb(dir);
            List<Entity> list = level.getEntities(null, progressDelta);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                        double f = 0.00;
                        entity.move(MoverType.SHULKER_BOX, new Vec3(
                                (progressDelta.getXsize() + f) * dir.getStepX(),
                                (progressDelta.getYsize() + f) * dir.getStepY(),
                                (progressDelta.getZsize() + f) * dir.getStepZ()));
                        entity.setOnGround(true);
                    }
                }
            }
            dir = dir.getOpposite();
        }
    }

    private void pushEntities(Direction facing, float period, float range, Level level) {

        double baseVelocity = CommonConfigs.Redstone.BELLOWS_BASE_VEL_SCALING.get() / period; // Affects acceleration
        double maxVelocity = CommonConfigs.Redstone.BELLOWS_MAX_VEL.get(); // Affects max speed
        if (facing == Direction.UP) maxVelocity *= 0.5D;

        AABB facingBox = AABB.encapsulatingFullBlocks(worldPosition, worldPosition.relative(facing, (int) range));

        for (Entity entity : level.getEntitiesOfClass(Entity.class, facingBox)) {

            //an entity on a ship has its coordinates in that ship's frame, not ours
            SableCompat.SubLevelTransform transform = SableCompat.subLevelsBetween(this, entity);

            double gap = gapInFront(transform.entityBoxToBlockSpace(entity.getBoundingBox()), facing);
            //behind us or already touching the nozzle
            if (gap < 0) continue;
            if (!this.inLineOfSight(gap, facing, level)) continue;

            double velocity = baseVelocity * Math.max(0, (range - gap) / range);
            Vec3 blowDir = transform.directionToEntitySpace(Vec3.atLowerCornerOf(facing.getNormal()));

            if (Math.abs(entity.getDeltaMovement().dot(blowDir)) < maxVelocity) {
                entity.push(blowDir.x * velocity, blowDir.y * velocity, blowDir.z * velocity);
                entity.hurtMarked = true;
            }
        }
    }

    private double gapInFront(AABB box, Direction facing) {
        return switch (facing) {
            case NORTH -> worldPosition.getZ() - box.maxZ;
            case SOUTH -> box.minZ - (worldPosition.getZ() + 1d);
            case WEST -> worldPosition.getX() - box.maxX;
            case EAST -> box.minX - (worldPosition.getX() + 1d);
            case DOWN -> worldPosition.getY() - box.maxY;
            case UP -> box.minY - (worldPosition.getY() + 1d);
        };
    }

    private void blowParticles(float air, Direction facing, Level level, boolean waterInFront) {
        if (level.random.nextFloat() < air) {
            AirType type = AirType.BUBBLE;
            BlockPos facingPos = this.worldPosition.relative(facing);
            BlockPos frontPos = facingPos;
            boolean hasSponge = false;
            if (!waterInFront) {
                BlockState frontState = level.getBlockState(facingPos);
                if (frontState.getBlock() instanceof WetSpongeBlock) {
                    hasSponge = true;
                    frontPos = frontPos.relative(facing);
                }
                type = AirType.AIR;
            }
            if (!Block.canSupportCenter(level, frontPos, facing.getOpposite())) {
                BlockPos p = this.worldPosition;
                if (hasSponge) {
                    EnumSet<Direction> directions = EnumSet.allOf(Direction.class);
                    directions.remove(facing.getOpposite());
                    directions.remove(facing);
                    for (Direction d : directions) {
                        if (level.getBlockState(facingPos.relative(d)).is(ModRegistry.SOAP_BLOCK.get())) {
                            type = AirType.SOAP;
                            p = facingPos;
                            break;
                        }
                    }
                    if (type != AirType.SOAP) return;
                }
                this.spawnParticle(level, p, facing, type);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> void tickFurnaces(BlockPos frontPos, BlockState frontState, Level level, T tile) {
        if (tile != null) {
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) frontState.getTicker(level, tile.getType());
            if (ticker != null) {
                ticker.tick(level, frontPos, frontState, tile);
            }
        }
    }

    private void tickFurnaces(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.BELLOWS_TICKABLE_TAG)) {
            BlockEntity te = level.getBlockEntity(pos);
            this.tickFurnaces(pos, state, level, te);
        }
        //maybe lower chance
        else if (state.getBlock() instanceof ChangeOverTimeBlock && level instanceof ServerLevel serverLevel) {
            state.randomTick(serverLevel, pos, level.random);
        }
    }

    private void refreshFire(int n, Direction facing, BlockPos frontPos, Level level) {
        for (int i = 0; i < n; i++) {
            BlockState fb = level.getBlockState(frontPos);
            if (fb.getBlock() instanceof FireBlock) {
                int age = fb.getValue(FireBlock.AGE);
                if (age != 0) {
                    level.setBlock(frontPos, fb.setValue(FireBlock.AGE,
                            Mth.clamp(age - 7, 0, 15)), 4);
                }
            }
            frontPos = frontPos.relative(facing);
        }
    }

    private float getPeriodForPower(int power) {
        return ((float) CommonConfigs.Redstone.BELLOWS_PERIOD.get()) - (power - 1) * ((float) CommonConfigs.Redstone.BELLOWS_POWER_SCALING.get());
    }

    private void pushAir(Level level, BlockPos pos, BlockState state, int power, long time, float period, float airIntensity) {
        Direction facing = state.getValue(BellowsBlock.FACING);
        BlockPos frontPos = pos.relative(facing);
        //TODO: optimize and add campfire smoke
        FluidState fluid = level.getFluidState(frontPos);

        //client. particles
        if (level.isClientSide) {
            this.blowParticles(airIntensity, facing, level, fluid.getType().is(FluidTags.WATER));

        }
        //server
        else if (fluid.isEmpty()) {
            float range = CommonConfigs.Redstone.BELLOWS_RANGE.get();
            //push entities (only if pushing air)
            if (airIntensity > 0) {
                this.pushEntities(facing, period, range, level);
            }

            //speeds up furnaces
            if (time % (10 - (power / 2)) == 0) {
                this.tickFurnaces(frontPos, level);
            }

            //refreshVisuals fire blocks
            //refreshTextures more frequently block closed to it
            //fire updates (previous random tick) at a minimum of 30 ticks
            int n = 0;
            for (int a = 0; a <= range; a++) {
                if (time % (15L * (a + 1)) != 0) {
                    n = a;
                    break;
                }
            }
            //only first 4 block will ultimately be kept active. this could change with random ticks if unlucky
            this.refreshFire(n, facing, frontPos, level);
        }
    }

    public boolean inLineOfSight(double gap, Direction facing, Level level) {
        for (int i = 1; i <= Mth.floor(gap); i++) {
            if (Block.canSupportCenter(level, this.worldPosition.relative(facing, i), facing.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    protected void spawnParticle(Level world, BlockPos pos, Direction dir, AirType airType) {
        if (airType == AirType.SOAP) {
            for (int m = 0; m < (1 + world.random.nextInt(3)); m++) {
                ParticleUtil.spawnParticleOnFace(world, pos, dir, ModParticles.SUDS_PARTICLE.get(), 0.3f, 0.5f, true);
            }

        } else {
            double xo = dir.getStepX();
            double yo = dir.getStepY();
            double zo = dir.getStepZ();
            double x = xo * 0.5 + pos.getX() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;
            double y = yo * 0.5 + pos.getY() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;
            double z = zo * 0.5 + pos.getZ() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;

            double vel = 0.125F + world.random.nextFloat() * 0.2F;

            double velX = xo * vel;
            double velY = yo * vel;
            double velZ = zo * vel;

            if (airType == AirType.BUBBLE) {
                world.addParticle(ParticleTypes.BUBBLE, x, y, z, velX * 0.8, velY * 0.8, velZ * 0.8);
            } else {
                world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
            }
        }

    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BellowsBlock.FACING);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.startTime = tag.getLong("Offset");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Offset", this.startTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public void onSteppedOn(Entity entityIn) {
        if (this.isPressed) return;
        double b = entityIn.getBoundingBox().getSize();
        if (b > 0.8 && this.getBlockState().getValue(BellowsBlock.FACING).getAxis() != Direction.Axis.Y) {
            this.isPressed = true;
        }
    }

    protected enum AirType {
        AIR, BUBBLE, SOAP
    }
}