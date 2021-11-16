package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class VectorUtils {

    //vector relative to a new basis
    public static Vector3d changeBasisN(Vector3d newBasisYVector, Vector3d rot) {
        Vector3d y = newBasisYVector.normalize();
        Vector3d x = new Vector3d(y.y, y.z, y.x).normalize();
        Vector3d z = y.cross(x).normalize();
        return changeBasis(x, y, z, rot);
    }

    public static Vector3d changeBasis(Vector3d newX, Vector3d newY, Vector3d newZ, Vector3d rot) {
        return newX.scale(rot.x).add(newY.scale(rot.y)).add(newZ.scale(rot.z));
    }

    public static Vector3d getNormalFrom3DData(int direction) {
        return ItoD(Direction.from3DDataValue(direction).getNormal());
    }

    public static Vector3d ItoD(Vector3i v) {
        return new Vector3d(v.getX(), v.getY(), v.getZ());
    }

}
