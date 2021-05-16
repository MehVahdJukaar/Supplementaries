package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class Const {

    public static final Quaternion Y180 = Vector3f.YP.rotationDegrees(180);
    public static final Quaternion Y90 = Vector3f.YP.rotationDegrees(90);
    public static final Quaternion Y45 = Vector3f.YP.rotationDegrees(45);
    public static final Quaternion YN45 = Vector3f.YP.rotationDegrees(-45);
    public static final Quaternion YN90 = Vector3f.YP.rotationDegrees(-90);
    public static final Quaternion YN180 = Vector3f.YP.rotationDegrees(-180);

    public static final Quaternion X180 = Vector3f.XP.rotationDegrees(180);
    public static final Quaternion X90 = Vector3f.XP.rotationDegrees(90);
    public static final Quaternion XN22 = Vector3f.XP.rotationDegrees(-22.5f);
    public static final Quaternion XN90 = Vector3f.XP.rotationDegrees(-90);
    public static final Quaternion XN180 = Vector3f.XP.rotationDegrees(-180);

    public static final Quaternion Z180 = Vector3f.ZP.rotationDegrees(180);
    public static final Quaternion Z135 = Vector3f.ZP.rotationDegrees(135);
    public static final Quaternion Z90 = Vector3f.ZP.rotationDegrees(90);
    public static final Quaternion ZN45 = Vector3f.ZP.rotationDegrees(-45);
    public static final Quaternion ZN90 = Vector3f.ZP.rotationDegrees(-90);
    public static final Quaternion ZN180 = Vector3f.ZP.rotationDegrees(-180);
}