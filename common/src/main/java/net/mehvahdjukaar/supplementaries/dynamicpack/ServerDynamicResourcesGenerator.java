package net.mehvahdjukaar.supplementaries.dynamicpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.Logger;

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
    public boolean dependsOnLoadedPacks() {
        return true;
    }

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {
        //sing posts
        {
            SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("way_signs"));
            builder.addEntries(ModRegistry.WAY_SIGN_ITEMS.values());
            dynamicPack.addTag(builder, Registries.ITEM);
        }

        //recipes
        if (CommonConfigs.Building.WAY_SIGN_ENABLED.get()) {
            addSignPostRecipes(manager);
        }

        //fabric has it done another way beucase it needs tag before this...
        if (PlatHelper.getPlatform().isForge()) {
            //way signs tag
            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_WAY_SIGNS);
                if (CommonConfigs.Building.ROAD_SIGN_ENABLED.get() && CommonConfigs.Building.WAY_SIGN_ENABLED.get()) {
                    builder.addTag(BiomeTags.IS_OVERWORLD);
                }
                dynamicPack.addTag(builder, Registries.BIOME);
            }

            //cave urns tag

            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_CAVE_URNS);

                if (CommonConfigs.Functional.URN_PILE_ENABLED.get() && CommonConfigs.Functional.URN_ENABLED.get()) {
                    builder.addTag(BiomeTags.IS_OVERWORLD);
                }
                dynamicPack.addTag(builder, Registries.BIOME);
            }

            //wild flax tag

            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_WILD_FLAX);

                if (CommonConfigs.Functional.WILD_FLAX_ENABLED.get()) {
                    builder.addTag(BiomeTags.IS_OVERWORLD);
                }
                dynamicPack.addTag(builder, Registries.BIOME);
            }

            //ash

            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_BASALT_ASH);

                if (CommonConfigs.Building.BASALT_ASH_ENABLED.get()) {
                    builder.add(Biomes.BASALT_DELTAS.location());
                    builder.addOptionalElement(ResourceLocation.parse("incendium:volcanic_deltas"));
                }
                dynamicPack.addTag(builder, Registries.BIOME);
            }
        }
    }

    private void addSignPostRecipes(ResourceManager manager) {
        Recipe<?> recipe = RPUtils.readRecipe(manager, Supplementaries.res("way_sign_oak"));
        Recipe<?> recipe2 = RPUtils.readRecipe(manager, Supplementaries.res("way_sign_mod_template"));

        WoodType oak = WoodTypeRegistry.OAK_TYPE;

        ModRegistry.WAY_SIGN_ITEMS.forEach((w, i) -> {
            if (w != oak) {
                try {
                    //Check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                    Recipe<?> recipeTemplate = w.getChild("sign") == null ? recipe2 : recipe;

                    var newR = RPUtils.makeSimilarRecipe(recipeTemplate, WoodTypeRegistry.OAK_TYPE, w, "way_sign_oak");
                    //newR = ForgeHelper.addRecipeConditions(newR, recipe);
                    this.dynamicPack.addRecipe(newR);
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("Failed to generate recipe for sign post {}:", i, e);
                }
            }
        });
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
