package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.resourcepack.DynamicDataPack;
import net.mehvahdjukaar.selene.resourcepack.RPAwareDynamicDataProvider;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.RegistryConstants;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerDynamicResourcesHandler extends RPAwareDynamicDataProvider {

    public ServerDynamicResourcesHandler() {
        super(new DynamicDataPack(Supplementaries.res("generated_pack")));
        this.dynamicPack.generateDebugResources = !FMLLoader.isProduction() || RegistryConfigs.Reg.DEBUG_RESOURCES.get();
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return false;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager resourceManager) {
    }

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {

        //hanging signs
        {
            List<ResourceLocation> signs = new ArrayList<>();

            //loot table
            ModRegistry.HANGING_SIGNS.forEach((wood, sign)->{
                dynamicPack.addSimpleBlockLootTable(sign);
                signs.add(sign.getRegistryName());

                makeHangingSignRecipe(wood, dynamicPack::addRecipe);
            });
            //tag
            dynamicPack.addTag(Supplementaries.res("hanging_signs"), signs, Registry.BLOCK_REGISTRY);
            dynamicPack.addTag(Supplementaries.res("hanging_signs"), signs, Registry.ITEM_REGISTRY);
        }
        //sing posts
        {
            List<ResourceLocation> posts = new ArrayList<>();

            //recipes
            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign)->{
                posts.add(sign.getRegistryName());

                makeSignPostRecipe(wood, dynamicPack::addRecipe);
            });

            //tag
            dynamicPack.addTag(Supplementaries.res("sign_posts"), posts, Registry.ITEM_REGISTRY);
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
                    if (!e.getValue().getRegistryName().getPath().equals("minecraft:mushroom_fields")) {

                        biomes.add(e.getValue().getRegistryName());
                    }
                }
                dynamicPack.addTag(Supplementaries.res("has_way_signs"), biomes, Registry.BIOME_REGISTRY);
            }
        }
    }

    private void makeConditionalRec(FinishedRecipe r, Consumer<FinishedRecipe> consumer, String name) {
        ConditionalRecipe.builder()
                .addCondition(new OptionalRecipeCondition(name))
                .addRecipe(r)
                .build(consumer, "supplementaries", name);
    }

    private void makeConditionalWoodRec(FinishedRecipe r, WoodType wood, Consumer<FinishedRecipe> consumer, String name) {

        ConditionalRecipe.builder().addCondition(new OptionalRecipeCondition(name))
                .addCondition(new ModLoadedCondition(wood.getNamespace()))
                .addRecipe(r)
                .generateAdvancement()
                .build(consumer, "supplementaries", name + "_" + wood.getAppendableId());
    }

    private void makeSignPostRecipe(WoodType wood, Consumer<FinishedRecipe> consumer) {
        try {
            Item plank = wood.planks.asItem();
            Preconditions.checkArgument(plank != Items.AIR);

            Item sign = wood.signItem.get();
            if (sign != null) {
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

    private void makeHangingSignRecipe(WoodType wood, Consumer<FinishedRecipe> consumer) {
        try {
            Item plank = wood.planks.asItem();
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
