package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.NoticeBoardGui;
import net.mehvahdjukaar.supplementaries.client.gui.OrangeMerchantGui;
import net.mehvahdjukaar.supplementaries.client.gui.PulleyBlockGui;
import net.mehvahdjukaar.supplementaries.client.gui.SackGui;
import net.mehvahdjukaar.supplementaries.client.models.FrameBlockLoader;
import net.mehvahdjukaar.supplementaries.client.models.MimicBlockLoader;
import net.mehvahdjukaar.supplementaries.client.models.RopeKnotBlockLoader;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.FireflyEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.LabelEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.OrangeTraderEntityRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RopeArrowRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.configured.ConfiguredCustomScreen;
import net.mehvahdjukaar.supplementaries.compat.decorativeblocks.DecoBlocksCompatClient;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.world.data.map.client.CMDclient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    //TODO: figure out why this is making everything crash without ONLY in
    //TODO: remove this onlyIn
    @OnlyIn(Dist.CLIENT)
    public static void onlyClientPls(final FMLClientSetupEvent event) {

        ItemRenderer itemRenderer = event.getMinecraftSupplier().get().getItemRenderer();
        RenderingRegistry.registerEntityRenderingHandler(Registry.BOMB.get(),
                renderManager -> new SpriteRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(Registry.THROWABLE_BRICK.get(),
                renderManager -> new SpriteRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(Registry.LABEL.get(),
                renderManager -> new LabelEntityRenderer(renderManager, itemRenderer));
    }

    public static void init(final FMLClientSetupEvent event) {

        //compat
        if(CompatHandler.deco_blocks) DecoBlocksCompatClient.registerRenderLayers();
        //registers custom screen instead of default configured one
        if(CompatHandler.configured && RegistryConfigs.reg.CUSTOM_CONFIGURED_SCREEN.get()){
            try {
                ConfiguredCustomScreen.registerScreen();
            }catch (Exception e){ Supplementaries.LOGGER.warn("Failed to register custom configured screen: "+e);}
        }

        //throwable brick
        onlyClientPls(event);

        //map markers
        CMDclient.init(event);

        //dynamic textures
        GlobeTextureManager.init(Minecraft.getInstance().textureManager);
        BlackboardTextureManager.init(Minecraft.getInstance().textureManager);


        //orange trader
        RenderingRegistry.registerEntityRenderingHandler(Registry.ORANGE_TRADER.get(), OrangeTraderEntityRenderer::new);
        ScreenManager.register(Registry.ORANGE_TRADER_CONTAINER.get(), OrangeMerchantGui::new);

        //rope arrow
        RenderingRegistry.registerEntityRenderingHandler(Registry.ROPE_ARROW.get(), RopeArrowRenderer::new);
        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler(Registry.FIREFLY_TYPE.get(), FireflyEntityRenderer::new);
        RenderTypeLookup.setRenderLayer(Registry.FIREFLY_JAR.get(), RenderType.cutout());
        //clock
        ClientRegistry.bindTileEntityRenderer(Registry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        ClientRegistry.bindTileEntityRenderer(Registry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(Registry.WIND_VANE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer(Registry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        ScreenManager.register(Registry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        RenderTypeLookup.setRenderLayer(Registry.CRANK.get(), RenderType.cutout());
        //jar
        RenderTypeLookup.setRenderLayer(Registry.JAR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.JAR_TINTED.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(Registry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(Registry.FAUCET.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        ClientRegistry.bindTileEntityRenderer(Registry.PISTON_LAUNCHER_ARM_TILE.get(), PistonLauncherArmBlockTileRenderer::new);
        //sign post
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.solid());
        ClientRegistry.bindTileEntityRenderer(Registry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        //hanging sign
        RenderTypeLookup.setRenderLayer(Registry.SIGN_POST.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        //wall lantern
        RenderTypeLookup.setRenderLayer(Registry.WALL_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        //bellows
        RenderTypeLookup.setRenderLayer(Registry.BELLOWS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        //laser
        ClientRegistry.bindTileEntityRenderer(Registry.LASER_BLOCK_TILE.get(), LaserBlockTileRenderer::new);
        //flag
        ClientRegistry.bindTileEntityRenderer(Registry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        //drawers
        //RenderTypeLookup.setRenderLayer(Registry.DRAWERS.get(), RenderType.getCutout());
        //ClientRegistry.bindTileEntityRenderer(Registry.DRAWERS_TILE.get(), DrawerBlockTileRenderer::new);
        //sconce
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_SOUL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_SOUL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_ENDER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_ENDER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_GLOW.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_GLOW.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_WALL_GREEN.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_GREEN.get(), RenderType.cutout());
        //candelabra
        RenderTypeLookup.setRenderLayer(Registry.CANDELABRA.get(), RenderType.cutout());
        //item shelf
        RenderTypeLookup.setRenderLayer(Registry.ITEM_SHELF.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        //cage
        RenderTypeLookup.setRenderLayer(Registry.CAGE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        //sconce lever
        RenderTypeLookup.setRenderLayer(Registry.SCONCE_LEVER.get(), RenderType.cutout());
        //globe
        ClientRegistry.bindTileEntityRenderer(Registry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        //hourglass
        RenderTypeLookup.setRenderLayer(Registry.HOURGLASS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        //sack
        ScreenManager.register(Registry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        RenderTypeLookup.setRenderLayer(Registry.BLACKBOARD.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        //soul jar
        RenderTypeLookup.setRenderLayer(Registry.SOUL_JAR.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(Registry.FIREFLY_JAR_TILE.get(), SoulJarBlockTileRenderer::new);
        //copper lantern
        RenderTypeLookup.setRenderLayer(Registry.COPPER_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        //brass lantern
        RenderTypeLookup.setRenderLayer(Registry.BRASS_LANTERN.get(), RenderType.cutout());
        //crimson lantern
        RenderTypeLookup.setRenderLayer(Registry.CRIMSON_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.CRIMSON_LANTERN_TILE.get(), CrimsonLanternBlockTileRenderer::new);
        //doormat
        ClientRegistry.bindTileEntityRenderer(Registry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        //hanging flower pot
        RenderTypeLookup.setRenderLayer(Registry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);
        //gold door & trapdoor
        RenderTypeLookup.setRenderLayer(Registry.GOLD_DOOR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        //spikes
        RenderTypeLookup.setRenderLayer(Registry.BAMBOO_SPIKES.get(), RenderType.cutout());
        //netherite door & trapdoor
        RenderTypeLookup.setRenderLayer(Registry.NETHERITE_DOOR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        //rope
        RenderTypeLookup.setRenderLayer(Registry.ROPE.get(), RenderType.cutout());
        //flax
        RenderTypeLookup.setRenderLayer(Registry.FLAX.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.FLAX_POT.get(), RenderType.cutout());
        //pulley
        ScreenManager.register(Registry.PULLEY_BLOCK_CONTAINER.get(), PulleyBlockGui::new);
        //boat
        RenderTypeLookup.setRenderLayer(Registry.JAR_BOAT.get(), RenderType.translucent());
        //magma cream block
        RenderTypeLookup.setRenderLayer(Registry.MAGMA_CREAM_BLOCK.get(), RenderType.translucent());
        //flower box
        RenderTypeLookup.setRenderLayer(Registry.FLOWER_BOX.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(Registry.FLOWER_BOX_TILE.get(), FlowerBoxBlockTileRenderer::new);
        //timber frames
        RenderTypeLookup.setRenderLayer(Registry.TIMBER_FRAME.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.TIMBER_BRACE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(Registry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        //goblet
        ClientRegistry.bindTileEntityRenderer(Registry.GOBLET_TILE.get(), GobletBlockTileRenderer::new);
        //cog block
        RenderTypeLookup.setRenderLayer(Registry.COG_BLOCK.get(), RenderType.cutout());
        //ceiling banner
        ClientRegistry.bindTileEntityRenderer(Registry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        //statue
        ClientRegistry.bindTileEntityRenderer(Registry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        //iron gate
        RenderTypeLookup.setRenderLayer(Registry.IRON_GATE.get(), RenderType.cutout());
        //gold gate
        RenderTypeLookup.setRenderLayer(Registry.GOLD_GATE.get(), RenderType.cutout());
        //cracked bell
        ClientRegistry.bindTileEntityRenderer(Registry.CRACKED_BELL_TILE.get(), CrackedBellTileEntityRenderer::new);

        ItemModelsProperties.register(Items.CROSSBOW, new ResourceLocation("rope_arrow"),
                (stack, world, entity) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Registry.ROPE_ARROW_ITEM.get()) ? 1.0F : 0.0F);


    }


    //particles
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        ParticleManager particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(Registry.FIREFLY_GLOW.get(), FireflyGlowParticle.Factory::new);
        particleManager.register(Registry.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);
        particleManager.register(Registry.ENDERGETIC_FLAME.get(), FlameParticle.Factory::new);
        particleManager.register(Registry.GREEN_FLAME.get(), FlameParticle.Factory::new);
        particleManager.register(Registry.DRIPPING_LIQUID.get(), DrippingLiquidParticle.Factory::new);
        particleManager.register(Registry.FALLING_LIQUID.get(), FallingLiquidParticle.Factory::new);
        particleManager.register(Registry.SPLASHING_LIQUID.get(), SplashingLiquidParticle.Factory::new);
        particleManager.register(Registry.BOMB_EXPLOSION_PARTICLE.get(), BombExplosionParticle.Factory::new);
        particleManager.register(Registry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), new BombExplosionEmitterParticle.Factory());
        particleManager.register(Registry.BOMB_SMOKE_PARTICLE.get(), BombSmokeParticle.Factory::new);
        particleManager.register(Registry.BOTTLING_XP_PARTICLE.get(), BottlingXpParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event){
        BlockColors colors = event.getBlockColors();
        colors.register(new TippedSpikesColor(), Registry.BAMBOO_SPIKES.get());
        colors.register(new DefWaterColor(), Registry.JAR_BOAT.get());
        colors.register(new BrewingStandColor(), Blocks.BREWING_STAND);
        colors.register(new MimicBlockColor(), Registry.TIMBER_BRACE.get(),Registry.TIMBER_FRAME.get(),Registry.TIMBER_CROSS_BRACE.get());
        colors.register(new CogBlockColor(),Registry.COG_BLOCK.get());

    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event){
        ItemColors colors = event.getItemColors();
        colors.register(new TippedSpikesColor(), Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        colors.register(new DefWaterColor(), Registry.JAR_BOAT_ITEM.get());
        colors.register(new CrossbowColor(), Items.CROSSBOW);
    }

    private static class DefWaterColor implements IItemColor, IBlockColor {

        @Override
        public int getColor(ItemStack stack, int color) {
            return 0x3F76E4;
        }

        @Override
        public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int color) {
            return reader != null && pos != null ? BiomeColors.getAverageWaterColor(reader, pos) : -1;
        }
    }


    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if(event.getMap().location().equals(Atlases.SIGN_SHEET)){
            for(IWoodType type : WoodTypes.TYPES.values()){
                //TODO: make hanging sign use java model
                //event.addSprite(HANGING_SIGNS_TEXTURES.get(type));
                event.addSprite(Textures.SIGN_POSTS_TEXTURES.get(type));
            }
        }
        if (event.getMap().location().equals(AtlasTexture.LOCATION_BLOCKS)) {
            for (ResourceLocation r : Textures.getTexturesToStitch()) {
                event.addSprite(r);
            }
        }
    }


    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event){
        //loaders
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "frame_block_loader"), new FrameBlockLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "mimic_block_loader"), new MimicBlockLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "rope_knot_loader"), new RopeKnotBlockLoader());

        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "blackboard_loader"), new BlackboardBlockLoader());


        //fake models & blockstates
        Map<ResourceLocation, StateContainer<Block, BlockState>> tempMap = new HashMap<>(ModelBakery.STATIC_DEFINITIONS);
        tempMap.put(Registry.LABEL.get().getRegistryName(), LabelEntityRenderer.LABEL_FAKE_DEFINITION);
        ModelBakery.STATIC_DEFINITIONS = tempMap;
    }


}
