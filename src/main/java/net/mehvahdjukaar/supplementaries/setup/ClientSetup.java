package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.NoticeBoardGui;
import net.mehvahdjukaar.supplementaries.client.gui.PulleyBlockGui;
import net.mehvahdjukaar.supplementaries.client.gui.SackGui;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.FluidColors;
import net.mehvahdjukaar.supplementaries.client.renderers.TippedSpikesColor;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.FireflyEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.MashlingEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RopeArrowRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.entities.MashlingEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {



    //TODO: figure out why this is making everything crash without ONLY in
    //TODO: remove this onlyIn
    @OnlyIn(Dist.CLIENT)
    public static void onlyClientPls(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registry.BOMB.get(),
                renderManager -> new SpriteRenderer<>(renderManager, event.getMinecraftSupplier().get().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(Registry.THROWABLE_BRICK.get(),
                renderManager -> new SpriteRenderer<>(renderManager, event.getMinecraftSupplier().get().getItemRenderer()));
    }



    public static void init(final FMLClientSetupEvent event) {

        RenderingRegistry.registerEntityRenderingHandler((EntityType<MashlingEntity>) Registry.POTAT_TYPE, MashlingEntityRenderer::new);

        //falling block tile entity
        //RenderingRegistry.registerEntityRenderingHandler( (EntityType<FallingBlockTileEntity>) Registry.FALLING_BLOCK_TILE_ENTITY.get(),
        //        FallingBlockRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(Registry.ROPE_ARROW.get(), RopeArrowRenderer::new);
        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler((EntityType<FireflyEntity>) Registry.FIREFLY_TYPE, FireflyEntityRenderer::new);
        RenderTypeLookup.setRenderLayer(Registry.FIREFLY_JAR.get(), RenderType.getCutout());

        //throwable brick
        onlyClientPls(event);
        //planter
        //RenderTypeLookup.setRenderLayer(Registry.PLANTER.get(), RenderType.getCutout());
        //clock
        //RenderTypeLookup.setRenderLayer(Registry.CLOCK_BLOCK.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        //RenderTypeLookup.setRenderLayer(Registry.PEDESTAL.get(), RenderType.getCutout());
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
        RenderTypeLookup.setRenderLayer(Registry.JAR_TINTED.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer(Registry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(Registry.FAUCET.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        //RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER.get(), RenderType.getCutout());
        //RenderTypeLookup.setRenderLayer(Registry.PISTON_LAUNCHER_HEAD.get(), RenderType.getCutout());
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
        //laser
        ClientRegistry.bindTileEntityRenderer(Registry.LASER_BLOCK_TILE.get(), LaserBlockTileRenderer::new);
        //flag
        RenderTypeLookup.setRenderLayer(Registry.FLAG.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
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
        ClientRegistry.bindTileEntityRenderer(Registry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        //cage
        RenderTypeLookup.setRenderLayer(Registry.CAGE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        //sconce lever
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_LEVER.get(), RenderType.getCutout());
        //globe
        //RenderTypeLookup.setRenderLayer(Registry.GLOBE.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        //hourglass
        RenderTypeLookup.setRenderLayer(Registry.HOURGLASS.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        //sack
        //RenderTypeLookup.setRenderLayer(Registry.SACK.get(), RenderType.getCutout());
        ScreenManager.registerFactory(Registry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        RenderTypeLookup.setRenderLayer(Registry.BLACKBOARD.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        //soul jar
        RenderTypeLookup.setRenderLayer(Registry.SOUL_JAR.get(), RenderType.getTranslucent());
        ClientRegistry.bindTileEntityRenderer(Registry.FIREFLY_JAR_TILE.get(), SoulJarBlockTileRenderer::new);
        //copper lantern
        RenderTypeLookup.setRenderLayer(Registry.COPPER_LANTERN.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        //crimson lantern
        RenderTypeLookup.setRenderLayer(Registry.CRIMSON_LANTERN.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CRIMSON_LANTERN_TILE.get(), CrimsonLanternBlockTileRenderer::new);
        //doormat
        ClientRegistry.bindTileEntityRenderer(Registry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        //hanging flower pot
        RenderTypeLookup.setRenderLayer(Registry.HANGING_FLOWER_POT.get(), RenderType.getCutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);
        //gold door & trapdoor
        RenderTypeLookup.setRenderLayer(Registry.GOLD_DOOR.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.GOLD_TRAPDOOR.get(), RenderType.getCutout());
        //spikes
        RenderTypeLookup.setRenderLayer(Registry.BAMBOO_SPIKES.get(), RenderType.getCutout());
        //netherite door & trapdoor
        RenderTypeLookup.setRenderLayer(Registry.NETHERITE_DOOR.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.NETHERITE_TRAPDOOR.get(), RenderType.getCutout());
        //rope
        RenderTypeLookup.setRenderLayer(Registry.ROPE.get(), RenderType.getCutout());
        //flax
        RenderTypeLookup.setRenderLayer(Registry.FLAX.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(Registry.FLAX_POT.get(), RenderType.getCutout());
        //pulley
        ScreenManager.registerFactory(Registry.PULLEY_BLOCK_CONTAINER.get(), PulleyBlockGui::new);
        //boat
        RenderTypeLookup.setRenderLayer(Registry.JAR_BOAT.get(), RenderType.getTranslucent());
    }

    //particles

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particles.registerFactory(Registry.FIREFLY_GLOW.get(), FireflyGlowParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.ENDERGETIC_FLAME.get(), FlameParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.GREEN_FLAME.get(), FlameParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.DRIPPING_LIQUID.get(), DrippingLiquidParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.FALLING_LIQUID.get(), FallingLiquidParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.SPLASHING_LIQUID.get(), SplashingLiquidParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.BOMB_EXPLOSION_PARTICLE.get(), BombExplosionParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(Registry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), new BombExplosionEmitterParticle.Factory());
        Minecraft.getInstance().particles.registerFactory(Registry.BOMB_SMOKE_PARTICLE.get(), BombSmokeParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event){
        event.getBlockColors().register(new TippedSpikesColor(), Registry.BAMBOO_SPIKES.get());
        event.getBlockColors().register(new defWater(), Registry.JAR_BOAT.get());

    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event){
        event.getItemColors().register(new TippedSpikesColor(), Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        event.getItemColors().register(new defWater(), Registry.JAR_BOAT_ITEM.get());
    }

    private static class defWater implements IItemColor, IBlockColor {

        @Override
        public int getColor(ItemStack stack, int color) {
            return 0x3F76E4;
        }

        @Override
        public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int color) {
            return reader != null && pos != null ? BiomeColors.getWaterColor(reader, pos) : -1;
        }
    }

    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if(event.getMap().getTextureLocation().equals(Atlases.SIGN_ATLAS)){
            for(IWoodType type : WoodTypes.TYPES.values()){
                //TODO: make hanging sign use java model
                //event.addSprite(HANGING_SIGNS_TEXTURES.get(type));
                event.addSprite(Textures.SIGN_POSTS_TEXTURES.get(type));
            }
        }
        if (event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
            for (ResourceLocation r : Textures.getBlockTextures()) {
                event.addSprite(r);
            }
        }
    }

    @SubscribeEvent
    public static void onResourcePackChanged(ModelBakeEvent event) {
        FluidColors.refresh();
    }


}
