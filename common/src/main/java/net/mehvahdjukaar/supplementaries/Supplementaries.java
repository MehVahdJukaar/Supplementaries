package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesManager;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.world.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
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


    //called on mod creation
    public static void commonInit() {

        Credits.fetchFromServer();
        RegistryConfigs.superEarlyLoad();
        CommonConfigs.init();
        ClientConfigs.init();

        RegHelper.registerSimpleRecipeCondition(Supplementaries.res("flag"), RegistryConfigs::isEnabled);

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);

        ModSounds.init();
        ModRegistry.init();
        ModRecipes.init();
        ModParticles.init();
        ModWorldgenRegistry.init();
        ModMapMarkers.init();


        ServerDynamicResourcesHandler.INSTANCE.register();

        PlatformHelper.addServerReloadListener(SongsManager.RELOAD_INSTANCE, res("flute_songs"));
        PlatformHelper.addServerReloadListener(AdventurerMapsHandler.RELOAD_INSTANCE, res("structure_maps"));
        PlatformHelper.addServerReloadListener(CapturedMobHandler.RELOAD_INSTANCE, res("catchable_mobs_properties"));

        if (PlatformHelper.getEnv().isClient()) {
            ClientDynamicResourcesHandler.INSTANCE.register();

            ClientPlatformHelper.addClientReloadListener(WallLanternTexturesManager.RELOAD_INSTANCE, res("wall_lanterns"));
        }
    }

    //mod init
    public static void commonSetup() {
        ModSetup.setup();
    }

    //yes this is where I write crap. deal with it XD

    //TODO: fix ash glm not workin
    //TODO relayer on piston retract
    //mod to wax anything to prevent interaction
    //special recipes conditions dont work
    //sugar block fall in water
    //soap in water makes soap particles
    //yeet java models in favor or json ones
    //chains pull down candle holders and lanterns

    //antique ink fishing


    //punching swings lanterns
    //ehcnahted books placed vertically. fix placement based off player look dir
    //wind physics for wind vane

    //ash makes mobs jump
    //squishy piston launcher. also rework them and fix on servers
    //TODO: readd nethers delight stuff & maybe IW planter
    //camera mod with screenshots

    //clicking on cage with lead will put the leashed animal inside
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
    //TODO: shift click to pickup placed book

    //todo: fix projectile hitbox being a single point on y = 0
    //divining rod
    //add chain knot

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

    //soap signs & finish notice board dye (add dye interface)
    //snow real magic compat
    //bugs: spring launcher broken on servers

    //bellows particles

}
