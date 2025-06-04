package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.client.IModelPartExtension;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IModelPartExtension {

    //just so we take up a bit less memory
    @Unique
    private byte supp$textWidth = (byte) (64 / 4);
    @Unique
    private byte supp$textHeight = (byte) (64 / 4);


    @Override
    public void supp$setDimensions(int texWidth, int texHeight) {
        this.supp$textWidth = (byte) (texWidth / 4);
        this.supp$textHeight = (byte) (texHeight / 4);
    }

    @Override
    public int supp$getTextHeight() {
        return this.supp$textHeight * 4;
    }

    @Override
    public int supp$getTextWidth() {
        return this.supp$textWidth * 4;
    }
}
