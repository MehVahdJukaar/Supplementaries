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
    public VertexConsumer addVertex(float x, float y, float z) {
        this.delegate.addVertex(x, y, z);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer setColor(int r, int g, int b, int a) {
        this.delegate.setColor(r, g, b, a);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer setUv(float u, float v) {
        this.delegate.setUv(this.sprite.getU(u), this.sprite.getV(v));
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer setUv2(int u, int v) {
        this.delegate.setUv2(u, v);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer setUv1(int u, int v) {
        this.delegate.setUv1(u, v);
        return this;
    }

    /**
     * @author MehVahdJukaar
     * @reason fixes MC-263524 not working with chained methods
     */
    @Overwrite
    public VertexConsumer setNormal(float x, float y, float z) {
        this.delegate.setNormal(x, y, z);
        return this;
    }

}
