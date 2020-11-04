package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardGui;
import net.mehvahdjukaar.supplementaries.particles.FireflyGlowParticle;
import net.mehvahdjukaar.supplementaries.particles.SpeakerSoundParticle;
import net.mehvahdjukaar.supplementaries.renderers.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event){

        //planter
        RenderTypeLookup.setRenderLayer(Registry.PLANTER.get(), RenderType.getCutout());
        //clock
        RenderTypeLookup.setRenderLayer(Registry.CLOCK_BLOCK.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        RenderTypeLookup.setRenderLayer(Registry.PEDESTAL.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(Registry.WIND_VANE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer(Registry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        ScreenManager.registerFactory(Registry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        RenderTypeLookup.setRenderLayer(Registry.CRANK.get(), RenderType.getCutout());
        //jar
        RenderTypeLookup.setRenderLayer(Registry.JAR.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(Registry.FAUCET.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER_HEAD.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.PISTON_LAUNCHER_ARM_TILE.get(), PistonLauncherArmBlockTileRenderer::new);
        //sign post
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        //hanging sign
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        //wall lantern
        RenderTypeLookup.setRenderLayer(Registry.WALL_LANTERN.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        //bellows
        RenderTypeLookup.setRenderLayer(Registry.BELLOWS.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);

    }

    //particles

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(Registry.FIREFLY_GLOW.get(), FireflyGlowParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);

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
