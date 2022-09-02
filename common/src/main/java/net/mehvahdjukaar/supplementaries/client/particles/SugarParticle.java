package net.mehvahdjukaar.supplementaries.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

public class SugarParticle extends TerrainParticle {

    //TODO:this is crap, make it snap to water surface

    public SugarParticle(ClientLevel clientLevel, double x, double y, double z, double speedX, double speedY, double speedZ) {
        super(clientLevel, x, y, z, speedX, speedY, speedZ, ModRegistry.SUGAR_CUBE.get().defaultBlockState());
        this.lifetime = (int)(30.0f / (this.random.nextFloat() * 0.7f + 0.3f));
        this.setColor(1,1,1);
        this.xd*=0.6;
        this.zd*=0.6;
    }



    @Override
    public void tick() {
        var pos = new BlockPos(this.x, this.y, this.z);
        var fluid = this.level.getFluidState(pos);
        float h = fluid.getOwnHeight();
        if (!this.removed && fluid.is(FluidTags.WATER)) {
            this.gravity = -0.125f;
        }else {
            if(this.yd>0) {
                this.yd = 0;
            }else {
                this.gravity = 1;
            }
        }
        super.tick();
        if(fluid.is(FluidTags.WATER) &&
                !this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)){
           if(Math.abs(pos.getY()+h-this.y)>1){
               int aaa = 1;
           }
            this.y = pos.getY()+h;
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        public Factory(SpriteSet set) {
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SugarParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TERRAIN_SHEET_OPAQUE;
    }

    public static final ParticleRenderType TERRAIN_SHEET_OPAQUE = new ParticleRenderType() {
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        public String toString() {
            return "TERRAIN_SHEET_OPAQUE";
        }
    };
}
