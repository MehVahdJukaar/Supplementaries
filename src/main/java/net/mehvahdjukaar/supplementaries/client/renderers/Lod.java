package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Lod {
    private final double dist;

    public Lod(double distance){
        this.dist = distance;
    }

    public Lod(TileEntityRendererDispatcher renderer, BlockPos pos){
        this(renderer.camera.getPosition(),pos);
    }
    public Lod(Vector3d cameraPos, BlockPos pos){
        this(Vector3d.atCenterOf(pos).distanceToSqr(cameraPos));
    }

    public boolean isNear(){
        return this.dist<NEAR_DIST;
    }
    public boolean isMedium(){
        return this.dist<MEDIUM_DIST;
    }
    public boolean isFar(){
        return this.dist<FAR_DIST;
    }




    public static final int NEAR_DIST = 32*32;
    public static final int MEDIUM_DIST = 64*64;
    public static final int FAR_DIST = 96*96;



}
