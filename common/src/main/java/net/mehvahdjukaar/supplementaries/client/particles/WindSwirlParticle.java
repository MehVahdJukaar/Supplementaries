package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// A wind streak spiralling up the 4 sides of an open ended box, like a tiny tornado hugging a rod.
public class WindSwirlParticle extends TextureSheetParticle {

    private static final float WIDTH = 4 / 16f;
    private static final float BOX_HEIGHT = 3 / 16f;
    private static final float STREAK_THICKNESS = 1 / 16f;
    private static final float STREAK_LENGTH = 0.5f;
    private static final float LAPS = 1.35f;

    private final Vector3f axisDir;
    private final Vector3f[] sideNormals;
    private final float startOffset;

    private WindSwirlParticle(ClientLevel level, double x, double y, double z, Direction.Axis rodAxis, SpriteSet sprites) {
        super(level, x, y, z);
        this.pickSprite(sprites);

        Vector3f right;
        Vector3f forward;
        switch (rodAxis) {
            case X -> {
                this.axisDir = new Vector3f(1, 0, 0);
                right = new Vector3f(0, 1, 0);
                forward = new Vector3f(0, 0, 1);
            }
            case Z -> {
                this.axisDir = new Vector3f(0, 0, 1);
                right = new Vector3f(1, 0, 0);
                forward = new Vector3f(0, 1, 0);
            }
            default -> {
                this.axisDir = new Vector3f(0, 1, 0);
                right = new Vector3f(1, 0, 0);
                forward = new Vector3f(0, 0, 1);
            }
        }
        if (this.random.nextBoolean()) {
            Vector3f swap = right;
            right = forward;
            forward = swap;
        }
        this.sideNormals = new Vector3f[]{forward.mul(-1, new Vector3f()), right, forward, right.mul(-1, new Vector3f())};

        this.startOffset = this.random.nextFloat();
        this.lifetime = 18 + this.random.nextInt(13);
        this.gravity = 0;
        this.friction = 1;
        this.hasPhysics = false;
        this.setSize(0.01f, 0.01f);

        //the whole ring creeps along the rod on top of the streak sliding up inside it
        float drift = 0.002f + this.random.nextFloat() * 0.004f;
        this.xd = this.axisDir.x() * drift;
        this.yd = this.axisDir.y() * drift;
        this.zd = this.axisDir.z() * drift;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static float fadeEnvelope(float progress) {
        float in = Mth.clamp(progress / 0.25f, 0, 1);
        float out = Mth.clamp((1 - progress) / 0.4f, 0, 1);
        return in * out;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float progress = Mth.clamp((this.age + partialTicks) / this.lifetime, 0, 1);
        float a = this.alpha * fadeEnvelope(progress);
        if (a <= 0.005f) return;

        Vec3 camPos = camera.getPosition();
        float cx = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camPos.x);
        float cy = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camPos.y);
        float cz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camPos.z);

        //head and tail as a position along the perimeter, 1 unit is a full lap
        float head = this.startOffset + progress * LAPS;
        float tail = head - STREAK_LENGTH;
        float wrap = Mth.floor(tail);
        tail -= wrap;
        head -= wrap;

        //the streak stays flat and slides up the box instead of tilting into a helix
        float climb = (progress - 0.5f) * (BOX_HEIGHT - STREAK_THICKNESS);

        int light = this.getLightColor(partialTicks);
        int lu = VertexUtil.lightU(light);
        int lv = VertexUtil.lightV(light);
        PoseStack poseStack = new PoseStack();

        //the streak never spans more than a lap so it can only hit a side twice
        for (int side = 0; side < 4; side++) {
            for (int lap = 0; lap < 2; lap++) {
                float sideStart = side * 0.25f + lap;
                float from = Math.max(tail, sideStart);
                float to = Math.min(head, sideStart + 0.25f);
                if (to <= from) continue;
                drawSegment(buffer, poseStack, side,
                        (from - sideStart) * 4, (to - sideStart) * 4,
                        (from - tail) / STREAK_LENGTH, (to - tail) / STREAK_LENGTH,
                        climb, cx, cy, cz, a, lu, lv);
            }
        }
    }

    private void drawSegment(VertexConsumer buffer, PoseStack poseStack, int side,
                             float from, float to, float u0, float u1, float climb,
                             float cx, float cy, float cz, float a, int lu, int lv) {
        Vector3f normal = this.sideNormals[side];
        Vector3f tangent = this.sideNormals[(side + 1) & 3];

        float su0 = this.sprite.getU(u0);
        float su1 = this.sprite.getU(u1);
        float sv0 = this.sprite.getV(0);
        float sv1 = this.sprite.getV(1);
        float top = climb + STREAK_THICKNESS / 2;
        float bottom = climb - STREAK_THICKNESS / 2;

        //both windings so the far sides of the ring show up too
        vertex(buffer, poseStack, normal, tangent, from, top, su0, sv0, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, to, top, su1, sv0, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, to, bottom, su1, sv1, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, from, bottom, su0, sv1, cx, cy, cz, a, lu, lv);

        vertex(buffer, poseStack, normal, tangent, from, bottom, su0, sv1, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, to, bottom, su1, sv1, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, to, top, su1, sv0, cx, cy, cz, a, lu, lv);
        vertex(buffer, poseStack, normal, tangent, from, top, su0, sv0, cx, cy, cz, a, lu, lv);
    }

    private void vertex(VertexConsumer buffer, PoseStack poseStack, Vector3f normal, Vector3f tangent,
                        float alongSide, float alongAxis, float u, float v,
                        float cx, float cy, float cz, float a, int lu, int lv) {
        float outwards = WIDTH / 2;
        float sideways = (alongSide - 0.5f) * WIDTH;
        float px = cx + normal.x() * outwards + tangent.x() * sideways + this.axisDir.x() * alongAxis;
        float py = cy + normal.y() * outwards + tangent.y() * sideways + this.axisDir.y() * alongAxis;
        float pz = cz + normal.z() * outwards + tangent.z() * sideways + this.axisDir.z() * alongAxis;
        VertexUtil.vert(buffer, poseStack, px, py, pz, u, v,
                this.rCol, this.gCol, this.bCol, a, lu, lv, normal.x(), normal.y(), normal.z());
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double axisOrdinal, double unusedY, double unusedZ) {
            return new WindSwirlParticle(level, x, y, z, Direction.Axis.values()[(int) axisOrdinal], this.sprites);
        }
    }
}
