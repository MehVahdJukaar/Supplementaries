package net.mehvahdjukaar.supplementaries.common.utils;

import net.mehvahdjukaar.supplementaries.client.FlowerBoxModelsManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Deprecated(forRemoval = true)
public class FlowerPotHandler {

    //Use resource pack way + tag
    @Deprecated(forRemoval = true)
    public synchronized static void registerCustomFlower(Item item, ResourceLocation model) {
    }

    /**
     * Same as above but just used for the "simple" mode. Ideally this just contains tall flowers
     */
    @Deprecated(forRemoval = true)
    public synchronized static void registerCustomSimpleFlower(Item item, ResourceLocation model) {
    }
}
