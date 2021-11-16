package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.common.VectorUtils;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RotationTrailParticle extends SimpleAnimatedParticle {

    public static final float SPEED = 11;//0.18f;//0.18/2;
    public static final int LIFE = 8;

    private final Vector3d axis;
    private final Vector3d origin;
    private final double radius;
    private float angularVelocity;
    private float currentAngle;
    private float fadeR;
    private float fadeG;
    private float fadeB;

    private RotationTrailParticle(ClientWorld clientWorld, double x, double y, double z, Vector3d center, Vector3d rotationAxis,
                                  int ccw, double radius, double angle, IAnimatedSprite sprite) {
        super(clientWorld, x, y, z, sprite, -5.0E-4F);
        this.origin = center;
        this.axis = rotationAxis;
        this.angularVelocity = (float) (ccw*SPEED*Math.PI/180f);
        this.radius = radius;
        this.currentAngle = (float) angle;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.setSize(0.01F, 0.01F);
        this.quadSize *= 0.625F;
        this.lifetime = LIFE;
        this.setColor(0x2a77ea);
        this.setFadeColor(0x32befa);
        this.setSpriteFromAge(sprite);
        this.alpha = al;
        this.hasPhysics = false;
    }

    private static final float al = 0.6f;

    @Override
    public void tick() {
        super.tick();
        this.setAlpha(al - (this.age / (float) this.lifetime) * al * 0.7f);
    }

    public void setFadeColor(int pRgb) {
        this.fadeR = (float) ((pRgb & 16711680) >> 16) / 255.0F;
        this.fadeG = (float) ((pRgb & '\uff00') >> 8) / 255.0F;
        this.fadeB = (float) ((pRgb & 255)) / 255.0F;
        super.setFadeColor(pRgb);
    }

    @Override
    public void move(double x, double y, double z) {
        //float speed = (float) angularVelocity;
        //float time = this.age + this.off;
        //float angle = time * speed - speed / 2.0F * (float) this.age *
        //        ((float) this.age / (float) this.lifetime);

        //double fade
        this.rCol += (this.fadeR - this.rCol) * 0.1F;
        this.gCol += (this.fadeG - this.gCol) * 0.1F;
        this.bCol += (this.fadeB - this.bCol) * 0.1F;

        //this.alpha *= 0.9F;

        this.currentAngle += this.angularVelocity;
                Vector3d rot = new Vector3d(this.radius, 0, 0).yRot(currentAngle);

        Vector3d newPos = VectorUtils.changeBasisN(this.axis, rot).add(this.origin);

        this.angularVelocity *= 0.75;

        super.move(newPos.x - this.x, newPos.y - this.y, newPos.z - this.z);
    }

    public static float increment(float age, int step) {
        return SPEED * step*(1 - (step + 2 * (age-1)) / (2 * LIFE));
    }

    @Override
    public int getLightColor(float pPartialTick) {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockpos) ? WorldRenderer.getLightColor(this.level, blockpos) : 0;
    }


    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Factory(IAnimatedSprite spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world,
                                       double centerX, double centerY, double centerZ,
                                       double direction, double radius, double initialAngle) {
            Vector3d center = new Vector3d(centerX, centerY, centerZ);

            int ccw = 1;
            if (direction < 0) {
                ccw = -1;
                direction = -direction;
            }

            Direction dir = Direction.from3DDataValue((int) direction);


            float radAngle = (float) (initialAngle*Math.PI/180);

            Vector3d axis = VectorUtils.ItoD(dir.getNormal());

            Vector3d rot = new Vector3d(radius, 0, 0).yRot(radAngle);
            Vector3d newPos = VectorUtils.changeBasisN(axis, rot).add(center);



            return new RotationTrailParticle(world, newPos.x, newPos.y, newPos.z, center, axis, ccw,
                    radius, radAngle, this.sprites);
        }

    }
}
