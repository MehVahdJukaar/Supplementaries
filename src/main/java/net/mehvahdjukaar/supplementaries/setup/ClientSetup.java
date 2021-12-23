package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.*;
import net.mehvahdjukaar.supplementaries.client.block_models.*;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.color.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.FallingBlockRendererGeneric;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RedMerchantRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RopeArrowRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.SlingshotProjectileRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FluteItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.client.tooltip.BlackboardTooltipComponent;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.mehvahdjukaar.supplementaries.integration.CompatHandlerClient;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.world.data.map.client.CMDclient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {


    @OnlyIn(Dist.CLIENT)
    public static void init(final FMLClientSetupEvent event) {

        //compat
        CompatHandlerClient.init(event);

        //tooltips
        MinecraftForgeClient.registerTooltipComponentFactory(BlackboardItem.BlackboardTooltip.class, BlackboardTooltipComponent::new);

        //map markers
        CMDclient.init(event);

        //dynamic textures
        GlobeTextureManager.init(Minecraft.getInstance().textureManager);
        BlackboardTextureManager.init(Minecraft.getInstance().textureManager);

        MenuScreens.register(ModRegistry.PULLEY_BLOCK_CONTAINER.get(), PulleyBlockGui::new);
        MenuScreens.register(ModRegistry.SACK_CONTAINER.get(), SackGui::new);
        MenuScreens.register(ModRegistry.RED_MERCHANT_CONTAINER.get(), RedMerchantGui::new);
        MenuScreens.register(ModRegistry.PRESENT_BLOCK_CONTAINER.get(), PresentBlockGui.GUI_FACTORY);
        MenuScreens.register(ModRegistry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        ModRegistry.HANGING_SIGNS.values().forEach(s -> ItemBlockRenderTypes.setRenderLayer(s.get(), RenderType.cutout()));

        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRANK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR_TINTED.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FAUCET.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BELLOWS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_WALL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_WALL_SOUL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_SOUL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_WALL_ENDER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_ENDER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_WALL_GLOW.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_GLOW.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_WALL_GREEN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_GREEN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CAGE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COPPER_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BRASS_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRIMSON_LANTERN.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_DOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BAMBOO_SPIKES.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.NETHERITE_DOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ROPE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX_WILD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX_POT.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR_BOAT.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLOWER_BOX.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_FRAME.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_BRACE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ROPE_KNOT.get(), RenderType.cutout());



        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("rope_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), new ResourceLocation("pull"),
                (stack, world, entity, s) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    } else {
                        return (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / SlingshotItem.getChargeDuration(stack);
                    }
                });
        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), new ResourceLocation("pulling"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

        ItemProperties.register(ModRegistry.BUBBLE_BLOWER.get(), new ResourceLocation("using"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && entity.getUseItem().equals(stack, true) ? 1.0F : 0.0F);


        ModRegistry.PRESENTS_ITEMS.values().forEach(i -> ItemProperties.register(i.get(), new ResourceLocation("packed"),
                (stack, world, entity, s) -> PresentBlockTile.isPacked(stack) ? 1.0F : 1F));

        ItemProperties.register(ModRegistry.CANDY_ITEM.get(), new ResourceLocation("wrapping"),
                (stack, world, entity, s) -> CommonUtil.FESTIVITY.getCandyWrappingIndex());

        //ItemModelsProperties.register(ModRegistry.SPEEDOMETER_ITEM.get(), new ResourceLocation("speed"),
        //       new SpeedometerItem.SpeedometerItemProperty());
    }

    public record CrossbowProperty(Item projectile) implements ItemPropertyFunction {

        @Override
        public float call(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            return entity != null && CrossbowItem.isCharged(stack)
                    && CrossbowItem.containsChargedProjectile(stack, projectile) ? 1.0F : 0.0F;
        }
    }

    //particles
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event) {
        ParticleEngine particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(ModRegistry.FIREFLY_GLOW.get(), FireflyGlowParticle.Factory::new);
        particleManager.register(ModRegistry.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);
        particleManager.register(ModRegistry.GREEN_FLAME.get(), FlameParticle.Provider::new);
        particleManager.register(ModRegistry.DRIPPING_LIQUID.get(), DrippingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.FALLING_LIQUID.get(), FallingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.SPLASHING_LIQUID.get(), SplashingLiquidParticle.Factory::new);
        particleManager.register(ModRegistry.BOMB_EXPLOSION_PARTICLE.get(), BombExplosionParticle.Factory::new);
        particleManager.register(ModRegistry.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), new BombExplosionEmitterParticle.Factory());
        particleManager.register(ModRegistry.BOMB_SMOKE_PARTICLE.get(), BombSmokeParticle.Factory::new);
        particleManager.register(ModRegistry.BOTTLING_XP_PARTICLE.get(), BottlingXpParticle.Factory::new);
        particleManager.register(ModRegistry.FEATHER_PARTICLE.get(), FeatherParticle.Factory::new);
        particleManager.register(ModRegistry.SLINGSHOT_PARTICLE.get(), SlingshotParticle.Factory::new);
        particleManager.register(ModRegistry.STASIS_PARTICLE.get(), StasisParticle.Factory::new);
        particleManager.register(ModRegistry.CONFETTI_PARTICLE.get(), ConfettiParticle.Factory::new);
        particleManager.register(ModRegistry.ROTATION_TRAIL.get(), RotationTrailParticle.Factory::new);
        particleManager.register(ModRegistry.ROTATION_TRAIL_EMITTER.get(), new RotationTrailEmitter.Factory());
        particleManager.register(ModRegistry.SUDS_PARTICLE.get(), SudsParticle.Factory::new);
        particleManager.register(ModRegistry.ASH_PARTICLE.get(), AshParticleFactory::new);
        particleManager.register(ModRegistry.BUBBLE_BLOCK_PARTICLE.get(), BubbleBlockParticle.Factory::new);

    }

    public static class AshParticleFactory extends SnowflakeParticle.Provider {
        public AshParticleFactory(SpriteSet pSprites) {
            super(pSprites);
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            Particle p = super.createParticle(pType, pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            if (p != null) p.setColor(108 / 255f, 103 / 255f, 103 / 255f);
            return p;
        }
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //entities
        event.registerEntityRenderer(ModRegistry.BOMB.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.registerEntityRenderer(ModRegistry.THROWABLE_BRICK.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.registerEntityRenderer(ModRegistry.SLINGSHOT_PROJECTILE.get(), SlingshotProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.RED_MERCHANT.get(), RedMerchantRenderer::new);
        event.registerEntityRenderer(ModRegistry.ROPE_ARROW.get(), RopeArrowRenderer::new);
        event.registerEntityRenderer(ModRegistry.FALLING_URN.get(), FallingBlockRendererGeneric::new);
        event.registerEntityRenderer(ModRegistry.FALLING_ASH.get(), FallingBlockRendererGeneric::new);

        //tiles
        event.registerBlockEntityRenderer(ModRegistry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.JAR_TILE.get(), JarBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.SPRING_LAUNCHER_ARM_TILE.get(), SpringLauncherArmBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.CRIMSON_LANTERN_TILE.get(), CrimsonLanternBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.GOBLET_TILE.get(), GobletBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.BOOK_PILE_TILE.get(), BookPileBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.JAR_BOAT_TILE.get(), JarBoatTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.SKULL_PILE_TILE.get(), DoubleSkullBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.SKULL_CANDLE_TILE.get(), CandleSkullBlockTileRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.BUBBLE_BLOCK_TILE.get(), BubbleBlockTileRenderer::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        colors.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES.get());
        colors.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT.get());
        colors.register(new BrewingStandColor(), Blocks.BREWING_STAND);
        colors.register(new MimicBlockColor(), ModRegistry.SIGN_POST.get(), ModRegistry.TIMBER_BRACE.get(), ModRegistry.TIMBER_FRAME.get(),
                ModRegistry.TIMBER_CROSS_BRACE.get(), ModRegistry.WALL_LANTERN.get(), ModRegistry.ROPE_KNOT.get());
        colors.register(new CogBlockColor(), ModRegistry.COG_BLOCK.get());
        colors.register(new GunpowderBlockColor(), ModRegistry.GUNPOWDER_BLOCK.get());
        colors.register(new FlowerBoxColor(), ModRegistry.FLOWER_BOX.get());

    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        colors.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT_ITEM.get());
        colors.register(new CrossbowColor(), Items.CROSSBOW);
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ClientRegistry.register(event);
    }

    //textures
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        Textures.stitchTextures(event);
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        //loaders
        ModelLoaderRegistry.registerLoader(Supplementaries.res("frame_block_loader"), new FrameBlockLoader());
        ModelLoaderRegistry.registerLoader(Supplementaries.res("mimic_block_loader"), new SignPostBlockLoader());
        ModelLoaderRegistry.registerLoader(Supplementaries.res("rope_knot_loader"), new RopeKnotBlockLoader());
        ModelLoaderRegistry.registerLoader(Supplementaries.res("wall_lantern_loader"), new WallLanternLoader());
        ModelLoaderRegistry.registerLoader(Supplementaries.res("flower_box_loader"), new FlowerBoxLoader());


        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Supplementaries.MOD_ID, "blackboard_loader"), new BlackboardBlockLoader());

        //fake models & blockstates

        //registerStaticBlockState(Supplementaries.res("jar_boat_ship"), Blocks.AIR);

        ForgeModelBakery.addSpecialModel(FluteItemRenderer.FLUTE_3D_MODEL);
        ForgeModelBakery.addSpecialModel(FluteItemRenderer.FLUTE_2D_MODEL);
        ForgeModelBakery.addSpecialModel(JarBoatTileRenderer.BOAT_MODEL);

        //registerStaticItemModel(new ModelResourceLocation("supplementaries:flute_in_hand#inventory"));

        FlowerPotHandler.registerCustomModels(n -> ForgeModelBakery.addSpecialModel(new ModelResourceLocation(n, "")));
    }

}
