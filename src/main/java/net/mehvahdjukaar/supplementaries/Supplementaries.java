package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.common.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.common.world.generation.WorldGenHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Supplementaries.MOD_ID)
public class Supplementaries {

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String n) {
        return new ResourceLocation(MOD_ID, n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }

    public Supplementaries() {
//TODO: fix ceiling banners
        //TODO: dynamic soap undye recipe
        /*
        To check if a given tag has a given object:
        Registry#getHolderOrThrow(objectID).is(tagKey)

        To get all objects in a given tag:
        Registry#getTagOrEmpty(tagKey)

        To get all tags for a given object:
        Registry#getHolderOrThrow(objectID).tags()

        Some things like blockstates and items have helper methods for doing the above as well
        */

        //  RegistryConfigs.createSpec();
        //  RegistryConfigs.load();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        CraftingHelper.register(new OptionalRecipeCondition.Serializer());

        ConfigHandler.registerBus(bus);
        ModRegistry.registerBus(bus);
        WorldGenHandler.registerBus(bus);

        ServerDynamicResourcesHandler.registerBus(bus);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> ClientDynamicResourcesHandler.registerBus(bus));

        bus.addListener(ModSetup::init);


    }


    //yes this is where I write crap. deal with it XD

    //animated lantern textures
    //add option for soul jar
    //ash jei plugin
    //bundle sound for sacks

    //FIx spikes piston movements
    //TODO: fish bucket on cages a
    //TODO: shift click to pickup placed book

    //TODO: fix slingshot proj not playing sound on client (all messed up)


    //todo: fix projectile hitbox being a single point on y = 0
    //divining rod
    //add chain knot

    //elytra acrobatics mod

    //swaying blocks water friction

    //horizontal shearable ropes

    //TODO: more flywheel stuff

    //TODO: improve feather particle


    //use feather particle on spriggans

    //TODO: fix JER loot tables percentages

    //GLOBE inv model
    //TODO: goblet & jars dynamic baked model
    //ghast fireball mob griefing

    //TODO: fireflies deflect arrows

    //firefly glow block

    //TODO: replace soft fluid system with forge caps to itemstacks and register actual forge fluids

    //TODO: bugs: bell ropes(add to flywheel instance), brewing stand colors(?)

    //TODO: mod ideas: particle block, blackboard banners and flags, lantern holding

    //TODO: add stick window loggable clipping

    //flute animation fix

    //add shift middle click to swap to correct tool

    //mod idea: blackboard banners and flags with villager
    //weed mod

    //throwable slimeballs

    //TODO: make dummy not show numbers at a distance, headshot

    //simple mode for doors and trapdoors

    //data driven fluid system

    //label

    //animated pulley texture

    //TODO: add more support for new game events

    //TODO: faucets create sprout

    // randomium item particle when drop

    //TODO: xp bottling whose cost depends on player total xp
    //TODO: randomium that can spawn in other dimensions via whitelist

    //todo: serene easons & moon stuff for haunted harvest

    //TODO: wiki for custom map markers icons. add simple icon datapacks

    //randomium can give onl stuff already obtained by a player in survival

    //golden carrots to breed baby pignis

    //directional books fixed
    //particles for randomium
    //data driven fluid system :0
    //TODO: credist screen

    //TODO: way signs as villages pieces
    //slime balls

    //small honey slime in cage

    //blackboard texture glitchyness (baked model)

    //ender pearls dispensers

    //idea: Increase range of enchantment table

    //TODO: fix faucet water is completely bugged
    //IRON gate connected model

    //hud mod. armor broken hud, items offhadn crafting

    //ash auto bonemeal, improve bubbles

    //redo achievement rendering
    //better badlands kindling gunpowder compat (whenevr it updates lol)
    //better fodder pathfinding
    //TODO fix randomium recipe jei extensin

    //TODO: add dispenser like interaction registry for faucet
    //TODO: flax upper stage grows depending on lower
    //add sack sound

    //smarter farmers not planting tomatoes & task needing to be rewritten
    //jeed mod loaded recipe condition
    //blackboard otline gui+
    //quiver that allows to select arrows
    //map function & data driven map markers

    //soap signs & finish notice board dye (add dye interface)
    //snow real magic compat
    //bugs: spring launcher broken on servers

    //add new cauldron stuff to cauldron interaction map


}
