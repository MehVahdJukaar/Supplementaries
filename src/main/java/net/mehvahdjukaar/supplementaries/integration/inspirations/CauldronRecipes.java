package net.mehvahdjukaar.supplementaries.integration.inspirations;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class CauldronRecipes {

    public static void registerRecipes(RegistryEvent.Register<RecipeSerializer<?>> event) {
        IForgeRegistry<RecipeSerializer<?>> reg = event.getRegistry();
        /*
        reg.register(new DyeableCauldronRecipe.Serializer(CauldronBlackboardRecipe::new)
                .setRegistryName("cauldron_blackboard"));

        reg.register(new DyeableCauldronRecipe.Serializer(CauldronFlagDyeRecipe::new)
                .setRegistryName("cauldron_flag_dye"));

        reg.register(new SpecialRecipeSerializer<>(CauldronFlagClearRecipe::new)
                .setRegistryName("cauldron_flag_clear"));
        */
    }
}
