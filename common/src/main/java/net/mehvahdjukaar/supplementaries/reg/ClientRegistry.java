package net.mehvahdjukaar.supplementaries.reg;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.client.renderer.FallingBlockRendererGeneric;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.BlackboardManager;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.block_models.*;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.color.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredHeadLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.JarredModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PickleModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.EndermanSkullModel;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.HatStandModel;
import net.mehvahdjukaar.supplementaries.client.renderers.items.ProjectileWeaponOverlayRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SelectableItemOverlayRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotItemOverlayRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.client.screens.*;
import net.mehvahdjukaar.supplementaries.client.tooltip.*;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.BannerPatternTooltip;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.PaintingTooltip;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.SelectableContainerTooltip;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.SherdTooltip;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.client.ModMapMarkersClient;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandlerClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClientRegistry {

    public static final ResourceLocation RAGE_SHADER = Supplementaries.res("shaders/post/rage.json");
    public static final String BARBARIC_RAGE_SHADER = Supplementaries.res("shaders/post/barbaric_rage.json").toString();
    public static final ResourceLocation FLARE_SHADER = Supplementaries.res("shaders/post/flare.json");
    public static final ResourceLocation BLACK_AND_WHITE_SHADER = Supplementaries.res("shaders/post/black_and_white.json");
    public static final ResourceLocation VANILLA_DESATURATE = new ResourceLocation("shaders/post/desaturate.json");
    //entity models
    public static final ModelLayerLocation BELLOWS_MODEL = loc("bellows");
    public static final ModelLayerLocation CLOCK_HANDS_MODEL = loc("clock_hands");
    public static final ModelLayerLocation GLOBE_BASE_MODEL = loc("globe");
    public static final ModelLayerLocation GLOBE_SPECIAL_MODEL = loc("globe_special");
    public static final ModelLayerLocation RED_MERCHANT_MODEL = loc("red_merchant");
    public static final ModelLayerLocation HAT_STAND_MODEL = loc("hat_stand");
    public static final ModelLayerLocation CANNONBALL_MODEL = loc("cannonball");
    public static final ModelLayerLocation HAT_STAND_MODEL_ARMOR = loc("hat_stand_armor");
    public static final ModelLayerLocation JARVIS_MODEL = loc("jarvis");
    public static final ModelLayerLocation JAR_MODEL = loc("jar");
    public static final ModelLayerLocation PICKLE_MODEL = loc("pickle");
    public static final ModelLayerLocation ENDERMAN_HEAD_MODEL = loc("enderman_head");
    public static final ModelLayerLocation CANNON_MODEL = loc("cannon");
    public static final ModelLayerLocation WIND_VANE_MODEL = loc("wind_vane");
    public static final ModelLayerLocation BUNTING_MODEL = loc("bunting");

    //special models locations
    public static final ResourceLocation FLUTE_3D_MODEL = Supplementaries.res("item/flute_in_hand");
    public static final ResourceLocation FLUTE_2D_MODEL = Supplementaries.res("item/flute_gui");
    public static final ResourceLocation QUIVER_3D_MODEL = Supplementaries.res("item/quiver_in_hand_dyed");
    public static final ResourceLocation QUIVER_2D_MODEL = Supplementaries.res("item/quiver_gui_dyed");
    public static final ResourceLocation ALTIMETER_TEMPLATE = Supplementaries.res("item/altimeter_template");
    public static final ResourceLocation ALTIMETER_OVERLAY = Supplementaries.res("item/altimeter_overlay");
    public static final ResourceLocation LUNCH_BOX_ITEM_MODEL = Supplementaries.res("item/lunch_basket_gui");
    public static final ResourceLocation LUNCH_BOX_OPEN_ITEM_MODEL = Supplementaries.res("item/lunch_basket_gui_open");

    public static final ResourceLocation BOAT_MODEL = Supplementaries.res("block/jar_boat_ship");
    public static final ResourceLocation BLACKBOARD_FRAME = Supplementaries.res("block/blackboard_frame");
    public static final Supplier<Map<WoodType, ResourceLocation>> SIGN_POST_MODELS = Suppliers.memoize(() ->
            WoodTypeRegistry.getTypes().stream().collect(Collectors.toMap(Function.identity(),
                    w -> Supplementaries.res("block/sign_posts/" + w.getVariantId("sign_post"))))
    );

    public static final KeyMapping QUIVER_KEYBIND = new KeyMapping("supplementaries.keybind.quiver",
            InputConstants.Type.KEYSYM,
            InputConstants.getKey("key.keyboard.v").getValue(),
            "supplementaries.gui.controls");

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(Supplementaries.res(name), name);
    }

    public static void init() {
        CompatHandlerClient.init();
        ClientHelper.addClientSetup(ClientRegistry::setup);

        ClientHelper.addEntityRenderersRegistration(ClientRegistry::registerEntityRenderers);
        ClientHelper.addBlockEntityRenderersRegistration(ClientRegistry::registerBlockEntityRenderers);
        ClientHelper.addBlockColorsRegistration(ClientRegistry::registerBlockColors);
        ClientHelper.addItemColorsRegistration(ClientRegistry::registerItemColors);
        ClientHelper.addParticleRegistration(ClientRegistry::registerParticles);
        ClientHelper.addModelLayerRegistration(ClientRegistry::registerModelLayers);
        ClientHelper.addSpecialModelRegistration(ClientRegistry::registerSpecialModels);
        ClientHelper.addTooltipComponentRegistration(ClientRegistry::registerTooltipComponent);
        ClientHelper.addModelLoaderRegistration(ClientRegistry::registerModelLoaders);
        ClientHelper.addItemDecoratorsRegistration(ClientRegistry::registerItemDecorators);
        ClientHelper.addKeyBindRegistration(ClientRegistry::registerKeyBinds);
    }

    public static void setup() {

        //compat
        CompatHandlerClient.setup(); //if this fails other stuff below will to. In other words we'll at least know that it failed since nothing will work anymore

        //map markers
        ModMapMarkersClient.init();

        //overlay
        //SlimedGuiOverlay.register();

        MenuScreens.register(ModMenuTypes.PULLEY_BLOCK.get(), PulleyScreen::new);
        MenuScreens.register(ModMenuTypes.VARIABLE_SIZE.get(), VariableSizeContainerScreen::new);
        MenuScreens.register(ModMenuTypes.SAFE.get(), ShulkerBoxScreen::new);
        MenuScreens.register(ModMenuTypes.PRESENT_BLOCK.get(), PresentScreen::new);
        MenuScreens.register(ModMenuTypes.TRAPPED_PRESENT_BLOCK.get(), TrappedPresentScreen::new);
        MenuScreens.register(ModMenuTypes.NOTICE_BOARD.get(), NoticeBoardScreen::new);
        MenuScreens.register(ModMenuTypes.CANNON.get(), CannonScreen::new);
        MenuScreens.register(ModMenuTypes.RED_MERCHANT.get(), RedMerchantScreen::new);

        ClientHelper.registerRenderType(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FIRE_PIT.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BUNTING_BLOCK.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BOOK_PILE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BOOK_PILE_H.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GLOBE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GLOBE_SEPIA.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.CRANK.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BELLOWS.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_WALL.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_WALL_SOUL.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_SOUL.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_WALL_GREEN.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_GREEN.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.CAGE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GOLD_DOOR.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.BAMBOO_SPIKES.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.NETHERITE_DOOR.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.ROPE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FLAX.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FLAX_WILD.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FLAX_POT.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.JAR_BOAT.get(), RenderType.translucent());
        ClientHelper.registerRenderType(ModRegistry.GOBLET.get(), RenderType.translucent(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FAUCET.get(), RenderType.translucent(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.JAR.get(), RenderType.translucent(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.FLOWER_BOX.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.TIMBER_FRAME.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.TIMBER_BRACE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.CANNON.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.ROPE_KNOT.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.LUNCH_BASKET.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.CRYSTAL_DISPLAY.get(), RenderType.cutout());
        ModRegistry.CANDLE_HOLDERS.values().forEach(c -> ClientHelper.registerRenderType(c.get(), RenderType.cutout()));

        ClientHelper.registerRenderType(ModFluids.LUMISENE_BLOCK.get(), RenderType.cutout());
        ClientHelper.registerFluidRenderType(ModFluids.LUMISENE_FLUID.get(), RenderType.translucent());

        ItemProperties.register(Items.CROSSBOW, Supplementaries.res("rope_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ClampedItemPropertyFunction antiqueProp = (itemStack, clientLevel, livingEntity, i) -> AntiqueInkItem.hasAntiqueInk(itemStack) ? 1 : 0;
        ItemProperties.register(Items.WRITTEN_BOOK, Supplementaries.res("antique_ink"), antiqueProp);
        ItemProperties.register(Items.FILLED_MAP, Supplementaries.res("antique_ink"), antiqueProp);

        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), Supplementaries.res("pull"),
                (stack, world, entity, s) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    } else {
                        return (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / SlingshotItem.getChargeDuration(stack);
                    }
                });


        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), Supplementaries.res("pulling"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);


        ItemProperties.register(ModRegistry.BUBBLE_BLOWER.get(), Supplementaries.res("using"),
                (stack, world, entity, s) -> entity != null && entity.isUsingItem() && ForgeHelper.areStacksEqual(stack, entity.getUseItem(), true) ? 1.0F : 0.0F);


        ModRegistry.PRESENTS.values().forEach(i -> ItemProperties.register(i.get().asItem(), Supplementaries.res("packed"),
                (stack, world, entity, s) -> 1));

        ModRegistry.TRAPPED_PRESENTS.values().forEach(i -> ItemProperties.register(i.get().asItem(), Supplementaries.res("primed"),
                (stack, world, entity, s) -> TrappedPresentBlockTile.isPrimed(stack) ? 1.0F : 0F));

        ItemProperties.register(ModRegistry.CANDY_ITEM.get(), Supplementaries.res("wrapping"),
                (stack, world, entity, s) -> MiscUtils.FESTIVITY.getCandyWrappingIndex());

        ItemProperties.register(ModRegistry.QUIVER_ITEM.get(), Supplementaries.res("dyed"),
                (stack, world, entity, s) -> ((DyeableLeatherItem) stack.getItem()).hasCustomColor(stack) ? 1 : 0);

        ItemProperties.register(ModRegistry.GLOBE_ITEM.get(), Supplementaries.res("type"),
                new GlobeProperty());

        ItemProperties.register(ModRegistry.BUNTING.get(), Supplementaries.res("dye"),
                (stack, world, entity, s) -> BuntingItem.getColor(stack).getId() / 100f);

        //ItemModelsProperties.register(ModRegistry.SPEEDOMETER_ITEM.get(), new ResourceLocation("speed"),
        //       new SpeedometerItem.SpeedometerItemProperty());
    }


    private static class GlobeProperty implements ClampedItemPropertyFunction {

        @Override
        public float call(ItemStack itemStack, @org.jetbrains.annotations.Nullable ClientLevel clientLevel, @org.jetbrains.annotations.Nullable LivingEntity livingEntity, int i) {
            CompoundTag compoundTag = itemStack.getTagElement("display");
            if (compoundTag != null) {
                var n = compoundTag.getString("Name");
                MutableComponent mutableComponent = Component.Serializer.fromJson(n);
                if (mutableComponent != null) {
                    var v = GlobeManager.Type.getTextureID(mutableComponent.getString());
                    if (v != null) return Float.valueOf(v);
                }
            }
            return Float.NEGATIVE_INFINITY;
        }

        @Override
        public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
            return call(itemStack, clientLevel, livingEntity, i);
        }

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
    private static void registerKeyBinds(ClientHelper.KeyBindEvent event) {
        event.register(QUIVER_KEYBIND);
    }

    @EventCalled
    private static void registerParticles(ClientHelper.ParticleEvent event) {
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
        event.register(ModParticles.STREAMER_PARTICLE.get(), StreamerParticle.Factory::new);
        event.register(ModParticles.WIND_STREAM.get(), WindTrailParticle.Factory::new);
        event.register(ModParticles.ROTATION_TRAIL.get(), RotationTrailParticle.Factory::new);
        event.register(ModParticles.ROTATION_TRAIL_EMITTER.get(), RotationTrailEmitter.Factory::new);
        event.register(ModParticles.SUDS_PARTICLE.get(), SudsParticle.Factory::new);
        event.register(ModParticles.ASH_PARTICLE.get(), AshParticleFactory::new);
        event.register(ModParticles.BUBBLE_BLOCK_PARTICLE.get(), BubbleBlockParticle.Factory::new);
        event.register(ModParticles.SUGAR_PARTICLE.get(), SugarParticle.Factory::new);
        event.register(ModParticles.CANNON_FIRE_PARTICLE.get(), CannonFireParticle.Factory::new);
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
    private static void registerEntityRenderers(ClientHelper.EntityRendererEvent event) {
        //entities
        event.register(ModEntities.BOMB.get(), context -> new ImprovedThrownItemRenderer<>(context, 1));
        event.register(ModEntities.THROWABLE_BRICK.get(), context -> new ImprovedThrownItemRenderer<>(context, 1));
        if(ClientConfigs.Items.CANNONBALL_3D.get()) {
            event.register(ModEntities.CANNONBALL.get(), context -> new CannonballRenderer<>(context, 1.615f));
        }else {
            event.register(ModEntities.CANNONBALL.get(), context -> new ImprovedThrownItemRenderer<>(context, 1.615f));
        }
        event.register(ModEntities.SLINGSHOT_PROJECTILE.get(), SlingshotProjectileRenderer::new);
        event.register(ModEntities.DISPENSER_MINECART.get(), c -> new MinecartRenderer<>(c, ModelLayers.HOPPER_MINECART));
        event.register(ModEntities.RED_MERCHANT.get(), RedMerchantRenderer::new);
        event.register(ModEntities.HAT_STAND.get(), HatStandRenderer::new);
        event.register(ModEntities.ROPE_ARROW.get(), RopeArrowRenderer::new);
        event.register(ModEntities.FALLING_URN.get(), FallingBlockRenderer::new);
        event.register(ModEntities.FALLING_ASH.get(), FallingBlockRendererGeneric::new);
        event.register(ModEntities.FALLING_SACK.get(), FallingBlockRenderer::new);
        event.register(ModEntities.PEARL_MARKER.get(), NoopRenderer::new);
    }

    @EventCalled
    private static void registerBlockEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(ModRegistry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        event.register(ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        event.register(ModRegistry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        event.register(ModRegistry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        event.register(ModRegistry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        event.register(ModRegistry.JAR_TILE.get(), JarBlockTileRenderer::new);
        event.register(ModRegistry.SPRING_LAUNCHER_ARM_TILE.get(), SpringLauncherArmBlockTileRenderer::new);
        event.register(ModRegistry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        event.register(ModRegistry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        event.register(ModRegistry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
        event.register(ModRegistry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        event.register(ModRegistry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        event.register(ModRegistry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        event.register(ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        event.register(ModRegistry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        event.register(ModRegistry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        event.register(ModRegistry.BOOK_PILE_TILE.get(), BookPileBlockTileRenderer::new);
        event.register(ModRegistry.JAR_BOAT_TILE.get(), JarBoatTileRenderer::new);
        event.register(ModRegistry.BUBBLE_BLOCK_TILE.get(), BubbleBlockTileRenderer::new);
        event.register(ModRegistry.ENDERMAN_SKULL_TILE.get(), EndermanSkullBlockTileRenderer::new);
        event.register(ModRegistry.CANNON_TILE.get(), CannonBlockTileRenderer::new);
        event.register(ModRegistry.BUNTING_TILE.get(), BuntingBlockTileRenderer::new);
        event.register(ModRegistry.MOVING_SLIDY_BLOCK_TILE.get(), PistonHeadRenderer::new);
    }

    @EventCalled
    private static void registerSpecialModels(ClientHelper.SpecialModelEvent event) {
        FlowerPotHandler.CUSTOM_MODELS.forEach(event::register);
        SIGN_POST_MODELS.get().values().forEach(event::register);
        PlaceableBookManager.getAll().forEach(b -> event.register(b.modelPath()));
        event.register(BLACKBOARD_FRAME);
        event.register(BOAT_MODEL);
        event.register(LUNCH_BOX_ITEM_MODEL);
        event.register(LUNCH_BOX_OPEN_ITEM_MODEL);
        event.register(ALTIMETER_TEMPLATE);
        event.register(ALTIMETER_OVERLAY);

        //not needed on forge
        if (PlatHelper.getPlatform().isFabric()) {
            event.register(FLUTE_3D_MODEL);
            event.register(FLUTE_2D_MODEL);
            event.register(QUIVER_2D_MODEL);
            event.register(QUIVER_3D_MODEL);
        }
    }

    @EventCalled
    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(Supplementaries.res("frame_block"), new NestedModelLoader("overlay", FrameBlockBakedModel::new));
        event.register(Supplementaries.res("flower_box"), new NestedModelLoader("box", FlowerBoxBakedModel::new));
        event.register(Supplementaries.res("rope_knot"), new NestedModelLoader("knot", RopeKnotBlockBakedModel::new));
        event.register(Supplementaries.res("blackboard"), new NestedModelLoader("frame", BlackboardBakedModel::new));
        event.register(Supplementaries.res("mimic_block"), SignPostBlockBakedModel::new);
        event.register(Supplementaries.res("goblet"), new GobletModelLoader());
        event.register(Supplementaries.res("faucet"), new FaucetModelLoader());
        event.register(Supplementaries.res("book_pile"), BookPileModel::new);
        event.register(Supplementaries.res("bunting"), BuntingsBakedModel::new);
        event.register(Supplementaries.res("jar"), new JarModelLoader());
    }

    @EventCalled
    private static void registerItemDecorators(ClientHelper.ItemDecoratorEvent event) {
        event.register(ModRegistry.SLINGSHOT_ITEM.get(), new SlingshotItemOverlayRenderer());
        if (ClientConfigs.Items.QUIVER_OVERLAY.get()) {
            event.register(ModRegistry.QUIVER_ITEM.get(), new SelectableItemOverlayRenderer());
        }
        if (ClientConfigs.Items.LUNCH_BOX_OVERLAY.get()) {
            event.register(ModRegistry.LUNCH_BASKET_ITEM.get(), new SelectableItemOverlayRenderer());
        }
        if (ClientConfigs.Tweaks.PROJECTILE_WEAPON_OVERLAY.get()) {
            for (var i : BuiltInRegistries.ITEM) {
                if (i instanceof ProjectileWeaponItem && i != ModRegistry.QUIVER_ITEM.get()) {
                    event.register(i, new ProjectileWeaponOverlayRenderer());
                }
            }
        }
    }

    @EventCalled
    private static void registerTooltipComponent(ClientHelper.TooltipComponentEvent event) {
        event.register(BlackboardManager.Key.class, BlackboardTooltipComponent::new);
        event.register(SelectableContainerTooltip.class, QuiverTooltipComponent::new);
        event.register(BannerPatternTooltip.class, BannerPatternTooltipComponent::new);
        event.register(PaintingTooltip.class, PaintingTooltipComponent::new);
        event.register(SherdTooltip.class, SherdTooltipComponent::new);
    }

    @EventCalled
    private static void registerBlockColors(ClientHelper.BlockColorEvent event) {
        event.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES.get());
        event.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT.get());
        event.register(new MimicBlockColor(), ModRegistry.SIGN_POST.get(), ModRegistry.TIMBER_BRACE.get(),
                ModRegistry.TIMBER_FRAME.get(), ModRegistry.TIMBER_CROSS_BRACE.get(),
                ModRegistry.ROPE_KNOT.get());
        event.register(new CogBlockColor(), ModRegistry.COG_BLOCK.get());
        event.register(new GunpowderBlockColor(), ModRegistry.GUNPOWDER_BLOCK.get());
        event.register(new FlowerBoxColor(), ModRegistry.FLOWER_BOX.get());
        event.register(new FluidColor(), ModRegistry.GOBLET.get(), ModRegistry.JAR.get());
    }

    @EventCalled
    private static void registerItemColors(ClientHelper.ItemColorEvent event) {
        event.register(new TippedSpikesColor(), ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get());
        event.register(new DefaultWaterColor(), ModRegistry.JAR_BOAT.get());
        event.register((itemStack, i) -> i != 1 ? -1 : ((DyeableLeatherItem) itemStack.getItem()).getColor(itemStack),
                ModRegistry.QUIVER_ITEM.get());
    }

    @EventCalled
    private static void registerModelLayers(ClientHelper.ModelLayerEvent event) {
        event.register(BELLOWS_MODEL, BellowsBlockTileRenderer::createMesh);
        event.register(CLOCK_HANDS_MODEL, ClockBlockTileRenderer::createMesh);
        event.register(GLOBE_BASE_MODEL, GlobeBlockTileRenderer::createBaseMesh);
        event.register(GLOBE_SPECIAL_MODEL, GlobeBlockTileRenderer::createSpecialMesh);
        event.register(RED_MERCHANT_MODEL, RedMerchantRenderer::createMesh);
        event.register(HAT_STAND_MODEL, HatStandModel::createMesh);
        event.register(CANNONBALL_MODEL, CannonballRenderer::createMesh);
        event.register(HAT_STAND_MODEL_ARMOR, HatStandModel::createArmorMesh);
        event.register(JARVIS_MODEL, JarredModel::createMesh);
        event.register(JAR_MODEL, JarredHeadLayer::createMesh);
        event.register(PICKLE_MODEL, PickleModel::createMesh);
        event.register(ENDERMAN_HEAD_MODEL, EndermanSkullModel::createMesh);
        event.register(CANNON_MODEL, CannonBlockTileRenderer::createMesh);
        event.register(WIND_VANE_MODEL, WindVaneBlockTileRenderer::createMesh);
        event.register(BUNTING_MODEL, BuntingBlockTileRenderer::createMesh);
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


    public static LevelLightEngine getLightEngine() {
        return Minecraft.getInstance().level.getLightEngine();
    }
}
