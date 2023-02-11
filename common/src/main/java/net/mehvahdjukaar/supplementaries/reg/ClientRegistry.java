package net.mehvahdjukaar.supplementaries.reg;

import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.client.renderer.FallingBlockRendererGeneric;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardManager;
import net.mehvahdjukaar.supplementaries.client.WallLanternTexturesManager;
import net.mehvahdjukaar.supplementaries.client.block_models.*;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.color.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.PearlMarkerRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RedMerchantRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.RopeArrowRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.SlingshotProjectileRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.SkullCandleOverlayModel;
import net.mehvahdjukaar.supplementaries.client.renderers.items.QuiverItemOverlayRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotItemOverlayRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.client.screens.*;
import net.mehvahdjukaar.supplementaries.client.tooltip.BannerPatternTooltipComponent;
import net.mehvahdjukaar.supplementaries.client.tooltip.BlackboardTooltipComponent;
import net.mehvahdjukaar.supplementaries.client.tooltip.QuiverTooltipComponent;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.BannerPatternTooltip;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.QuiverTooltip;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.client.ModMapMarkersClient;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatHandlerClient;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class ClientRegistry {

    //we cant load sheets class thanks to new banners shit
    private static final ResourceLocation SHULKER_SHEET = new ResourceLocation("textures/atlas/shulker_boxes.png");
    public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");

    public static final ResourceLocation FLARE_SHADER = Supplementaries.res("shaders/post/flare.json");
    public static final ResourceLocation BLACK_AND_WHITE_SHADER = Supplementaries.res("shaders/post/black_and_white.json");

    //entity models
    public static final ModelLayerLocation BELLOWS_MODEL = loc("bellows");
    public static final ModelLayerLocation BOOK_MODEL = loc("book");
    public static final ModelLayerLocation CLOCK_HANDS_MODEL = loc("clock_hands");
    public static final ModelLayerLocation GLOBE_BASE_MODEL = loc("globe");
    public static final ModelLayerLocation GLOBE_SPECIAL_MODEL = loc("globe_special");
    public static final ModelLayerLocation SIGN_POST_MODEL = loc("sign_post");
    public static final ModelLayerLocation RED_MERCHANT_MODEL = loc("red_merchant");
    public static final ModelLayerLocation SKULL_CANDLE_OVERLAY = loc("skull_candle");
    public static final ModelLayerLocation JARVIS_MODEL = loc("jarvis");
    public static final ModelLayerLocation PICKLE_MODEL = loc("pickle");
    //public static ModelLayerLocation BELL_EXTENSION = loc("bell_extension");

    //special models locations
    public static final ResourceLocation FLUTE_3D_MODEL = Supplementaries.res("item/flute_in_hand");
    public static final ResourceLocation FLUTE_2D_MODEL = Supplementaries.res("item/flute_gui");
    public static final ResourceLocation QUIVER_3D_MODEL = Supplementaries.res("item/quiver_in_hand_dyed");
    public static final ResourceLocation QUIVER_2D_MODEL = Supplementaries.res("item/quiver_gui_dyed");

    public static final ResourceLocation BELL_ROPE = Supplementaries.res("block/bell_rope");
    public static final ResourceLocation BELL_CHAIN = Supplementaries.res("block/bell_chain");
    public static final ResourceLocation BOAT_MODEL = Supplementaries.res("block/jar_boat_ship");
    public static final ResourceLocation WIND_VANE_BLOCK_MODEL = Supplementaries.res("block/wind_vane_up");
    public static final ResourceLocation BLACKBOARD_FRAME = Supplementaries.res("block/blackboard_frame");
    public static final Map<WoodType, ResourceLocation> HANGING_SIGNS_BLOCK_MODELS = new IdentityHashMap<>();

    public static KeyMapping QUIVER_KEYBIND = null;

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Supplementaries.res(name), name);
    }

    public static void init() {

        CompatHandlerClient.init();

        ClientPlatformHelper.addEntityRenderersRegistration(ClientRegistry::registerEntityRenderers);
        ClientPlatformHelper.addBlockEntityRenderersRegistration(ClientRegistry::registerBlockEntityRenderers);
        ClientPlatformHelper.addBlockColorsRegistration(ClientRegistry::registerBlockColors);
        ClientPlatformHelper.addItemColorsRegistration(ClientRegistry::registerItemColors);
        ClientPlatformHelper.addParticleRegistration(ClientRegistry::registerParticles);
        ClientPlatformHelper.addModelLayerRegistration(ClientRegistry::registerModelLayers);
        ClientPlatformHelper.addSpecialModelRegistration(ClientRegistry::registerSpecialModels);
        ClientPlatformHelper.addTooltipComponentRegistration(ClientRegistry::registerTooltipComponent);
        ClientPlatformHelper.addModelLoaderRegistration(ClientRegistry::registerModelLoaders);
        ClientPlatformHelper.addItemDecoratorsRegistration(ClientRegistry::registerItemDecorators);
        ClientPlatformHelper.addKeyBindRegistration(ClientRegistry::registerKeyBinds);
        ClientPlatformHelper.addAtlasTextureCallback(TextureAtlas.LOCATION_BLOCKS, e -> {
            ModTextures.getTexturesForBlockAtlas().forEach(e::addSprite);
        });
        ClientPlatformHelper.addAtlasTextureCallback(SHULKER_SHEET, e -> {
            ModTextures.getTexturesForShulkerAtlas().forEach(e::addSprite);
        });
        ClientPlatformHelper.addAtlasTextureCallback(BANNER_SHEET, e -> {
            ModTextures.getTexturesForBannerAtlas().forEach(e::addSprite);
        });

    }


    public static void setup() {

        //compat
        CompatHandlerClient.setup(); //if this fails other stuff below will to. In other words we'll at least know that it failed since nothing will work anymore

        //map markers
        ModMapMarkersClient.init();

        //overlay
        //SlimedGuiOverlay.register();

        MenuScreens.register(ModMenuTypes.PULLEY_BLOCK.get(), PulleyBlockGui::new);
        MenuScreens.register(ModMenuTypes.SACK.get(), SackGui::new);
        MenuScreens.register(ModMenuTypes.RED_MERCHANT.get(), RedMerchantGui::new);
        MenuScreens.register(ModMenuTypes.PRESENT_BLOCK.get(), PresentBlockScreen.GUI_FACTORY);
        MenuScreens.register(ModMenuTypes.TRAPPED_PRESENT_BLOCK.get(), TrappedPresentBlockGui.GUI_FACTORY);
        MenuScreens.register(ModMenuTypes.NOTICE_BOARD.get(), NoticeBoardGui::new);

        ModRegistry.HANGING_SIGNS.values().forEach(s -> ClientPlatformHelper.registerRenderType(s, RenderType.cutout()));

        ClientPlatformHelper.registerRenderType(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.CRANK.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.JAR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.FAUCET.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.BELLOWS.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_WALL.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_WALL_SOUL.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_SOUL.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_WALL_ENDER.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_ENDER.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_WALL_GLOW.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_GLOW.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_WALL_GREEN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_GREEN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.CAGE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.COPPER_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.BRASS_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SILVER_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.LEAD_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.CRIMSON_LANTERN.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GOLD_DOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.BAMBOO_SPIKES.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.NETHERITE_DOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.ROPE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.FLAX.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.FLAX_WILD.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.FLAX_POT.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.JAR_BOAT.get(), RenderType.translucent());
        ClientPlatformHelper.registerRenderType(ModRegistry.FLOWER_BOX.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.TIMBER_FRAME.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.TIMBER_BRACE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.ROPE_KNOT.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SILVER_DOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.SILVER_TRAPDOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.LEAD_DOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.LEAD_TRAPDOOR.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ClientPlatformHelper.registerRenderType(ModRegistry.CRYSTAL_DISPLAY.get(), RenderType.cutout());
        ModRegistry.CANDLE_HOLDERS.values().forEach(c-> ClientPlatformHelper.registerRenderType(c.get(), RenderType.cutout()));


        ClientPlatformHelper.registerItemProperty(Items.CROSSBOW, Supplementaries.res("rope_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ClientPlatformHelper.registerItemProperty(ModRegistry.SLINGSHOT_ITEM.get(), Supplementaries.res("pull"),
                (stack, world, entity, s) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    } else {
                        return (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / SlingshotItem.getChargeDuration(stack);
                    }
                });


        ClientPlatformHelper.registerItemProperty(ModRegistry.SLINGSHOT_ITEM.get(), Supplementaries.res("pulling"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);


        ClientPlatformHelper.registerItemProperty(ModRegistry.BUBBLE_BLOWER.get(), Supplementaries.res("using"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && ForgeHelper.areStacksEqual(stack, entity.getUseItem(), true) ? 1.0F : 0.0F);


        ModRegistry.PRESENTS.values().forEach(i -> ClientPlatformHelper.registerItemProperty(i.get().asItem(), Supplementaries.res("packed"),
                (stack, world, entity, s) -> 1));

        ModRegistry.TRAPPED_PRESENTS.values().forEach(i -> ClientPlatformHelper.registerItemProperty(i.get().asItem(), Supplementaries.res("primed"),
                (stack, world, entity, s) -> TrappedPresentBlockTile.isPrimed(stack) ? 1.0F : 0F));

        ClientPlatformHelper.registerItemProperty(ModRegistry.CANDY_ITEM.get(), Supplementaries.res("wrapping"),
                (stack, world, entity, s) -> MiscUtils.FESTIVITY.getCandyWrappingIndex());

        ClientPlatformHelper.registerItemProperty(ModRegistry.QUIVER_ITEM.get(), Supplementaries.res("dyed"),
                (stack, world, entity, s) -> ((DyeableLeatherItem)stack.getItem()).hasCustomColor(stack) ? 1 : 0);

        //ItemModelsProperties.register(ModRegistry.SPEEDOMETER_ITEM.get(), new ResourceLocation("speed"),
        //       new SpeedometerItem.SpeedometerItemProperty());

    }

    private record CrossbowProperty(Item projectile) implements ClampedItemPropertyFunction {

        @Override
        public float call(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            return entity != null && CrossbowItem.isCharged(stack)
                    && CrossbowItem.containsChargedProjectile(stack, projectile) ? 1.0F : 0.0F;
        }

        @Override
        public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
            return 0;
        }
    }

    @EventCalled
    private static void registerKeyBinds(ClientPlatformHelper.KeyBindEvent event) {
        if(PlatformHelper.getPlatform().isForge()) {
            QUIVER_KEYBIND = new KeyMapping("supplementaries.keybind.quiver",
                    InputConstants.Type.KEYSYM,
                    InputConstants.getKey("key.keyboard.v").getValue(),
                    "supplementaries.gui.controls");
            event.register(QUIVER_KEYBIND);
        }
    }

    @EventCalled
    private static void registerParticles(ClientPlatformHelper.ParticleEvent event) {
        event.register(ModParticles.SPEAKER_SOUND.get(), SpeakerSoundParticle.Factory::new);
        event.register(ModParticles.GREEN_FLAME.get(), FlameParticle.Provider::new);
        event.register(ModParticles.DRIPPING_LIQUID.get(), DrippingLiquidParticle.Factory::new);
        event.register(ModParticles.FALLING_LIQUID.get(), FallingLiquidParticle.Factory::new);
        event.register(ModParticles.SPLASHING_LIQUID.get(), SplashingLiquidParticle.Factory::new);
        event.register(ModParticles.BOMB_EXPLOSION_PARTICLE.get(), BombExplosionParticle.Factory::new);
        event.register(ModParticles.BOMB_EXPLOSION_PARTICLE_EMITTER.get(), BombExplosionEmitterParticle.Factory::new);
        event.register(ModParticles.BOMB_SMOKE_PARTICLE.get(), BombSmokeParticle.Factory::new);
        event.register(ModParticles.BOTTLING_XP_PARTICLE.get(), BottlingXpParticle.Factory::new);
        event.register(ModParticles.FEATHER_PARTICLE.get(), FeatherParticle.Factory::new);
        event.register(ModParticles.SLINGSHOT_PARTICLE.get(), SlingshotParticle.Factory::new);
        event.register(ModParticles.STASIS_PARTICLE.get(), StasisParticle.Factory::new);
        event.register(ModParticles.CONFETTI_PARTICLE.get(), ConfettiParticle.Factory::new);
        event.register(ModParticles.ROTATION_TRAIL.get(), RotationTrailParticle.Factory::new);
        event.register(ModParticles.ROTATION_TRAIL_EMITTER.get(), RotationTrailEmitter.Factory::new);
        event.register(ModParticles.SUDS_PARTICLE.get(), SudsParticle.Factory::new);
        event.register(ModParticles.ASH_PARTICLE.get(), AshParticleFactory::new);
        event.register(ModParticles.BUBBLE_BLOCK_PARTICLE.get(), BubbleBlockParticle.Factory::new);
        event.register(ModParticles.SUGAR_PARTICLE.get(), SugarParticle.Factory::new);
    }

    private static class AshParticleFactory extends SnowflakeParticle.Provider {
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

    @EventCalled
    private static void registerEntityRenderers(ClientPlatformHelper.EntityRendererEvent event) {
        //entities
        event.register(ModEntities.BOMB.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.register(ModEntities.THROWABLE_BRICK.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.register(ModEntities.SLINGSHOT_PROJECTILE.get(), SlingshotProjectileRenderer::new);
        event.register(ModEntities.DISPENSER_MINECART.get(), c -> new MinecartRenderer<>(c, ModelLayers.HOPPER_MINECART));
        event.register(ModEntities.RED_MERCHANT.get(), RedMerchantRenderer::new);
        event.register(ModEntities.ROPE_ARROW.get(), RopeArrowRenderer::new);
        event.register(ModEntities.FALLING_URN.get(), FallingBlockRenderer::new);
        event.register(ModEntities.FALLING_ASH.get(), FallingBlockRendererGeneric::new);
        event.register(ModEntities.FALLING_LANTERN.get(), FallingBlockRenderer::new);
        event.register(ModEntities.FALLING_SACK.get(), FallingBlockRenderer::new);
        event.register(ModEntities.PEARL_MARKER.get(), PearlMarkerRenderer::new);
    }

    @EventCalled
    private static void registerBlockEntityRenderers(ClientPlatformHelper.BlockEntityRendererEvent event) {
        event.register(ModRegistry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        event.register(ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        event.register(ModRegistry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        event.register(ModRegistry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        event.register(ModRegistry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        event.register(ModRegistry.JAR_TILE.get(), JarBlockTileRenderer::new);
        event.register(ModRegistry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        event.register(ModRegistry.SPRING_LAUNCHER_ARM_TILE.get(), SpringLauncherArmBlockTileRenderer::new);
        event.register(ModRegistry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        event.register(ModRegistry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        event.register(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        event.register(ModRegistry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        event.register(ModRegistry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        event.register(ModRegistry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        event.register(ModRegistry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        event.register(ModRegistry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        event.register(ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        event.register(ModRegistry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        event.register(ModRegistry.GOBLET_TILE.get(), GobletBlockTileRenderer::new);
        event.register(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        event.register(ModRegistry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        event.register(ModRegistry.BOOK_PILE_TILE.get(), BookPileBlockTileRenderer::new);
        event.register(ModRegistry.JAR_BOAT_TILE.get(), JarBoatTileRenderer::new);
        event.register(ModRegistry.SKULL_PILE_TILE.get(), DoubleSkullBlockTileRenderer::new);
        event.register(ModRegistry.SKULL_CANDLE_TILE.get(), CandleSkullBlockTileRenderer::new);
        event.register(ModRegistry.BUBBLE_BLOCK_TILE.get(), BubbleBlockTileRenderer::new);
        event.register(ModRegistry.ENDERMAN_SKULL_TILE.get(), EndermanSkullBlockTileRenderer::new);
    }

    @EventCalled
    private static void registerSpecialModels(ClientPlatformHelper.SpecialModelEvent event) {
        if (HANGING_SIGNS_BLOCK_MODELS.isEmpty()) {
            ModRegistry.HANGING_SIGNS.forEach((wood, block) -> HANGING_SIGNS_BLOCK_MODELS
                    .put(wood, Supplementaries.res("block/hanging_signs/" + Utils.getID(block).getPath())));
        }

        FlowerPotHandler.CUSTOM_MODELS.forEach(event::register);
        WallLanternTexturesManager.SPECIAL_TEXTURES.values().forEach(event::register);
        HANGING_SIGNS_BLOCK_MODELS.values().forEach(event::register);
        event.register(BLACKBOARD_FRAME);
        event.register(WIND_VANE_BLOCK_MODEL);
        event.register(BOAT_MODEL);
        event.register(BELL_ROPE);
        event.register(BELL_CHAIN);
        //not needed on forge
        if(PlatformHelper.getPlatform().isFabric()){
            event.register(FLUTE_3D_MODEL);
            event.register(FLUTE_2D_MODEL);
            event.register(QUIVER_2D_MODEL);
            event.register(QUIVER_3D_MODEL);
        }
    }

    @EventCalled
    private static void registerModelLoaders(ClientPlatformHelper.ModelLoaderEvent event) {
        event.register(Supplementaries.res("frame_block"), new NestedModelLoader("overlay", FrameBlockBakedModel::new));
        event.register(Supplementaries.res("wall_lantern"), new NestedModelLoader("support", WallLanternBakedModel::new));
        event.register(Supplementaries.res("flower_box"), new NestedModelLoader("box", FlowerBoxBakedModel::new));
        event.register(Supplementaries.res("hanging_pot"), new NestedModelLoader("rope", HangingPotBakedModel::new));
        event.register(Supplementaries.res("rope_knot"), new NestedModelLoader("knot", RopeKnotBlockBakedModel::new));
        event.register(Supplementaries.res("hanging_sign"), new HangingSignLoader());
        event.register(Supplementaries.res("blackboard"), new BlackboardBlockLoader());
        event.register(Supplementaries.res("mimic_block"), new SignPostBlockLoader());
    }

    @EventCalled
    private static void registerItemDecorators(ClientPlatformHelper.ItemDecoratorEvent event) {
        event.register(ModRegistry.SLINGSHOT_ITEM.get(), new SlingshotItemOverlayRenderer());
        event.register(ModRegistry.QUIVER_ITEM.get(), new QuiverItemOverlayRenderer());
    }

    @EventCalled
    private static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
        event.register(BlackboardManager.Key.class, BlackboardTooltipComponent::new);
        event.register(QuiverTooltip.class, QuiverTooltipComponent::new);
        event.register(BannerPatternTooltip.class, BannerPatternTooltipComponent::new);
        if (CompatHandler.QUARK) QuarkClientCompat.registerTooltipComponent(event);
    }

    @EventCalled
    private static void registerBlockColors(ClientPlatformHelper.BlockColorEvent event) {
        event.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES.get());
        event.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT.get());
        event.register(new BrewingStandColor(), Blocks.BREWING_STAND);
        event.register(new MimicBlockColor(), ModRegistry.SIGN_POST.get(), ModRegistry.TIMBER_BRACE.get(),
                ModRegistry.TIMBER_FRAME.get(), ModRegistry.TIMBER_CROSS_BRACE.get(), ModRegistry.WALL_LANTERN.get(),
                ModRegistry.ROPE_KNOT.get());
        event.register(new CogBlockColor(), ModRegistry.COG_BLOCK.get());
        event.register(new GunpowderBlockColor(), ModRegistry.GUNPOWDER_BLOCK.get());
        event.register(new FlowerBoxColor(), ModRegistry.FLOWER_BOX.get());
    }

    @EventCalled
    private static void registerItemColors(ClientPlatformHelper.ItemColorEvent event) {
        event.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        event.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT.get());
        event.register(new CrossbowColor(), Items.CROSSBOW);
        event.register((itemStack, i) -> i != 1 ? -1 : ((DyeableLeatherItem) itemStack.getItem()).getColor(itemStack),
                ModRegistry.QUIVER_ITEM.get());


    }

    @EventCalled
    private static void registerModelLayers(ClientPlatformHelper.ModelLayerEvent event) {
        event.register(BELLOWS_MODEL, BellowsBlockTileRenderer::createMesh);
        event.register(BOOK_MODEL, BookPileBlockTileRenderer::createMesh);
        event.register(CLOCK_HANDS_MODEL, ClockBlockTileRenderer::createMesh);
        event.register(GLOBE_BASE_MODEL, GlobeBlockTileRenderer::createBaseMesh);
        event.register(GLOBE_SPECIAL_MODEL, GlobeBlockTileRenderer::createSpecialMesh);
        event.register(SIGN_POST_MODEL, SignPostBlockTileRenderer::createMesh);
        event.register(RED_MERCHANT_MODEL, RedMerchantRenderer::createMesh);
        event.register(SKULL_CANDLE_OVERLAY, SkullCandleOverlayModel::createMesh);
        event.register(JARVIS_MODEL, JarredModel::createMesh);
        event.register(PICKLE_MODEL, PickleModel::createMesh);
    }


    /*
    //unused
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (true) return;
        //adds to all entities
        var entityTypes = ImmutableList.copyOf(
                ForgeRegistries.ENTITIES.getValues().stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .filter(e -> (e != EntityType.ENDER_DRAGON))
                        .map(entityType -> (EntityType<LivingEntity>) entityType)
                        .collect(Collectors.toList()));

        entityTypes.forEach((entityType -> addLayer(event.getRenderer(entityType))));

        //player skins
        for (String skinType : event.getSkins()) {
            var renderer = event.getSkin(skinType);
            if (renderer != null) renderer.addLayer(new SlimedLayer(renderer));
        }
    }

    private static <T extends LivingEntity, M extends EntityModel<T>, R extends LivingEntityRenderer<T, M>> void
    addLayer(@Nullable R renderer) {
        if (renderer != null) {
            renderer.addLayer(new SlimedLayer<>(renderer));
        }
    }

    public static ShaderInstance instance;

    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceManager(), Supplementaries.res("banner_mask"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), s -> instance = s);
    }
 */


}
