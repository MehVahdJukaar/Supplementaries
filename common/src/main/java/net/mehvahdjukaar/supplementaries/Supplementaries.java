package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.core.mixins.BeeGoalMixin;
import net.mehvahdjukaar.supplementaries.client.RopeSlideSoundInstance;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.entities.trades.AdventurerMapsHandler;
import net.mehvahdjukaar.supplementaries.common.entities.trades.ModVillagerTrades;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Supplementaries {

    public static final String MOD_ID = "supplementaries";

    public static final Logger LOGGER = LogManager.getLogger("Supplementaries");

    public static ResourceLocation res(String n) {
        return new ResourceLocation(MOD_ID, n);
    }

    public static String str(String n) {
        return MOD_ID + ":" + n;
    }

    //called on mod creation
    public static void commonInit() {

        Credits.fetchFromServer();

        CommonConfigs.init();
        PlatHelper.getPhysicalSide().ifClient(ClientConfigs::init);

        NetworkHandler.registerMessages();

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isEnabled);

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);

        ModSounds.init();
        ModRegistry.init();
        ModRecipes.init();
        ModMenuTypes.init();
        ModEntities.init();
        ModParticles.init();
        ModCommands.init();
        ModVillagerTrades.init();
        ModWorldgenRegistry.init();
        ModMapMarkers.init();
        ModCreativeTabs.init();
        LootTablesInjects.init();

        ServerDynamicResourcesGenerator.INSTANCE.register();


        PlatHelper.addServerReloadListener(SongsManager.RELOAD_INSTANCE, res("flute_songs"));
        PlatHelper.addServerReloadListener(HourglassTimesManager.RELOAD_INSTANCE, res("hourglass_data"));
        PlatHelper.addServerReloadListener(FaucetBehaviorsManager.RELOAD_INSTANCE, res("faucet_interactions"));
        PlatHelper.addServerReloadListener(AdventurerMapsHandler.RELOAD_INSTANCE, res("structure_maps"));
        PlatHelper.addServerReloadListener(CapturedMobHandler.RELOAD_INSTANCE, res("catchable_mobs_properties"));

        if (PlatHelper.getPhysicalSide().isClient()) {
            ClientDynamicResourcesGenerator.INSTANCE.register();

            try {
                ClientHelper.registerOptionalTexturePack(res("darker_ropes"));
            } catch (Exception e) {
                Supplementaries.LOGGER.error(e);
            }
        }
    }

    //yes this is where I write crap. deal with it XD

    //sled emissions skulk

    //async setup stuff
    // sack and sled texture bugs
    //HH cookies in sacks
    //use mixin extras
    //refactor moonlight to use objects
//item frame opt mod
    //sack insert sound
//3d jukebox thingie
    //snowy spirit disc
    //chef villager mod
    //mystical tree index out of bound
    //caibrated stuff
    //Tree chat gtp
    //tree splash text
    //lectern colors in gui
    //dummy goes down and hay particles

    //better sounds for item dislays
    //item lore clear

    //finish bedbugs
    //blaze head ghast

    //bellows push hanging signs and globe
    //create sprout support
    //quark gui sack open
    //heartstone highlight and pulse when nearby

    //enchantable horse armor
    //cow remodel
    //sheep animations and textyres
    //3d particle mod
    //quiver not rendering in curio
    //pulley flint
    //TODO relayer on piston retract
    //mod to wax anything to prevent interaction
    //sugar block fall in water
    //soap in water makes soap particles
    //chains pull down candle holders and lanterns


    //punching swings lanterns
    //wind physics for wind vane

    //ash makes mobs jump
    //squishy piston launcher. also rework them and fix on servers
    //camera mod with screenshots

    //clicking on cage with lead will put the leashed animal inside
    //wrench rotation overlay
    //create moving dynamic blocks like rope knot
    //jei villagers addon
    //corona mod
    //trollium interaction mod
    //ash jei plugin
    //bubble sound for bellows
    //bundle sound for sacks

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


    //firefly glow block

    //TODO: bugs: bell ropes(add to flywheel instance), brewing stand colors(?)

    //TODO: mod ideas: particle block, blackboard banners and flags, lantern holding

    //TODO: add stick window loggable clipping

    //flute animation fix

    //add shift middle click to swap to correct tool

    //mod idea: blackboard banners and flags with villager

    //throwable slimeballs

    //simple mode for doors and trapdoors

    //animated pulley texture

    //TODO: faucets create sprout

    // randomium item particle when drop

    //TODO: xp bottling whose cost depends on player total xp

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

    //blackboard otline gui+

    //soap signs & finish notice board dye (add dye interface)
    //snow real magic compat
    //bugs: spring launcher broken on servers


}
