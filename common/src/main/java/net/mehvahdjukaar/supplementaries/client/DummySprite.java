package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;

public class DummySprite extends TextureAtlasSprite {
    protected static final ResourceLocation LOCATION = Supplementaries.res("dummy");

    public static final DummySprite INSTANCE = new DummySprite();

    private DummySprite() {
        super(LOCATION, new SpriteContents(LOCATION,
                new FrameSize(1, 1),
                new NativeImage(1, 1, false),
                AnimationMetadataSection.EMPTY), 1, 1, 0, 0);
    }

    @Override
    public float getU(double u) {
        return (float) u / 16;
    }

    @Override
    public float getV(double v) {
        return (float) v / 16;
    }
}
