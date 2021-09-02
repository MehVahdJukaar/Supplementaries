package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.gui.*;
import net.mehvahdjukaar.supplementaries.client.models.*;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.color.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.*;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandlerClient;
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
import net.minecraft.state.BooleanProperty;
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


    @OnlyIn(Dist.CLIENT)
    public static void init(final FMLClientSetupEvent event) {

        //compat
        CompatHandlerClient.init(event);

        //map markers
        CMDclient.init(event);

        //projectiles
        ItemRenderer itemRenderer = event.getMinecraftSupplier().get().getItemRenderer();

        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.BOMB.get(),
                renderManager -> new SpriteRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.THROWABLE_BRICK.get(),
                renderManager -> new SpriteRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.LABEL.get(),
                renderManager -> new LabelEntityRenderer(renderManager, itemRenderer));

        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.AMETHYST_SHARD.get(),
                ShardProjectileRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.FLINT_SHARD.get(),
                ShardProjectileRenderer::new);


        //dynamic textures
        GlobeTextureManager.init(Minecraft.getInstance().textureManager);
        BlackboardTextureManager.init(Minecraft.getInstance().textureManager);


        //orange trader
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.RED_MERCHANT_TYPE.get(), OrangeTraderEntityRenderer::new);
        ScreenManager.register(ModRegistry.RED_MERCHANT_CONTAINER.get(), OrangeMerchantGui::new);

        //rope arrow
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.ROPE_ARROW.get(), RopeArrowRenderer::new);
        //amethyst arrow
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.AMETHYST_ARROW.get(), AmethystArrowRenderer::new);

        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.FIREFLY_TYPE.get(), FireflyEntityRenderer::new);
        RenderTypeLookup.setRenderLayer(ModRegistry.FIREFLY_JAR.get(), RenderType.cutout());
        //clock
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        ClientRegistry.bindTileEntityRenderer(ModRegistry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        RenderTypeLookup.setRenderLayer(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer(ModRegistry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        ScreenManager.register(ModRegistry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        RenderTypeLookup.setRenderLayer(ModRegistry.CRANK.get(), RenderType.cutout());
        //jar
        RenderTypeLookup.setRenderLayer(ModRegistry.JAR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.JAR_TINTED.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        RenderTypeLookup.setRenderLayer(ModRegistry.FAUCET.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        ClientRegistry.bindTileEntityRenderer(ModRegistry.PISTON_LAUNCHER_ARM_TILE.get(), PistonLauncherArmBlockTileRenderer::new);
        //sign post
        RenderTypeLookup.setRenderLayer(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        //hanging sign
        //RenderTypeLookup.setRenderLayer(Registry.HANGING_SIGNS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        //wall lantern
        RenderTypeLookup.setRenderLayer(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        //bellows
        RenderTypeLookup.setRenderLayer(ModRegistry.BELLOWS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        //laser
        ClientRegistry.bindTileEntityRenderer(ModRegistry.LASER_BLOCK_TILE.get(), LaserBlockTileRenderer::new);
        //flag
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        //sconce
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_WALL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_WALL_SOUL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_SOUL.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_WALL_ENDER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_ENDER.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_WALL_GLOW.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_GLOW.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_WALL_GREEN.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_GREEN.get(), RenderType.cutout());
        //candelabra
        RenderTypeLookup.setRenderLayer(ModRegistry.CANDELABRA.get(), RenderType.cutout());
        //item shelf
        RenderTypeLookup.setRenderLayer(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        //cage
        RenderTypeLookup.setRenderLayer(ModRegistry.CAGE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        //sconce lever
        RenderTypeLookup.setRenderLayer(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        //globe
        ClientRegistry.bindTileEntityRenderer(ModRegistry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        //hourglass
        RenderTypeLookup.setRenderLayer(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        //sack
        ScreenManager.register(ModRegistry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        RenderTypeLookup.setRenderLayer(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        //soul jar
        RenderTypeLookup.setRenderLayer(ModRegistry.SOUL_JAR.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FIREFLY_JAR_TILE.get(), SoulJarBlockTileRenderer::new);
        //copper lantern
        RenderTypeLookup.setRenderLayer(ModRegistry.COPPER_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        //brass lantern
        RenderTypeLookup.setRenderLayer(ModRegistry.BRASS_LANTERN.get(), RenderType.cutout());
        //crimson lantern
        RenderTypeLookup.setRenderLayer(ModRegistry.CRIMSON_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CRIMSON_LANTERN_TILE.get(), CrimsonLanternBlockTileRenderer::new);
        //doormat
        ClientRegistry.bindTileEntityRenderer(ModRegistry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        //hanging flower pot
        RenderTypeLookup.setRenderLayer(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);
        //gold door & trapdoor
        RenderTypeLookup.setRenderLayer(ModRegistry.GOLD_DOOR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        //spikes
        RenderTypeLookup.setRenderLayer(ModRegistry.BAMBOO_SPIKES.get(), RenderType.cutout());
        //netherite door & trapdoor
        RenderTypeLookup.setRenderLayer(ModRegistry.NETHERITE_DOOR.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        //rope
        RenderTypeLookup.setRenderLayer(ModRegistry.ROPE.get(), RenderType.cutout());
        //flax
        RenderTypeLookup.setRenderLayer(ModRegistry.FLAX.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.FLAX_WILD.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.FLAX_POT.get(), RenderType.cutout());
        //pulley
        ScreenManager.register(ModRegistry.PULLEY_BLOCK_CONTAINER.get(), PulleyBlockGui::new);
        //boat
        RenderTypeLookup.setRenderLayer(ModRegistry.JAR_BOAT.get(), RenderType.translucent());
        //magma cream block
        RenderTypeLookup.setRenderLayer(ModRegistry.MAGMA_CREAM_BLOCK.get(), RenderType.translucent());
        //flower box
        RenderTypeLookup.setRenderLayer(ModRegistry.FLOWER_BOX.get(), RenderType.cutout());
        //timber frames
        RenderTypeLookup.setRenderLayer(ModRegistry.TIMBER_FRAME.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.TIMBER_BRACE.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(ModRegistry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        //goblet
        ClientRegistry.bindTileEntityRenderer(ModRegistry.GOBLET_TILE.get(), GobletBlockTileRenderer::new);
        //cog block
        RenderTypeLookup.setRenderLayer(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        //ceiling banner
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        //statue
        ClientRegistry.bindTileEntityRenderer(ModRegistry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        //iron gate
        RenderTypeLookup.setRenderLayer(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        //gold gate
        RenderTypeLookup.setRenderLayer(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        //cracked bell
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CRACKED_BELL_TILE.get(), CrackedBellTileEntityRenderer::new);
        //present
        ScreenManager.register(ModRegistry.PRESENT_BLOCK_CONTAINER.get(), PresentBlockGui.GUI_FACTORY);
        //gunpowder
        RenderTypeLookup.setRenderLayer(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());

        ItemModelsProperties.register(Items.CROSSBOW, new ResourceLocation("rope_arrow"),
                (stack, world, entity) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, ModRegistry.ROPE_ARROW_ITEM.get()) ? 1.0F : 0.0F);

        ItemModelsProperties.register(Items.CROSSBOW, new ResourceLocation("amethyst_arrow"),
                (stack, world, entity) -> entity != null && CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, ModRegistry.AMETHYST_ARROW_ITEM.get()) ? 1.0F : 0.0F);

        //Registry.PRESENTS_ITEMS.values().forEach(i ->
         //       ItemModelsProperties.register(i.get(), new ResourceLocation("packed"),
        //                (stack, world, entity) -> PresentBlockTile.isPacked(stack) ? 1.0F : 0.0F));

        //ItemModelsProperties.register(Registry.SPEEDOMETER_ITEM.get(), new ResourceLocation("speed"),
        //        new SpeedometerItem.SpeedometerItemProperty());

    }




    //particles
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        ParticleManager particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(ModRegistry.FIREFLY_GLOW.get(), FireflyGlowParticle.Factory::new);
        particleManager.register(ModRegistry.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);
        particleManager.register(ModRegistry.GREEN_FLAME.get(), FlameParticle.Factory::new);
        particleManager.register(ModRegistry.DRIPPING_LIQUID.get(), DrippingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.FALLING_LIQUID.get(), FallingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.SPLASHING_LIQUID.get(), SplashingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.BOMB_EXPLOSION_PARTICLE.get(), BombExplosionParticle.Factory::new);
        particleManager.register(ModRegistry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), new BombExplosionEmitterParticle.Factory());
        particleManager.register(ModRegistry.BOMB_SMOKE_PARTICLE.get(), BombSmokeParticle.Factory::new);
        particleManager.register(ModRegistry.BOTTLING_XP_PARTICLE.get(), BottlingXpParticle.Factory::new);
        particleManager.register(ModRegistry.FEATHER_PARTICLE.get(), FeatherParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event){
        BlockColors colors = event.getBlockColors();
        colors.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES.get());
        colors.register(new DefWaterColor(), ModRegistry.JAR_BOAT.get());
        colors.register(new BrewingStandColor(), Blocks.BREWING_STAND);
        colors.register(new MimicBlockColor(), ModRegistry.SIGN_POST.get(), ModRegistry.TIMBER_BRACE.get(), ModRegistry.TIMBER_FRAME.get(),
                ModRegistry.TIMBER_CROSS_BRACE.get(), ModRegistry.WALL_LANTERN.get());
        colors.register(new CogBlockColor(), ModRegistry.COG_BLOCK.get());
        colors.register(new GunpowderBlockColor(), ModRegistry.GUNPOWDER_BLOCK.get());

    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event){
        ItemColors colors = event.getItemColors();
        colors.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        colors.register(new DefWaterColor(), ModRegistry.JAR_BOAT_ITEM.get());
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
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "mimic_block_loader"), new SignPostBlockLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "rope_knot_loader"), new RopeKnotBlockLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "wall_lantern_loader"), new WallLanternLoader());
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "flower_box_loader"), new FlowerBoxLoader());



        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "blackboard_loader"), new BlackboardBlockLoader());

        //fake models & blockstates
        registerStaticBlockState(ModRegistry.LABEL.get().getRegistryName(), Blocks.AIR, "jar");
        registerStaticBlockState(WindVaneBlockTileRenderer.MODEL_RES, Blocks.AIR);

    }

    private static void registerStaticBlockState(ResourceLocation name, Block parent, String... booleanProperties){
        Map<ResourceLocation, StateContainer<Block, BlockState>> mapCopy = new HashMap<>(ModelBakery.STATIC_DEFINITIONS);

        StateContainer.Builder<Block, BlockState> builder = (new StateContainer.Builder<>(parent));

        for(String p : booleanProperties) builder.add(BooleanProperty.create(p));

        mapCopy.put(name, builder.create(Block::defaultBlockState, BlockState::new));

        ModelBakery.STATIC_DEFINITIONS = mapCopy;
    }


}
