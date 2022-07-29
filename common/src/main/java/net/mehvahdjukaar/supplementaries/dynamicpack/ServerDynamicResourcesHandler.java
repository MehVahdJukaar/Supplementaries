package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.resources.recipe.TemplateRecipeManager;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.RegistryConstants;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Logger;

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
        addHangingSignRecipes(resourceManager);

        //recipes
        addSignPostRecipes(resourceManager);

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

    @Override
    public void generateStaticAssetsOnStartup(ResourceManager manager) {

        //hanging signs
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("hanging_signs"));
            //loot table
            ModRegistry.HANGING_SIGNS.forEach((wood, sign) -> {
                dynamicPack.addSimpleBlockLootTable(sign);
                builder.addEntry(sign);
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
        }
    }


    private void addHangingSignRecipes(ResourceManager manager) {
        IRecipeTemplate<?> template = RPUtils.readRecipeAsTemplate(manager,
                ResType.RECIPES.getPath(Supplementaries.res("hanging_sign_oak")));

        ModRegistry.HANGING_SIGNS.forEach((w, b) -> {
            if (w != WoodTypeRegistry.OAK_TYPE) {
                Item i = b.asItem();
                //check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                if (i.getItemCategory() != null) {
                    FinishedRecipe newR = template.createSimilar(WoodTypeRegistry.OAK_TYPE, w, w.mainChild().asItem());
                    if (newR == null) return;
                    newR = ForgeHelper.addRecipeConditions(newR, template.getConditions());
                    this.dynamicPack.addRecipe(newR);
                }
            }
        });
    }

    private void addSignPostRecipes(ResourceManager manager) {
        IRecipeTemplate<?> template = RPUtils.readRecipeAsTemplate(manager,
                ResType.RECIPES.getPath(Supplementaries.res("sign_post_oak")));

        WoodType oak = WoodTypeRegistry.OAK_TYPE;

        if (signPostTemplate2 == null) {
            ShapedRecipeBuilder.shaped(ModRegistry.SIGN_POST_ITEMS.get(oak), 3)
                    .pattern("   ")
                    .pattern("222")
                    .pattern(" 1 ")
                    .define('1', Items.STICK)
                    .define('2', oak.planks)
                    .group(RegistryConstants.SIGN_POST_NAME)
                    .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(oak.planks))
                    .save((s) -> signPostTemplate2 = TemplateRecipeManager.read(s.serializeRecipe()));
        }

        ModRegistry.SIGN_POST_ITEMS.forEach((w, i) -> {
            if (w != oak) {
                //check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                if (i.getItemCategory() != null) {
                    IRecipeTemplate<?> recipeTemplate = w.getChild("sign") == null ? signPostTemplate2 : template;

                    FinishedRecipe newR = recipeTemplate.createSimilar(WoodTypeRegistry.OAK_TYPE, w, w.mainChild().asItem());
                    if (newR == null) return;
                    newR = ForgeHelper.addRecipeConditions(newR, template.getConditions());
                    this.dynamicPack.addRecipe(newR);
                }
            }
        });
    }

    private IRecipeTemplate<?> signPostTemplate2;


}
