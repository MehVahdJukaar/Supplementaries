package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.*;
import net.mehvahdjukaar.supplementaries.client.Textures;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.client.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.client.gui.NoticeBoardGui;
import net.mehvahdjukaar.supplementaries.client.gui.SackContainer;
import net.mehvahdjukaar.supplementaries.client.gui.SackGui;
import net.mehvahdjukaar.supplementaries.client.particles.FireflyGlowParticle;
import net.mehvahdjukaar.supplementaries.client.particles.SpeakerSoundParticle;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.FireflyEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    //TODO: figure out why this is making everything crash without ONLY in
    //TODO: remove this onlyIn
    @OnlyIn(Dist.CLIENT)
    public static void onlyClientPls(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registry.THROWABLE_BRICK,
                renderManager -> new SpriteRenderer(renderManager, event.getMinecraftSupplier().get().getItemRenderer()));

    }

    public static void init(final FMLClientSetupEvent event) {

        //falling block tile entity
        //RenderingRegistry.registerEntityRenderingHandler( (EntityType<FallingBlockTileEntity>) Registry.FALLING_BLOCK_TILE_ENTITY.get(),
        //        FallingBlockRenderer::new);


        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler((EntityType<FireflyEntity>) Registry.FIREFLY_TYPE, FireflyEntityRenderer::new);
        RenderTypeLookup.setRenderLayer(Registry.FIREFLY_JAR.get(), RenderType.getCutout());

        //throwable brick
        onlyClientPls(event);
        //planter
        //RenderTypeLookup.setRenderLayer(Registry.PLANTER.get(), RenderType.getCutout());
        //clock
        //RenderTypeLookup.setRenderLayer(Registry.CLOCK_BLOCK.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<ClockBlockTile>) Registry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        //RenderTypeLookup.setRenderLayer(Registry.PEDESTAL.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<PedestalBlockTile>) Registry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(Registry.WIND_VANE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<WindVaneBlockTile>) Registry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer((TileEntityType<NoticeBoardBlockTile>) Registry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        ScreenManager.registerFactory((ContainerType<NoticeBoardContainer>) Registry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        RenderTypeLookup.setRenderLayer(Registry.CRANK.get(), RenderType.getCutout());
        //jar
        RenderTypeLookup.setRenderLayer(Registry.JAR.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.JAR_TINTED.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<JarBlockTile>) Registry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(Registry.FAUCET.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FaucetBlockTile>) Registry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        //RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER.get(), RenderType.getCutout());
        //RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER_HEAD.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<PistonLauncherArmBlockTile>) Registry.PISTON_LAUNCHER_ARM_TILE.get(), PistonLauncherArmBlockTileRenderer::new);
        //sign post
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<SignPostBlockTile>) Registry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        //hanging sign
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<HangingSignBlockTile>) Registry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        //wall lantern
        RenderTypeLookup.setRenderLayer(Registry.WALL_LANTERN.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<WallLanternBlockTile>) Registry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        //bellows
        RenderTypeLookup.setRenderLayer(Registry.BELLOWS.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<BellowsBlockTile>) Registry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        //laser
        ClientRegistry.bindTileEntityRenderer((TileEntityType<LaserBlockTile>) Registry.LASER_BLOCK_TILE.get(), LaserBlockTileRenderer::new);
        //flag
        RenderTypeLookup.setRenderLayer(Registry.FLAG.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FlagBlockTile>) Registry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        //drawers
        //RenderTypeLookup.setRenderLayer(Registry.DRAWERS.get(), RenderType.getCutout());
        //ClientRegistry.bindTileEntityRenderer(Registry.DRAWERS_TILE.get(), DrawerBlockTileRenderer::new);
        //sconce
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_SOUL.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_SOUL.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_ENDER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_ENDER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_GREEN.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_GREEN.get(), RenderType.getCutout());
        //candelabra
        RenderTypeLookup.setRenderLayer(Registry.CANDELABRA.get(), RenderType.getCutout());
        //item shelf
        RenderTypeLookup.setRenderLayer(Registry.ITEM_SHELF.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<ItemShelfBlockTile>) Registry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        //cage
        RenderTypeLookup.setRenderLayer(Registry.CAGE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<CageBlockTile>) Registry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        //sconce lever
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_LEVER.get(), RenderType.getCutout());
        //globe
        //RenderTypeLookup.setRenderLayer(Registry.GLOBE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<GlobeBlockTile>) Registry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        //hourglass
        RenderTypeLookup.setRenderLayer(Registry.HOURGLASS.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<HourGlassBlockTile>) Registry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        //sack
        //RenderTypeLookup.setRenderLayer(Registry.SACK.get(), RenderType.getCutout());
        ScreenManager.registerFactory((ContainerType<SackContainer>) Registry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        RenderTypeLookup.setRenderLayer(Registry.BLACKBOARD.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<BlackboardBlockTile>) Registry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        //soul jar
        RenderTypeLookup.setRenderLayer(Registry.SOUL_JAR.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FireflyJarBlockTile>) Registry.FIREFLY_JAR_TILE.get(), SoulJarBlockTileRenderer::new);
        //copper lantern
        RenderTypeLookup.setRenderLayer(Registry.COPPER_LANTERN.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<OilLanternBlockTile>) Registry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        //doormat
        ClientRegistry.bindTileEntityRenderer((TileEntityType<DoormatBlockTile>) Registry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        //hanging flower pot
        RenderTypeLookup.setRenderLayer(Registry.HANGING_FLOWER_POT.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<HangingFlowerPotBlockTile>) Registry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);


    }

    //particles

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(Registry.FIREFLY_GLOW, FireflyGlowParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.SPEAKER_SOUND, SpeakerSoundParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.ENDERGETIC_FLAME, FlameParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.GREEN_FLAME, FlameParticle.Factory::new);
    }


    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {

        Textures.stitchAll(event);



    }

}
