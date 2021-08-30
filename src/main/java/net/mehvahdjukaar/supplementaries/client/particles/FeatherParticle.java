package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FeatherParticle extends SpriteTexturedParticle {
    private float rotSpeed;

    private boolean fallingAnim = false;
    private int animationOffset = 0;
    private float rotOffset = 0;

    private FeatherParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double speedX, double speedY, double speedZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.quadSize *= 1F + this.random.nextFloat() * 0.2;
        this.lifetime = 120 + this.random.nextInt(20);
        this.rotSpeed = 2f * (0.05f + this.random.nextFloat() * 0.08f);
        this.roll = 0;//this.random.nextFloat() * ((float) Math.PI * 2F);
        //this.xd = speedX + (this.random.nextFloat() * 2.0D - 1.0D) * (double) 0.05F;
        this.yd = speedY; //+ (this.random.nextFloat() * 2.0D - 1.0D) * (double) 0.05F;
        //this.zd = speedZ + (this.random.nextFloat() * 2.0D - 1.0D) * (double) 0.05F;

    }

    public void setRotOffset(int spriteIndex) {
        int[] offsets = new int[]{-45,0,16};
        this.rotOffset = (float) (offsets[spriteIndex] * Math.PI/180f);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);

            this.xd *= 0.98F;

            this.zd *= 0.98F;

            this.yd -= 0.002F;
            this.yd = Math.max(this.yd, -0.0008F); //0.008

            this.oRoll = this.roll;


            if (this.onGround && this.yd > 0) {
                this.onGround = false;
            }

            if (!this.onGround) {

                if (!this.fallingAnim) {
                    float rot = (float) ((rotOffset + (this.age * this.rotSpeed)) % (2 * Math.PI));
                    if (this.yd <= 0 && rot > 0 && rot < 0.01 + this.rotSpeed * 2) {
                        this.oRoll = 0;
                        this.fallingAnim = true;
                        this.animationOffset = this.age - 1;

                    }
                    else{
                        this.roll =  rot;
                    }
                }

                if (this.fallingAnim) {
                    int ageWithOffset = age-animationOffset;

                    float amp = 1;

                    float w = rotSpeed/amp;

                    this.roll = rotOffset + MathHelper.sin(ageWithOffset * this.rotSpeed) * amp; //(float) Math.PI * this.rotSpeed * 1.6F;
                    /*
                    float amp = 0.5f;
                    if(ageWithOffset < 30){
                        amp += (30 - ageWithOffset)*0.01;
                        this.rotSpeed -= 0.002f;
                    }*/
                }
            } else {
                this.yd = 0.0D;
            }
        }
    }



    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FeatherParticle particle = new FeatherParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setColor(1, 1, 1);
            int i = particle.random.nextInt(3); //hard coding sprite set size. ugly
            particle.setRotOffset(i);
            particle.setSprite(spriteSet.get(i, 2));
            return particle;
        }


    }
}