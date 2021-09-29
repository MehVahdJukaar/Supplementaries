package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.client.renderer.model.ModelResourceLocation;

/**
 * implement this in your item class if it can provide a custom model to be displayed in flower boxes
 */
public interface IFlowerModelProvider {

    /**
     * @return resource location of the model to be used in flower boxes
     */
    ModelResourceLocation getModel();
}
