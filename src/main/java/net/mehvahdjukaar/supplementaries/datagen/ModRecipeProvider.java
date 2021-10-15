package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }
    @Override
    protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumerIn) {

        for(DyeColor color : DyeColor.values()){
            //makeFlagRecipe(color,consumerIn);
        }

        for (IWoodType wood : WoodTypes.TYPES.values()) {
            makeSignPostRecipe(wood, consumerIn);
            makeHangingSignRecipe(wood,consumerIn);
        }


    }


    public static void makeConditionalRec(FinishedRecipe r, Consumer<FinishedRecipe> consumer, String name){


        ConditionalRecipe.builder()
                .addCondition(new RecipeCondition(name, RecipeCondition.MY_FLAG))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer,"supplementaries",name);
    }

    public static void makeConditionalWoodRec(FinishedRecipe r, IWoodType wood, Consumer<FinishedRecipe> consumer, String name){


        ConditionalRecipe.builder().addCondition(new RecipeCondition(name, RecipeCondition.MY_FLAG))
                .addCondition(new ModLoadedCondition(wood.getNamespace()))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer,"supplementaries",name+"_"+wood.getRegName());
    }

    private static void makeSignPostRecipe(IWoodType wood, Consumer<FinishedRecipe> consumer) {
        try{
            Item plank = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getPlankRegName()));
            Item sign = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getSignRegName()));
            if (plank == null || plank == Items.AIR) return;
            if(sign!=null && sign != Items.AIR) {
                ShapelessRecipeBuilder.shapeless(ModRegistry.SIGN_POST_ITEMS.get(wood).get(), 2)
                        .requires(sign)
                        .group(ModRegistry.SIGN_POST_NAME)
                        .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                        //.build(consumer);
                        .save((s) -> makeConditionalWoodRec(s, wood, consumer, ModRegistry.SIGN_POST_NAME)); //
            }
            else{
                ShapedRecipeBuilder.shaped(ModRegistry.SIGN_POST_ITEMS.get(wood).get(), 3)
                        .pattern("   ")
                        .pattern("222")
                        .pattern(" 1 ")
                        .define('1', Items.STICK)
                        .define('2', plank)
                        .group(ModRegistry.SIGN_POST_NAME)
                        .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                        //.build(consumer);
                        .save((s) -> makeConditionalWoodRec(s, wood, consumer, ModRegistry.SIGN_POST_NAME)); //
            }
        }
        catch (Exception ignored){}
    }


    private static void makeHangingSignRecipe(IWoodType wood, Consumer<FinishedRecipe> consumer) {

            Item plank = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wood.getPlankRegName()));
            if (plank == null || plank == Items.AIR){
                return;
            }
            ShapedRecipeBuilder.shaped(ModRegistry.HANGING_SIGNS.get(wood).get(), 2)
                    .pattern("010")
                    .pattern("222")
                    .pattern("222")
                    .define('0', Items.IRON_NUGGET)
                    .define('1', Items.STICK)
                    .define('2', plank)
                    .group(ModRegistry.HANGING_SIGN_NAME)
                    .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                    //.build(consumer);
                    .save((s) -> makeConditionalWoodRec(s, wood, consumer, ModRegistry.HANGING_SIGN_NAME)); //


    }

    private static void makeFlagRecipe(DyeColor color, Consumer<FinishedRecipe> consumer) {

        Item wool = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", color.name().toLowerCase()+"_wool"));
        if (wool == null || wool == Items.AIR){
            return;
        }
        ShapedRecipeBuilder.shaped(ModRegistry.FLAGS.get(color).get(), 1)
                .pattern("222")
                .pattern("222")
                .pattern("1  ")
                .define('1', Items.STICK)
                .define('2', wool)
                .group(ModRegistry.FLAG_NAME)
                .unlockedBy("has_wool", InventoryChangeTrigger.TriggerInstance.hasItems(wool))
                //.build(consumer);
                .save((s) -> makeConditionalRec(s, consumer, ModRegistry.HANGING_SIGN_NAME+"_"+color.getName())); //


    }


}
