package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CannonFireParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final double yaw;
    private final double pitch;

    private CannonFireParticle(ClientLevel world, double x, double y, double z, double pitch, double yaw, SpriteSet sprites) {
        super(world, x, y, z, 0, 0, 0);
        this.setParticleSpeed(0, 0, 0);
        this.pitch = pitch;
        this.yaw = yaw;
        this.spriteSet = sprites;
        this.lifetime = 5;
        this.hasPhysics = false;

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(spriteSet);
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float f = (float) (Mth.lerp((double) partialTicks, this.xo, this.x) - vec3.x());
        float g = (float) (Mth.lerp((double) partialTicks, this.yo, this.y) - vec3.y());
        float h = (float) (Mth.lerp((double) partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = renderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(renderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float i = this.getQuadSize(partialTicks);

        for (int j = 0; j < 4; ++j) {
            Vector3f vector3f = vector3fs[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(i);
            vector3f.add(f, g, h);
        }

        float k = this.getU0();
        float l = this.getU1();
        float m = this.getV0();
        float n = this.getV1();
        int o = this.getLightColor(partialTicks);
        buffer.vertex((double) vector3fs[0].x(), (double) vector3fs[0].y(), (double) vector3fs[0].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex((double) vector3fs[1].x(), (double) vector3fs[1].y(), (double) vector3fs[1].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex((double) vector3fs[2].x(), (double) vector3fs[2].y(), (double) vector3fs[2].z()).uv(k, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex((double) vector3fs[3].x(), (double) vector3fs[3].y(), (double) vector3fs[3].z()).uv(k, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();

    }

    @Override
    public int getLightColor(float partialTick) {
        float f = ((float) this.age + partialTick) / (float) this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z,
                                       double pitch, double yaw, double zSpeed) {
            return new CannonFireParticle(worldIn, x, y, z, pitch, yaw, sprite);
        }
    }

}
