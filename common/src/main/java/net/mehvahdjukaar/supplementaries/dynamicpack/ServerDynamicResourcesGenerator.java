package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.recipe.IRecipeTemplate;
import net.mehvahdjukaar.moonlight.api.resources.recipe.TemplateRecipeManager;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerDynamicResourcesGenerator extends DynServerResourcesGenerator {

    public static final ServerDynamicResourcesGenerator INSTANCE = new ServerDynamicResourcesGenerator();

    public ServerDynamicResourcesGenerator() {
        super(new DynamicDataPack(Supplementaries.res("generated_pack")));
        this.dynamicPack.setGenerateDebugResources(PlatHelper.isDev() || CommonConfigs.General.DEBUG_RESOURCES.get());
    }

    @Override
    public Logger getLogger() {
        return Supplementaries.LOGGER;
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {

        //recipes
        if (CommonConfigs.Building.SIGN_POST_ENABLED.get()) {
            executor.accept(this::addSignPostRecipes);

            executor.accept((manager, sink) -> {
                //sing posts
                {
                    SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("sign_posts"));
                    builder.addEntries(ModRegistry.SIGN_POST_ITEMS.values());
                    sink.addTag(builder, Registries.ITEM);
                }


                //fabric has it done another way beucase it needs tag before this...
                if (PlatHelper.getPlatform().isForge()) {
                    //way signs tag
                    {
                        SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_WAY_SIGNS);
                        if (CommonConfigs.Building.WAY_SIGN_ENABLED.get() && CommonConfigs.Building.SIGN_POST_ENABLED.get()) {
                            builder.addTag(BiomeTags.IS_OVERWORLD);
                        }
                        sink.addTag(builder, Registries.BIOME);
                    }

                    //cave urns tag

                    {
                        SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_CAVE_URNS);

                        if (CommonConfigs.Functional.URN_PILE_ENABLED.get() && CommonConfigs.Functional.URN_ENABLED.get()) {
                            builder.addTag(BiomeTags.IS_OVERWORLD);
                        }
                        sink.addTag(builder, Registries.BIOME);
                    }

                    //wild flax tag

                    {
                        SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_WILD_FLAX);

                        if (CommonConfigs.Functional.WILD_FLAX_ENABLED.get()) {
                            builder.addTag(BiomeTags.IS_OVERWORLD);
                        }
                        sink.addTag(builder, Registries.BIOME);
                    }

                    //ash

                    {
                        SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_BASALT_ASH);

                        if (CommonConfigs.Building.BASALT_ASH_ENABLED.get()) {
                            builder.add(Biomes.BASALT_DELTAS.location());
                            builder.addOptionalElement(new ResourceLocation("incendium:volcanic_deltas"));
                        }
                        sink.addTag(builder, Registries.BIOME);
                    }
                }

            });
        }

//        genAllRecipesAdv(Supplementaries.MOD_ID);
    }

    private void addSignPostRecipes(ResourceManager manager, ResourceSink sink) {
        IRecipeTemplate<?> template = RPUtils.readRecipeAsTemplate(manager,
                ResType.RECIPES.getPath(Supplementaries.res("sign_post_oak")));

        WoodType oak = VanillaWoodTypes.OAK;

        if (signPostTemplate2 == null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS,
                            ModRegistry.SIGN_POST_ITEMS.get(oak), 3)
                    .pattern("   ")
                    .pattern("222")
                    .pattern(" 1 ")
                    .define('1', Items.STICK)
                    .define('2', oak.planks)
                    .group(ModConstants.SIGN_POST_NAME)
                    .unlockedBy("has_plank", InventoryChangeTrigger.TriggerInstance.hasItems(oak.planks))
                    .save(s -> signPostTemplate2 = TemplateRecipeManager.read(s.serializeRecipe()));
        }

        ModRegistry.SIGN_POST_ITEMS.forEach((w, i) -> {
            if (w != oak) {
                try {
                    //Check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                    IRecipeTemplate<?> recipeTemplate = w.getChild("sign") == null ? signPostTemplate2 : template;

                    FinishedRecipe newR = recipeTemplate.createSimilar(VanillaWoodTypes.OAK, w, w.mainChild().asItem());
                    if (newR == null) return;
                    newR = ForgeHelper.addRecipeConditions(newR, template.getConditions());
                    sink.addRecipe(newR);
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("Failed to generate recipe for sign post {}:", i, e);
                }
            }
        });
    }

    private IRecipeTemplate<?> signPostTemplate2;

    public void genAllRecipesAdv(String modId, ResourceSink sink) {
        if (true || !PlatHelper.isDev()) return;
        var level = PlatHelper.getCurrentServer().overworld();
        var man = level.getRecipeManager();
        for (var r : man.getRecipes()) {
            ResourceLocation recipeId = r.getId();
            if (recipeId.getNamespace().equals(modId) && !r.isSpecial()) {
                Set<Item> ii = new HashSet<>();
                try {
                    var builder = Advancement.Builder.recipeAdvancement()
                            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                            .rewards(AdvancementRewards.Builder.recipe(recipeId))
                            .requirements(RequirementsStrategy.OR);
                    Set<TagKey<Item>> tags = new HashSet<>();
                    for (var i : r.getIngredients()) {
                        if (!i.isEmpty()) {
                            if (i.values[0] instanceof Ingredient.TagValue tv) {
                                tags.add(tv.tag);
                            } else
                                ii.addAll(Arrays.stream(i.getItems()).map(ItemStack::getItem).collect(Collectors.toSet()));
                        }
                    }

                    for (var ing : ii) {
                        builder.addCriterion("has_" + Utils.getID(ing).getPath(), RecipeProvider.has(ing));
                    }
                    for (var tag : tags) {
                        builder.addCriterion(tag.location().toString(), RecipeProvider.has(tag));
                    }
                    var res = recipeId.withPrefix("recipes/");

                    JsonObject json = builder.serializeToJson();
                    removeNullEntries(json);
                    sink.addJson(res, json, ResType.ADVANCEMENTS);
                } catch (Exception e) {
                    int aa = 1; //error
                }
            }
        }
    }

    private static void removeNullEntries(JsonObject jsonObject) {
        jsonObject.entrySet().removeIf(entry -> entry.getValue().isJsonNull());

        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();
            if (element.isJsonObject()) {
                removeNullEntries(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                removeNullEntries(element.getAsJsonArray());
            }
        });
    }

    private static void removeNullEntries(JsonArray jsonArray) {
        JsonArray newArray = new JsonArray();
        jsonArray.forEach(element -> {
            if (!element.isJsonNull()) {
                if (element.isJsonObject()) {
                    removeNullEntries(element.getAsJsonObject());
                } else if (element.isJsonArray()) {
                    removeNullEntries(element.getAsJsonArray());
                }
                newArray.add(element);
            }
        });
        for (int i = 0; i < jsonArray.size(); i++) jsonArray.remove(i);
        jsonArray.addAll(newArray);
    }
}
