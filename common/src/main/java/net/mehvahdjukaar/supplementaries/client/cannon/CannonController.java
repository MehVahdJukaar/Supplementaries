package net.mehvahdjukaar.supplementaries.client.cannon;

import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class CannonController {

    protected static CannonAccess access;

    private static CameraType lastCameraType;
    protected static HitResult hit;
    private static boolean firstTick = true;

    // values controlled by player mouse movement. Not actually what camera uses
    private static float yawIncrease;
    private static float pitchIncrease;

    private static boolean needsToUpdateServer;
    protected static ShootingMode shootingMode = ShootingMode.DOWN;

    @Nullable
    protected static CannonTrajectory trajectory;

    // lerp camera
    private static Vec3 lastCameraPos;
    private static float lastZoomOut = 0;
    private static float lastCameraYaw = 0;
    private static float lastCameraPitch = 0;

    protected static boolean showsTrajectory = true;

    public static void startControlling(CannonAccess cannonAccess) {
        firstTick = true;
        Minecraft mc = Minecraft.getInstance();

        if (access == null) {
            access = cannonAccess;
            shootingMode = cannonAccess.getCannon().getTrajectoryData().drag() != 0 ? ShootingMode.DOWN : ShootingMode.STRAIGHT;
            lastCameraType = mc.options.getCameraType();
        } //if not it means we entered from manouver mode gui
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        mc.gui.setOverlayMessage(Component.translatable("message.supplementaries.cannon_maneuver",
                mc.options.keyShift.getTranslatedKeyMessage(),
                mc.options.keyAttack.getTranslatedKeyMessage()), false);
    }

    // only works if we are already controlling
    private static void stopControllingAndSync() {
        if (access == null) return;
        access.syncToServer(false, true);
        stopControlling();
    }

    public static void stopControlling() {
        if (access == null) return;
        access = null;
        lastCameraYaw = 0;
        lastCameraPitch = 0;
        lastZoomOut = 0;
        lastCameraPos = null;
        if (lastCameraType != null) {
            Minecraft.getInstance().options.setCameraType(lastCameraType);
        }
    }

    public static boolean isActive() {
        return access != null;
    }

    public static boolean setupCamera(Camera camera, BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse, float partialTick) {

        if (isActive()) {
            Vec3 centerCannonPos = access.getCannonGlobalPosition();

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
            lastZoomOut = camera.getMaxZoom(4);

            float horizontalOffset = -1;

            camera.move(-lastZoomOut, 0, horizontalOffset);

            yawIncrease = 0;
            pitchIncrease = 0;

            //TODO: no perfect solution exist: add config
            CannonBlockTile cannonTile = access.getCannon();
            if (!cannonTile.isFiring()) {


                // find hit result
                Vec3 lookDir2 = new Vec3(camera.getLookVector());
                float maxRange = 128;
                Vec3 actualCameraPos = camera.getPosition().add(lookDir2.normalize());
                Vec3 endPos = actualCameraPos.add(lookDir2.scale(maxRange));

                hit = level.clip(new ClipContext(actualCameraPos, endPos,
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));

                Vec3 targetVector = hit.getLocation().subtract(centerCannonPos);
                //rotate so we can work in 2d
                Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);
                target = target.add(target.normalized().scale(0.05f)); //so we hopefully hit the block we are looking at

                // calculate the yaw of target. no clue why its like this
                float wantedCannonYaw = Mth.PI + (float) Mth.atan2(-targetVector.x, targetVector.z);

                var restraints = access.getPitchAndYawRestrains();
                var ballistic = cannonTile.getTrajectoryData();
                trajectory = CannonTrajectory.findBest(target,
                        ballistic.gravity(), ballistic.drag(),
                        cannonTile.getFirePower() * ballistic.initialSpeed(),
                        shootingMode,
                        restraints.minPitch(), restraints.maxPitch());

                setCannonAngles(partialTick, wantedCannonYaw * Mth.RAD_TO_DEG);
            }

            return true;
        }
        return false;
    }

    private static void setCannonAngles(float partialTick, float targetYawDeg) {
        if (trajectory != null) {
            float followSpeed = 1;
            CannonBlockTile cannon = access.getCannon();
            //TODO: improve
            cannon.setRestrainedPitch(access, Mth.rotLerp(followSpeed, cannon.getPitch(),
                    trajectory.pitch() * Mth.RAD_TO_DEG));
            // targetYawDeg = Mth.rotLerp(followSpeed, cannon.getYaw(0), targetYawDeg);
            cannon.setRenderYaw(access, targetYawDeg);
        }
    }

    // true cancels the thing
    public static boolean onPlayerRotated(double yawAdd, double pitchAdd) {
        if (CannonController.isActive()) {
            float scale = 0.2f;
            yawIncrease += (float) (yawAdd * scale);
            pitchIncrease += (float) (pitchAdd * scale);
            if (yawAdd != 0 || pitchAdd != 0) needsToUpdateServer = true;
            return true;
        }
        return false;
    }

    public static void onKeyJump() {
        if (trajectory != null && trajectory.gravity() != 0) {
            shootingMode = shootingMode.cycle();
            needsToUpdateServer = true;
        }
    }

    public static void onKeyInventory() {
        //Disabled, too buggy
        access.sendOpenGuiRequest();
    }

    public static void onKeyShift() {
        stopControllingAndSync();
    }

    public static void onMouseScrolled(double scrollDelta) {
        if (scrollDelta != 0) {
            access.getCannon().changeFirePower((int) scrollDelta);
            needsToUpdateServer = true;
        }
    }


    public static void onPlayerAttack() {
        if (access != null && access.getCannon().readyToFire()) {
            access.syncToServer(true, false);
        }
    }

    public static void onPlayerUse() {
        showsTrajectory = !showsTrajectory;
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
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (access.stillValid(player)) {
            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                access.syncToServer(false, false);
            }
        } else {
            stopControllingAndSync();
        }
    }

}

