package net.mehvahdjukaar.supplementaries.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeRegistration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class SupplementariesJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Supplementaries.MOD_ID, "plugin_" + Supplementaries.MOD_ID);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(createTippedBambooSpikesRecipes(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createBlackboardDuplicate(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createBlackboardClear(),VanillaRecipeCategoryUid.CRAFTING);
    }

    public static List<IRecipe<?>> createTippedBambooSpikesRecipes() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.tipped_spikes";

        for (Potion potionType : ForgeRegistries.POTION_TYPES.getValues()) {
            if (potionType.getEffects().stream().map(EffectInstance::getPotion).anyMatch(effect -> effect.equals(Effects.POISON))) {
                ItemStack spikes = new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
                ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
                Ingredient spikeIngredient = Ingredient.fromStacks(spikes);
                Ingredient potionIngredient = Ingredient.fromStacks(lingeringPotion);
                NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, spikeIngredient, potionIngredient);
                ItemStack output = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get(), 1);
                ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_tipped_spikes");
                ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public static List<IRecipe<?>> createBlackboardDuplicate() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.blackboard_duplicate";

        ItemStack blackboard = new ItemStack(Registry.BLACKBOARD_ITEM.get());
        CompoundNBT com = new CompoundNBT();

        com.putByteArray("pixels_"+0, new byte[]{0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,0});
        com.putByteArray("pixels_"+1, new byte[]{0,0,0,1,1,0,0,1,1,1,1,0,0,0,1,0});
        com.putByteArray("pixels_"+2, new byte[]{0,0,1,0,0,1,0,0,0,1,0,1,0,0,1,0});
        com.putByteArray("pixels_"+3, new byte[]{0,0,1,0,0,1,1,1,0,1,1,1,0,0,1,0});
        com.putByteArray("pixels_"+4, new byte[]{0,0,1,0,0,1,1,0,1,1,0,1,0,0,1,0});
        com.putByteArray("pixels_"+5, new byte[]{0,0,1,0,0,0,0,0,1,1,1,1,0,0,1,0});
        com.putByteArray("pixels_"+6, new byte[]{0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0});
        com.putByteArray("pixels_"+7, new byte[]{0,0,1,0,0,1,0,0,0,1,1,1,0,1,0,0});
        com.putByteArray("pixels_"+8, new byte[]{0,0,1,0,0,1,1,0,0,1,0,1,0,1,0,0});
        com.putByteArray("pixels_"+9, new byte[]{0,0,1,0,0,1,1,0,1,0,1,0,0,1,0,0});
        com.putByteArray("pixels_"+10, new byte[]{0,0,1,0,0,0,0,0,1,1,0,0,1,0,0,0});
        com.putByteArray("pixels_"+11, new byte[]{0,0,1,0,0,0,1,0,1,0,0,0,1,0,0,0});
        com.putByteArray("pixels_"+12, new byte[]{0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0});
        com.putByteArray("pixels_"+13, new byte[]{0,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0});
        com.putByteArray("pixels_"+14, new byte[]{0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0});
        com.putByteArray("pixels_"+15, new byte[]{0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0});
        blackboard.setTagInfo("BlockEntityTag", com);

        Ingredient emptyBoard = Ingredient.fromStacks(new ItemStack(Registry.BLACKBOARD_ITEM.get()));
        Ingredient fullBoard = Ingredient.fromStacks(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_blackboard_duplicate");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, blackboard, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static List<IRecipe<?>> createBlackboardClear() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.blackboard_clear";

        ItemStack blackboard = new ItemStack(Registry.BLACKBOARD_ITEM.get());
        CompoundNBT com = new CompoundNBT();

        com.putByteArray("pixels_1", new byte[]{0,0,0,1,1,0,0,0,0,1,1,0,0,1,0,0});
        com.putByteArray("pixels_0", new byte[]{0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0});
        com.putByteArray("pixels_10", new byte[]{0,1,0,0,0,1,1,1,1,0,0,1,0,1,0,1});
        com.putByteArray("pixels_11", new byte[]{0,1,0,0,1,0,1,0,1,0,0,1,1,0,1,0});
        com.putByteArray("pixels_12", new byte[]{0,0,1,0,0,0,1,1,1,0,1,1,0,0,1,0});
        com.putByteArray("pixels_13", new byte[]{0,0,1,0,0,0,0,1,0,0,0,0,0,1,0,0});
        com.putByteArray("pixels_14", new byte[]{0,0,0,1,1,0,0,0,0,1,1,0,0,1,0,0});
        com.putByteArray("pixels_15", new byte[]{0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0});
        com.putByteArray("pixels_3", new byte[]{0,0,1,0,0,0,1,1,1,0,1,1,0,0,1,0});
        com.putByteArray("pixels_2", new byte[]{0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0});
        com.putByteArray("pixels_5", new byte[]{0,1,0,0,0,0,1,1,1,0,0,1,1,1,0,1});
        com.putByteArray("pixels_4", new byte[]{0,1,0,0,0,0,1,1,1,0,0,1,0,0,1,0});
        com.putByteArray("pixels_7", new byte[]{0,1,0,0,0,0,0,0,0,1,0,1,1,1,0,1});
        com.putByteArray("pixels_6", new byte[]{0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1});
        com.putByteArray("pixels_9", new byte[]{0,1,0,0,0,0,0,1,0,0,0,1,1,1,0,1});
        com.putByteArray("pixels_8", new byte[]{0,1,0,0,0,0,0,0,0,1,0,1,0,1,0,1});
        blackboard.setTagInfo("BlockEntityTag", com);

        Ingredient emptyBoard = Ingredient.fromStacks(new ItemStack(Items.WATER_BUCKET));
        Ingredient fullBoard = Ingredient.fromStacks(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_blackboard_clear");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, new ItemStack(Registry.BLACKBOARD_ITEM.get()), inputs);
        recipes.add(recipe);
        return recipes;

    }

}