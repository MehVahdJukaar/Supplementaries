package net.mehvahdjukaar.supplementaries.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.ModTags;
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

            Potion potionType = PotionUtils.getPotion(itemStack);
            String potionTypeString = potionType.getName("");
            StringBuilder stringBuilder = new StringBuilder(potionTypeString);
            List<EffectInstance> effects = PotionUtils.getMobEffects(itemStack);

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

    //TODO: fix ropes
    public static List<IRecipe<?>> createRopeArrowCreateRecipe() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.rope_arrow";

        ItemStack ropeArrow = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
        ropeArrow.setDamageValue(ropeArrow.getMaxDamage()-4);

        Ingredient arrow = Ingredient.of(new ItemStack(Items.ARROW));
        Ingredient rope = Ingredient.of(ModTags.ROPES);//fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, arrow, rope,rope,rope,rope);
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
        ropeArrow2.setDamageValue(8);

        Ingredient arrow = Ingredient.of(ropeArrow2);
        Ingredient rope = Ingredient.of(ModTags.ROPES);//.fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, rope,rope,rope,rope,arrow,rope,rope,rope,rope);
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
        ItemStack lingeringPotion = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potionType);
        Ingredient spikeIngredient = Ingredient.of(spikes);
        Ingredient potionIngredient = Ingredient.of(lingeringPotion);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, spikeIngredient, potionIngredient);
        ItemStack output = BambooSpikesTippedItem.makeSpikeItem(potionType);
        //ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei.bamboo_spikes." + output.getTranslationKey());
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, potionType.getName("jei.tipped_spikes."));
        return new ShapelessRecipe(id, group, output, inputs);
    }

    public static List<IRecipe<?>> createBlackboardDuplicate() {
        List<IRecipe<?>> recipes = new ArrayList<>();
        String group = "supplementaries.jei.blackboard_duplicate";

        ItemStack blackboard = new ItemStack(Registry.BLACKBOARD_ITEM.get());
        CompoundNBT com = new CompoundNBT();
        byte[][] pixels = new byte[][]{
                {0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,0},
                {0,0,0,1,1,0,0,1,1,1,1,0,0,0,1,0},
                {0,0,1,0,0,1,0,0,0,1,0,1,0,0,1,0},
                {0,0,1,0,0,1,1,1,0,1,1,1,0,0,1,0},
                {0,0,1,0,0,1,1,0,1,1,0,1,0,0,1,0},
                {0,0,1,0,0,0,0,0,1,1,1,1,0,0,1,0},
                {0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0},
                {0,0,1,0,0,1,0,0,0,1,1,1,0,1,0,0},
                {0,0,1,0,0,1,1,0,0,1,0,1,0,1,0,0},
                {0,0,1,0,0,1,1,0,1,0,1,0,0,1,0,0},
                {0,0,1,0,0,0,0,0,1,1,0,0,1,0,0,0},
                {0,0,1,0,0,0,1,0,1,0,0,0,1,0,0,0},
                {0,0,1,0,0,0,1,1,0,0,0,1,0,0,0,0},
                {0,0,0,1,1,0,0,1,1,0,1,0,0,0,0,0},
                {0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0},
                {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0}};
        com.putLongArray("Pixels", BlackboardBlockTile.packPixels(pixels));
        blackboard.addTagElement("BlockEntityTag", com);

        Ingredient emptyBoard = Ingredient.of(new ItemStack(Registry.BLACKBOARD_ITEM.get()));
        Ingredient fullBoard = Ingredient.of(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyBoard, fullBoard);
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

        byte[][] pixels = new byte[][]{
                {0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0},
                {0,0,0,1,1,0,0,0,0,1,1,0,0,1,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0},
                {0,0,1,0,0,0,1,1,1,0,1,1,0,0,1,0},
                {0,1,0,0,0,0,1,1,1,0,0,1,0,0,1,0},
                {0,1,0,0,0,0,1,1,1,0,0,1,1,1,0,1},
                {0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1},
                {0,1,0,0,0,0,0,0,0,1,0,1,1,1,0,1},
                {0,1,0,0,0,0,0,0,0,1,0,1,0,1,0,1},
                {0,1,0,0,0,0,0,1,0,0,0,1,1,1,0,1},
                {0,1,0,0,0,1,1,1,1,0,0,1,0,1,0,1},
                {0,1,0,0,1,0,1,0,1,0,0,1,1,0,1,0},
                {0,0,1,0,0,0,1,1,1,0,1,1,0,0,1,0},
                {0,0,1,0,0,0,0,1,0,0,0,0,0,1,0,0},
                {0,0,0,1,1,0,0,0,0,1,1,0,0,1,0,0},
                {0,0,0,0,0,1,1,1,1,0,1,1,1,0,0,0}};
        com.putLongArray("Pixels", BlackboardBlockTile.packPixels(pixels));
        blackboard.addTagElement("BlockEntityTag", com);

        Ingredient emptyBoard = Ingredient.of(new ItemStack(Items.WATER_BUCKET));
        Ingredient fullBoard = Ingredient.of(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_blackboard_clear");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, new ItemStack(Registry.BLACKBOARD_ITEM.get()), inputs);
        recipes.add(recipe);
        return recipes;

    }

}