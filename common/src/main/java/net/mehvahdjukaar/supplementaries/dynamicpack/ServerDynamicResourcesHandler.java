package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.resources.pack.DynServerResourcesProvider;
import net.mehvahdjukaar.moonlight.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegistryConstants;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ServerDynamicResourcesHandler extends DynServerResourcesProvider {

    public ServerDynamicResourcesHandler() {
        super(new DynamicDataPack(Supplementaries.res("generated_pack")));
        this.dynamicPack.generateDebugResources = PlatformHelper.isDev() || RegistryConfigs.DEBUG_RESOURCES.get();
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public boolean dependsOnLoadedPacks() {
        return RegistryConfigs.PACK_DEPENDANT_ASSETS.get();
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager resourceManager) {
    }

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {

        //hanging signs
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("hanging_signs"));
            //loot table
            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                dynamicPack.addSimpleBlockLootTable(sign);
                builder.addEntry(sign);
                makeHangingSignRecipe(wood, dynamicPack::addRecipe);
            });
            //tag
            dynamicPack.addTag(builder, Registry.BLOCK_REGISTRY);
            dynamicPack.addTag(builder, Registry.ITEM_REGISTRY);
        }
        //sing posts
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("sign_posts"));
            builder.addEntries(ModRegistry.SIGN_POST_ITEMS.values());
            dynamicPack.addTag(builder, Registry.ITEM_REGISTRY);
            //recipes
            ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> makeSignPostRecipe(wood, dynamicPack::addRecipe));
        }
        //way signs tag
        {
            //TODO: re add
            /*
            List<ResourceLocation> biomes = new ArrayList<>();
            if(ServerConfigs.spawn.WAY_SIGN_ENABLED.get()) {
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
                }
            }
            dynamicPack.addTag(Supplementaries.res("has_way_signs"), biomes, Registry.BIOME_REGISTRY);
            */

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

            Item sign = wood.getItemOfThis("sign");
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
