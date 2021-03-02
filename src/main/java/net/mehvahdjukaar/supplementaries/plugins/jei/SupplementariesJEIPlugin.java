package net.mehvahdjukaar.supplementaries.plugins.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
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
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get(), SpikesSubtypeInterpreter.INSTANCE);
    }

    public static class SpikesSubtypeInterpreter implements ISubtypeInterpreter {
        public static final SpikesSubtypeInterpreter INSTANCE = new SpikesSubtypeInterpreter();

        private SpikesSubtypeInterpreter() {
        }

        public String apply(ItemStack itemStack) {

            Potion potionType = PotionUtils.getPotionFromItem(itemStack);
            String potionTypeString = potionType.getNamePrefixed("");
            StringBuilder stringBuilder = new StringBuilder(potionTypeString);
            List<EffectInstance> effects = PotionUtils.getEffectsFromStack(itemStack);

            for (EffectInstance effect : effects) {
                stringBuilder.append(";").append(effect);
            }

            return stringBuilder.toString();
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(createTippedBambooSpikesRecipes(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createBlackboardDuplicate(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createBlackboardClear(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createRopeArrowCreateRecipe(),VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipes(createRopeArrowAddRecipe(),VanillaRecipeCategoryUid.CRAFTING);
    }

    public static List<IRecipe<?>> createRopeArrowCreateRecipe() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.rope_arrow";

        ItemStack ropeArrow = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
        ropeArrow.setDamage(ropeArrow.getMaxDamage()-4);

        Ingredient arrow = Ingredient.fromStacks(new ItemStack(Items.ARROW));
        Ingredient rope = Ingredient.fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, arrow, rope,rope,rope,rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_rope_arrow_create");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }
    public static List<IRecipe<?>> createRopeArrowAddRecipe() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.rope_arrow";

        ItemStack ropeArrow = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
        ItemStack ropeArrow2 = ropeArrow.copy();
        ropeArrow2.setDamage(8);

        Ingredient arrow = Ingredient.fromStacks(ropeArrow2);
        Ingredient rope = Ingredient.fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, rope,rope,rope,rope,arrow,rope,rope,rope,rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_rope_arrow_add");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static List<IRecipe<?>> createTippedBambooSpikesRecipes() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.tipped_spikes";

        for (Potion potionType : ForgeRegistries.POTION_TYPES.getValues()) {
           if (!potionType.getEffects().isEmpty()) {
                recipes.add(makeSpikeRecipe(potionType,group));
            }
        }
        return recipes;
    }

    private static ShapelessRecipe makeSpikeRecipe(Potion potionType, String group){
        ItemStack spikes = new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
        ItemStack lingeringPotion = PotionUtils.addPotionToItemStack(new ItemStack(Items.LINGERING_POTION), potionType);
        Ingredient spikeIngredient = Ingredient.fromStacks(spikes);
        Ingredient potionIngredient = Ingredient.fromStacks(lingeringPotion);
        NonNullList<Ingredient> inputs = NonNullList.from(Ingredient.EMPTY, spikeIngredient, potionIngredient);
        ItemStack output = BambooSpikesTippedItem.makeSpikeItem(potionType);
        //ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei.bamboo_spikes." + output.getTranslationKey());
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, potionType.getNamePrefixed("jei.tipped_spikes."));
        return new ShapelessRecipe(id, group, output, inputs);
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