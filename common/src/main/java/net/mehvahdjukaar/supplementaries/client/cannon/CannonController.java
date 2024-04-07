package net.mehvahdjukaar.supplementaries.client.cannon;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


public class CannonController {

    @Nullable
    protected static CannonBlockTile cannon;

    private static CameraType lastCameraType;
    protected static HitResult hit;
    private static boolean firstTick = true;

    // values controlled by player mouse movement. Not actually what camera uses
    private static float yawIncrease;
    private static float pitchIncrease;

    private static boolean needsToUpdateServer;
    private static boolean preferShootingDown = true;

    @Nullable
    protected static CannonTrajectory trajectory;

    // lerp camera
    private static Vec3 lastCameraPos;
    private static float lastZoomOut = 0;
    private static float lastCameraYaw = 0;
    private static float lastCameraPitch = 0;

    public static void activateCannonCamera(CannonBlockTile tile) {
        cannon = tile;
        firstTick = true;
        preferShootingDown = true;
        Minecraft mc = Minecraft.getInstance();
        lastCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        mc.gui.setOverlayMessage(Component.translatable("message.supplementaries.cannon_maneuver",
                mc.options.keyShift.getTranslatedKeyMessage(),
                mc.options.keyAttack.getTranslatedKeyMessage()), false);
    }

    public static void turnOff() {
        cannon = null;
        lastCameraYaw = 0;
        lastCameraPitch = 0;
        lastZoomOut = 0;
        lastCameraPos = null;
        if (lastCameraType != null) {
            Minecraft.getInstance().options.setCameraType(lastCameraType);
        }
    }

    public static boolean isActive() {
        return cannon != null;
    }

    public static boolean setupCamera(Camera camera, BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse, float partialTick) {

        if (isActive()) {
            Vec3 centerCannonPos = cannon.getBlockPos().getCenter();

            if (lastCameraPos == null) {
                lastCameraPos = camera.getPosition();
                lastCameraYaw = camera.getYRot();
                lastCameraPitch = camera.getXRot();
            }

            // lerp camera
            Vec3 targetCameraPos = centerCannonPos.add(0, 2, 0);
            float targetYRot = camera.getYRot() + yawIncrease;
            float targetXRot = Mth.clamp(camera.getXRot() + pitchIncrease, -90, 90);

            camera.setPosition(targetCameraPos);
            camera.setRotation(targetYRot, targetXRot);

            lastCameraPos = camera.getPosition();
            lastCameraYaw = camera.getYRot();
            lastCameraPitch = camera.getXRot();
            lastZoomOut = (float) camera.getMaxZoom(4);


            camera.move(-lastZoomOut, 0, -1);

            yawIncrease = 0;
            pitchIncrease = 0;


            // find hit result
            Vec3 lookDir2 = new Vec3(camera.getLookVector());
            float maxRange = 128;
            Vec3 actualCameraPos = camera.getPosition();
            Vec3 endPos = actualCameraPos.add(lookDir2.scale(maxRange));

            hit = level.clip(new ClipContext(actualCameraPos, endPos,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));

            Vec3 targetVector = hit.getLocation().subtract(cannon.getBlockPos().getCenter());
            //rotate so we can work in 2d
            Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);
            target = target.add(target.normalized().scale(0.05f)); //so we hopefully hit the block we are looking at

            // calculate the yaw of target. no clue why its like this
            float wantedCannonYaw = (Mth.PI + (float) Mth.atan2(-targetVector.x, targetVector.z));

            var restraints = getPitchAndYawRestrains(cannon.getBlockState());
            trajectory = CannonTrajectory.findBest(target,
                    cannon.getProjectileGravity(), cannon.getProjectileDrag(), cannon.getFirePower(), preferShootingDown,
                    restraints.minPitch, restraints.maxPitch);

            if (trajectory != null) {
                float followSpeed = 0.4f;
                //TODO: improve
                cannon.setPitch(Mth.rotLerp(followSpeed, cannon.getPitch(1), trajectory.pitch() * Mth.RAD_TO_DEG));
                float yaw = Mth.rotLerp(followSpeed, cannon.getYaw(1), wantedCannonYaw * Mth.RAD_TO_DEG);
                cannon.setYaw(Mth.clamp(yaw, restraints.minYaw, restraints.maxYaw));
            }

            return true;
        }
        return false;
    }

    private record Restraint(float minYaw, float maxYaw, float minPitch, float maxPitch) {
    }

    private static Restraint getPitchAndYawRestrains(BlockState state) {
        return switch (state.getValue(CannonBlock.FACING).getOpposite()) {
            case NORTH -> new Restraint(70, 290, -360, 360);
            case SOUTH -> new Restraint(-110, 110, -360, 360);
            case EAST -> new Restraint(-200, 20, -360, 360);
            case WEST -> new Restraint(-20, 200, -360, 360);
            case UP -> new Restraint(-360, 360, -200, 20);
            case DOWN -> new Restraint(-360, 360, -20, 200);
        };
    }


    public static void onPlayerRotated(double yawAdd, double pitchAdd) {
        float scale = 0.2f;
        yawIncrease += yawAdd * scale;
        pitchIncrease += pitchAdd * scale;
        if (yawAdd != 0 || pitchAdd != 0) needsToUpdateServer = true;
    }


    public static void onKeyPressed(int key, int action, int modifiers) {
        if (action != GLFW.GLFW_PRESS) return;
        if (Minecraft.getInstance().options.keyShift.matches(key, action)) {
            turnOff();
        }
        if (Minecraft.getInstance().options.keyJump.matches(key, action)) {
            preferShootingDown = !preferShootingDown;
            needsToUpdateServer = true;
        }
    }

    public static void onMouseScrolled(double scrollDelta){
        if (scrollDelta != 0) {
            cannon.changeFirePower((int) scrollDelta);
            needsToUpdateServer = true;
        }
    }


    public static void onPlayerAttack(boolean attack) {
        if (attack) {
            if (cannon != null && cannon.readyToFire()) {
                ModNetwork.CHANNEL.sendToServer(new ServerBoundSyncCannonPacket(
                        cannon.getYaw(1),
                        cannon.getPitch(1), cannon.getFirePower(), true, cannon.getBlockPos()));
            }
        }
    }

    public static void onInputUpdate(Input input) {
        if (firstTick) {
            // resets input
            firstTick = false;
            input.down = false;
            input.jumping = false;
            input.up = false;
            input.left = false;
            input.right = false;
            input.shiftKeyDown = false;
            input.forwardImpulse = 0;
            input.leftImpulse = 0;
        }
    }

    public static void onClientTick(Minecraft mc) {
        if (!isActive()) return;
        ClientLevel level = mc.level;
        BlockPos pos = cannon.getBlockPos();
        Player player = Minecraft.getInstance().player;
        float maxDist = 7;
        if (level.getBlockEntity(pos) == cannon && !cannon.isRemoved() &&
                pos.distToCenterSqr(player.position()) < maxDist * maxDist) {
            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                ModNetwork.CHANNEL.sendToServer(new ServerBoundSyncCannonPacket(
                        cannon.getYaw(0), cannon.getPitch(0), cannon.getFirePower(),
                        false, cannon.getBlockPos()));
            }
        } else turnOff();
    }

}

