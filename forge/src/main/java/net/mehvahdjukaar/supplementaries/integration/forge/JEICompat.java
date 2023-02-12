package net.mehvahdjukaar.supplementaries.integration.forge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.common.items.crafting.ItemLoreRecipe;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEICompat implements IModPlugin {

    private static final ResourceLocation ID = Supplementaries.res("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        if (CommonConfigs.Utilities.TIPPED_SPIKES_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createTippedBambooSpikesRecipes());
        }
        if (CommonConfigs.Building.BLACKBOARD_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createBlackboardDuplicate());
        }
        if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createRopeArrowCreateRecipe());
            registry.addRecipes(RecipeTypes.CRAFTING, createRopeArrowAddRecipe());
        }
        if (CommonConfigs.Building.FLAG_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createFlagFromBanner());
        }
        if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createAntiqueMapRecipe());
        }
        if (CommonConfigs.Tools.BUBBLE_BLOWER_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createBubbleBlowerChargeRecipe());
        }

        registry.addRecipes(RecipeTypes.CRAFTING, createItemLoreRecipe());

        if (CommonConfigs.Utilities.SOAP_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, createSoapCleanRecipe());
            if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
                registry.addRecipes(RecipeTypes.CRAFTING, createAntiqueMapSoapRecipe());
            }
        }
        if (CommonConfigs.Utilities.PRESENT_ENABLED.get()) {
            registry.addRecipes(RecipeTypes.CRAFTING, makePresentCloringRecipes());
            registry.addRecipes(RecipeTypes.CRAFTING, makeTrappedPresentRecipes());
        }
    }


    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get(), SpikesSubtypeInterpreter.INSTANCE);
    }

    public static class SpikesSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
        public static final SpikesSubtypeInterpreter INSTANCE = new SpikesSubtypeInterpreter();

        private SpikesSubtypeInterpreter() {
        }

        public String apply(ItemStack itemStack, UidContext uidContext) {

            Potion potionType = PotionUtils.getPotion(itemStack);
            String potionTypeString = potionType.getName("");
            StringBuilder stringBuilder = new StringBuilder(potionTypeString);
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(itemStack);

            for (MobEffectInstance effect : effects) {
                stringBuilder.append(";").append(effect);
            }

            return stringBuilder.toString();
        }
    }

    public static List<CraftingRecipe> createAntiqueMapSoapRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.antique_map";

        ItemStack antique = new ItemStack(Items.FILLED_MAP);
        antique.setHoverName(Component.translatable("filled_map.antique"));

        Ingredient soap = Ingredient.of(new ItemStack(ModRegistry.SOAP.get()));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(antique), soap);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_antique_map_clean");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, new ItemStack(Items.FILLED_MAP), inputs);
        recipes.add(recipe);

        return recipes;
    }


    public static List<CraftingRecipe> createAntiqueMapRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.antique_map";

        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.setHoverName(Component.translatable("filled_map.antique"));

        Ingredient ink = Ingredient.of(new ItemStack(ModRegistry.ANTIQUE_INK.get()));
        Ingredient map = Ingredient.of(new ItemStack(Items.FILLED_MAP));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, map, ink);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_antique_map_create");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, stack, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static List<CraftingRecipe> createRopeArrowCreateRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.rope_arrow";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ropeArrow.setDamageValue(ropeArrow.getMaxDamage() - 4);

        Ingredient arrow = Ingredient.of(new ItemStack(Items.ARROW));
        Ingredient rope = Ingredient.of(ModTags.ROPES);//fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, arrow, rope, rope, rope, rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_rope_arrow_create");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static List<CraftingRecipe> createRopeArrowAddRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.rope_arrow";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ItemStack ropeArrow2 = ropeArrow.copy();
        ropeArrow2.setDamageValue(8);

        Ingredient arrow = Ingredient.of(ropeArrow2);
        Ingredient rope = Ingredient.of(ModTags.ROPES);//.fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, rope, rope, rope, rope, arrow, rope, rope, rope, rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_rope_arrow_add");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static List<CraftingRecipe> createSoapCleanRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.soap";

        for (String k : BlocksColorAPI.getBlockKeys()) {
            var n = BlocksColorAPI.getItemHolderSet(k);
            Item out = BlocksColorAPI.getColoredItem(k, null);
            if (n == null || out == null) continue;

            Ingredient ing = n.unwrap().map(Ingredient::of, l ->
                    Ingredient.of(l.stream().map(Holder::value).map(Item::getDefaultInstance)));
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                    ing, Ingredient.of(ModRegistry.SOAP.get()));

            ItemStack output = out.getDefaultInstance();
            ResourceLocation id = Supplementaries.res("soap_clean_" + k.replace(":", "_"));
            ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
            recipes.add(recipe);
        }

        return recipes;
    }

    public static List<CraftingRecipe> makeTrappedPresentRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.presents";

        for (DyeColor color : DyeColor.values()) {
            Ingredient baseShulkerIngredient = Ingredient.of(ModRegistry.PRESENTS.get(color).get());
            ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, Ingredient.of(Items.TRIPWIRE_HOOK));

            ResourceLocation id = Supplementaries.res("jei_trapped_present_" + color.getName());
            recipes.add(new ShapelessRecipe(id, group, output, inputs));
        }
        Ingredient baseShulkerIngredient = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(null).get().asItem().getDefaultInstance();

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, Ingredient.of(Items.TRIPWIRE_HOOK));

        ResourceLocation id = Supplementaries.res("jei_trapped_present");
        recipes.add(new ShapelessRecipe(id, group, output, inputs));
        return recipes;
    }

    public static List<CraftingRecipe> makePresentCloringRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.presents";
        Ingredient baseShulkerIngredient = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        for (DyeColor color : DyeColor.values()) {
            DyeItem dye = DyeItem.byColor(color);
            ItemStack output = ModRegistry.PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, Ingredient.of(dye));

            ResourceLocation id = Supplementaries.res("jei_present_" + color.getName());
            recipes.add(new ShapelessRecipe(id, group, output, inputs));
        }
        return recipes;
    }

    public static List<CraftingRecipe> createItemLoreRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.item_lore";

        ItemStack output = new ItemStack(Items.SLIME_BALL);
        ItemStack tag = new ItemStack(Items.NAME_TAG);
        var c = Component.literal("Ew sticky!");
        tag.setHoverName(c);
        ItemLoreRecipe.addLore(c, output);

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.SLIME_BALL), Ingredient.of(tag));
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_item_lore");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
        recipes.add(recipe);

        return recipes;
    }


    public static List<CraftingRecipe> createBubbleBlowerChargeRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.bubble_blower";

        ItemStack ropeArrow = new ItemStack(ModRegistry.BUBBLE_BLOWER.get());
        ItemStack empty = ropeArrow.copy();
        empty.setDamageValue(empty.getMaxDamage());

        Ingredient base = Ingredient.of(empty);
        Ingredient soap = Ingredient.of(ModRegistry.SOAP.get());//.fromStacks(new ItemStack(Registry.ROPE_ITEM.get()));
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, base, soap);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_bubble_blower_charge");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }


    public static List<CraftingRecipe> createTippedBambooSpikesRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.tipped_spikes";

        for (Potion potionType : ForgeRegistries.POTIONS.getValues()) {
            if (!potionType.getEffects().isEmpty() && BambooSpikesTippedItem.areEffectsValid(potionType.getEffects())) {
                recipes.add(makeSpikeRecipe(potionType, group));
            }
        }
        return recipes;
    }

    private static ShapelessRecipe makeSpikeRecipe(Potion potionType, String group) {
        ItemStack spikes = new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get());
        ItemStack lingeringPotion = PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potionType);
        Ingredient spikeIngredient = Ingredient.of(spikes);
        Ingredient potionIngredient = Ingredient.of(lingeringPotion);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, spikeIngredient, potionIngredient);
        ItemStack output = BambooSpikesTippedItem.makeSpikeItem(potionType);
        //ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei.bamboo_spikes." + output.getTranslationKey());
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, potionType.getName("jei.tipped_spikes."));
        return new ShapelessRecipe(id, group, output, inputs);
    }

    public static List<CraftingRecipe> createFlagFromBanner() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.flag_from_banner";

        for (DyeColor color : DyeColor.values()) {


            ItemStack banner = new ItemStack(BannerBlock.byColor(color).asItem());
            ItemStack fullFlag = new ItemStack(ModRegistry.FLAGS.get(color).get());

            ListTag list = new ListTag();
            CompoundTag compoundTag = new CompoundTag();

            compoundTag.putString("Pattern", "mojang");
            compoundTag.putInt("Color", color == DyeColor.WHITE ? DyeColor.BLACK.getId() : DyeColor.WHITE.getId());
            list.add(compoundTag);

            CompoundTag com = banner.getOrCreateTagElement("BlockEntityTag");
            com.put("Patterns", list);
            CompoundTag com2 = fullFlag.getOrCreateTagElement("BlockEntityTag");
            com2.put("Patterns", list);

            Ingredient emptyFlag = Ingredient.of(new ItemStack(ModRegistry.FLAGS.get(color).get()));
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyFlag, Ingredient.of(banner));
            ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_flag_from_banner");

            ShapelessRecipe recipe = new ShapelessRecipe(id, group, fullFlag, inputs);
            recipes.add(recipe);

        }

        return recipes;
    }

    public static List<CraftingRecipe> createBlackboardDuplicate() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.jei.blackboard_duplicate";

        ItemStack blackboard = new ItemStack(ModRegistry.BLACKBOARD_ITEM.get());
        CompoundTag com = new CompoundTag();
        byte[][] pixels = new byte[][]{
                {0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}};
        com.putLongArray("Pixels", BlackboardBlockTile.packPixels(pixels));
        blackboard.addTagElement("BlockEntityTag", com);

        Ingredient emptyBoard = Ingredient.of(new ItemStack(ModRegistry.BLACKBOARD_ITEM.get()));
        Ingredient fullBoard = Ingredient.of(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "jei_blackboard_duplicate");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, blackboard, inputs);
        recipes.add(recipe);

        return recipes;
    }

    public static ItemStack getSans() {
        ItemStack blackboard = new ItemStack(ModRegistry.BLACKBOARD_ITEM.get());
        CompoundTag com = new CompoundTag();

        byte[][] pixels = new byte[][]{
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0},
                {0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1},
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1},
                {0, 1, 0, 0, 0, 0, 0, 3, 0, 0, 0, 1, 1, 1, 0, 1},
                {0, 1, 0, 0, 0, 3, 3, 3, 3, 0, 0, 1, 0, 1, 0, 1},
                {0, 1, 0, 0, 3, 0, 3, 0, 3, 0, 0, 1, 1, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 3, 3, 3, 0, 1, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0}};
        com.putLongArray("Pixels", BlackboardBlockTile.packPixels(pixels));
        blackboard.addTagElement("BlockEntityTag", com);
        return blackboard;
    }


}