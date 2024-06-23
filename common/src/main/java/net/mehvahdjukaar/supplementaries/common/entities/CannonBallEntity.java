package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.misc.explosion.CannonBallExplosion;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundExplosionPacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CannonBallEntity extends ImprovedProjectileEntity {

    //for collisions we should ignored since they were handled by the other entity
    private final List<CannonBallEntity> justCollidedWith = new ArrayList<>();
    private int bounces = 0; //bounces

    public CannonBallEntity(LivingEntity thrower) {
        super(ModEntities.CANNONBALL.get(), thrower, thrower.level());
        this.maxAge = 30;
        this.blocksBuilding = true;
        this.setNoGravity(true);
    }

    public CannonBallEntity(EntityType<CannonBallEntity> type, Level level) {
        super(type, level);
        this.maxAge = 30;
        this.setNoGravity(true);

        this.blocksBuilding = true;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bounces", this.bounces);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.bounces = compound.getInt("bounces");
    }


    @Override
    protected Item getDefaultItem() {
        return ModRegistry.CANNONBALL.get().asItem();
    }

    //@Override
    public void tick1() {
        super.tick();
        justCollidedWith.clear();
    }

    @Override
    public void spawnTrailParticles() {
        var speed = this.getDeltaMovement();
        var normalSpeed = speed.normalize();
        // Calculate pitch and yaw in radians
        double pitch = Math.asin(normalSpeed.y);
        double yaw = Math.atan2(normalSpeed.x, normalSpeed.z);

        double dx = getX() - xo;
        double dy = getY() - yo;
        double dz = getZ() - zo;


        for (int k = 0; k < 2; k++) {
            if (random.nextFloat() < speed.length() * 0.35) {
                // random circular vector
                Vector3f offset = new Vector3f(0, (random.nextFloat() * this.getBbWidth() * 0.7f), 0);
                offset.rotateZ(level().random.nextFloat() * Mth.TWO_PI);

                // Apply rotations
                offset.rotateX((float) pitch);
                offset.rotateY((float) yaw);
                float j = random.nextFloat() * -0.5f;

                this.level().addParticle(ModParticles.WIND_STREAM.get(),
                        offset.x + j * dx,
                        offset.y + j * dy + this.getBbWidth() / 3,
                        offset.z + j * dz,
                        this.getId(), 0, 0
                );
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        var particleData = ClientConfigs.Items.CANNONBALL_3D.get() ?
                new BlockParticleOption(ParticleTypes.BLOCK, ModRegistry.CANNONBALL.get().defaultBlockState()) :
                new ItemParticleOption(ParticleTypes.ITEM, this.getItem());

        //TODO use block particles with config, spawn more and maybe use send particles insteaed
        if (id == 3) {
            float speed = 0.05f;
            for (int i = 0; i < 8; ++i) {
                double k = this.random.nextGaussian() * speed;
                double l = this.random.nextGaussian() * speed;
                double m = this.random.nextGaussian() * speed;
                this.level().addParticle(particleData,
                        this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5),
                        k, l, m);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide && maybeBounce(result)) {
            return;
        }

        if (!level().isClientSide) {
            float radius = 1.1f;

            Vec3 movement = this.getDeltaMovement();
            double vel = Math.abs(movement.length());

            // this derives from kinetic energy calculation
            float scaling = 30;
            float maxAmount = (float) (vel * vel * scaling);

            Vec3 loc = result.getLocation();

            BlockPos pos = result.getBlockPos();
            CannonBallExplosion exp = new CannonBallExplosion(this.level(), this,
                    loc.x(), loc.y(), loc.z(), pos, maxAmount, radius);
            exp.explode();
            exp.finalizeExplosion(true);


            float exploded = exp.getExploded();

            if (exploded != 0) {
                double speedUsed = exploded / maxAmount;
                this.setDeltaMovement(movement.normalize().scale(1 - speedUsed));
                Message message = ClientBoundExplosionPacket.cannonball(exp, this);

                ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(this.level(), pos, message);
            }

            if (this.getDeltaMovement().length() < 0.4 || exploded == 0 && !level().isClientSide) {
                this.playSound(ModSounds.CANNONBALL_BREAK.get(), 1.0F, 1.5F);
                this.level().broadcastEntityEvent(this, (byte) 3);
                this.discard();
            }
        }
    }

    private boolean maybeBounce(BlockHitResult hit) {
        if (bounces >= 3) return false;
        bounces++;

        Direction hitDirection = hit.getDirection();
        BlockPos pos = hit.getBlockPos();

        boolean shouldBounce;
        Vec3 velocity = this.getDeltaMovement();
        Vector3f surfaceNormal = hitDirection.step();

        Level level = level();
        BlockState hitBlock = level.getBlockState(pos);
        if (hitBlock.getBlock() instanceof SlimeBlock) {
            shouldBounce = true;
        } else {
            double dot = surfaceNormal.dot(velocity.toVector3f());

            double cosAngle = Math.abs(dot / velocity.length());

            float bounceCosAngle = Mth.cos(75 * Mth.DEG_TO_RAD);
            shouldBounce = cosAngle < bounceCosAngle;
        }
        if (shouldBounce || true) {
            Vec3 newVel = new Vec3(velocity.toVector3f().reflect(surfaceNormal));
            this.setDeltaMovement(newVel);
            this.hasImpulse = true;
            level.gameEvent(GameEvent.HIT_GROUND, this.position(), GameEvent.Context.of(this, hitBlock));
            this.addLandingEffects(hit);

            return true;
        }
        return false;

    }

    private void addLandingEffects(BlockHitResult hit) {
        this.playSound(ModSounds.CANNONBALL_BOUNCE.get(), 1 * 2, 1);

        BlockPos pos = hit.getBlockPos();
        BlockState state = level().getBlockState(pos);

        double speed = this.getDeltaMovement().lengthSqr();
        double minSpeed = 0.2f;
        if (!this.level().isClientSide && !state.isAir() && speed > minSpeed) {
            double x = hit.getLocation().x;
            double y = hit.getLocation().y;
            double z = hit.getLocation().z;

            int count = Math.min(10, (int) (speed * 4) + 1);

            BlockParticleOption blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, state);
            SuppPlatformStuff.setParticlePos(blockParticleOption, pos);
            ((ServerLevel) this.level()).sendParticles(blockParticleOption,
                    x, y, z, count, 0.0, 0.0, 0.0, 0.15);

        }

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();

        // elastic collision with some loss
        float lossFactor = target instanceof LivingEntity le ? 0.1f : 0.8f;
        float cannonballDensity = 8f; //denser than other entities
        float m2 = (float) target.getBoundingBox().getSize() * (target instanceof CannonBallEntity ? cannonballDensity : 0);
        float m1 = (float) this.getBoundingBox().getSize() * cannonballDensity;
        Vector3f v2i = target.getDeltaMovement().toVector3f();
        Vector3f v1i = this.getDeltaMovement().toVector3f();

        double initialKineticEnergy = 0.5f * m1 * v1i.lengthSquared() + 0.5f * m2 * v2i.lengthSquared();

        // smaller means more losses
        float elasticity = 1;
        if (target instanceof LivingEntity le) {
            double lostEnergy = initialKineticEnergy * (1 - lossFactor);
            float dmgMult = 3; //TODO: config
            if (le.hurt(ModDamageSources.cannonBallExplosion(this, this.getOwner()), (float) lostEnergy * dmgMult)) {
                elasticity = Mth.sqrt(1 - lossFactor);
            }
        }

        Vector3f v1f = new Vector3f();
        Vector3f v2f = new Vector3f();

        // Calculate final velocities in each dimension. has conservation of momentum and kinetic energy
        for (int i = 0; i < 3; i++) {
            float c1 = (v1i.get(i) * (m1 - m2) + elasticity * 2 * m2 * v2i.get(i)) / (m1 + m2);
            float c2 = (v2i.get(i) * (m2 - m1) + elasticity * 2 * m1 * v1i.get(i)) / (m1 + m2);
            v1f.setComponent(i, c1);
            v2f.setComponent(i, c2);
        }

        // Apply new velocities
        this.setDeltaMovement(new Vec3(v1f));
        target.setDeltaMovement(new Vec3(v2f));

        // Calculate final momentum
        Vector3f finalMomentum = v1f.mul(m1, new Vector3f()).add(v2f.mul(m2, new Vector3f()));


        // Calculate final kinetic energy
        double finalKineticEnergy = 0.5f * m1 * v1f.lengthSquared() + 0.5f * m2 * v2f.lengthSquared();

        if (target instanceof CannonBallEntity c) {
            c.justCollidedWith.add(this);
            this.playSound(ModSounds.CANNONBALL_BOUNCE.get(), 1 * 2, 1);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof CannonBallEntity c) {
            return !c.justCollidedWith.contains(this);
        }
        return super.canHitEntity(target);
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        //solid! needed so collisions wont be called multiple times
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean collidesWithBlocks() {
        return true;
    }


    //mix of projectile + arrow code to do what both do+  fix some issues
    @SuppressWarnings("ConstantConditions")
    @Override
    public void tick() {
        // Projectile tick stuff
        if (!this.hasBeenShot) {
            this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
            this.hasBeenShot = true;
        }


        this.baseTick();

        // end of projectile tick stuff

        // some move() stuff
        this.wasOnFire = this.isOnFire();
        // end of move() stuff

        // AbstractArrow + ThrowableProjectile stuff

        //fixed vanilla arrow code. You're welcome

        BlockPos blockpos = this.blockPosition();
        Level level = this.level();
        BlockState blockstate = level.getBlockState(blockpos);

        //sets on ground
        if (!blockstate.isAir()) {
            VoxelShape voxelshape = blockstate.getCollisionShape(level, blockpos, CollisionContext.of(this));
            if (!voxelshape.isEmpty()) {
                Vec3 centerPos = this.getEyePosition();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(centerPos)) {
                        this.isInBlock = true;
                        break;
                    }
                }
            }
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW)) {
            this.clearFire();
        }

        if (this.isInBlock && !noPhysics) {
            this.inBlockTime++;
            return;
        }

        this.inBlockTime = 0;

        this.updateRotation();

        Vec3 movement = this.getDeltaMovement();
        Vec3 pos = this.getEyePosition();
        boolean client = level.isClientSide;

        Vec3 newPos = pos.add(movement);

        //this just calculate the hit pos. Does NOT calculate our actual new position
        HitResult hitResult = level.clip(new ClipContext(pos, newPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        // actually moves
        if (this.collidesWithBlocks()) {
            //gets the actual new pos
            Vec3 newMovement = this.collide(movement);

            //this is so it doesn't get pushed in diff direction and keeps ongoing straight
            double xs = newMovement.x / movement.x;
            double ys = newMovement.y / movement.y;
            double zs = newMovement.z / movement.z;
            double s = Math.min(xs, Math.min(ys, zs));
            Vec3 collidedPos = pos.add(movement.x * s, movement.y * s, movement.z * s);

            //if raytrace didn't find a hit and pos is different it means we hit something that not directly infront of us.. this is ugly
            Vec3 diff = newMovement.subtract(movement);

            //need to set here so BB is correct for collision
            newPos = collidedPos;
            this.setPos(newPos.x, newPos.y - this.getEyeHeight(), newPos.z);

            if (hitResult.getType() == HitResult.Type.MISS) {
                if (diff.lengthSqr() > (0.01 * 0.01)) {
                    //hit face must be the direction in which we didn't move the most
                    Direction hitFace = Direction.getNearest(diff.x, diff.y, diff.z);
                    //super hacky
                    hitResult = findCornerHitResult(collidedPos, hitFace, 2 / 16f);
                }
            }
        }
        //this is actually eye pos
        else this.setPos(newPos.x, newPos.y - this.getEyeHeight(), newPos.z);

        // update movement and particles
        float deceleration = this.isInWater() ? this.getWaterInertia() : this.getInertia();
        if (client) {
            this.spawnTrailParticles();
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(deceleration));
        if (!this.isNoGravity() && !noPhysics) {
            this.setDeltaMovement(this.getDeltaMovement().subtract(0, this.getGravity(), 0));
        }

        this.checkInsideBlocks();

        if (this.hasReachedEndOfLife() && !isRemoved()) {
            this.reachedEndOfLife();
        }

        if (this.isRemoved()) return;

        //try hit entity
        EntityHitResult entityHitResult = this.findHitEntity(pos, newPos);
        if (entityHitResult != null) {
            hitResult = entityHitResult;
        }

        if (hitResult.getType() == HitResult.Type.MISS) return;

        boolean portalHit = false;
        if (hitResult instanceof EntityHitResult ei) {
            Entity hitEntity = ei.getEntity();
            if (hitEntity == this.getOwner()) {
                if (!canHarmOwner()) {
                    hitResult = null;
                }
            } else if (hitEntity instanceof Player p1 && this.getOwner() instanceof Player p2 && !p2.canHarmPlayer(p1)) {
                hitResult = null;
            }
        } else if (hitResult instanceof BlockHitResult bi) {
            //portals. done here and not in onBlockHit to prevent any further calls
            BlockPos hitPos = bi.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);

            if (hitState.is(Blocks.NETHER_PORTAL)) {
                this.handleInsidePortal(hitPos);
                portalHit = true;
            } else if (hitState.is(Blocks.END_GATEWAY)) {
                if (level.getBlockEntity(hitPos) instanceof TheEndGatewayBlockEntity tile && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                    TheEndGatewayBlockEntity.teleportEntity(level, hitPos, hitState, this, tile);
                }
                portalHit = true;
            }
        }

        if (!portalHit && hitResult != null && hitResult.getType() != HitResult.Type.MISS && !noPhysics &&
                !ForgeHelper.onProjectileImpact(this, hitResult)) {
            this.onHit(hitResult);
            this.hasImpulse = true; //idk what this does
        }
    }

    private BlockHitResult findCornerHitResult(Vec3 newPos, Direction dir, float margin) {
        float halfWidth = this.getBbWidth() * 0.5f;
        Vec3 step = new Vec3(dir.getOpposite().step());
        Vec3 center = newPos.add(step.scale(halfWidth));
        Set<Vec3> vertices = new HashSet<>();


        AABB orBB = this.makeBoundingBox();
        float f = 0.9f;
        AABB aabb = orBB
                .move(step.scale(halfWidth))
                .deflate(step.x * halfWidth * 2 * 0.8f,
                        step.y * halfWidth * 2 * 0.8f,
                        step.z * halfWidth * 2 * 0.8f);
        Set<BlockPos> collided = new HashSet<>();
        for(var v : BlockPos.betweenClosed(BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ),
                BlockPos.containing(aabb.maxX, aabb.maxY, aabb.maxZ) )){
            collided.add(v.immutable());
        }
        for (var p : collided) {
            BlockState state = level().getBlockState(p);
            VoxelShape collisionShape = state.getCollisionShape(level(), p);
            for (var box : collisionShape.move(p.getX(), p.getY(), p.getZ()).toAabbs()) {
                if (box.intersects(aabb)) {
                    return new BlockHitResult(newPos, dir, p, false);
                }
            }
        }


        if (dir.getAxis() == Direction.Axis.X) {
            vertices.add(new Vec3(-0, -halfWidth, -halfWidth));
            vertices.add(new Vec3(0, -halfWidth, halfWidth));
            vertices.add(new Vec3(0, halfWidth, halfWidth));
            vertices.add(new Vec3(0, halfWidth, -halfWidth));
        } else if (dir.getAxis() == Direction.Axis.Y) {
            vertices.add(new Vec3(-halfWidth, 0, -halfWidth));
            vertices.add(new Vec3(-halfWidth, 0, halfWidth));
            vertices.add(new Vec3(halfWidth, 0, halfWidth));
            vertices.add(new Vec3(halfWidth, 0, -halfWidth));
        } else if (dir.getAxis() == Direction.Axis.Z) {
            vertices.add(new Vec3(-halfWidth, -halfWidth, 0));
            vertices.add(new Vec3(-halfWidth, halfWidth, 0));
            vertices.add(new Vec3(halfWidth, halfWidth, 0));
            vertices.add(new Vec3(halfWidth, -halfWidth, 0));
        }

        Level level = this.level();
        for (var v : vertices) {
            Vec3 corner = center.add(v);
            Vec3 projectCorner = corner.add(step.scale(margin));
            BlockPos pp = BlockPos.containing(projectCorner);
            HitResult blockHitResult = level.clip(new ClipContext(corner, projectCorner,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (blockHitResult.getType() != HitResult.Type.MISS && blockHitResult instanceof BlockHitResult bi && bi.getDirection() == dir) {
                return bi;
            }
        }
        //if all that fails (somehow) we just get a random one in front of the hit face
        AABB frontBox = this.getBoundingBox().move(step.scale(halfWidth * 2)); //box in front of hit face
        Set<BlockPos> list = new HashSet<>(BlockPos.betweenClosedStream(frontBox).toList());
        // bullshit code for fences
        list.add(BlockPos.containing(newPos.add(step.scale(margin))).below());
        for (var p : list) {
            BlockState state = level.getBlockState(p);
            if (state.getCollisionShape(level, p).isEmpty()) continue;
            return new BlockHitResult(newPos, dir, p, false);
        }


        //error
        return new BlockHitResult(newPos, dir, BlockPos.containing(newPos.add(step.scale(margin))), true);
    }


}
