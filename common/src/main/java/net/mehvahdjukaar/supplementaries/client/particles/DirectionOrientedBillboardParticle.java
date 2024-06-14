package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DirectionOrientedBillboardParticle extends TextureSheetParticle {

    protected DirectionOrientedBillboardParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    protected DirectionOrientedBillboardParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float f = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float g = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float h = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf = new Quaternionf();
        Vec3 dir = new Vec3(this.xd, this.yd, this.zd).normalize();

        Vec3 cameraLook = new Vec3(renderInfo.getLookVector());
        Vec3 cross = dir.cross(cameraLook);

        double pitch = MthUtils.getPitch(dir);
        double yaw = MthUtils.getYaw(dir);

        Vector3f dirUp = new Vector3f(0, 1, 0).rotate(quaternionf);
        float roll = dirUp.angleSigned(cross.toVector3f(), dir.toVector3f());

        quaternionf.rotateY((float) (-Mth.DEG_TO_RAD * yaw));

        quaternionf.rotateX((float) (Mth.DEG_TO_RAD * (pitch - 90)));
        quaternionf.rotateY(-roll + Mth.HALF_PI);


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
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

}
