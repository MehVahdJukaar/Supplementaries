package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynServerResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicDataPack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.ResourceManager;
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
                // makeHangingSignRecipe(wood, dynamicPack::addRecipe);
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
            //  ModRegistry.SIGN_POST_ITEMS.forEach((wood, sign) -> makeSignPostRecipe(wood, dynamicPack::addRecipe));
        }
        //TODO: add recipes


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


}
