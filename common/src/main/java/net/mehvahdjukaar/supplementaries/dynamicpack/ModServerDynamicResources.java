package net.mehvahdjukaar.supplementaries.dynamicpack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicServerResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.VanillaBackportCompat;
import net.mehvahdjukaar.supplementaries.reg.ModConstants;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ModServerDynamicResources extends DynamicServerResourceProvider {

    public static final Map<ResourceLocation, Resource> TAG_TRANSLATION_HACK = new HashMap<>();

    public ModServerDynamicResources() {
        super(Supplementaries.res("generated_pack"),
                PlatHelper.isDev() ? PackGenerationStrategy.CACHED : CommonConfigs.General.DYNAMIC_ASSETS_GEN_MODE.get().toStrategy());
    }

    @Override
    public boolean needsToRegenerate() {
        return super.needsToRegenerate() || PlatHelper.isDev();
    }

    @Override
    protected Collection<String> gatherSupportedNamespaces() {
        return List.of("minecraft", "neoforge");
    }

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

            if (CompatHandler.VANILLA_BACKPORT) {
                VanillaBackportCompat.addCopperDataMaps(sink);
            }

            if (PlatHelper.getPlatform().isFabric()) {
                //fabric stuff
                sink.appendItemToEnchantment(manager, Enchantments.QUICK_CHARGE, ModRegistry.SLINGSHOT_ITEM.get());
                sink.appendItemToEnchantment(manager, Enchantments.MULTISHOT, ModRegistry.SLINGSHOT_ITEM.get());
                sink.appendItemToEnchantment(manager, Enchantments.KNOCKBACK, ModRegistry.WRENCH.get());
            }
        });
    }

    private void addCannonBoatRecipes(ResourceManager manager, ResourceSink sink) {
        ModRegistry.CANNON_BOAT_ITEMS.forEach((w, i) -> {
            if (w == VanillaWoodTypes.OAK) return;

            if (w.getChild("boat") == null) {
                //plenty of modded woods have no boat, so this is expected and not worth a warning
                Supplementaries.LOGGER.debug("Could not find Boat for wood {}. Skipping cannon boat generation", w);
                return;
            }
            try {
                sink.addBlockTypeSwapRecipe(manager, Supplementaries.res("cannon_boat_oak"),
                        VanillaWoodTypes.OAK, w, Supplementaries.res(ModConstants.cannonBoatBaseName(w) + "_" + w.getTypeName()));
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to generate recipe for cannon boat {}:", i, e);
            }

        });
    }


    private void addSignPostRecipes(ResourceManager manager, ResourceSink sink) {
        AtomicInteger am = new AtomicInteger();
        ModRegistry.WAY_SIGN_ITEMS.forEach((w, i) -> {
            if (w == VanillaWoodTypes.OAK) return;
            if (w.getChild("sign") == null) {
                Supplementaries.LOGGER.debug("Could not find Sign for wood {}. Skipping way sign recipe generation", w);
                return;
            }
            try {
                am.addAndGet(1);
                sink.addBlockTypeSwapRecipe(manager, Supplementaries.res("way_sign_oak"), VanillaWoodTypes.OAK, w,
                        Supplementaries.res("way_sign" + "_" + w.getTypeName()));
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to generate recipe for sign post {}:", i, e);
            }
        });
        Supplementaries.LOGGER.info("Generated {} sign post recipes", am.get());
    }
}
