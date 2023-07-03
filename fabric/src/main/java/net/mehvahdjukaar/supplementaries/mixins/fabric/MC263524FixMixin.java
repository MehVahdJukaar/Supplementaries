package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SpriteCoordinateExpander.class, priority = 500)
public abstract class MC263524FixMixin implements VertexConsumer{

    @Shadow
    @Final
    private VertexConsumer delegate;

    @Shadow @Final private TextureAtlasSprite sprite;

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer vertex(double x, double y, double z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer color(int r, int g, int b, int a) {
        this.delegate.color(r, g, b, a);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer uv(float u, float v) {
        this.delegate.uv(this.sprite.getU((double)(u * 16.0F)), this.sprite.getV((double)(v * 16.0F)));
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer overlayCoords(int u, int v) {
        this.delegate.overlayCoords(u, v);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer uv2(int u, int v) {
        this.delegate.uv2(u, v);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }

}
