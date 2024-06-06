package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConfettiParticle extends TextureSheetParticle {

    private ConfettiParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z);

        this.pickSprite(sprites);

        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.setSize(0.001F, 0.001F);
        this.gravity = 0.015F / 0.04F;
        //longer
        this.lifetime = (int) (80.0D / (this.random.nextFloat() * 0.3D + 0.7D));

        int col = ColorHelper.getRandomBrightColor(this.random);
        /*
        float i = random.nextFloat();
        this.rCol = Math.max(0.0F, MathHelper.sin((i + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, MathHelper.sin((i + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, MathHelper.sin((i + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        */

        this.rCol = FastColor.ARGB32.red(col) / 255f;
        this.gCol = FastColor.ARGB32.green(col) / 255f;
        this.bCol = FastColor.ARGB32.blue(col) / 255f;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float f = (float)(Mth.lerp((double)partialTicks, this.xo, this.x) - vec3.x());
        float g = (float)(Mth.lerp((double)partialTicks, this.yo, this.y) - vec3.y());
        float h = (float)(Mth.lerp((double)partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = renderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(renderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float i = this.getQuadSize(partialTicks);

        for(int j = 0; j < 4; ++j) {
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
        buffer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(l, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(l, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(k, m).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();
        buffer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(k, n).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(o).endVertex();

    }

    @Override
    public void tick() {
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