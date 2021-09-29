package net.mehvahdjukaar.supplementaries.client.renderers;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static final Map<Direction, Quaternion> DIR2ROT = Maps.newEnumMap((Map<Direction, Quaternion>) Arrays.stream(Direction.values())
            .collect(Collectors.toMap(Functions.identity(), Direction::getRotation)));

    public static final Map<Integer, Quaternion> YAW2ROT = Arrays.stream(Direction.values()).filter(d -> d.getAxis() != Direction.Axis.Y)
            .map(d -> (int) -d.toYRot()).collect(Collectors.toMap(Functions.identity(), y -> Vector3f.YP.rotationDegrees(y)));

    public static Quaternion rot(Direction dir) {
        return DIR2ROT.get(dir);
    }

    private static final Quaternion def = Vector3f.YP.rotationDegrees(0);

    public static Quaternion rot(int rot) {
        return YAW2ROT.getOrDefault(rot, def);
    }

}