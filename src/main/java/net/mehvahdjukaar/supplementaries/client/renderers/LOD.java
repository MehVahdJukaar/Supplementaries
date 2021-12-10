package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LOD {
    private final double distSq;

    private LOD(double distance) {
        this.distSq = distance;
    }

    public LOD(Camera camera, BlockPos pos) {
        this(camera.getPosition(), pos);
    }

    public LOD(Vec3 cameraPos, BlockPos pos) {
        this(isScoping() ? 1 : Vec3.atCenterOf(pos).distanceToSqr(cameraPos));
    }

    public static boolean isScoping(){
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        return localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping();
    }

    public boolean isVeryNear() {
        return this.distSq < VERY_NEAR_DIST;
    }

    public boolean isNear() {
        return this.distSq < NEAR_DIST;
    }

    public boolean isNearMed() {
        return this.distSq < NEAR_MED_DIST;
    }

    public boolean isMedium() {
        return this.distSq < MEDIUM_DIST;
    }

    public boolean isFar() {
        return this.distSq < FAR_DIST;
    }


    //all squared
    public static final int BUFFER = 2 * 2;
    public static final int VERY_NEAR_DIST = 16 * 16;
    public static final int NEAR_DIST = 32 * 32;
    public static final int NEAR_MED_DIST = 48 * 48;
    public static final int MEDIUM_DIST = 64 * 64;
    public static final int FAR_DIST = 96 * 96;

    public static boolean isOutOfFocus(Vec3 cameraPos, BlockPos pos, float blockYaw) {
        return isOutOfFocus(cameraPos, pos, blockYaw, 0, Direction.UP, 0);
    }

    public static boolean isOutOfFocus(Vec3 cameraPos, BlockPos pos, float blockYaw, float degMargin, Direction dir, float offset) {
        float relAngle = getRelativeAngle(cameraPos, pos, dir, offset);
        return isOutOfFocus(relAngle, blockYaw, degMargin);
    }

    public static boolean isOutOfFocus(float relativeAngle, float blockYaw, float degMargin) {
        return (Mth.degreesDifference(relativeAngle, blockYaw - 90) > -degMargin);
    }

    public static float getRelativeAngle(Vec3 cameraPos, BlockPos pos) {
        return getRelativeAngle(cameraPos, pos, Direction.UP, 0);
    }

    public static float getRelativeAngle(Vec3 cameraPos, BlockPos pos, Direction dir, float offset) {
        return (float) (Mth.atan2(
                offset * dir.getStepX() + cameraPos.x - (pos.getX() + 0.5f),
                offset * dir.getStepZ() + cameraPos.z - (pos.getZ() + 0.5f)) * 180 / Math.PI);
    }

}
