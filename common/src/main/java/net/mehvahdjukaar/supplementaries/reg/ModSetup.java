package net.mehvahdjukaar.supplementaries.reg;


import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.CauldronBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.dispenser.DispenserBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.present.PresentBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.ArrayList;
import java.util.List;

//Merge with supplementaries main class
public class ModSetup {

    //damn I hate this. If setup fails forge doesn't do anything, and it keeps on going quietly
    private static boolean hasFinishedSetup = false;
    private static int setupStage = 0;
    private static boolean firstTagLoad = false;

    private static final List<Runnable> MOD_SETUP_WORK = List.of(
            CompatHandler::setup,
            FlowerPotHandler::setup,
            ModSetup::registerCompostables,
            ModSetup::registerMobFoods,
            ModSetup::registerFabricFlammable,
            CauldronBehaviorsManager::registerBehaviors,
            ModCreativeTabs::setup,
            () -> FireworkStarRecipe.SHAPE_BY_ITEM.put(ModRegistry.ENDERMAN_SKULL_ITEM.get(), FireworkRocketItem.Shape.CREEPER)
    );

    public static void asyncSetup() {
        PresentBehaviorsManager.registerBehaviors();
        FaucetBehaviorsManager.registerBehaviors();
        RandomArrowFunction.setup();
        LootTablesInjects.setup();
        ModSetup.registerFrameBlocks();
        CurseLootFunction.setup();
        PlaceableBookManager.setup();
    }

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

    private static void registerFabricFlammable() {
        RegHelper.registerBlockFlammability(ModRegistry.ROPE.get(), 60, 100);
    }

    private static void registerMobFoods() {
        RegHelper.registerChickenFood(ModRegistry.FLAX_SEEDS_ITEM.get());
        RegHelper.registerHorseFood(ModRegistry.FLAX_BLOCK.get(), ModRegistry.SUGAR_CUBE.get(), ModRegistry.FLAX_ITEM.get());
        RegHelper.registerParrotFood(ModRegistry.FLAX_SEEDS_ITEM.get());
    }

    private static void registerFrameBlocks() {
        ModRegistry.TIMBER_FRAME.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_FRAME.get());
        ModRegistry.TIMBER_BRACE.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_BRACE.get());
        ModRegistry.TIMBER_CROSS_BRACE.get().registerFilledBlock(ModRegistry.DAUB.get(), ModRegistry.DAUB_CROSS_BRACE.get());
    }

    private static void registerCompostables() {
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_SEEDS_ITEM.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_ITEM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_WILD.get().asItem(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_BLOCK.get().asItem(), 1);
    }

    //events on setup. fire on world load
    public static void tagDependantSetup(RegistryAccess registryAccess) {
        if (!firstTagLoad) {
            //using this as a post-setup event that can access tags
            Stopwatch watch = Stopwatch.createStarted();
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

            //stuff that needs tags
            DispenserBehaviorsManager.registerBehaviors(registryAccess);

            Supplementaries.LOGGER.info("Finished additional setup in {} ms", watch.elapsed().toMillis());
        }
        // this we can properly refresh every time
        InteractEventsHandler.setupOverrides();

    }

}
