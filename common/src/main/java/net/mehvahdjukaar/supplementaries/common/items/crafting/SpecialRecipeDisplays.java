package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.common.items.crafting.ItemLoreRecipe;
import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpecialRecipeDisplays {

    private static List<CraftingRecipe> createAntiqueMapSoapRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.antique_map_clean";

        ItemStack antique = new ItemStack(Items.FILLED_MAP);
        antique.setHoverName(Component.translatable("filled_map.antique"));
        AntiqueInkHelper.setAntiqueInk(antique, true);

        Ingredient soap = Ingredient.of(new ItemStack(ModRegistry.SOAP.get()));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(antique), soap);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "antique_map_clean_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, new ItemStack(Items.FILLED_MAP), inputs);
        recipes.add(recipe);

        return recipes;
    }


    private static List<CraftingRecipe> createAntiqueMapRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.antique_map";

        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.setHoverName(Component.translatable("filled_map.antique"));
        AntiqueInkHelper.setAntiqueInk(stack, true);

        Ingredient ink = Ingredient.of(new ItemStack(ModRegistry.ANTIQUE_INK.get()));
        Ingredient map = Ingredient.of(new ItemStack(Items.FILLED_MAP));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, map, ink);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "antique_map_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, stack, inputs);
        recipes.add(recipe);

        return recipes;
    }

    private static List<CraftingRecipe> createAntiqueBookRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.antique_book";

        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        AntiqueInkHelper.setAntiqueInk(stack, true);

        Ingredient ink = Ingredient.of(new ItemStack(ModRegistry.ANTIQUE_INK.get()));
        Ingredient map = Ingredient.of(new ItemStack(Items.WRITTEN_BOOK));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, map, ink);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "antique_book_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, stack, inputs);
        recipes.add(recipe);

        return recipes;
    }

    private static List<CraftingRecipe> createRopeArrowCreateRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.rope_arrow";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ropeArrow.setDamageValue(ropeArrow.getMaxDamage() - 4);

        Ingredient arrow = Ingredient.of(new ItemStack(Items.ARROW));
        Ingredient rope = Ingredient.of(ModTags.ROPES);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, arrow, rope, rope, rope, rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "rope_arrow_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }

    private static List<CraftingRecipe> createRopeArrowAddRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.rope_arrow_add";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ItemStack ropeArrow2 = ropeArrow.copy();
        ropeArrow2.setDamageValue(8);

        Ingredient arrow = Ingredient.of(ropeArrow2);
        Ingredient rope = Ingredient.of(ModTags.ROPES);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, rope, rope, rope, rope, arrow, rope, rope, rope, rope);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "rope_arrow_add_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }

    private static List<CraftingRecipe> createSoapCleanRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.soap";

        for (String k : BlocksColorAPI.getBlockKeys()) {
            var n = BlocksColorAPI.getItemHolderSet(k);
            Item out = BlocksColorAPI.getColoredItem(k, null);
            if (n == null || out == null) continue;

            Ingredient ing = n.unwrap().map(Ingredient::of, l ->
                    Ingredient.of(l.stream().map(Holder::value)
                            .filter(i -> !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(i)))
                            .map(Item::getDefaultInstance)));
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                    ing, Ingredient.of(ModRegistry.SOAP.get()));

            ItemStack output = out.getDefaultInstance();
            ResourceLocation id = Supplementaries.res("soap_clean_" + k.replace(":", "_"));
            ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
            recipes.add(recipe);
        }
        return recipes;
    }

    private static List<CraftingRecipe> makeTrappedPresentRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.trapped_presents";

        for (DyeColor color : DyeColor.values()) {
            Ingredient baseShulkerIngredient = Ingredient.of(ModRegistry.PRESENTS.get(color).get());
            ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, Ingredient.of(Items.TRIPWIRE_HOOK));

            ResourceLocation id = Supplementaries.res("trapped_present_" + color.getName()+"_display");
            recipes.add(new ShapelessRecipe(id, group, output, inputs));
        }
        Ingredient ingredients = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(null).get().asItem().getDefaultInstance();

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients, Ingredient.of(Items.TRIPWIRE_HOOK));

        ResourceLocation id = Supplementaries.res("trapped_present_display");
        recipes.add(new ShapelessRecipe(id, group, output, inputs));
        return recipes;
    }

    private static List<CraftingRecipe> makePresentCloringRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "presents";
        Ingredient ingredients = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        for (DyeColor color : DyeColor.values()) {
            DyeItem dye = DyeItem.byColor(color);
            ItemStack output = ModRegistry.PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients, Ingredient.of(dye));

            ResourceLocation id = Supplementaries.res("present_" + color.getName()+"_display");
            recipes.add(new ShapelessRecipe(id, group, output, inputs));
        }
        return recipes;
    }

    private static List<CraftingRecipe> createItemLoreRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "item_lore";

        ItemStack output = new ItemStack(Items.SLIME_BALL);
        ItemStack tag = new ItemStack(Items.NAME_TAG);
        var c = Component.literal("Ew sticky!");
        tag.setHoverName(c);
        ItemLoreRecipe.addLore(c, output);

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.SLIME_BALL), Ingredient.of(tag));
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "item_lore_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, output, inputs);
        recipes.add(recipe);

        return recipes;
    }


    private static List<CraftingRecipe> createBubbleBlowerChargeRecipe() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "bubble_blower";

        ItemStack ropeArrow = new ItemStack(ModRegistry.BUBBLE_BLOWER.get());
        ItemStack empty = ropeArrow.copy();
        empty.setDamageValue(empty.getMaxDamage());

        Ingredient base = Ingredient.of(empty);
        Ingredient soap = Ingredient.of(ModRegistry.SOAP.get());
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, base, soap);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "bubble_blower_charge_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, ropeArrow, inputs);
        recipes.add(recipe);

        return recipes;
    }


    private static List<CraftingRecipe> createTippedBambooSpikesRecipes() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "tipped_spikes";

        for (Potion potionType : Registry.POTION) {
            if (!potionType.getEffects().isEmpty() && BambooSpikesTippedItem.areEffectsValid(potionType.getEffects())) {
               // recipes.add(makeSpikeRecipe(potionType, group));
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
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, potionType.getName("tipped_spikes_display."));

        return new ShapelessRecipe(id, group, output, inputs);
    }

    private static List<CraftingRecipe> createFlagFromBanner() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "flag_from_banner";

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
            ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "flag_from_banner_display_"+color.getName());

            ShapelessRecipe recipe = new ShapelessRecipe(id, group, fullFlag, inputs);
            recipes.add(recipe);
        }

        return recipes;
    }

    private static List<CraftingRecipe> createBlackboardDuplicate() {
        List<CraftingRecipe> recipes = new ArrayList<>();
        String group = "supplementaries.blackboard_duplicate";

        ItemStack blackboard = getTroll();

        Ingredient emptyBoard = Ingredient.of(new ItemStack(ModRegistry.BLACKBOARD_ITEM.get()));
        Ingredient fullBoard = Ingredient.of(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = new ResourceLocation(Supplementaries.MOD_ID, "blackboard_duplicate_display");
        ShapelessRecipe recipe = new ShapelessRecipe(id, group, blackboard, inputs);
        recipes.add(recipe);

        return recipes;
    }

    private static ItemStack getTroll() {
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
        return blackboard;
    }

    private static ItemStack getSans() {
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


    public static void registerCraftingRecipes(Consumer<List<CraftingRecipe>> registry) {
        for (var c : RecipeBookCategories.AGGREGATE_CATEGORIES.get(RecipeBookCategories.CRAFTING_SEARCH)) {
            registerRecipes(c, registry);
        }
    }

    public static void registerRecipes(RecipeBookCategories category, Consumer<List<CraftingRecipe>> registry) {

        if (category == RecipeBookCategories.CRAFTING_MISC) {
            if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get()) {
                registry.accept(createTippedBambooSpikesRecipes());
            }
            if (CommonConfigs.Building.FLAG_ENABLED.get()) {
                registry.accept(createFlagFromBanner());
            }
            if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
                registry.accept(createAntiqueMapRecipe());
                registry.accept(createAntiqueBookRecipe());
            }
            registry.accept(createItemLoreRecipe());
            if (CommonConfigs.Functional.SOAP_ENABLED.get()) {
                registry.accept(createSoapCleanRecipe());
                if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
                    registry.accept(createAntiqueMapSoapRecipe());
                }
            }
            if (CommonConfigs.Functional.PRESENT_ENABLED.get()) {
                registry.accept(makePresentCloringRecipes());
                registry.accept(makeTrappedPresentRecipes());
            }
        } else if (category == RecipeBookCategories.CRAFTING_BUILDING_BLOCKS) {
            if (CommonConfigs.Building.BLACKBOARD_ENABLED.get()) {
                registry.accept(createBlackboardDuplicate());
            }
        } else if (category == RecipeBookCategories.CRAFTING_EQUIPMENT) {
            if (CommonConfigs.Tools.ROPE_ARROW_ENABLED.get()) {
                registry.accept(createRopeArrowCreateRecipe());
                registry.accept(createRopeArrowAddRecipe());
            }
            if (CommonConfigs.Tools.BUBBLE_BLOWER_ENABLED.get()) {
                registry.accept(createBubbleBlowerChargeRecipe());
            }
        }
    }
}
