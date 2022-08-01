package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * implement this in your item class if it can provide a custom model to be displayed in flower boxes
 * Call FlowerPotHandler::registerCustomFlower if this item is not something that can already go in a vanilla flower pot
 */
public interface IFlowerModelProvider {

    /**
     * @return resource location of the model to be used in flower boxes
     */
    ResourceLocation getModel();
}
