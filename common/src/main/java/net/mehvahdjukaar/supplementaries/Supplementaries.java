package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.common.world.songs.FluteSongsReloadListener;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.mehvahdjukaar.supplementaries.reg.generation.WorldGenHandler;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Supplementaries {

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String n) {
        return new ResourceLocation(MOD_ID, n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }


    //called either on mod creation
    public static void commonInit() {

        RegistryConfigs.superEarlyLoad();
        CommonConfigs.init();
        ClientConfigs.init();


        //yeet java models in favor or json ones
        // TODO: re add soap stuff
        //Fix throwing bricks not working on glass
        //fix snowy spirit chest not sincing after reload
        //banner patterns not working
        //potions in jars are bugged ;_;
        //feather block

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);
        ModSounds.init();
        ModRegistry.init();
        ModRecipes.init();
        ModParticles.init();
        CMDreg.init();


        WorldGenHandler.registerBus();


        var serverRes = new ServerDynamicResourcesHandler();
        serverRes.register();

        PlatformHelper.addServerReloadListener(new FluteSongsReloadListener(), res("flute_songs"));

        if (PlatformHelper.getEnv().isClient()) {
            var clientRes = new ClientDynamicResourcesHandler();
            clientRes.register();

            ClientPlatformHelper.addClientReloadListener(WallLanternTexturesRegistry.RELOAD_INSTANCE, res("wall_lanterns"));
        }
    }

    //mod setup
    public static void commonSetup() {
        ModSetup.setup();
    }


    //yes this is where I write crap. deal with it XD

    //  RegistryConfigs.createSpec();
    //  RegistryConfigs.load();
    //punching swings lanterns
    //ehcnahted books placed vertically. fix placement based off player look dir
    //wind physics for wind vane

    //ash makes mobs jump
    //squishy piston launcher. also rework them and fix on servers
    //TODO: readd nethers delight stuff & maybe IW planter
    //camera mod with screenshots

    //clicking on cage with lead will put the leashed animal inside
    //bug: tipped spikes not getting placed. rope not playing up sound
    //TODO: flint block and steel
    //wrench rotation overlay
    //TODO: dynamic soap undye recipe
    //create moving dynamic blocks like rope knot
    //jei villagers addon
    //corona mod
    //lilypad mod
    //trollium interaction mod
    //animated lantern textures
    //ash jei plugin
    //bubble sound for bellows
    //bundle sound for sacks

    //FIx spikes piston movements
    //TODO: fish bucket on cages a
    //TODO: shift click to pickup placed book

    //todo: fix projectile hitbox being a single point on y = 0
    //divining rod
    //add chain knot

    //elytra acrobatics mod
    //enderman hold block in rain
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

    //TODO: bugs: bell ropes(add to flywheel instance), brewing stand colors(?)

    //TODO: mod ideas: particle block, blackboard banners and flags, lantern holding

    //TODO: add stick window loggable clipping

    //flute animation fix

    //add shift middle click to swap to correct tool

    //mod idea: blackboard banners and flags with villager
    //weed mod

    //throwable slimeballs

    //simple mode for doors and trapdoors

    //label

    //animated pulley texture

    //TODO: faucets create sprout

    // randomium item particle when drop

    //TODO: xp bottling whose cost depends on player total xp
    //TODO: randomium that can spawn in other dimensions via whitelist

    //TODO: wiki for custom map markers icons. add simple icon datapacks

    //randomium can give onl stuff already obtained by a player in survival

    //golden carrots to breed baby pignis

    //directional books fixed
    //particles for randomium

    //TODO: credist screen

    //TODO: way signs as villages pieces

    //small honey slime in cage

    //idea: Increase range of enchantment table

    //IRON gate connected model

    //hud mod. armor broken hud, items offhadn crafting

    //ash auto bonemeal, improve bubbles

    //better badlands kindling gunpowder compat (whenevr it updates lol)
    //better fodder pathfinding
    //TODO fix randomium recipe jei extensin

    //TODO: add dispenser like interaction registry for faucet
    //TODO: flax upper stage grows depending on lower

    //jeed mod loaded recipe condition
    //blackboard otline gui+
    //quiver that allows to select arrows
    //map function & data driven map markers

    //soap signs & finish notice board dye (add dye interface)
    //snow real magic compat
    //bugs: spring launcher broken on servers

    //rope sliding sound instance
    //bellows particles
    //rope walking

}
