package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.*;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardGui;
import net.mehvahdjukaar.supplementaries.particles.FireflyGlowParticle;
import net.mehvahdjukaar.supplementaries.particles.SpeakerSoundParticle;
import net.mehvahdjukaar.supplementaries.renderers.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
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

    public static void init(final FMLClientSetupEvent event){

        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler( (EntityType<FireflyEntity>) Registry.FIREFLY_TYPE, FireflyEntityRenderer::new);
        RenderTypeLookup.setRenderLayer(Registry.FIREFLY_JAR, RenderType.getCutout());

        //planter
        RenderTypeLookup.setRenderLayer(Registry.PLANTER, RenderType.getCutout());
        //clock
        RenderTypeLookup.setRenderLayer(Registry.CLOCK_BLOCK, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<ClockBlockTile>)Registry.CLOCK_BLOCK_TILE, ClockBlockTileRenderer::new);
        //pedestal
        RenderTypeLookup.setRenderLayer(Registry.PEDESTAL, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<PedestalBlockTile>)Registry.PEDESTAL_TILE, PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(Registry.WIND_VANE, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<WindVaneBlockTile>)Registry.WIND_VANE_TILE, WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer((TileEntityType<NoticeBoardBlockTile>)Registry.NOTICE_BOARD_TILE, NoticeBoardBlockTileRenderer::new);
        ScreenManager.registerFactory((ContainerType<NoticeBoardContainer>)Registry.NOTICE_BOARD_CONTAINER, NoticeBoardGui::new);
        //crank
        RenderTypeLookup.setRenderLayer(Registry.CRANK, RenderType.getCutout());
        //jar
        RenderTypeLookup.setRenderLayer(Registry.JAR, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.JAR_TINTED, RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<JarBlockTile>)Registry.JAR_TILE, JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(Registry.FAUCET, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FaucetBlockTile>)Registry.FAUCET_TILE, FaucetBlockTileRenderer::new);
        //piston launcher
        RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER_HEAD, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<PistonLauncherArmBlockTile>)Registry.PISTON_LAUNCHER_ARM_TILE, PistonLauncherArmBlockTileRenderer::new);
        //sign post
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<SignPostBlockTile>)Registry.SIGN_POST_TILE, SignPostBlockTileRenderer::new);
        //hanging sign
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<HangingSignBlockTile>)Registry.HANGING_SIGN_TILE, HangingSignBlockTileRenderer::new);
        //wall lantern
        RenderTypeLookup.setRenderLayer(Registry.WALL_LANTERN, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<WallLanternBlockTile>)Registry.WALL_LANTERN_TILE, WallLanternBlockTileRenderer::new);
        //bellows
        RenderTypeLookup.setRenderLayer(Registry.BELLOWS, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<BellowsBlockTile>)Registry.BELLOWS_TILE, BellowsBlockTileRenderer::new);
        //laser
        ClientRegistry.bindTileEntityRenderer((TileEntityType<LaserBlockTile>)Registry.LASER_BLOCK_TILE, LaserBlockTileRenderer::new);
        //flag
        RenderTypeLookup.setRenderLayer(Registry.FLAG, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<FlagBlockTile>)Registry.FLAG_TILE, FlagBlockTileRenderer::new);
        //drawers
        //RenderTypeLookup.setRenderLayer(Registry.DRAWERS, RenderType.getCutout());
        //ClientRegistry.bindTileEntityRenderer(Registry.DRAWERS_TILE, DrawerBlockTileRenderer::new);
        //sconce
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_SOUL, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_SOUL, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_ENDER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_ENDER, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_GREEN, RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_GREEN, RenderType.getCutout());
        //candelabra
        RenderTypeLookup.setRenderLayer(Registry.CANDELABRA, RenderType.getCutout());
        //item shelf
        RenderTypeLookup.setRenderLayer(Registry.ITEM_SHELF, RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer((TileEntityType<ItemShelfBlockTile>)Registry.ITEM_SHELF_TILE, ItemShelfBlockTileRenderer::new);


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
        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            return;
        }
        List<ResourceLocation> l = CommonUtil.getTextures();
        for(ResourceLocation r : l) {
            event.addSprite(r);
        }
    }


}
