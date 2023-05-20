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

    private static final Supplier<Double> HAMMOCK_FREQUENCY = () -> 0.35;
    private static final Supplier<Double> HAMMOCK_MAX_ANGLE = () -> 180.0;
    private static final Supplier<Double> HAMMOCK_MIN_ANGLE = () -> 0.4;
    private static final Supplier<Double> DAMPING = () -> 0.000;

    static {

        onChange();
    }

    private float angularVel = 0.0001f;
    private boolean hasDrag = true;

    private float lastImpulse;

    private int immunity = 0;

    public PendulumAnimation(Function<BlockState, Vec3i> axisGetter) {
        super(axisGetter);
    }


    @Override
    public float getAngle(float partialTicks) {
        return (float) Math.toDegrees(Mth.lerp(partialTicks, prevAngle, angle));
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


        if (hasAcc && (energy < maxAngleEnergy) || (lastImpulse * angularVel) < 0) {
            angularVel += lastImpulse;
            if (calculateEnergy(k, angularVel, angle) > maxAngleEnergy) {
                angularVel = (0.1f * angularVel + 0.9f * capVelocity(k, angularVel, angle, maxAngleEnergy));
            }

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

    private static float capVelocity(float k, float currentVel, float angle, float targetEnergy) {
        //we need max as its an approximation and might get negative with some values
        float newVel = (float) Math.sqrt(Math.max(0, 2 * (targetEnergy - k * (1 - Mth.cos(angle)))));
        if (currentVel < 0) newVel *= -1;
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
        Vec3 entityMotion = entity.getDeltaMovement();

        if (entityMotion.length() < 0.01) return true;

        AABB boundingBox = entity.getBoundingBox();

        //scale velocity for more swing forge
        //entity mass
        float velScale = 50;
        entityMotion = entityMotion.scale(velScale);
        float massScale = 1f; //controls how much the velocity is distributed on impact. sign has mass of 1.
        //this means that n impact it wil keep 10% of its direction in opposite collisions
        double eMass = boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize() * massScale;
        double selfMass = 1;

        Vec3 rotationAxis = MthUtils.V3itoV3(this.getRotationAxis(state));

        Vec3 normalVec = rotationAxis.cross(new Vec3(0, 1, 0));

        //vector in 2d space. y and z
        Vec3 entityPlaneVector = entityMotion.subtract(entityMotion.multiply(rotationAxis.multiply(rotationAxis)));


        float radius = 1;
        double magnitude = angularVel * radius;

        // Calculate x and y components of velocity vector
        double selfVZ = magnitude * Mth.cos(angle);
        double selfVY = magnitude * Mth.sin(angle);

        // Create the velocity vector
        Vec3 selfPlaneVector = new Vec3(0, selfVY, 0).add(normalVec.scale(selfVZ));

        double entityIntensityAcrossMine = entityMotion.dot(selfPlaneVector.scale(1000000).normalize());


        double entityVZ;
        if (normalVec.z != 0) {
            entityVZ = entityPlaneVector.z;
        } else entityVZ = entityPlaneVector.x;

        double entityVY = entityPlaneVector.y;


        //constant
        //this is wrong
        double systemEnergy = (0.5 * selfMass * (selfVY * selfVY + selfVZ * selfVZ))
                + (0.5 * eMass * Mth.sqrt((float) (entityVY * entityVY + entityVZ * entityVZ)));
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
        float scale = 0.1f;
        double entityVZf = entityVZ * 0.5;//(systemEnergy * Math.cos(angle) - selfVZ - eMass * entityVZ) / eMass;
        double entityVYf = entityVY * 0.5;//(systemEnergy * Math.sin(angle) - selfVY - eMass * entityVY) / eMass;

        //calculate again
        double vel = Math.sqrt(2 * systemEnergy - (eMass * (entityVZf * entityVZf + entityVYf * entityVYf)));
        double fen = (0.5 * vel * vel) + (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));


        //if they have different directions its the inverse. square root stuff...
        //dont ask me why its like this
        boolean invertedAxis = (normalVec.z < 0 || normalVec.x < 0);
        if (entityVZ < 0 ^ invertedAxis) vel *= -1;
        //this does preserve energy at least

        if (Double.isNaN(vel)) {
            int aa = 1;
        }

        this.lastImpulse = (float) (vel / radius) - angularVel;

        //  if(rotationAxis.z<0 || rotationAxis.x<0)lastImpulse*=-1;

        immunity = 10;

        //we cant set that as its client only
        //entity.setDeltaMovement(entityMotion.add(normalVec.scale(entityVZf)).add(new Vec3(0,1,0).scale(entityVYf)));

        entity.getLevel().playSound(Minecraft.getInstance().player, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.75f, 1.5f);


        /*
        float entityVZf = (selfVZ + eMass * entityVZ - vel*Mth.cos(angle))/eMass ;

        float  entityVYf =  (selfVY + eMass * entityVY - vel*Mth.sin(angle))/eMass;

        (0.5 * vel * vel) =  systemEnergy - (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));
*/

        /*

        selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * P*cos(l);
            a + m *b = x*c + m*z

        selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * P*sin(l);;
            d + m*e = x*s + m*y

        double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * P*P);

            0.5*x*x + 0.5*m*sqrt(z*z+y*y) = k

            z =  (a + m *b -x*c)/m

            y=   (d + m*e -x*s)/m

            x*x  = 2*k - m*sqrt(z*z+y*y)

            float a = selfVZ;
            float m = eMass;
            float b = entityVZ;
            float x = Mth.cos(angle);
            float d = selfVY;
            float s = Mth.sin(angle);
            float e = entityVY;

            A = (1 - c*c / m*2 - s*s / m*m)
            B = -(2 * a * c + 2 * m * b * c - 2 * d * s - 2 * m * e * s)
            C = 2 * k - a^2 - m^2 * b^2 + 2 * a * m * b + 2 * d * m * e - m^2 * e^2

            float x = (-B + Mth.sqrt(B*B - 4*A*C)) / (2*A)

         */


        /*

            a+mb = G
            d + m*e = H

            0.5*x*x + 0.5*m*sqrt(z*z+y*y) = k

            sqrt(i*cos(a)*i*cos(a)+i*sin(a)*i*sin(a))

            x*x  = 2*k - ((a + m *b -x*c)*(a + m *b -x*c)+(d + m*e -x*s)*(d + m*e -x*s))/m

            xx = 2k - ((G -xc)*(G-xc) + (H -xs)*(H - xs))

            xx = 2k - sqrt( (GG - xcG - xcG -+xxcc) - (HH - xsH - xsH + xxss))
            xx = 2k - sqrt(GG + 2xcG - xxcc - HH + 2xsH - xxss)

            xx + xxcc + xxss - 2xcG - 2xsH  = 2k -GG -HH

            xx(1 + cc + ss) - 2x(cGsH) = 2k - GG -HH
            xxA + 2xB +

            A = (1+cc+ss);
            B = -2cGsH;
            C = -2k + GG +HH


x = (2 * c * s * (ad + a * me + d * mb + mbe) ± sqrt((2 * c * s)^2 * ((a + mb)^2 + (d + me)^2 - 2k))) / (2 * (1 + c^2 + s^2))
         */



        /*

        selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * P*cos(l);

        a + m*b = x*cos(t) + m* y*cos(z)

        selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * P*sin(l);;

        c + m*d = x*sin(t) + m*y*sin(z)

        0.5*x*x + 0.5*m*y*y = k

        double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * P*P);


        a + m*b = x*cos(t) + m*y*cos(z)

        c + m*d = x*sin(t) + m*y*sin(z)


        xx + myy = 2k


        A = a+m*b

        C = c+m*d

        A - x*cos(t) = my*cos(z)
        C - x*sin(t) = m*y*sin(z)
        xx + myy = 2k

        if(z=t)


        a + m*b = x*cos(h) + m*y*cos(h)

        c + m*d = x*sin(h) + m*y*sin(h)

        x*x + m*y*y = 2k

        S = sin(h)
        C = cos(h)

        A = a + m*b

        P = xC + myC
        x = (A/C -my)

        (A/C - my)*(A/C - my) + m*y*y = 2k

        AA/CC - 2myA/C + mmyy + myy = 2k
        yy(mm+m) + y(2mA/C) + (AA/CC -2k)

        j = m*m+m;
        k = 2*m*A/C;
        l = (A*A/(C*C) -2systemEn);

        y = (-b + Mth.sqrt(k*k - 4*j*l))/(2*j)


        x = my final vec
        y = other final vec
        v = my speed
        X = other intensity speed
        mv = mv
        v + m*X = x + m*y

        x = v +mX - m*y


        x*x + m*y*y = 2k

        k = 0.5*v*v + 0.5*m*X*X

//these!
        x*x+m*y*y=v*v+m*X*X

        x=v+mX-m*y


        (v+mX-m*y)(v+mX-m*y)+m*y*y=v*v+m*X*X

        (v + mN - my)(v + mN - my) + myy = vv + mNN

        vv + vmN - vmy + vmN + mmNN - mmNy -mvy - mmNy + mmyy + myy = vv + mNN

        2vmN - 2ymv - 2ymmN + mmNN + mmyy + myy = mNN

        yy(mm + m) + y2m(-v -mN) + (mmNN - mNN + 2vmN) = 0

        A = (mm + m);
        B = 2m(-v -mN);
        C = (mmNN - mNN + 2vmN);

        y = (-B + Mth.sqrt(B*B - 4*A*C))/(2*A)

        vmN - 2yvm + mmNN - 2ymmN
         */

        double nEn = (0.5 * magnitude * magnitude) + (0.5 * eMass * entityIntensityAcrossMine * entityIntensityAcrossMine);


        double m = eMass;
        double N = entityIntensityAcrossMine;
        double v = magnitude;


        double A = (m * m + m);
        double B = 2 * m * (-v - m * N);
        double C = (m * m * N * N - m * N * N + 2 * v * m * N);

        double y1 = (-B + Mth.sqrt((float) (B * B - 4 * A * C))) / (2 * A);

        double y2 = (-B - Mth.sqrt((float) (B * B - 4 * A * C))) / (2 * A);

        double x1 = v + m * N - m * y1;

        double x2 = v + m * N - m * y2;

        double x = x2;
        //chooses the right one. one is always the same vector
        if (Mth.abs((float) (x - magnitude)) < 0.0001) {
            x = x1;
        }

        float dW = (float) (x / radius) - angularVel;

        //dont even ask me whats going on here. needed to handle all the faces
        if (entityIntensityAcrossMine < 0 ^ entityVZ < 0) {
            dW *= -1;
        }
        if (invertedAxis) {
            dW *= -1;
        }
        this.lastImpulse = dW;

        Minecraft.getInstance().player.displayClientMessage(Component.literal("speed: " + dW + " " + angularVel + " " + (x>angularVel)), false);

        if (Double.isNaN(lastImpulse)) {
            int aaa = 1;
        }
        return true;
    }

}

