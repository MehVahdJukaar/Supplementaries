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
import org.joml.Vector3f;

public class FeatherParticle extends TextureSheetParticle {
    private final float rotSpeed;

    private boolean fallingAnim = false;
    private int animationOffset;
    private float rotOffset = 0;
    private int groundTime = 0;

    private FeatherParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double speedX, double speedY, double speedZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.quadSize *= 1.3125F + this.random.nextFloat() * 0.15;
        this.lifetime = 360 + this.random.nextInt(60);
        this.rotSpeed = 2f * (0.045f + this.random.nextFloat() * 0.08f) + ((float) speedY - 0.03f);
        this.animationOffset = (int) ((this.random.nextFloat() * ((float) Math.PI * 2F)) / this.rotSpeed);
        this.xd = speedX + (this.random.nextFloat() * 2.0D - 1.0D) *  0.008F;
        this.yd = speedY; //+ (this.random.nextFloat() * 2.0D - 1.0D) * (double) 0.05F;
        this.zd = speedZ + (this.random.nextFloat() * 2.0D - 1.0D) *  0.008F;
        this.gravity = 0.007F;
    }

    public void setRotOffset(int spriteIndex) {
        int[] offsets = new int[]{-45, 0, 16};
        this.rotOffset = offsets[spriteIndex] * Mth.DEG_TO_RAD;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime || this.groundTime > 20) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);

            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;

            //this.yd = Math.max(this.yd, -0.02F); //0.008

            if (this.onGround && this.yd > 0) {
                this.onGround = false;
            }

            if (!this.onGround) {

                if (!this.fallingAnim) {
                    float rot = (float) (((this.age + this.animationOffset) * this.rotSpeed) % (2 * Math.PI));

                    if (this.yd <= 0 && rot > 0 && rot < 0.01 + this.rotSpeed * 2) {
                        this.fallingAnim = true;
                        if (this.oRoll > 6) {
                            //this.oRoll = (float) (this.oRoll - Math.PI*2);
                        }
                        this.animationOffset = this.age;
                    }

                    this.oRoll = this.roll;
                    this.roll = rot;

                } else {
                    int t = this.age - this.animationOffset;

                    //0.5
                    //frequency scaling
                    //TODO: tweak these 2
                    double freq = 1 - this.rotSpeed; //ClientConfigs.general.TEST1.get() - this.rotSpeed

                    //attenuation
                    double k = 20 * 1;//ClientConfigs.general.TEST2.get();

                    //minimum amplitude
                    float min = (float) (freq / 2f);

                    float amp = (float) ((freq - min) * Math.exp(-t / k)) + min;

                    //amp(0)
                    float w = (float) (this.rotSpeed / (freq));

                    this.oRoll = this.roll;
                    this.roll = Mth.sin(t * w) * amp; //(float) Math.PI * this.rotSpeed * 1.6F;
                    /*
                    float amp = 0.5f;
                    if(ageWithOffset < 30){
                        amp += (30 - ageWithOffset)*0.01;
                        this.rotSpeed -= 0.002f;
                    }*/


                }
            } else {
                this.groundTime++;
                this.oRoll = this.roll;
                this.yd = 0.0D;
            }
        }
    }

    @Override
    public void render(VertexConsumer builder, Camera info, float partialTicks) {
        Vec3 vector3d = info.getPosition();
        float f = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vector3d.x());
        float f1 = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vector3d.y());
        float f2 = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vector3d.z());
        Quaternionf quaternion;
        if (this.roll == 0.0F) {
            quaternion = info.rotation();
        } else {
            quaternion = new Quaternionf(info.rotation());
            float p = Mth.RAD_TO_DEG;
            float f3 = Mth.rotLerp(partialTicks, (this.rotOffset + this.oRoll) * p,
                    (this.rotOffset + this.roll) * p);
            quaternion.mul(Axis.ZP.rotation(f3 / p));
        }
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(partialTicks);
        double offset = 0.125;
        builder.vertex(avector3f[0].x(), avector3f[0].y() + offset, avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        builder.vertex(avector3f[1].x(), avector3f[1].y() + offset, avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        builder.vertex(avector3f[2].x(), avector3f[2].y() + offset, avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        builder.vertex(avector3f[3].x(), avector3f[3].y() + offset, avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FeatherParticle particle = new FeatherParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setColor(1, 1, 1);
            int i = particle.random.nextInt(3); //hard coding sprite set size (3). ugly
            particle.setRotOffset(i);
            particle.setSprite(spriteSet.get(i, 2));
            return particle;
        }
    }
}