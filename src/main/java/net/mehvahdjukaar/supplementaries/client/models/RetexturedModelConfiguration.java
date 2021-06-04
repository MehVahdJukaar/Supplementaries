package net.mehvahdjukaar.supplementaries.client.models;

import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;

import javax.annotation.Nullable;
import java.util.Set;

public class RetexturedModelConfiguration implements IModelConfiguration {
    private final IModelConfiguration base;
    private final Set<String> retextured;
    private final RenderMaterial texture;

    public RetexturedModelConfiguration(IModelConfiguration base, Set<String> retextured, ResourceLocation texture) {
        this.base = base;
        this.retextured = retextured;
        this.texture = ModelLoaderRegistry.blockMaterial (texture);
    }

    public boolean isTexturePresent(String name) {
        if (this.retextured.contains(name)) {
            return !MissingTextureSprite.getLocation().equals(this.texture.texture());
        } else {
            return this.base.isTexturePresent(name);
        }
    }

    public RenderMaterial resolveTexture(String name) {
        return this.retextured.contains(name) ? this.texture : this.base.resolveTexture(name);
    }

    @Nullable
    public IUnbakedModel getOwnerModel() {
        return this.base.getOwnerModel();
    }

    public String getModelName() {
        return this.base.getModelName();
    }

    public boolean isShadedInGui() {
        return this.base.isShadedInGui();
    }

    public boolean isSideLit() {
        return this.base.isSideLit();
    }

    public boolean useSmoothLighting() {
        return this.base.useSmoothLighting();
    }

    public ItemCameraTransforms getCameraTransforms() {
        return this.base.getCameraTransforms();
    }

    public IModelTransform getCombinedTransform() {
        return this.base.getCombinedTransform();
    }

    public boolean getPartVisibility(IModelGeometryPart part, boolean fallback) {
        return this.base.getPartVisibility(part, fallback);
    }
}