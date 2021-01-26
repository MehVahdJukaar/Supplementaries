package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.mehvahdjukaar.supplementaries.setup.registration.Variants;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }
    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumerIn) {


        for (IWoodType wood : WoodTypes.TYPES.values()) {
            makeSignPostRecipe(wood, consumerIn);
        }
    }

    public static void makeConditionalRec(IFinishedRecipe r, IWoodType wood,Consumer<IFinishedRecipe> consumer){
        ConditionalRecipe.builder().addCondition(new ModLoadedCondition(wood.getNamespace())).addRecipe(r).build(consumer,"supplementaries",Variants.getSignPostName(wood));
    }

    private static void makeSignPostRecipe(IWoodType wood, Consumer<IFinishedRecipe> consumer) {

        ShapelessRecipeBuilder.shapelessRecipe(Registry.SIGN_POST_ITEMS.get(wood).get(), 2)
                .addIngredient(ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getNamespace()+":"+wood.toString()+"_sign")))
                .setGroup(Registry.SIGN_POST_NAME)
                .addCriterion("has_plank", InventoryChangeTrigger.Instance.forItems(ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getNamespace()+":"+wood.toString()+"_planks"))))
                .build((s)->makeConditionalRec(s,wood,consumer)); //
    }


}
