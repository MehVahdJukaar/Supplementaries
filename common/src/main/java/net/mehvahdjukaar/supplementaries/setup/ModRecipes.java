package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.moonlight.platform.registry.RegHelper;
import net.mehvahdjukaar.supplementaries.common.items.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import java.util.function.Supplier;

public class ModRecipes {


    //recipes
    public static final Supplier<RecipeSerializer<?>> BLACKBOARD_DUPLICATE_RECIPE = RegHelper.registerRecipeSerializer("blackboard_duplicate", () ->
            new SimpleRecipeSerializer<>(BlackboardDuplicateRecipe::new));
    public static final Supplier<RecipeSerializer<?>> BAMBOO_SPIKES_TIPPED_RECIPE = RegHelper.registerRecipeSerializer("bamboo_spikes_tipped", () ->
            new SimpleRecipeSerializer<>(TippedBambooSpikesRecipe::new));
    public static final Supplier<RecipeSerializer<?>> ROPE_ARROW_CREATE_RECIPE = RegHelper.registerRecipeSerializer("rope_arrow_create", () ->
            new SimpleRecipeSerializer<>(RopeArrowCreateRecipe::new));
    public static final Supplier<RecipeSerializer<?>> ROPE_ARROW_ADD_RECIPE = RegHelper.registerRecipeSerializer("rope_arrow_add", () ->
            new SimpleRecipeSerializer<>(RopeArrowAddRecipe::new));
    public static final Supplier<RecipeSerializer<?>> BUBBLE_BLOWER_REPAIR_RECIPE = RegHelper.registerRecipeSerializer("bubble_blower_charge", () ->
            new SimpleRecipeSerializer<>(RepairBubbleBlowerRecipe::new));
    public static final Supplier<RecipeSerializer<?>> FLAG_FROM_BANNER_RECIPE = RegHelper.registerRecipeSerializer("flag_from_banner", () ->
            new SimpleRecipeSerializer<>(FlagFromBannerRecipe::new));
    public static final Supplier<RecipeSerializer<?>> TREASURE_MAP_RECIPE = RegHelper.registerRecipeSerializer("treasure_map", () ->
            new SimpleRecipeSerializer<>(WeatheredMapRecipe::new));
    public static final Supplier<RecipeSerializer<?>> SOAP_CLEARING_RECIPE = RegHelper.registerRecipeSerializer("soap_clearing", () ->
            new SimpleRecipeSerializer<>(SoapClearRecipe::new));
    public static final Supplier<RecipeSerializer<?>> PRESENT_DYE_RECIPE = RegHelper.registerRecipeSerializer("present_dye", () ->
            new SimpleRecipeSerializer<>(PresentDyeRecipe::new));
    public static final Supplier<RecipeSerializer<?>> TRAPPED_PRESENT_RECIPE = RegHelper.registerRecipeSerializer("trapped_present", () ->
            new SimpleRecipeSerializer<>(TrappedPresentRecipe::new));
}
