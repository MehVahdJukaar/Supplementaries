package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.client.particles.CannonFireParticle;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestOpenCannonGuiMessage;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

//used to access a cannon position and rotation, be it in a block or an entity
public interface CannonAccess {

    CannonBlockTile getInternalCannon();

    TileOrEntityTarget makeNetworkTarget();

    void applyRecoil();

    boolean canManeuverFromGUI(Player player);

    void syncToServer(boolean fire, boolean removeOwner);

    Vec3 getCannonGlobalPosition(float partialTicks);

    float getCannonGlobalYawOffset(float partialTicks);

    Vec3 getCannonGlobalOffset();

    void sendOpenGuiRequest();

    void openCannonGui(ServerPlayer player);

    boolean stillValid(Player player);

    void updateClients();

    default Vec3 getCannonGlobalFacing(float partialTicks) {
        CannonBlockTile cannon = this.getInternalCannon();
        return Vec3.directionFromRotation(cannon.getPitch(partialTicks),
                cannon.getYaw(partialTicks) - this.getCannonGlobalYawOffset(partialTicks)).scale(-1);
    }

    Restraint getPitchAndYawRestrains();

    Vec3 getCannonGlobalVelocity();

    class Block implements CannonAccess {
        private final CannonBlockTile cannon;

        public Block(CannonBlockTile cannon) {
            this.cannon = cannon;
        }

        @Override
        public TileOrEntityTarget makeNetworkTarget() {
            return TileOrEntityTarget.of(this.cannon);
        }

        @Override
        public Vec3 getCannonRecoil() {
            return Vec3.ZERO; //no recoil for block cannons
        }

        @Override
        public void applyRecoil() {
        }

        @Override
        public Vec3 getCannonGlobalVelocity() {
            return Vec3.ZERO;
        }

        @Override
        public boolean canManeuverFromGUI(Player player) {
            return true; //if gui is open it means we can always maneuver
        }

        @Override
        public Vec3 getCannonGlobalPosition(float ticks) {
            return cannon.getBlockPos().getCenter();
        }

        @Override
        public Vec3 getCannonGlobalOffset() {
            return new Vec3(0.5, 0.5, 0.5);
        }

        @Override
        public float getCannonGlobalYawOffset(float partialTicks) {
            return 0;
        }

        @Override
        public CannonBlockTile getInternalCannon() {
            return this.cannon;
        }

        @Override
        public void updateClients() {
            var level = cannon.getLevel();
            level.sendBlockUpdated(cannon.getBlockPos(), cannon.getBlockState(), cannon.getBlockState(), 3);
        }

        @Override
        public void syncToServer(boolean fire, boolean removeOwner) {
            NetworkHelper.sendToServer(new ServerBoundSyncCannonPacket(
                    cannon.getYaw(), cannon.getPitch(), cannon.getPowerLevel(),
                    fire, removeOwner, TileOrEntityTarget.of(this.cannon)));
        }

        @Override
        public void sendOpenGuiRequest() {
            NetworkHelper.sendToServer(new ServerBoundRequestOpenCannonGuiMessage(cannon));
        }

        @Override
        public void openCannonGui(ServerPlayer player) {
            this.cannon.tryOpeningEditGui(player, this.cannon.getBlockPos(),
                    player.getMainHandItem(), Direction.UP);
        }

        @Override
        public boolean stillValid(Player player) {
            Level level = player.level();
            float maxDist = 7;
            return !cannon.isRemoved() && level.getBlockEntity(cannon.getBlockPos()) == cannon &&
                    cannon.getBlockPos().distToCenterSqr(player.position()) < maxDist * maxDist;
        }

        public Restraint getPitchAndYawRestrains() {
            BlockState state = cannon.getBlockState();
            return switch (state.getValue(CannonBlock.FACING).getOpposite()) {
                case NORTH -> new Restraint(70, 290, -180, 180);
                case SOUTH -> new Restraint(-110, 110, -180, 180);
                case EAST -> new Restraint(-200, 20, -180, 180);
                case WEST -> new Restraint(-20, 200, -180, 180);
                case UP -> new Restraint(-360, 360, -200, 20);
                case DOWN -> new Restraint(-360, 360, -20, 200);
            };
        }

    }

    static CannonAccess find(Level level, TileOrEntityTarget target) {
        var obj = target.getTarget(level);
        if (obj instanceof CannonBlockTile cannon) {
            return new Block(cannon);
        } else if (obj instanceof CannonAccess cannon) {
            return cannon;
        }
        return null;
    }


    static CannonAccess block(CannonBlockTile cannonBlockTile) {
        return new Block(cannonBlockTile);
    }

    record Restraint(float minYaw, float maxYaw, float minPitch, float maxPitch) {
    }


    default void playIgniteEffects() {
        Level level = this.getInternalCannon().getLevel();
        PoseStack poseStack = calculateGlobalCannonPose(this);
        Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1.752f, 1));

        Vec3 speed = this.getCannonGlobalVelocity();
        level.addParticle(ParticleTypes.CRIT,
                p.x, p.y, p.z, speed.x, speed.y, speed.z);

        Vec3 pos = this.getCannonGlobalPosition(1);
        level.playLocalSound(pos.x, pos.y, pos.z, ModSounds.CANNON_IGNITE.get(), SoundSource.BLOCKS, 0.6f,
                1.2f + level.getRandom().nextFloat() * 0.2f, false);
    }


    default void playFiringEffects() {
        PoseStack poseStack = calculateGlobalCannonPose(this);
        CannonBlockTile cannon = this.getInternalCannon();
        Level level = cannon.getLevel();
        float yaw = cannon.getYaw() - this.getCannonGlobalYawOffset(1);
        float pitch = cannon.getPitch();
        float power = cannon.getPowerLevel();
        Vec3 pos = this.getCannonGlobalPosition(1);
        Vec3 speed = this.getCannonGlobalVelocity();
        speed = speed.scale(0.3);
        var opt = new CannonFireParticle.Options(pitch, yaw, 1);
        speed = Vec3.ZERO;
        level.addParticle(opt, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
        RandomSource ran = level.random;

        CannonUtils.spawnDustRing(level, poseStack, speed);
        CannonUtils.spawnSmokeTrail(level, poseStack, ran, speed);

        // power from 1 to 4
        float soundPitch = 1.3f - power * 0.1f;
        float soundVolume = 2f + power * 0.6f;
        level.playLocalSound(pos.x, pos.y, pos.z, ModSounds.CANNON_FIRE.get(), SoundSource.BLOCKS,
                soundVolume, soundPitch, false);
    }

    Vec3 getCannonRecoil();


    private static PoseStack calculateGlobalCannonPose(CannonAccess access) {
        CannonBlockTile tile = access.getInternalCannon();
        float yaw = tile.getYaw() - access.getCannonGlobalYawOffset(1);
        float pitch = tile.getPitch();

        PoseStack poseStack = new PoseStack();
        var pos = access.getCannonGlobalPosition(1);
        poseStack.translate(pos.x, pos.y + 1 / 16f, pos.z);

        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.translate(0, 0, -1.4);
        return poseStack;
    }
}
