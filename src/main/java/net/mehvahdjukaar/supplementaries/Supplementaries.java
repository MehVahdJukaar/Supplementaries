package net.mehvahdjukaar.supplementaries;

import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesRegistry;
import net.mehvahdjukaar.supplementaries.common.items.crafting.OptionalRecipeCondition;
import net.mehvahdjukaar.supplementaries.common.world.generation.WorldGenHandler;
import net.mehvahdjukaar.supplementaries.configs.ConfigHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
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

        var serverRes = new ServerDynamicResourcesHandler();
        serverRes.register(bus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            var clientRes = new ClientDynamicResourcesHandler();
            clientRes.register(bus);

            //wall lantern and jar jsons
            if(!FMLLoader.getLaunchHandler().isData()) {
                ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                        .registerReloadListener(new WallLanternTexturesRegistry());
            }
        }

        bus.addListener(ModSetup::init);

    }

    //yes this is where I write crap. deal with it XD
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
