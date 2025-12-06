package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModServerDynamicResources extends DynamicServerResourceProvider {

    public ModServerDynamicResources() {
        super(Supplementaries.res("generated_pack"),
                CommonConfigs.General.DYNAMIC_ASSETS_GEN_MODE.get().toStrategy());
    }

    @Override
    public boolean needsToRegenerate() {
        return super.needsToRegenerate() || PlatHelper.isDev();
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of();
    }

    public static final Map<ResourceLocation, Resource> TAG_TRANSLATION_HACK = new HashMap<>();

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        executor.accept((manager, sink) -> {
            //TAG_TRANSLATION_HACK.putAll(manager.listResources("tags", r->true);

            //sing posts
            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("way_signs"));
                builder.addEntries(ModRegistry.WAY_SIGN_ITEMS.values());
                sink.addTag(builder, Registries.ITEM);
            }

            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(Supplementaries.res("cannon_boats"));
                builder.addEntries(ModRegistry.CANNON_BOAT_ITEMS.values());
                sink.addTag(builder, Registries.ITEM);
            }

            //recipes
            if (CommonConfigs.Building.WAY_SIGN_ENABLED.get()) {
                addSignPostRecipes(manager, sink);
            }

            if (CommonConfigs.Functional.CANNON_BOAT_ENABLED.get()) {
                addCannonBoatRecipes(manager, sink);
            }

            //way signs tag
            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_ROAD_SIGNS);
                if (CommonConfigs.Building.ROAD_SIGN_ENABLED.get()) {
                    builder.addTag(BiomeTags.IS_OVERWORLD);
                }
                sink.addTag(builder, Registries.BIOME);
            }

            //galleons
            {
                SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_GALLEONS);
                if (CommonConfigs.Functional.GALLEONS_ENABLED.get()) {
                    builder.addTag(BiomeTags.IS_OCEAN);
                }
                sink.addTag(builder, Registries.BIOME);
            }

            //fabric has it done another way beucase it needs tag before this... for features only
            if (PlatHelper.getPlatform().isForge()) {
                //cave urns tag

                {
                    SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_CAVE_URNS);

                    if (CommonConfigs.Functional.URN_PILE_ENABLED.get()) {
                        builder.addTag(BiomeTags.IS_OVERWORLD);
                    }
                    sink.addTag(builder, Registries.BIOME);
                }

                //barnacles
                {
                    SimpleTagBuilder builder = SimpleTagBuilder.of(ModTags.HAS_BARNACLES);

                    if (CommonConfigs.Building.BARNACLES_ENABLED.get()) {
                        builder.addTag(BiomeTags.IS_OCEAN);
                        builder.add(Biomes.STONY_SHORE.location());
                        builder.addTag(BiomeTags.IS_BEACH);
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
                        builder.addOptionalElement(ResourceLocation.parse("incendium:volcanic_deltas"));
                    }
                    sink.addTag(builder, Registries.BIOME);
                }
            }
        });
    }

    private void addCannonBoatRecipes(ResourceManager manager, ResourceSink sink) {
        WoodType oak = VanillaWoodTypes.OAK;
        ModRegistry.CANNON_BOAT_ITEMS.forEach((w, i) -> {
            WoodType bamboo = VanillaWoodTypes.BAMBOO;
            if (w != oak && w != bamboo) {
                try {
                    sink.addBlockTypeSwapRecipe(manager, Supplementaries.res("cannon_boat_oak"),
                            VanillaWoodTypes.OAK, w, Supplementaries.res("cannon_boat_oak"));
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("Failed to generate recipe for cannon boat {}:", i, e);
                }
            }
        });
    }


    private void addSignPostRecipes(ResourceManager manager, ResourceSink sink) {
        WoodType oak = VanillaWoodTypes.OAK;

        ModRegistry.WAY_SIGN_ITEMS.forEach((w, i) -> {
            if (w != oak) {
                try {
                    //Check for disabled ones. Will actually crash if its null since vanilla recipe builder expects a non-null one
                    ResourceLocation recipeTemplate = w.getChild("sign") == null ?
                            Supplementaries.res("way_sign_oak") : Supplementaries.res("way_sign_mod_template");

                    sink.addBlockTypeSwapRecipe(manager, recipeTemplate, VanillaWoodTypes.OAK, w,
                            Supplementaries.res("way_sign_oak"));
                } catch (Exception e) {
                    Supplementaries.LOGGER.error("Failed to generate recipe for sign post {}:", i, e);
                }
            }
        });
    }
}
