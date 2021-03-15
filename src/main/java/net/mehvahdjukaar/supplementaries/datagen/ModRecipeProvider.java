package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.datagen.types.EnvironmentalWoodTypes;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
            makeHangingSignRecipe(wood,consumerIn);

        }
    }



    public static void makeConditionalRec(IFinishedRecipe r, IWoodType wood, Consumer<IFinishedRecipe> consumer,String name){


        ConditionalRecipe.builder().addCondition(new RecipeCondition(name, RecipeCondition.MY_FLAG))
                .addCondition(new ModLoadedCondition(wood.getNamespace()))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer,"supplementaries",name+"_"+wood.getRegName());
    }

    private static void makeSignPostRecipe(IWoodType wood, Consumer<IFinishedRecipe> consumer) {
        try{
            if(wood == EnvironmentalWoodTypes.WISTERIA){
                int a = 1;
            }
            Item plank = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getPlankRegName()));
            Item sign = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getSignRegName()));
            if (plank == null || plank == Items.AIR) return;
            if(sign!=null && sign != Items.AIR) {
                ShapelessRecipeBuilder.shapelessRecipe(Registry.SIGN_POST_ITEMS.get(wood).get(), 2)
                        .addIngredient(sign)
                        .setGroup(Registry.SIGN_POST_NAME)
                        .addCriterion("has_plank", InventoryChangeTrigger.Instance.forItems(plank))
                        //.build(consumer);
                        .build((s) -> makeConditionalRec(s, wood, consumer, Registry.SIGN_POST_NAME)); //
            }
            else{
                ShapedRecipeBuilder.shapedRecipe(Registry.SIGN_POST_ITEMS.get(wood).get(), 3)
                        .patternLine("   ")
                        .patternLine("222")
                        .patternLine(" 1 ")
                        .key('1', Items.STICK)
                        .key('2', plank)
                        .setGroup(Registry.SIGN_POST_NAME)
                        .addCriterion("has_plank", InventoryChangeTrigger.Instance.forItems(plank))
                        //.build(consumer);
                        .build((s) -> makeConditionalRec(s, wood, consumer,Registry.SIGN_POST_NAME)); //
            }
        }
        catch (Exception ignored){}
    }


    private static void makeHangingSignRecipe(IWoodType wood, Consumer<IFinishedRecipe> consumer) {

            Item plank = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getPlankRegName()));
            if (plank == null || plank == Items.AIR){
                return;
            }
            ShapedRecipeBuilder.shapedRecipe(Registry.HANGING_SIGNS.get(wood).get(), 2)
                    .patternLine("010")
                    .patternLine("222")
                    .patternLine("222")
                    .key('0', Items.IRON_NUGGET)
                    .key('1', Items.STICK)
                    .key('2', plank)
                    .setGroup(Registry.HANGING_SIGN_NAME)
                    .addCriterion("has_plank", InventoryChangeTrigger.Instance.forItems(plank))
                    //.build(consumer);
                    .build((s) -> makeConditionalRec(s, wood, consumer,Registry.HANGING_SIGN_NAME)); //


    }



}
