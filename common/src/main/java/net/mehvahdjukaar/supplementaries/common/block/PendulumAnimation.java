package net.mehvahdjukaar.supplementaries.common.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;
import java.util.function.Supplier;

public class PendulumAnimation extends SwingAnimation {
    private static final RandomSource RAND = RandomSource.create();

    private final Supplier<Config> config;
    private float angularVel;
    private boolean hasDrag = true;
    private float lastImpulse;
    private int immunity = 0;

    public PendulumAnimation(Supplier<Config> config, Function<BlockState, Vec3i> axisGetter) {
        super(axisGetter);
        this.config = config;
        Config c = config.get();
        this.angle = (RAND.nextFloat() - 0.5f) * c.minAngle * 2;
        this.angularVel = capVelocity(c.k, 1000, angle, c.minAngleEnergy);
    }


    @Override
    public float getAngle(float partialTicks) {
        return (float) Math.toDegrees(Mth.lerp(partialTicks, prevAngle, angle));
    }

    @Override
    public void reset() {
        angle = config.get().minAngle;
        angularVel = 0;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        prevAngle = angle;
        if (immunity > 0) immunity--;

        float dt = 1 / 20f; //time step

        float energy = 0;

        Config config = this.config.get();

        float k = config.k;

        boolean hasAcc = lastImpulse != 0;
        if (hasAcc) hasDrag = true;
        if (hasDrag) energy = calculateEnergy(k, angularVel, angle);


        if (hasAcc && (energy < config.maxAngleEnergy) || (lastImpulse * angularVel) < 0) {
            angularVel += lastImpulse;
            if (calculateEnergy(k, angularVel, angle) > config.maxAngleEnergy) {
                angularVel = (0.1f * angularVel + 0.9f * capVelocity(k, angularVel, angle, config.maxAngleEnergy));
            }

        }
        lastImpulse = 0;

        float acc = -k * Mth.sin(angle);

        if (hasDrag && !hasAcc) {
            //note that since its proportional to speed this effectively limits the max angle
            if (energy > config.minAngleEnergy) {
                double damping = config.damping;

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


    private static float capVelocity(float k, float currentVel, float angle, float targetEnergy) {
        //we need max as its an approximation and might get negative with some values
        float newVel = (float) Math.sqrt(Math.max(0, 2 * (targetEnergy - k * (1 - Mth.cos(angle)))));
        if (currentVel < 0) newVel *= -1;
        return newVel;
    }

    private static float calculateEnergy(float k, float vel, float radAngle) {
        return angleToEnergy(k, radAngle) + 0.5f * (vel * vel);
    }

    private static float angleToEnergy(float k, float radAngle) {
        //E = mgh, m =1
        return k * (1 - Mth.cos(radAngle));
    }

    @Override
    public boolean hitByEntity(Entity entity, BlockState state, BlockPos pos) {
        if (immunity != 0) return true;
        Vec3 eVel = entity.getDeltaMovement();

        if (eVel.length() < 0.01) return false; //too little

        Config config = ClientConfigs.Blocks.HANGING_SIGN_CONFIG.get();
        double eMass;
        if (config.considerEntityHitbox) {
            AABB boundingBox = entity.getBoundingBox();
            eMass = boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize();
        } else eMass = 1;
        //controls how much the velocity is distributed on impact. sign has mass of 1.
        //this means that n impact it wil keep 10% of its direction in opposite collisions
        eMass *= config.collisionInertia;

        //scale velocity for more swing forge
        //entity mass
        eVel = eVel.scale(config.collisionForce);


        Vec3 rotationAxis = MthUtils.V3itoV3(this.getRotationAxis(state));

        Vec3 normalVec = rotationAxis.cross(new Vec3(0, 1, 0));

        //vector in 2d space. y and z
        Vec3 entityPlaneVector = eVel.subtract(eVel.multiply(rotationAxis.multiply(rotationAxis)));


        float radius = 1;
        double magnitude = angularVel * radius;

        if (magnitude == 0) magnitude = 0.00001;

        // Create the velocity vector
        Vec3 signVel = new Vec3(0, Mth.sin(angle), 0).add(normalVec.scale(Mth.cos(angle)));

        double eRelVel = eVel.dot(signVel.scale(1000000).normalize());

        if (eRelVel * eRelVel < 0.0001) return false;//too little

        double entityForwardMotion;
        if (normalVec.z != 0) {
            entityForwardMotion = entityPlaneVector.z;
        } else entityForwardMotion = entityPlaneVector.x;


        double v = magnitude;


        double f = (eMass * eMass + eMass);
        double g = 2 * eMass * (-v - eMass * eRelVel);
        double h = (eMass * eMass * eRelVel * eRelVel - eMass * eRelVel * eRelVel + 2 * v * eMass * eRelVel);

        float delta = Mth.sqrt((float) (g * g - 4 * f * h));
        double y1 = (-g + delta) / (2 * f);

        double y2 = (-g - delta) / (2 * f);

        double x1 = v + eMass * eRelVel - eMass * y1;

        double x2 = v + eMass * eRelVel - eMass * y2;

        double x;
        //chooses the right one. one is always the same vector
        if (Mth.abs((float) (x2 - magnitude)) < 0.0001) {
            x = x1;
        } else x = x2;

        float dW = (float) (x / radius) - angularVel;

        //dont even ask me whats going on here. needed to handle all the faces
        if (eRelVel < 0 ^ entityForwardMotion < 0) {
            dW *= -1;
        }
        boolean invertedAxis = (normalVec.z < 0 || normalVec.x < 0);

        if (invertedAxis) {
            dW *= -1;
        }
        this.lastImpulse = dW;
        this.immunity = 10;

        //we cant set that as its client only
        //entity.setDeltaMovement(eVel.add(normalVec.scale(entityVZf)).add(new Vec3(0,1,0).scale(entityVYf)));

        entity.level().playLocalSound(pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.75f, 1.5f, false);

        return true;
    }


    //some math here just in case i need to derive it again

    /*
    selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * entityVZf;

    selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * entityVYf;

    double finalEnergy = (0.5 * vel * vel) + (0.5 * eMass * (entityVZf * entityVZf + entityVYf * entityVYf));


    selfVZ + eMass * entityVZ = vel*Mth.cos(angle) + eMass * P*cos(l);

    a + m*b = x*cos(t) + m* y*cos(z)

    selfVY + eMass * entityVY = vel*Mth.sin(angle) + eMass * P*sin(l);;

    c + m*d = x*sin(t) + m*y*sin(z)

    0.5*x*x + 0.5*m*y*y = k


    a + m*b = x*cos(t) + m*y*cos(z)

    c + m*d = x*sin(t) + m*y*sin(z)


    xx + myy = 2k


    a + m*b = x*cos(h) + m*y*cos(h)

    c + m*d = x*sin(h) + m*y*sin(h)

    x*x + m*y*y = 2k


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
    //conservation of energy
    x*x+m*y*y=v*v+m*X*X

    //conservation of momentum along motion dir
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


    public static class Config {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0, 360).fieldOf("min_angle").forGetter(c -> (float) Math.toDegrees(c.minAngle)),
                Codec.floatRange(0, 360).fieldOf("max_angle").forGetter(c -> (float) Math.toDegrees(c.maxAngle)),
                Codec.FLOAT.fieldOf("damping").forGetter(c -> c.damping),
                Codec.FLOAT.fieldOf("frequency").forGetter(c -> c.frequency),
                Codec.BOOL.fieldOf("collision_considers_entity_hitbox").forGetter(c -> c.considerEntityHitbox),
                ExtraCodecs.POSITIVE_FLOAT.fieldOf("collision_inertia").forGetter(c -> c.collisionInertia),
                ExtraCodecs.POSITIVE_FLOAT.fieldOf("collision_force").forGetter(c -> c.collisionForce)

        ).apply(instance, Config::new));


        protected final float minAngle;
        protected final float maxAngle;
        protected final float damping;
        protected final float frequency;
        protected final float maxAngleEnergy;
        protected final float minAngleEnergy;
        protected final float k;

        protected final boolean considerEntityHitbox;
        protected final float collisionInertia;
        protected final float collisionForce;


        public Config(float minAngle, float maxAngle, float damping, float frequency, boolean hitbox, float mass, float force) {
            this.minAngle = (float) Math.toRadians(minAngle);
            this.maxAngle = (float) Math.toRadians(maxAngle);
            this.damping = damping;
            this.frequency = frequency;
            // g/L. L = length = 1 k=g
            // spring constant of pendulum and other constants included here like gravity
            //can this be scaled too? what does t affect? it should be equivalent to increase length
            //freq is proportional to k so increasing f is like increasing l. mass doesnt play a role here
            k = (float) Math.pow(2 * Math.PI * frequency, 2);
            maxAngleEnergy = angleToEnergy(k, this.maxAngle);
            minAngleEnergy = angleToEnergy(k, this.minAngle);

            this.considerEntityHitbox = hitbox;
            this.collisionInertia = mass;
            this.collisionForce = force;
        }

        public Config() {
            this(0.8f, 60, 0.525f, 0.60f, true, 1f, 15);
        }

    }
}

