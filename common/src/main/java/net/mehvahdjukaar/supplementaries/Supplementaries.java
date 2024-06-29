package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.entities.trades.ModVillagerTrades;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.common.items.SliceMapItem;
import net.mehvahdjukaar.supplementaries.common.misc.ColoredMapHandler;
import net.mehvahdjukaar.supplementaries.common.misc.MapLightHandler;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.ModMapMarkers;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.WeatheredMap;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.CapturedMobHandler;
import net.mehvahdjukaar.supplementaries.common.misc.songs.SongsManager;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesGenerator;
import net.mehvahdjukaar.supplementaries.reg.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
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

        ModNetwork.init();

        RegHelper.registerSimpleRecipeCondition(res("flag"), CommonConfigs::isEnabled);

        MoonlightEventsHelper.addListener(ServerEvents::onFireConsume, IFireConsumeBlockEvent.class);

        ModSounds.init();
        ModFluids.init();
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
        InteractEventsHandler.init();
        SliceMapItem.init();
        WeatheredMap.init();
        ColoredMapHandler.init();
        MapLightHandler.init();

        ServerDynamicResourcesGenerator.INSTANCE.register();

        PlatHelper.addServerReloadListener(SongsManager.RELOAD_INSTANCE, res("flute_songs"));
        PlatHelper.addServerReloadListener(HourglassTimesManager.INSTANCE, res("hourglass_data"));
        PlatHelper.addServerReloadListener(FaucetBehaviorsManager.RELOAD_INSTANCE, res("faucet_interactions"));
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

    public static void error() {
        if (PlatHelper.isDev()) {
            LOGGER.error("This should not happen");
        }
    }

    public static void error(String message, Object... params) {
        error();
        LOGGER.error(message, params);
    }

    // test blackboard cannon and other 1 player containers in mutliplayer
    // test sounds and particles on server
    // test sculk
    // test dispenser and present stuff

    // yes this is where I write crap. deal with it XD
    // flute pacifier
    // wolf howl

    // vampires mummy cript necromancer curse stuff, skull trap block (nether fortess like big overworld boss)
    // warp fly and warper block
    // randomize enchant for quiver and lunch boxes
    // dispensers shoow quiver content
    // bundle and quiver dispenser interaction
    // zombie villagers spawn away from center. biome spawn
    // fletching table quiver fill
    // emi world interaction (ash)
    // raycon mod
    // luck makes crit happen
    // death marke off map
    // fisherman fish
    // middle click flower box stuff
    // more trades to villagers (planter)
    // villager that fishes. shepard that shears
    // soap washes trims
    // quiver holds fireworks
    // rope arrow inplace recipe
    // bellows blow ash out of ash block and IW ash one too
    // brush remove 1 layer ash or snow
    // brush works on blackboards
    // airpods portable jukebox mod
    //damage numbers mod
    //more IF compat (map stuff, markers with internals)
    //more flywheel stuff
    //Fix doormat highlight text box
    //steal from quark backpack
    //quark bubble hollow log and grates
    //blackboard gui to add color
    //wind mechanic mod with kite, paper lanterns...

    //hat + present jack in the box anim
//potion flask that works liqui quiver/big pot storage
    //group rally ping map aylas
    //identity hasmap vs object2objecthasmap
    //quark pipes projectiles
    //villagers regen health and trades when sleeping. malus otherwise
    //enchantment durability bar
    //heartstone pets with system taht keeps track in unloaded chunks
//sign post highlight text bug
    //villagers close eyelids sleep tight
    //sleep tight particles
    //snowy spirit bingerbread house
    //sled emissions skulk
    //egg crack sound HH
    //heartstone custom sounds

    //map atlas stuff
    //infested beds in woodland mansions
    //dummy particle mod for mobs
    //sack on head gives blindness
    //enderman animation teleport mod
    // cat loaf mod
    //dispenser brick use same texture

//cheap map recie and altimeter stuff
    //safe shulker recipe broken as it deletes items inside
    //sleep tight beds infested in mansion
    //dynamic candy bag with just tile
    //async setup stuff
    //HH cookies in sacks
    //farmland smarter farmers rich soil
//item frame opt mod
    //endrman teleport shader animation
    //villsger psrticle when inv is full
    //sack insert sound
    //chef villager mod
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

    //divining rod
    //add chain knot

    //enderman hold block in rain
    //horizontal shearable ropes

    //TODO: improve feather particle

    //use feather particle on spriggans

    //TODO: fix JER loot tables percentages

    //ghast fireball mob griefing


    //firefly glow block

    //TODO: bugs: bell ropes(add to flywheel instance), brewing stand colors(?)

    //TODO: mod ideas: particle block, blackboard banners and flags

    //TODO: add stick window loggable clipping

    //flute animation fix

    //add shift middle click to swap to correct tool

    //mod idea: blackboard banners and flags with villager

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
