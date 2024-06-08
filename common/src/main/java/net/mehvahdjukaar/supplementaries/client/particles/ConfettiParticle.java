package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class ConfettiParticle extends TextureSheetParticle {

    // yes we sample 36 perlin noises per tick per particle
    protected static final PerlinSimplexNoise X_NOISE = noise(58637214);
    protected static final PerlinSimplexNoise Z_NOISE = noise(823917);
    protected static final PerlinSimplexNoise YAW_NOISE = noise(28943157);
    protected static final PerlinSimplexNoise ROLL_NOISE = noise(80085);
    protected static final PerlinSimplexNoise PITCH_NOISE = noise(49715286);

    private static PerlinSimplexNoise noise(int seed) {
        return new PerlinSimplexNoise(new LegacyRandomSource(seed),
                List.of(-4 -3, -2, -1, 0, 1, 2));
    }

    private final int particleRandom;
    private float pitch = 0;
    private float oPitch = 0;
    private float yaw = 0;
    private float oYaw = 0;

    private float dPitch = 0;
    private float dYaw = 0;
    private float dRoll = 0;

    private ConfettiParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z);

        this.pickSprite(sprites);

        this.particleRandom = random.nextInt();
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.setSize(0.001F, 0.001F);
        this.gravity = 0.2F;
        this.friction = 0.94f;
        //longer
        this.lifetime = random.nextInt(400, 700);

        int col = ColorHelper.getRandomBrightColor(this.random);

        this.rCol = FastColor.ARGB32.red(col) / 255f;
        this.gCol = FastColor.ARGB32.green(col) / 255f;
        this.bCol = FastColor.ARGB32.blue(col) / 255f;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float f = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float g = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float h = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        quaternionf.rotateY(Mth.lerp(partialTicks, this.oYaw, this.yaw));
        quaternionf.rotateX(Mth.lerp(partialTicks, this.oPitch, this.pitch));

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float i = this.getQuadSize(partialTicks);

        for (int j = 0; j < 4; ++j) {
            Vector3f vector3f = vector3fs[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(i);
            vector3f.add(f, g, h);
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int o = this.getLightColor(partialTicks);

        buffer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();

        buffer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
    }

    @Override
    public void tick() {
        boolean still = this.x == this.xo && this.z == this.zo && this.y == this.yo && this.age != 0;

        boolean hasLanded = (onGround || still);

        float posChange = 0.01f;
        this.xd += posChange * X_NOISE.getValue(particleRandom, this.age, false);
        this.zd += posChange * Z_NOISE.getValue(particleRandom, this.age, false);
        this.oYaw = this.yaw;
        this.oPitch = this.pitch;
        this.oRoll = this.roll;

        if (!hasLanded) {

            float rotChange = 0.1f;
            this.dYaw += (float) (rotChange * YAW_NOISE.getValue(particleRandom, this.age, false));
            this.dRoll += (float) (rotChange * ROLL_NOISE.getValue(particleRandom, this.age, false));
            this.dPitch += (float) (rotChange * PITCH_NOISE.getValue(particleRandom, this.age, false));

            this.yaw += this.dYaw;
            this.pitch += this.dPitch;
            this.roll += this.dRoll;
        } else {
            this.age = Math.max(this.age, this.lifetime - 20);
        }

        float moment = 0.98f;
        this.dYaw *= moment;
        this.dRoll *= moment;
        this.dPitch *= moment;

        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {

            return new ConfettiParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }

}