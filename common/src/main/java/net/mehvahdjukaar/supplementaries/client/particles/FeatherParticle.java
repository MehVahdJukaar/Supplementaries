package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class FeatherParticle extends TextureSheetParticle {

    //each feather texture is drawn at a different tilt. this cancels it out so they all spin around the same axis
    private static final float[] SPRITE_TILTS = {43, 0, -16};
    private static final int GROUND_LIFETIME = 20;
    private static final int FADE_TICKS = 10;
    //how fast the flutter dies down, in ticks
    private static final double FLUTTER_DAMPING = 20;

    private final float rotSpeed;
    private final int spinPhaseOffset;

    private boolean fluttering = false;
    private int flutterStartAge;
    private float rotOffset = 0;
    private int groundTime = 0;

    private FeatherParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double speedX, double speedY, double speedZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.quadSize *= (float) (1.3125F + this.random.nextFloat() * 0.15);
        this.lifetime = 360 + this.random.nextInt(60);
        this.rotSpeed = Mth.clamp(2f * (0.045f + this.random.nextFloat() * 0.08f) + ((float) speedY - 0.03f), 0.02f, 0.5f);
        this.spinPhaseOffset = (int) ((this.random.nextFloat() * ((float) Math.PI * 2F)) / this.rotSpeed);
        this.xd = speedX + (this.random.nextFloat() * 2.0D - 1.0D) * 0.008F;
        this.yd = speedY;
        this.zd = speedZ + (this.random.nextFloat() * 2.0D - 1.0D) * 0.008F;
        this.gravity = 0.007F;
    }

    private void setRotOffset(int spriteIndex) {
        this.rotOffset = SPRITE_TILTS[spriteIndex % SPRITE_TILTS.length] * Mth.DEG_TO_RAD;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime || this.groundTime > GROUND_LIFETIME) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;

            if (!this.onGround) {

                if (!this.fluttering) {
                    float rot = (float) (((this.age + this.spinPhaseOffset) * this.rotSpeed) % (2 * Math.PI));

                    //wait for the spin to come back around to flat before handing over, so the angle doesn't jump
                    if (this.yd <= 0 && rot > 0 && rot < 0.01 + this.rotSpeed * 2) {
                        this.fluttering = true;
                        this.flutterStartAge = this.age;
                    }

                    this.oRoll = this.roll;
                    this.roll = rot;

                } else {
                    int t = this.age - this.flutterStartAge;

                    double swingFreq = 1 - this.rotSpeed;
                    float minAmplitude = (float) (swingFreq / 2f);
                    float amplitude = (float) ((swingFreq - minAmplitude) * Math.exp(-t / FLUTTER_DAMPING)) + minAmplitude;
                    float angularSpeed = (float) (this.rotSpeed / swingFreq);

                    this.oRoll = this.roll;
                    this.roll = Mth.sin(t * angularSpeed) * amplitude;
                }
            } else {
                this.groundTime++;
                this.oRoll = this.roll;
                this.yd = 0.0D;
            }

            int ticksLeft = Math.min(this.lifetime - this.age, GROUND_LIFETIME - this.groundTime);
            this.alpha = ticksLeft >= FADE_TICKS ? 1 : Math.max(0, ticksLeft / (float) FADE_TICKS);
        }
    }

    @Override
    public void render(VertexConsumer builder, Camera camera, float partialTicks) {
        Quaternionf quaternion;
        if (this.roll == 0.0F && this.rotOffset == 0.0F) {
            quaternion = camera.rotation();
        } else {
            quaternion = new Quaternionf(camera.rotation());
            float p = Mth.RAD_TO_DEG;
            float f3 = Mth.rotLerp(partialTicks, (this.rotOffset + this.oRoll) * p,
                    (this.rotOffset + this.roll) * p);
            quaternion.mul(Axis.ZP.rotation(f3 / p));
        }

        this.renderRotatedQuad(builder, camera, quaternion, partialTicks);
    }

    @Override
    protected void renderRotatedQuad(VertexConsumer vertexConsumer, Camera camera, Quaternionf quaternionf, float f) {
        Vec3 vec3 = camera.getPosition();
        //lift by the half extent so a landed feather rests on the surface instead of sinking into it
        float lift = this.getQuadSize(f);

        float g = (float) (Mth.lerp(f, this.xo, this.x) - vec3.x());
        float h = (float) (Mth.lerp(f, this.yo, this.y) - vec3.y()) + lift;
        float i = (float) (Mth.lerp(f, this.zo, this.z) - vec3.z());
        this.renderRotatedQuad(vertexConsumer, quaternionf, g, h, i, f);
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FeatherParticle particle = new FeatherParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            int i = particle.random.nextInt(SPRITE_TILTS.length);
            particle.setRotOffset(i);
            particle.setSprite(spriteSet.get(i, SPRITE_TILTS.length - 1));
            return particle;
        }
    }
}
