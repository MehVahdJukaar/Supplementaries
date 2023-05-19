package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.Supplier;

public class PendulumAnimation extends SwingAnimation {

    private static final Supplier<Double> HAMMOCK_FREQUENCY = () -> 0.3;
    private static final Supplier<Double> HAMMOCK_MAX_ANGLE = () -> 40.0;
    private static final Supplier<Double> HAMMOCK_MIN_ANGLE = () -> 0.4;
    private static final Supplier<Double> DAMPING = () -> 0.0;

    static {

        onChange();
    }

    private float angularVel = 0f;
    private boolean hasDrag = true;

    private float lastImpulse;

    private int immunity = 0;

    public PendulumAnimation(Function<BlockState, Vec3i> axisGetter) {
        super(axisGetter);
    }


    @Override
    public float getAngle(float partialTicks) {
        return (180 / Mth.PI) * Mth.rotLerp(partialTicks, prevAngle, angle);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        prevAngle = angle;
        if (immunity > 0) immunity--;
        onChange();
        float dt = 1 / 20f; //time step

        float energy = 0;

        float k = getK();

        boolean hasAcc = lastImpulse != 0;
        if (hasAcc) hasDrag = true;
        if (hasDrag) energy = calculateEnergy(k, angularVel, angle);


        if (hasAcc && (energy < maxAngleEnergy) || (lastImpulse*angularVel)<0) {
            angularVel += lastImpulse;
            angularVel = capVelocity(k, angularVel, angle, maxAngleEnergy);
        }
        lastImpulse = 0;

        float acc = -k * Mth.sin(angle);

        if (hasDrag && !hasAcc) {
            //note that since its proportional to speed this effectively limits the max angle
            if (energy > minAngleEnergy) {
                double damping = DAMPING.get();

                float drag = (float) (damping * angularVel);

                acc -= drag;
            } else {
                hasDrag = false;
            }
        }

        /* //more precise method
        float k1v, k2v, k3v =0;
        k1v =  angularVel;

        float  k1a =  -k * Mth.sin(angle);

        k2v = angularVel + 0.5f * dt * k1a;
        float   k2a =  -k * Mth.sin(angle + 0.5f * dt * k1v);

        k3v = angularVel + dt * k2a;
        float  k3a = -k * Mth.sin(angle + dt * k2v);

        angle += (dt / 4.0) * (k1v + 2.0 * k2v + k3v);
        angularVel += (dt / 4.0) * (k1a + 2 * k2a + k3a);
        */

        angularVel += dt * acc;

        angle += (angularVel * dt);


        //float max_yaw = max_swing_angle(self.yaw, self.angular_velocity, ff)


    }


    public void addImpulse(double vel) {
        this.angularVel += vel;
        this.hasDrag = true;
    }


    private static void onChange() {
        double frequency = HAMMOCK_FREQUENCY.get();
        // g/L. L = length = 1 k=g
        // spring costant of pendulum and other constants included here like gravity
        k = (float) Math.pow(2 * Math.PI * frequency, 2);
        maxAngleEnergy = angleToEnergy(k, (float) Math.toRadians(HAMMOCK_MAX_ANGLE.get()));
        minAngleEnergy = angleToEnergy(k, (float) Math.toRadians(HAMMOCK_MIN_ANGLE.get()));
    }

    private static float capVelocity(float k, float currentVel, float angle, float targetEnergy){
       float newVel = (float) Math.sqrt(2*(targetEnergy - k * (1 - Mth.cos(angle))));
       if(currentVel<0)newVel*=-1;
       return newVel;
    }

    private static float calculateEnergy(float k, float vel, float angle) {
        return angleToEnergy(k, angle) + 0.5f * (vel * vel);
    }

    private static float angleToEnergy(float k, float radAngle) {
        //E = mgh
        return k * (1 - Mth.cos(radAngle));
    }

    private static float k;
    private static float maxAngleEnergy;
    private static float minAngleEnergy;

    public static float getK() {
        return k;
    }

    @Override
    public boolean hitByEntity(Entity entity, BlockState state, BlockPos pos) {
        if (immunity != 0) return true;
        Vec3 motion = entity.getDeltaMovement();

        if (motion.length() < 0.001) return true;

        AABB boundingBox = entity.getBoundingBox();

        // Scale the mass of the entity by a constant (e.g., 2)
        //entity mass
        float massScale = 500;
        double eMass = boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize() * massScale;
        double selfMass = 1;

        Vec3 rotationAxis = MthUtils.V3itoV3(this.getRotationAxis(state));
        Vec3 normalVec = rotationAxis.cross(new Vec3(0, 1, 0));

        //vector in 2d space. y and z
        Vec3 entityPlaneVector = motion.subtract(rotationAxis.scale(motion.dot(rotationAxis)));

        float radius = 1;
        double magnitude = angularVel * radius;

        // Calculate x and y components of velocity vector
        double selfVZ = magnitude * Mth.cos(angle);
        double selfVY = magnitude * Mth.sin(angle);

        // Create the velocity vector
        Vec3 selfPlaneVector = new Vec3(0, selfVY, 0).add(normalVec.scale(selfVZ));

        double entityVZ;
        if (normalVec.z == 1) {
            entityVZ = entityPlaneVector.z;
        } else entityVZ = entityPlaneVector.x;

        double entityVY = entityPlaneVector.y;


        //constant
        double systemEnergy = (0.5 * selfMass * (selfVY * selfVY + selfVZ * selfVZ))
                + (0.5 * eMass * (entityVY * entityVY + entityVZ * entityVZ));
        /*
        //equations of whats going on below
        double vel; //?
        double entityVZf; //?
        double entityVYf; //?

        selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * entityVZf;

        selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * entityVYf;

        double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));
        */

        //we cant solve this.. just say that half ot the energy is is transferred always
        double entityVZf = entityVZ * 0.5;//(systemEnergy * Math.cos(angle) - selfVZ - eMass * entityVZ) / eMass;
        double entityVYf = entityVY * 0.5;//(systemEnergy * Math.sin(angle) - selfVY - eMass * entityVY) / eMass;

        double vel = Math.sqrt(2 * (systemEnergy - (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf))));
        if (entityVZ > 0) vel *= -1;

        double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));


        double c1 = selfVZ + eMass * entityVZ;
        double c2 = vel * Mth.cos(angle) + eMass * entityVZf;

        double d1 = selfVY + eMass * entityVY;
        double d2 = vel * Mth.sin(angle) + eMass * entityVYf;


        if (Double.isNaN(vel)) {
            int aa = 1;
        }

        this.lastImpulse = (float) (vel / radius) - angularVel;

        immunity = 10;

        Minecraft.getInstance().player.displayClientMessage(Component.literal("aa"+vel), false);

        //entity.setDeltaMovement(motion.add(normalVec.scale(entityVZf)).add(new Vec3(0,1,0).scale(entityVYf)));

        entity.getLevel().playSound(Minecraft.getInstance().player, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.75f, 1.5f);


        /*
        float entityVZf = (selfVZ + eMass * entityVZ - vel*Mth.cos(angle))/eMass ;

        float  entityVYf =  (selfVY + eMass * entityVY - vel*Mth.sin(angle))/eMass;

        (0.5 * vel * vel) =  systemEnergy - (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));
*/

        /*

        selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * entityVZf;
            a + m *b = x*cos(t) + m*z

        selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * entityVYf;
            c + m*d = x*sin(t) + m*y

        double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));

            0.5*x*x + 0.5*m*(z*z+y*y)

         */

        return true;
    }

}

