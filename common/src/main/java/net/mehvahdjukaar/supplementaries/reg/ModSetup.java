package net.mehvahdjukaar.supplementaries.reg;


import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.CauldronBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.FireBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.worldgen.WaySignStructure;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.FireworkStarRecipe;

import java.util.ArrayList;
import java.util.List;

//Merge with supplementaries main class
public class ModSetup {

    //damn I hate this. If setup fails forge doesn't do anything, and it keeps on going quietly
    private static boolean hasFinishedSetup = false;
    private static int setupStage = 0;
    private static boolean firstTagLoad = false;

    public static void init() {
        PlatHelper.addCommonSetup(ModSetup::setup);
        PlatHelper.addCommonSetup(ModSetup::asyncSetup);
        PlatHelper.addReloadableCommonSetup(ModSetup::tagDependantSetup);

    }

    private static final List<Runnable> MOD_SETUP_WORK = List.of(
            CompatHandler::setup,
            RegUtils::registerAdditionalPlacements,
            FlowerPotHandler::setup,
            ModSetup::registerFlammables,
            CauldronBehaviorsManager::registerBehaviors,
            () -> FireworkStarRecipe.SHAPE_BY_ITEM.put(ModRegistry.ENDERMAN_SKULL_ITEM.get(), FireworkExplosion.Shape.CREEPER)
    );

    @EventCalled
    public static void asyncSetup() {
        RandomArrowFunction.setup();
        LootTablesInjects.setup();
        ModSetup.registerFrameBlocks();
        PlaceableBookManager.setup();
        //  PlaceableBookManager.setup();
    }

    @EventCalled
    public static void setup() {
        var list = new ArrayList<Long>();
        try {
            Stopwatch watch = Stopwatch.createStarted();

            for (int i = 0; i < MOD_SETUP_WORK.size(); i++) {
                setupStage = i;
                MOD_SETUP_WORK.get(i).run();
                list.add(watch.elapsed().toMillis());
                watch.reset();
                watch.start();
            }
            hasFinishedSetup = true;

            Supplementaries.LOGGER.info("Finished mod setup in: {} ms", list);

        } catch (Exception e) {
            terminateWhenSetupFails(e);
        }
    }

    private static void terminateWhenSetupFails(Exception e) {
        //if setup fails crash the game. idk why it doesn't do that on its own wtf
        throw new IllegalStateException("Mod setup has failed to complete (" + setupStage + ").\n" +
                " This might be due to some mod incompatibility or outdated dependencies (check if everything is up to date).\n" +
                " Refusing to continue loading with a broken modstate. Next step: crashing this game, no survivors", e);
    }

    private static void registerFlammables() {
        //guess no need to use forge overrides unless we need the extra param
        //tile entities are not meant to be flammable
        RegHelper.registerBlockFlammability(ModRegistry.ROPE.get(), 60, 100);
        RegHelper.registerBlockFlammability(ModRegistry.FINE_WOOD.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.FINE_WOOD_SLAB.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.FINE_WOOD_STAIRS.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.TIMBER_FRAME.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.TIMBER_BRACE.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.TIMBER_CROSS_BRACE.get(), 5, 20);
        RegHelper.registerBlockFlammability(ModRegistry.STICK_BLOCK.get(), 30, 60);
        RegHelper.registerBlockFlammability(ModRegistry.FLAX_BLOCK.get(), 60, 20);
        RegHelper.registerBlockFlammability(ModRegistry.FLAX_WILD.get(), 60, 100);
        RegHelper.registerBlockFlammability(ModRegistry.FEATHER_BLOCK.get(), 30, 60);
        RegHelper.registerBlockFlammability(ModRegistry.GUNPOWDER_BLOCK.get(), 200, 0);
        RegHelper.registerBlockFlammability(ModFluids.LUMISENE_BLOCK.get(), 200, 0);
        RegHelper.registerBlockFlammability(ModRegistry.WICKER_FENCE.get(), 30, 60);
        for(var f : ModRegistry.FLAGS.values()){
            RegHelper.registerBlockFlammability(f.get(),60,60);
        }
        for(var b : ModRegistry.BUNTING_BLOCKS.values()){
            RegHelper.registerBlockFlammability(b.get(), 60, 100);
        }
        for(var b : ModRegistry.BUNTING_WALL_BLOCKS.values()){
            RegHelper.registerBlockFlammability(b.get(), 60, 100);
        }
        for (var a : ModRegistry.AWNINGS.values()) {
            RegHelper.registerBlockFlammability(a.get(), 60, 20);
        }
    }

    private static void registerFrameBlocks() {
        ModRegistry.TIMBER_FRAME.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_FRAME.get());
        ModRegistry.TIMBER_BRACE.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_BRACE.get());
        ModRegistry.TIMBER_CROSS_BRACE.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_CROSS_BRACE.get());
    }

    @EventCalled
    public static void tagDependantSetup(RegistryAccess registryAccess, boolean client) {
        if (!firstTagLoad) {

            //using this as a post-setup event that can access tags
            firstTagLoad = true;
            if (!hasFinishedSetup) {
                //if mod setup fails (without throwing errors) we try to replicate what caused it to crash and printing that error
                try {
                    Supplementaries.LOGGER.error("Something went wrong during mod setup, exiting");
                    MOD_SETUP_WORK.get(setupStage).run();
                    Supplementaries.LOGGER.error("No error found. Weird");
                } catch (Exception e) {
                    terminateWhenSetupFails(e);
                }
            }
        }
        // this we can properly refresh every time
        InteractEventsHandler.registerOverrides(registryAccess);

        FireBehaviorsManager.registerBehaviors(registryAccess);


        if (!client) {
            WaySignStructure.recomputeValidStructureCache(registryAccess);

            try {
                SoftFluidRegistry.get(registryAccess).get(MLBuiltinSoftFluids.EMPTY.getID());
                SoftFluidRegistry.get(registryAccess).get(MLBuiltinSoftFluids.WATER.getID());
            } catch (Exception e) {
                throw new RuntimeException("Failed to get empty soft fluid from datapack. How?", e);
            }
            var server = PlatHelper.getCurrentServer();
            if (server != null) {
                FaucetBehaviorsManager.reloadWithLevel(server.overworld());
            }
        }
    }

}
