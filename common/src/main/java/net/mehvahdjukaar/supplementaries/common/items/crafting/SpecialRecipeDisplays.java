package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.common.items.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.common.utils.SoapWashableHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.*;
import java.util.function.Consumer;

public class SpecialRecipeDisplays {


    private static List<RecipeHolder<? extends CraftingRecipe>> createAntiqueMapRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.antique_map";

        ItemStack stack = new ItemStack(Items.FILLED_MAP);
        stack.set(DataComponents.ITEM_NAME, Component.translatable("filled_map.antique"));
        AntiqueInkItem.setAntiqueInk(stack, true);

        Ingredient ink = Ingredient.of(new ItemStack(ModRegistry.ANTIQUE_INK.get()));
        Ingredient map = Ingredient.of(new ItemStack(Items.FILLED_MAP));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, map, ink);
        ResourceLocation id = Supplementaries.res("antique_map_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, stack, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createAntiqueBookRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.antique_book";

        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        AntiqueInkItem.setAntiqueInk(stack, true);

        Ingredient ink = Ingredient.of(new ItemStack(ModRegistry.ANTIQUE_INK.get()));
        Ingredient map = Ingredient.of(new ItemStack(Items.WRITTEN_BOOK));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, map, ink);
        ResourceLocation id = Supplementaries.res("antique_book_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, stack, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createRopeArrowCreateRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.rope_arrow";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ropeArrow.setDamageValue(ropeArrow.getMaxDamage() - 4);

        Ingredient arrow = Ingredient.of(new ItemStack(Items.ARROW));
        Ingredient rope = Ingredient.of(ModTags.ROPES);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, arrow, rope, rope, rope, rope);
        ResourceLocation id = Supplementaries.res("rope_arrow_create_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.EQUIPMENT, ropeArrow, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createRopeArrowAddRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.rope_arrow_add";

        ItemStack ropeArrow = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        ItemStack ropeArrow2 = ropeArrow.copy();
        ropeArrow2.setDamageValue(8);

        Ingredient arrow = Ingredient.of(ropeArrow2);
        Ingredient rope = Ingredient.of(ModTags.ROPES);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, rope, rope, rope, rope, arrow, rope, rope, rope, rope);
        ResourceLocation id = Supplementaries.res("rope_arrow_add_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.EQUIPMENT, ropeArrow, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createSoapCleanRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.soap";

        for (String k : BlocksColorAPI.getBlockKeys()) {
            Item out = BlocksColorAPI.getColoredItem(k, null);
            if (out != null && SoapWashableHelper.canCleanColor(out)) {
                var n = BlocksColorAPI.getItemHolderSet(k);
                if (n == null) continue;

                Ingredient ing = n.unwrap().map(Ingredient::of, l ->
                        Ingredient.of(l.stream().map(Holder::value)
                                .map(Item::getDefaultInstance)));
                NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                        ing, Ingredient.of(ModRegistry.SOAP.get()));

                ItemStack output = out.getDefaultInstance();
                ResourceLocation id = Supplementaries.res("soap_clean_" + k.replace(":", "_"));
                ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
                recipes.add(new RecipeHolder<>(id, recipe));
            }
        }
        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> makeTrappedPresentRecipes() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.trapped_presents";

        for (DyeColor color : DyeColor.values()) {
            Ingredient baseShulkerIngredient = Ingredient.of(ModRegistry.PRESENTS.get(color).get());
            ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, baseShulkerIngredient, Ingredient.of(Items.TRIPWIRE_HOOK));

            ResourceLocation id = Supplementaries.res("trapped_present_" + color.getName() + "_display");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, output, inputs);
            recipes.add(new RecipeHolder<>(id, shapelessRecipe));
        }
        Ingredient ingredients = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        ItemStack output = ModRegistry.TRAPPED_PRESENTS.get(null).get().asItem().getDefaultInstance();

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients, Ingredient.of(Items.TRIPWIRE_HOOK));

        ResourceLocation id = Supplementaries.res("trapped_present_display");
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, output, inputs);
        recipes.add(new RecipeHolder<>(id, shapelessRecipe));
        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> makePresentColoringRecipes() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "presents";
        Ingredient ingredients = Ingredient.of(ModRegistry.PRESENTS.get(null).get());
        for (DyeColor color : DyeColor.values()) {
            DyeItem dye = DyeItem.byColor(color);
            ItemStack output = ModRegistry.PRESENTS.get(color).get().asItem().getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients, Ingredient.of(dye));

            ResourceLocation id = Supplementaries.res("present_" + color.getName() + "_display");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, output, inputs);
            recipes.add(new RecipeHolder<>(id, shapelessRecipe));
        }
        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> makeSackColoringRecipes() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "sacks";
        Ingredient ingredients = Ingredient.of(ModRegistry.SACK.get());
        for (DyeColor color : DyeColor.values()) {
            DyeItem dye = DyeItem.byColor(color);
            ItemStack output = BlocksColorAPI.changeColor(ModRegistry.SACK.get().asItem(), color).getDefaultInstance();

            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, ingredients, Ingredient.of(dye));

            ResourceLocation id = Supplementaries.res("sack_" + color.getName() + "_display");
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, output, inputs);
            recipes.add(new RecipeHolder<>(id, shapelessRecipe));
        }
        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createItemLoreRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();

        String group = "item_lore";

        ItemStack output = new ItemStack(Items.SLIME_BALL);
        ItemStack tag = new ItemStack(Items.NAME_TAG);
        var c = Component.literal("Ew sticky!");
        tag.set(DataComponents.ITEM_NAME, c);
        output.set(DataComponents.LORE, new ItemLore(List.of(c)));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.SLIME_BALL), Ingredient.of(tag));
        ResourceLocation id = Supplementaries.res("item_lore_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createRemoveLoreRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "remove_lore";

        ItemStack output = new ItemStack(Items.COCOA_BEANS);
        ItemStack soap = new ItemStack(ModRegistry.SOAP.get());
        var c = Component.literal("Stinky!");
        ItemStack input = output.copy();
        input.set(DataComponents.LORE, new ItemLore(List.of(c)));

        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(input), Ingredient.of(soap));
        ResourceLocation id = Supplementaries.res("remove_lore_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createSusRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();

        String group = "sus_crafting";
        Map<Block,Block> blocks = new HashMap<>();
        if (CommonConfigs.Tweaks.SUS_RECIPES.get()) {
            blocks.put(Blocks.SAND, Blocks.SUSPICIOUS_SAND);
            blocks.put(Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL);
            if (CommonConfigs.Building.GRAVEL_BRICKS_ENABLED.get()) {
                blocks.put(ModRegistry.GRAVEL_BRICKS.get(), ModRegistry.SUS_GRAVEL_BRICKS.get());
            }
        }

        ItemStack content = Items.GOLD_INGOT.getDefaultInstance();
        content.set(DataComponents.ITEM_NAME, Component.literal("Precious Item"));
        for (var e : blocks.entrySet()) {
            ItemStack output = new ItemStack(e.getValue());
            ItemStack input = new ItemStack(e.getKey());
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, Ingredient.of(input), Ingredient.of(content));
            ResourceLocation id = Supplementaries.res(Utils.getID(output.getItem()).getPath());
            ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, output, inputs);
            recipes.add(new RecipeHolder<>(id, recipe));
        }
        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createBubbleBlowerChargeRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "bubble_blower";

        ItemStack ropeArrow = new ItemStack(ModRegistry.BUBBLE_BLOWER.get());
        ItemStack empty = ropeArrow.copy();
        empty.setDamageValue(empty.getMaxDamage());

        Ingredient base = Ingredient.of(empty);
        Ingredient soap = Ingredient.of(ModRegistry.SOAP.get());
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, base, soap);
        ResourceLocation id = Supplementaries.res("bubble_blower_charge_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, ropeArrow, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createSafeRecipe() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "safe";

        ItemStack safe = new ItemStack(ModRegistry.SAFE.get());

        Ingredient base = Ingredient.of(ModTags.SHULKER_BOXES);
        Ingredient ingot = Ingredient.of(Items.NETHERITE_INGOT);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, base, ingot);

        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, safe, inputs);
        recipes.add(new RecipeHolder<>(Supplementaries.res("safe_display"), recipe));

        return recipes;
    }


    private static List<RecipeHolder<? extends CraftingRecipe>> createTippedBambooSpikesRecipes() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "tipped_spikes";

        for (var potionType : BuiltInRegistries.POTION.holders().toList()) {
            if (!potionType.value().getEffects().isEmpty() && BambooSpikesTippedItem.isPotionValid(
                    new PotionContents(potionType))) {
                recipes.add(makeSpikeRecipe(potionType, group));
            }
        }
        return recipes;
    }

    private static RecipeHolder<ShapelessRecipe> makeSpikeRecipe(Holder<Potion> potionType, String group) {
        ItemStack spikes = new ItemStack(ModRegistry.BAMBOO_SPIKES.get());
        ItemStack lingeringPotion = PotionContents.createItemStack(Items.LINGERING_POTION, potionType);
        Ingredient spikeIngredient = Ingredient.of(spikes);
        Ingredient potionIngredient = Ingredient.of(lingeringPotion);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, spikeIngredient, potionIngredient);
        ItemStack output = BambooSpikesTippedItem.createItemStack(potionType);
        ResourceLocation id = Supplementaries.res(Potion.getName(Optional.of(potionType), "tipped_spikes_display."));

        var recipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, output, inputs);
        return new RecipeHolder<>(id, recipe);
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createFlagFromBanner() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "flag_from_banner";

        for (DyeColor color : DyeColor.values()) {


            ItemStack banner = new ItemStack(BannerBlock.byColor(color).asItem());
            ItemStack fullFlag = new ItemStack(ModRegistry.FLAGS.get(color).get());

            BannerPatternLayers patterns = new BannerPatternLayers(
                    List.of(new BannerPatternLayers.Layer(HolderReference.of(BannerPatterns.BASE).getHolderUnsafe(),
                            color == DyeColor.WHITE ? DyeColor.BLACK : DyeColor.WHITE))
            );

            banner.set(DataComponents.BANNER_PATTERNS, patterns);
            fullFlag.set(DataComponents.BANNER_PATTERNS, patterns);

            Ingredient emptyFlag = Ingredient.of(new ItemStack(ModRegistry.FLAGS.get(color).get()));
            NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyFlag, Ingredient.of(banner));
            ResourceLocation id = Supplementaries.res("flag_from_banner_display_" + color.getName());

            ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, fullFlag, inputs);
            recipes.add(new RecipeHolder<>(id, recipe));
        }

        return recipes;
    }

    private static List<RecipeHolder<? extends CraftingRecipe>> createBlackboardDuplicate() {
        List<RecipeHolder<? extends CraftingRecipe>> recipes = new ArrayList<>();
        String group = "supplementaries.blackboard_duplicate";

        ItemStack blackboard = getTroll();

        Ingredient emptyBoard = Ingredient.of(new ItemStack(ModRegistry.BLACKBOARD_ITEM.get()));
        Ingredient fullBoard = Ingredient.of(blackboard);
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY, emptyBoard, fullBoard);
        ResourceLocation id = Supplementaries.res("blackboard_duplicate_display");
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.BUILDING, blackboard, inputs);
        recipes.add(new RecipeHolder<>(id, recipe));

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
        blackboard.set(ModComponents.BLACKBOARD.get(), new BlackboardData(pixels, false, false));
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
        blackboard.set(ModComponents.BLACKBOARD.get(), new BlackboardData(pixels, false, false));
        return blackboard;
    }


    public static void registerCraftingRecipes(Consumer<List<RecipeHolder<? extends CraftingRecipe>>> registry) {
        for (var c : RecipeBookCategories.AGGREGATE_CATEGORIES.get(RecipeBookCategories.CRAFTING_SEARCH)) {
            registerRecipes(c, registry);
        }
    }

    public static void registerRecipes(RecipeBookCategories category, Consumer<List<RecipeHolder<? extends CraftingRecipe>>> registry) {

        if (category == RecipeBookCategories.CRAFTING_MISC) {
            if (CommonConfigs.Functional.TIPPED_SPIKES_ENABLED.get()) {
                registry.accept(createTippedBambooSpikesRecipes());
            }
            if (CommonConfigs.Building.FLAG_ENABLED.get()) {
                registry.accept(createFlagFromBanner());
            }
            if (CommonConfigs.Functional.SAFE_ENABLED.get()) {
                registry.accept(createSafeRecipe());
            }
            if (CommonConfigs.Tools.ANTIQUE_INK_ENABLED.get()) {
                registry.accept(createAntiqueMapRecipe());
                registry.accept(createAntiqueBookRecipe());
            }
            if (CommonConfigs.Functional.SACK_ENABLED.get() && CompatHandler.SUPPSQUARED) {
                registry.accept(makeSackColoringRecipes());
            }
            if (CommonConfigs.Tweaks.ITEM_LORE.get()) {
                registry.accept(createItemLoreRecipe());
                registry.accept(createRemoveLoreRecipe());
            }
            if (CommonConfigs.Functional.SOAP_ENABLED.get()) {
                registry.accept(createSoapCleanRecipe());
            }
            if (CommonConfigs.Functional.PRESENT_ENABLED.get()) {
                registry.accept(makePresentColoringRecipes());
                if (CommonConfigs.Functional.TRAPPED_PRESENT_ENABLED.get()) {
                    registry.accept(makeTrappedPresentRecipes());
                }
            }
            registry.accept(createSusRecipe());

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
