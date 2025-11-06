package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.controllers.BoatPathNavigation;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;

public class DummySprite extends TextureAtlasSprite {
    protected static final ResourceLocation LOCATION = Supplementaries.res("dummy");

    public static final DummySprite INSTANCE = new DummySprite();

    private DummySprite() {
        super(LOCATION, new SpriteContents(LOCATION,
                new FrameSize(1, 1),
                new NativeImage(1, 1, false),
                ResourceMetadata.EMPTY), 1, 1, 0, 0);
    }

    @Override
    public float getU(float u) {
        return u / 16;
    }

    @Override
    public float getV(float v) {
        return v / 16;
    }
}
