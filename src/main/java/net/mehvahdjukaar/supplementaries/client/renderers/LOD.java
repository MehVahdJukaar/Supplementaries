package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LOD {
    private final double dist;

    public LOD(double distance){
        this.dist = distance;
    }

    public LOD(TileEntityRendererDispatcher renderer, BlockPos pos){
        this(renderer.camera.getPosition(),pos);
    }
    public LOD(Vector3d cameraPos, BlockPos pos){
        this(Vector3d.atCenterOf(pos).distanceToSqr(cameraPos));
    }

    public boolean isNear(){
        return this.dist<NEAR_DIST;
    }
    public boolean isNearMed(){
        return this.dist<NEAR_MED_DIST;
    }
    public boolean isMedium(){
        return this.dist<MEDIUM_DIST;
    }
    public boolean isFar(){
        return this.dist<FAR_DIST;
    }




    public static final int NEAR_DIST = 32*32;
    public static final int NEAR_MED_DIST = 48*48;
    public static final int MEDIUM_DIST = 64*64;
    public static final int FAR_DIST = 96*96;

    public static boolean isOutOfFocus(Vector3d cameraPos, BlockPos pos, float blockYaw){
        float relAngle = (float) (MathHelper.atan2(cameraPos.x-(pos.getX()+0.5f),cameraPos.z-(pos.getZ()+0.5f))*180/Math.PI);
        return(MathHelper.degreesDifference(relAngle,blockYaw-90)>-0);
    }
    public static boolean isOutOfFocus(Vector3d cameraPos, BlockPos pos, float blockYaw, Direction dir, float offset){
        float relAngle = (float) (MathHelper.atan2(
                offset*dir.getStepX()+cameraPos.x-(pos.getX()+0.5f),
                offset*dir.getStepZ()+cameraPos.z-(pos.getZ()+0.5f))*180/Math.PI);
        return(MathHelper.degreesDifference(relAngle,blockYaw-90)>-0);
    }


}
