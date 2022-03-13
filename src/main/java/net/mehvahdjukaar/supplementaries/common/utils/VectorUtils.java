package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

public class VectorUtils {

    //vector relative to a new basis
    public static Vec3 changeBasisN(Vec3 newBasisYVector, Vec3 rot) {
        Vec3 y = newBasisYVector.normalize();
        Vec3 x = new Vec3(y.y, y.z, y.x).normalize();
        Vec3 z = y.cross(x).normalize();
        return changeBasis(x, y, z, rot);
    }

    public static Vec3 changeBasis(Vec3 newX, Vec3 newY, Vec3 newZ, Vec3 rot) {
        return newX.scale(rot.x).add(newY.scale(rot.y)).add(newZ.scale(rot.z));
    }

    public static Vec3 getNormalFrom3DData(int direction) {
        return ItoD(Direction.from3DDataValue(direction).getNormal());
    }

    public static Vec3 ItoD(Vec3i v) {
        return new Vec3(v.getX(), v.getY(), v.getZ());
    }

    @Immutable
    public record Vec2i(int x, int y) implements Comparable<Vec2i> {

        public Vec2i subtract(Vec2i vec2i) {
            return this.subtract(vec2i.x, vec2i.y);
        }

        public Vec2i subtract(int x, int y) {
            return this.add(-x, -y);
        }

        public Vec2i add(Vec2i vec2i) {
            return this.add(vec2i.x, vec2i.y);
        }

        public Vec2i add(int x, int y) {
            return new Vec2i(this.x + x, this.y + y);
        }

        public float lengthSqr(){
            return this.x*this.x + this.y*this.y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof Vec2i vec2i) {
                return vec2i.x == this.x && vec2i.y == this.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y);
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ")";
        }

        @Override
        public int compareTo(@NotNull Vec2i other) {
            return Float.compare(this.lengthSqr(), other.lengthSqr());
        }
    }
}
