package net.mehvahdjukaar.supplementaries.common.entities;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CannonFlight {
    private final Player player;
    private final IntSet rammedEntities = new IntOpenHashSet();

    private int ticksLeft = 0;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 prevVelocity = Vec3.ZERO;
    private Vec3 lastPos = Vec3.ZERO;

    public CannonFlight(Player player) {
        this.player = player;
    }

    public boolean isFlying() {
        return ticksLeft > 0;
    }

    public void start(Vec3 launchVelocity, int maxTicks) {
        player.setDeltaMovement(launchVelocity);
        player.setDiscardFriction(true);
        player.fallDistance = 0;

        this.ticksLeft = maxTicks;
        this.velocity = launchVelocity;
        this.prevVelocity = launchVelocity;
        this.lastPos = player.position();
        rammedEntities.clear();
    }

    public void stop() {
        this.ticksLeft = 0;
        player.setDiscardFriction(false);
    }

    public Vec3 getVelocity(float partialTick) {
        return prevVelocity.lerp(velocity, partialTick);
    }


    public void applyDrag() {
        player.setDeltaMovement(player.getDeltaMovement().scale(BallisticData.PLAYER.drag()));
    }

    public void tick() {
        Vec3 moved = this.trackVelocity();
        if (!player.level().isClientSide) this.ramEntitiesInPath(moved);

        boolean landed = player.onGround() || player.isInWater() || player.isPassenger();
        if (landed || --ticksLeft <= 0) stop();
    }

    // measured from position since remote players and server players dont have a usable delta movement..
    private Vec3 trackVelocity() {
        Vec3 pos = player.position();
        Vec3 moved = pos.subtract(lastPos);
        lastPos = pos;
        prevVelocity = velocity;
        velocity = player.isLocalPlayer() ? moved : velocity.lerp(moved, 0.5);
        return moved;
    }

    private void ramEntitiesInPath(Vec3 moved) {
        double speed = moved.length();
        if (speed < 0.5) return;

        AABB sweptBox = player.getBoundingBox().expandTowards(moved.reverse()).inflate(0.2);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, sweptBox,
                e -> e != player && e.isAlive() && !e.isSpectator() && !e.isPassengerOfSameVehicle(player))) {
            if (!rammedEntities.add(target.getId())) continue;
            if (target.hurt(player.damageSources().playerAttack(player), (float) (speed * 4))) {
                target.knockback(speed * 0.6, -moved.x, -moved.z);
            }
        }
    }

    public void playLandingSound(float fallDistance) {
        if (player.level().isClientSide) return;
        var sounds = player.getFallSounds();
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                fallDistance > 4 ? sounds.big() : sounds.small(), player.getSoundSource(), 1, 0.8f);
    }
}
