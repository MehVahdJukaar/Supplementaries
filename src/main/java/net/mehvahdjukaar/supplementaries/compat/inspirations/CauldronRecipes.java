package net.mehvahdjukaar.supplementaries.compat.inspirations;

import knightminer.inspirations.library.recipe.cauldron.special.DyeableCauldronRecipe;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.item.crafting.SpecialRecipeSerializer;

public class CauldronRecipes {

    public static void registerStuff() {

        ModRegistry.RECIPES.register("cauldron_blackboard",()->new DyeableCauldronRecipe.Serializer(CauldronBlackboardRecipe::new));

        ModRegistry.RECIPES.register("cauldron_flag_dye",()->new DyeableCauldronRecipe.Serializer(CauldronFlagDyeRecipe::new));

        ModRegistry.RECIPES.register("cauldron_flag_clear",()->new SpecialRecipeSerializer<>(CauldronFlagClearRecipe::new));
    }
}
