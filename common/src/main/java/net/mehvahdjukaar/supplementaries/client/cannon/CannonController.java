package net.mehvahdjukaar.supplementaries.client.cannon;

import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory3D;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.mehvahdjukaar.supplementaries.integration.SableCompatClient;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


public class CannonController {

    @Nullable
    protected static BallisticTrajectory trajectory;
    protected static CannonBlockTile cannon;
    protected static HitResult hit;
    protected static ShootingMode shootingMode = ShootingMode.DOWN;
    protected static boolean showsTrajectory = true;

    private static CameraType lastCameraType;
    // values controlled by player mouse movement. Not actually what camera uses
    private static float yawIncrease;
    private static float pitchIncrease;
    private static boolean needsToUpdateServer;
    // lerp camera
    private static Vec3 lastCameraPos;
    private static float lastZoomOut = 0;
    private static float lastCameraYaw = 0;
    private static float lastCameraPitch = 0;
    private static boolean turnedLastTick = false;
    private static boolean startedInside = false;
    private static boolean wasInside = false;

    public static void startControlling(CannonBlockTile cannon) {
        Minecraft mc = Minecraft.getInstance();
        if (CannonController.cannon == null) {
            CannonController.cannon = cannon;
            shootingMode = cannon.getBallisticData().drag() != 0 ? ShootingMode.DOWN : ShootingMode.STRAIGHT;
            lastCameraType = mc.options.getCameraType();
        } //if not it means we entered from manoeuvre mode gui
        startedInside = cannon.isRider(mc.player);
        wasInside = startedInside;
        mc.options.setCameraType(startedInside ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_BACK);
        MutableComponent message = Component.translatable("message.supplementaries.cannon.maneuver",
                mc.options.keyShift.getTranslatedKeyMessage(),
                mc.options.keyAttack.getTranslatedKeyMessage());
        mc.gui.setOverlayMessage(message, false);
        mc.getNarrator().sayNow(message);

    }

    // only works if we are already controlling
    private static void stopControllingAndSync() {
        if (cannon == null) return;
        LocalPlayer player = Minecraft.getInstance().player;
        cannon.syncToServer(false, true, player);
        stopControlling();
    }

    public static void stopControlling() {
        if (cannon == null) return;
        cannon = null;
        lastCameraYaw = 0;
        lastCameraPitch = 0;
        lastZoomOut = 0;
        lastCameraPos = null;
        turnedLastTick = false;
        startedInside = false;
        wasInside = false;
        var options = Minecraft.getInstance().options;
        if (lastCameraType != null && !options.getCameraType().isFirstPerson()) {
            options.setCameraType(lastCameraType);
        }
    }

    public static boolean isActive() {
        return cannon != null;
    }

    public static boolean isInside() {
        LocalPlayer player = Minecraft.getInstance().player;
        return cannon != null && player != null && cannon.isRider(player);
    }

    public static boolean isFirstPersonAiming() {
        return isInside() && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static ShootingMode getShootingMode() {
        return isFirstPersonAiming() ? ShootingMode.STRAIGHT : shootingMode;
    }

    public static boolean hidesCannon(CannonBlockTile tile) {
        return cannon == tile && isFirstPersonAiming();
    }

    public static boolean setupCamera(Camera camera, BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse, float partialTick) {

        if (!isActive()) return false;

        if (lastCameraPos == null) {
            lastCameraPos = camera.getPosition();
            lastCameraYaw = camera.getYRot();
            lastCameraPitch = camera.getXRot();
        }

        // lerp camera
        float targetYRot = lastCameraYaw + yawIncrease;
        float targetXRot = Mth.clamp(lastCameraPitch + pitchIncrease, -90, 90);
        yawIncrease = 0;
        pitchIncrease = 0;

        boolean inside = isInside();
        var options = Minecraft.getInstance().options;

        if (inside && options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            options.setCameraType(CameraType.FIRST_PERSON);
        }
        boolean firstPerson = inside && options.getCameraType().isFirstPerson();

        if (firstPerson) {
            cannon.setWorldOrientation(EntityAngles.of(targetXRot, targetYRot).toQuaternion());
            cannon.snapToWantedRotationInstantly();
        }

        Vec3 centerCannonPos = SableCompatClient.projectOutOfSubLevel(cannon,
                firstPerson ? cannon.getMuzzlePosition(partialTick) : cannon.getGlobalPosition(partialTick), partialTick);

        Vec3 targetCameraPos = firstPerson ? centerCannonPos : centerCannonPos.add(0, 2, 0);

        camera.setPosition(targetCameraPos);
        camera.setRotation(targetYRot, targetXRot);

        lastCameraPos = camera.getPosition();
        lastCameraYaw = targetYRot;
        lastCameraPitch = targetXRot;
        lastZoomOut = camera.getMaxZoom(4);

        if (!firstPerson) {
            float horizontalOffset = -1;
            camera.move(-lastZoomOut, 0, horizontalOffset);
        }

        if (firstPerson) {
            trajectory = null;
            turnedLastTick = false;
            return true;
        }

        if (!cannon.isFiring()) {

            // find hit result
            Vec3 lookDir2 = new Vec3(camera.getLookVector());
            float maxRange = 128;
            Vec3 actualCameraPos = camera.getPosition().add(lookDir2.normalize());
            Vec3 endPos = actualCameraPos.add(lookDir2.scale(maxRange));

            hit = SableCompatClient.clipIncludingSubLevels(level, entity, actualCameraPos, endPos, partialTick);

            Vec3 target = SableCompatClient.projectIntoSubLevel(cannon, hit.getLocation(), partialTick);
            BallisticTrajectory3D comp = CannonUtils.computeTrajectory(cannon, target, shootingMode,
                    inside ? BallisticData.PLAYER : cannon.getBallisticData());

            if (comp != null) {
                trajectory = comp.trajectory();
                cannon.setRotationToMatchTrajectory(comp, partialTick);
                if (turnedLastTick) cannon.snapToWantedRotationInstantly();
                turnedLastTick = true;
                return true;
            }
        }
        turnedLastTick = false;
        return true;
    }

    // true cancels the thing
    public static boolean onPlayerRotated(double yawAdd, double pitchAdd) {
        if (CannonController.isActive()) {
            if (isFirstPersonAiming() && cannon.isFiring()) return true;
            float scale = 0.2f;
            yawIncrease += (float) (yawAdd * scale);
            pitchIncrease += (float) (pitchAdd * scale);
            if (yawAdd != 0 || pitchAdd != 0) needsToUpdateServer = true;

            if (cannon.shouldRotatePlayerFaceWhenManeuvering()) {
                LocalPlayer player = Minecraft.getInstance().player;
                float wantedYaw = (float) Mth.wrapDegrees(lastCameraYaw + yawAdd);
                float wantedPitch = (float) Mth.clamp(lastCameraPitch + pitchAdd, -90, 90);
                player.setYRot(wantedYaw);
                player.yRotO = wantedYaw;
                player.yBodyRot = wantedYaw;
                player.yBodyRotO = wantedYaw;
                player.yHeadRot = wantedYaw;
                player.yHeadRotO = wantedYaw;
                player.setXRot(wantedPitch);
                player.xRotO = wantedPitch;
            }
            return true;
        }
        return false;
    }

    private static void onKeyJump() {
        if (trajectory != null && trajectory.gravity() != 0) {
            shootingMode = shootingMode.cycle();
            needsToUpdateServer = true;
        }
    }

    private static void onKeyInventory() {
        //Disabled, too buggy
        cannon.sendOpenGuiRequest();
    }

    private static boolean onKeyShift() {
        if (isActive()) {
            stopControllingAndSync();
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        CannonBlockTile ridden = CannonBlockTile.riddenBy(player);
        if (ridden == null) return false;
        ridden.syncToServer(false, true, player);
        return true;
    }

    public static boolean onMouseScrolled(double scrollDelta) {
        if (!isActive()) return false;

        if (scrollDelta != 0) {
            byte newPower = (byte) (1 + Math.floorMod((int) (cannon.getPowerLevel() - 1 + scrollDelta), CannonBlockTile.MAX_POWER_LEVEL));
            cannon.setFirePower(newPower);
            needsToUpdateServer = true;
        }
        return true;
    }


    public static boolean onPlayerAttack() {
        if (!isActive()) return false;
        if (cannon != null && cannon.readyToFire()) {
            LocalPlayer player = Minecraft.getInstance().player;
            cannon.syncToServer(true, false, player);
        }
        return true;
    }

    public static boolean onPlayerUse() {
        if (!isActive()) return false;
        showsTrajectory = !showsTrajectory;
        return true;
    }

    public static void onInputUpdate(Input input) {
        // resets input
        if (cannon.impedePlayerMovementWhenManeuvering()) {
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
        if (!isActive()) return;
        boolean inside = isInside();
        if (wasInside && !inside) {
            stopControllingAndSync();
            return;
        }
        wasInside = inside;
        if (cannon.stillValid(player)) {
            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                cannon.syncToServer(false, false, player);
            }
        } else {
            stopControllingAndSync();
        }
    }

    //called by mixin. its cancellable. maybe switch all to this
    public static boolean onEarlyKeyPress(int key, int scanCode, int action, int modifiers) {
        if (action != GLFW.GLFW_PRESS) return false;
        var options = Minecraft.getInstance().options;

        if (options.keyShift.matches(key, scanCode)) return onKeyShift();
        if (!isActive()) return false;
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
        return false;
    }

    public static boolean cancelsXPBar() {
        return isActive() || Minecraft.getInstance().player.getVehicle() instanceof CannonBoatEntity;
    }

    public static boolean cancelsHotBar() {
        return isActive();
    }
}

