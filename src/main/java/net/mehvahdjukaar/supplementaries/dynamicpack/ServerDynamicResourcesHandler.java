package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.resourcepack.DynamicDataPack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.RegistryConstants;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerDynamicResourcesHandler {

    public static final DynamicDataPack DYNAMIC_DATA_PACK =
            new DynamicDataPack(Supplementaries.res("virtual_resourcepack"));

    //fired on mod setup
    public static void registerBus(IEventBus forgeBus) {
        DYNAMIC_DATA_PACK.registerPack(forgeBus);
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(ServerDynamicResourcesHandler::generateAssets);
        //TODO: fix tags not working
        DYNAMIC_DATA_PACK.generateDebugResources = !FMLLoader.isProduction();
    }

    public static void generateAssets(final FMLCommonSetupEvent event) {

        Stopwatch watch = Stopwatch.createStarted();

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
            DYNAMIC_DATA_PACK.addTag(Supplementaries.res("hanging_signs"), signs, Registry.BLOCK_REGISTRY);
            DYNAMIC_DATA_PACK.addTag(Supplementaries.res("hanging_signs"), signs, Registry.ITEM_REGISTRY);
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
            DYNAMIC_DATA_PACK.addTag(Supplementaries.res("sign_posts"), posts, Registry.ITEM_REGISTRY);
        }
        //way signs tag
        {
            List<ResourceLocation> biomes = new ArrayList<>();
            for (var e : ForgeRegistries.BIOMES.getEntries()) {
                Holder<Biome> holder = BuiltinRegistries.BIOME.getHolderOrThrow(e.getKey());
                Biome.BiomeCategory biomeCategory = Biome.getBiomeCategory(holder);

                if (biomeCategory != Biome.BiomeCategory.OCEAN && biomeCategory != Biome.BiomeCategory.THEEND &&
                        biomeCategory != Biome.BiomeCategory.RIVER &&
                        biomeCategory != Biome.BiomeCategory.UNDERGROUND &&
                        biomeCategory != Biome.BiomeCategory.JUNGLE &&
                        biomeCategory != Biome.BiomeCategory.NETHER && biomeCategory != Biome.BiomeCategory.NONE) {
                    if(!e.getValue().getRegistryName().getPath().equals("minecraft:mushroom_fields")) {

                        biomes.add(e.getValue().getRegistryName());
                    }
                }
                DYNAMIC_DATA_PACK.addTag(Supplementaries.res("has_way_signs"), biomes, Registry.BIOME_REGISTRY);
            }
        }

        Supplementaries.LOGGER.info("Generated runtime data resources in: {} seconds", watch.elapsed().toSeconds());
    }

    public static void makeConditionalRec(FinishedRecipe r, Consumer<FinishedRecipe> consumer, String name) {
        ConditionalRecipe.builder()
                .addCondition(new OptionalRecipeCondition(name))
                .addRecipe(r)
                .build(consumer, "supplementaries", name);
    }

    public static void makeConditionalWoodRec(FinishedRecipe r, WoodType wood, Consumer<FinishedRecipe> consumer, String name) {

        ConditionalRecipe.builder().addCondition(new OptionalRecipeCondition(name))
                .addCondition(new ModLoadedCondition(wood.getNamespace()))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer, "supplementaries", name + "_" + wood.getAppendableId());
    }

    private static ResourceLocation getPlankRegName(WoodType wood) {
        return new ResourceLocation(wood.getNamespace(), wood.getWoodName() + "_planks");
    }

    private static ResourceLocation getSignRegName(WoodType wood) {
        return new ResourceLocation(wood.getNamespace(), wood.getWoodName() + "_sign");
    }

    private static void makeSignPostRecipe(WoodType wood, Consumer<FinishedRecipe> consumer) {
        try {
            Item plank = wood.plankBlock.asItem();
            Preconditions.checkArgument(plank != Items.AIR);

            Item sign = ForgeRegistries.ITEMS.getValue(getSignRegName(wood));

            if (sign != null && sign != Items.AIR) {
                ShapelessRecipeBuilder.shapeless(ModRegistry.SIGN_POST_ITEMS.get(wood), 2)
                        .requires(sign)
                        .group(RegistryConstants.SIGN_POST_NAME)
                        .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                        //.build(consumer);
                        .save((s) -> makeConditionalWoodRec(s, wood, consumer, RegistryConstants.SIGN_POST_NAME)); //
            } else {
                ShapedRecipeBuilder.shaped(ModRegistry.SIGN_POST_ITEMS.get(wood), 3)
                        .pattern("   ")
                        .pattern("222")
                        .pattern(" 1 ")
                        .define('1', Items.STICK)
                        .define('2', plank)
                        .group(RegistryConstants.SIGN_POST_NAME)
                        .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                        //.build(consumer);
                        .save((s) -> makeConditionalWoodRec(s, wood, consumer, RegistryConstants.SIGN_POST_NAME)); //
            }
        } catch (Exception ignored) {
            Supplementaries.LOGGER.error("Failed to generate sign post recipe for wood type {}", wood);
        }
    }

    private static void makeHangingSignRecipe(WoodType wood, Consumer<FinishedRecipe> consumer) {
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
                    .group(RegistryConstants.HANGING_SIGN_NAME)
                    .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(plank))
                    //.build(consumer);
                    .save((s) -> makeConditionalWoodRec(s, wood, consumer, RegistryConstants.HANGING_SIGN_NAME)); //

        } catch (Exception ignored) {
            Supplementaries.LOGGER.error("Failed to generate hanging sign recipe for wood type {}", wood);
        }
    }


}
