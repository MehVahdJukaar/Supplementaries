package net.mehvahdjukaar.supplementaries.datagen.dynamicpack;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.selene.resourcepack.DynamicDataPack;
import net.mehvahdjukaar.selene.resourcepack.DynamicDataPack.TagType;
import net.mehvahdjukaar.selene.util.WoodSetType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerDynamicResourcesHandler {

    public static final DynamicDataPack DYNAMIC_DATA_PACK =
            new DynamicDataPack(Supplementaries.res("virtual_resourcepack"));

    //fired on mod setup
    public static void init() {
        long mills = System.currentTimeMillis();

        //registers the pack
        MinecraftForge.EVENT_BUS.addListener(DYNAMIC_DATA_PACK::register);

        //hanging signs
        {
            List<ResourceLocation> signs = new ArrayList<>();

            //loot table
            for (var r : ModRegistry.HANGING_SIGNS.values()) {
                DYNAMIC_DATA_PACK.addSimpleBlockLootTable(r);
                signs.add(r.getRegistryName());

                makeHangingSignRecipe(r.woodType, DYNAMIC_DATA_PACK::addRecipe);
            }
            //tag
            DYNAMIC_DATA_PACK.addTag(Supplementaries.res("hanging_signs"), signs, TagType.BLOCKS, TagType.ITEMS);
        }
        //sing posts
        {

            List<ResourceLocation> posts = new ArrayList<>();

            //recipes
            for (var r : ModRegistry.SIGN_POST_ITEMS.values()) {
                posts.add(r.getRegistryName());

                makeSignPostRecipe(r.woodType, DYNAMIC_DATA_PACK::addRecipe);
            }

            //tag
            DYNAMIC_DATA_PACK.addTag(Supplementaries.res("sign_posts"), posts, TagType.ITEMS);
        }

        Supplementaries.LOGGER.info("Generated runtime data resources in: {} seconds", (System.currentTimeMillis() - mills) / 1000f);
    }

    public static void makeConditionalRec(FinishedRecipe r, Consumer<FinishedRecipe> consumer, String name) {
        ConditionalRecipe.builder()
                .addCondition(new OptionalRecipeCondition(name))
                .addRecipe(r)
                .build(consumer, "supplementaries", name);
    }

    public static void makeConditionalWoodRec(FinishedRecipe r, WoodSetType wood, Consumer<FinishedRecipe> consumer, String name) {

        ConditionalRecipe.builder().addCondition(new OptionalRecipeCondition(name))
                .addCondition(new ModLoadedCondition(wood.getNamespace()))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer, "supplementaries", name + "_" + wood.getAppendableId());
    }

    private static ResourceLocation getPlankRegName(WoodSetType wood) {
        return new ResourceLocation(wood.getNamespace(), wood.getWoodName() + "_planks");
    }

    private static ResourceLocation getSignRegName(WoodSetType wood) {
        return new ResourceLocation(wood.getNamespace(), wood.getWoodName() + "_sign");
    }

    private static void makeSignPostRecipe(WoodSetType wood, Consumer<FinishedRecipe> consumer) {
        try {
            Item plank = wood.plankBlock.asItem();
            Preconditions.checkArgument(plank != Items.AIR);

            Item sign = ForgeRegistries.ITEMS.getValue(getSignRegName(wood));

            if (sign != null && sign != Items.AIR) {
                ShapelessRecipeBuilder.shapeless(ModRegistry.SIGN_POST_ITEMS.get(wood), 2)
                        .requires(sign)
                        .group(ModRegistry.SIGN_POST_NAME)
                        .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                        //.build(consumer);
                        .save((s) -> makeConditionalWoodRec(s, wood, consumer, ModRegistry.SIGN_POST_NAME)); //
            } else {
                ShapedRecipeBuilder.shaped(ModRegistry.SIGN_POST_ITEMS.get(wood), 3)
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
        } catch (Exception ignored) {
            Supplementaries.LOGGER.error("Failed to generate sign post recipe for wood type {}", wood);
        }
    }

    private static void makeHangingSignRecipe(WoodSetType wood, Consumer<FinishedRecipe> consumer) {
        try {
            Item plank = wood.plankBlock.asItem();
            Preconditions.checkArgument(plank != Items.AIR);
            ShapedRecipeBuilder.shaped(ModRegistry.HANGING_SIGNS.get(wood), 2)
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

        } catch (Exception ignored) {
            Supplementaries.LOGGER.error("Failed to generate hanging sign recipe for wood type {}", wood);
        }
    }


}
