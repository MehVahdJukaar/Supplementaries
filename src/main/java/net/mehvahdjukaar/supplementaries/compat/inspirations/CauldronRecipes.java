package net.mehvahdjukaar.supplementaries.compat.inspirations;

import knightminer.inspirations.library.recipe.cauldron.special.DyeableCauldronRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class CauldronRecipes {

    public static void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event){
        IForgeRegistry<IRecipeSerializer<?>> reg = event.getRegistry();

        reg.register(new DyeableCauldronRecipe.Serializer(CauldronBlackboardRecipe::new)
                .setRegistryName("cauldron_blackboard"));

        reg.register(new DyeableCauldronRecipe.Serializer(CauldronFlagDyeRecipe::new)
                .setRegistryName("cauldron_flag_dye"));

        reg.register(new SpecialRecipeSerializer<>(CauldronFlagClearRecipe::new)
                .setRegistryName("cauldron_flag_clear"));

    }
}
