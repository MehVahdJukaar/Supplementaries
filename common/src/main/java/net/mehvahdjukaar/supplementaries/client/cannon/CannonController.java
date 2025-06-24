package net.mehvahdjukaar.supplementaries.client.cannon;

import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


public class CannonController {

    protected static boolean isOnCannonBoat = false;
    protected static CannonAccess access;

    private static CameraType lastCameraType;
    protected static HitResult hit;

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
        Minecraft mc = Minecraft.getInstance();
        if (access == null) {
            access = cannonAccess;
            shootingMode = cannonAccess.getInternalCannon().getTrajectoryData().drag() != 0 ? ShootingMode.DOWN : ShootingMode.STRAIGHT;
            lastCameraType = mc.options.getCameraType();
        } //if not it means we entered from manouver mode gui
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        MutableComponent message = Component.translatable("message.supplementaries.cannon_maneuver",
                mc.options.keyShift.getTranslatedKeyMessage(),
                mc.options.keyAttack.getTranslatedKeyMessage());
        mc.gui.setOverlayMessage(message, false);
        mc.getNarrator().sayNow(message);

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

        if (!isActive()) return false;
        Vec3 centerCannonPos = access.getCannonGlobalPosition(partialTick);

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
        CannonBlockTile cannonTile = access.getInternalCannon();
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

            float wantedCannonYaw = Mth.PI + (float) Mth.atan2(-targetVector.x, targetVector.z);

            var comp = CannonUtils.computeTrajectory(access, hit.getLocation(), shootingMode);

            trajectory = comp.getFirst();
            float wantedYaw = comp.getSecond();

            updateCannonRenderAngles(partialTick, wantedYaw);
        }

        return true;
    }

    private static void updateCannonRenderAngles(float partialTick, float wantedYaw) {
        if (trajectory != null) {
            float followSpeed = 1;
            CannonBlockTile cannon = access.getInternalCannon();
            //TODO: improve
            cannon.setPitch(access, Mth.rotLerp(followSpeed, cannon.getPitch(),
                    trajectory.pitch() * Mth.RAD_TO_DEG));
            // targetYawDeg = Mth.rotLerp(followSpeed, cannon.getYaw(0), targetYawDeg);
            cannon.setRenderYaw(access,
                    wantedYaw * Mth.RAD_TO_DEG +
                            access.getCannonGlobalYawOffset(partialTick));
        }
    }

    // true cancels the thing
    public static boolean onPlayerRotated(double yawAdd, double pitchAdd) {
        if (CannonController.isActive()) {
            float scale = 0.2f;
            yawIncrease += (float) (yawAdd * scale);
            pitchIncrease += (float) (pitchAdd * scale);
            if (yawAdd != 0 || pitchAdd != 0) needsToUpdateServer = true;

            if (isOnCannonBoat) {
                //make player face camera while maneuvering
                LocalPlayer player = Minecraft.getInstance().player;
                player.turn(Mth.wrapDegrees((lastCameraYaw + yawAdd) - player.yHeadRot),
                        Mth.wrapDegrees((lastCameraPitch + pitchAdd) - player.getXRot()));
                player.yHeadRotO = player.yHeadRot;
                player.xRotO = player.getXRot();
            }
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
            CannonBlockTile tile = access.getInternalCannon();
            byte newPower = (byte) (1 + Math.floorMod((int) (tile.getPowerLevel() - 1 + scrollDelta), 4));
            tile.setPowerLevel(newPower);
            needsToUpdateServer = true;
        }
    }


    public static void onPlayerAttack() {
        if (access != null && access.getInternalCannon().readyToFire()) {
            access.syncToServer(true, false);
        }
    }

    public static void onPlayerUse() {
        showsTrajectory = !showsTrajectory;
    }

    public static void onInputUpdate(Input input) {
        // resets input
        if (!isOnCannonBoat) {
            input.down = false;
            input.up = false;
            input.left = false;
            input.right = false;
            input.forwardImpulse = 0;
            input.leftImpulse = 0;
        }
        input.shiftKeyDown = false;
        input.jumping = false;
    }

    public static void onClientTick(Minecraft mc) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        isOnCannonBoat = player.getVehicle() instanceof CannonAccess;
        if (!isActive()) return;
        if (access.stillValid(player)) {
            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                access.syncToServer(false, false);
            }
        } else {
            stopControllingAndSync();
        }
    }

    //called by mixin. its cancellable. maybe switch all to this
    public static boolean onEarlyKeyPress(int key, int scanCode, int action, int modifiers) {
        if (!isActive()) return false;
        if (action != GLFW.GLFW_PRESS) return false;
        var options = Minecraft.getInstance().options;
        if (key == 256) {
            stopControllingAndSync();
            return true;
        } else if (options.keyInventory.matches(key, scanCode)) {
            onKeyInventory();
            return true;
        }
        if (options.keyJump.matches(key, scanCode)) {
            onKeyJump();
            return true;
        }
        if (options.keyShift.matches(key, scanCode)) {
            onKeyShift();
            return true;
        }
        return false;
    }

    public static boolean rendersXpBar() {
        return isActive() || isOnCannonBoat;
    }
}

