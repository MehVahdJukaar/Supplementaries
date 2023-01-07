package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModRecipes {


    public static void init() {
    }

    //recipes
    public static final Supplier<RecipeSerializer<BlackboardDuplicateRecipe>> BLACKBOARD_DUPLICATE = reg(
            "blackboard_duplicate", BlackboardDuplicateRecipe::new);
    public static final Supplier<RecipeSerializer<TippedBambooSpikesRecipe>> BAMBOO_SPIKES_TIPPED = reg(
            "bamboo_spikes_tipped", TippedBambooSpikesRecipe::new);
    public static final Supplier<RecipeSerializer<RopeArrowCreateRecipe>> ROPE_ARROW_CREATE = reg(
            "rope_arrow_create", RopeArrowCreateRecipe::new);
    public static final Supplier<RecipeSerializer<RopeArrowAddRecipe>> ROPE_ARROW_ADD = reg(
            "rope_arrow_add", RopeArrowAddRecipe::new);
    public static final Supplier<RecipeSerializer<RepairBubbleBlowerRecipe>> BUBBLE_BLOWER_REPAIR = reg(
            "bubble_blower_charge", RepairBubbleBlowerRecipe::new);
    public static final Supplier<RecipeSerializer<FlagFromBannerRecipe>> FLAG_FROM_BANNER = reg(
            "flag_from_banner", FlagFromBannerRecipe::new);
    public static final Supplier<RecipeSerializer<WeatheredMapRecipe>> ANTIQUE_MAP = reg(
            "weathered_map", WeatheredMapRecipe::new);
    public static final Supplier<RecipeSerializer<SoapClearRecipe>> SOAP_CLEARING = reg(
            "soap_clearing", SoapClearRecipe::new);
    public static final Supplier<RecipeSerializer<PresentDyeRecipe>> PRESENT_DYE = reg(
            "present_dye", PresentDyeRecipe::new);
    public static final Supplier<RecipeSerializer<TrappedPresentRecipe>> TRAPPED_PRESENT = reg(
            "trapped_present", TrappedPresentRecipe::new);
    public static final Supplier<RecipeSerializer<ItemLoreRecipe>> ITEM_LORE = reg(
            "item_lore", ItemLoreRecipe::new);


    private static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> reg(String name, Function<ResourceLocation, T> factory) {
        return RegHelper.registerRecipeSerializer(Supplementaries.res(name), () -> new SimpleRecipeSerializer<>(factory));
    }
}
