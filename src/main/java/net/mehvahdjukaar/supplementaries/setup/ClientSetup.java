package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.*;
import net.mehvahdjukaar.supplementaries.client.models.*;
import net.mehvahdjukaar.supplementaries.client.particles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.color.*;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.*;
import net.mehvahdjukaar.supplementaries.client.renderers.items.CageItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandlerClient;
import net.mehvahdjukaar.supplementaries.compat.optifine.OptifineHandler;
import net.mehvahdjukaar.supplementaries.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.world.data.map.client.CMDclient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;

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

        //dynamic textures
        GlobeTextureManager.init(Minecraft.getInstance().textureManager);
        BlackboardTextureManager.init(Minecraft.getInstance().textureManager);

        MenuScreens.register(ModRegistry.RED_MERCHANT_CONTAINER.get(), OrangeMerchantGui::new);

        //wind vane
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        //notice board
        MenuScreens.register(ModRegistry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRANK.get(), RenderType.cutout());
        //jar
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR_TINTED.get(), r -> r == RenderType.translucent() || r == RenderType.solid());
        //faucet
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FAUCET.get(), RenderType.cutout());
        //sign post
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        //hanging sign
        ModRegistry.HANGING_SIGNS.values().forEach(s -> ItemBlockRenderTypes.setRenderLayer(s.get(), RenderType.translucent()));
        //wall lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        //bellows
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BELLOWS.get(), RenderType.cutout());
        //sconce
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
        //item shelf
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        //cage
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CAGE.get(), RenderType.cutout());
        //sconce lever
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        //hourglass
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        //sack
        MenuScreens.register(ModRegistry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        //copper lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COPPER_LANTERN.get(), RenderType.cutout());
        //brass lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BRASS_LANTERN.get(), RenderType.cutout());
        //crimson lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRIMSON_LANTERN.get(), RenderType.cutout());
        //hanging flower pot
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        //gold door & trapdoor
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_DOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_TRAPDOOR.get(), RenderType.cutout());
        //spikes
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BAMBOO_SPIKES.get(), RenderType.cutout());
        //netherite door & trapdoor
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.NETHERITE_DOOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.NETHERITE_TRAPDOOR.get(), RenderType.cutout());
        //rope
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ROPE.get(), RenderType.cutout());
        //flax
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX_WILD.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLAX_POT.get(), RenderType.cutout());
        //pulley
        MenuScreens.register(ModRegistry.PULLEY_BLOCK_CONTAINER.get(), PulleyBlockGui::new);
        //boat
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR_BOAT.get(), RenderType.translucent());
        //magma cream block
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.MAGMA_CREAM_BLOCK.get(), RenderType.translucent());
        //flower box
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FLOWER_BOX.get(), RenderType.cutout());
        //timber frames
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_FRAME.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_BRACE.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.TIMBER_CROSS_BRACE.get(), RenderType.cutout());
        //cog block
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        //iron gate
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        //gold gate
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        //present
        //MenuScreens.register(ModRegistry.PRESENT_BLOCK_CONTAINER.get(), PresentBlockGui.GUI_FACTORY);
        //gunpowder
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());
        //rope knot
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ROPE_KNOT.get(), RenderType.cutout());


        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("rope_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("amethyst_arrow"),
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


        ModRegistry.PRESENTS_ITEMS.values().forEach(i -> ItemProperties.register(i.get(), new ResourceLocation("packed"),
                (stack, world, entity, s) -> PresentBlockTile.isPacked(stack) ? 1.0F : 0F));

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
    }

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //entities
        event.registerEntityRenderer(ModRegistry.BOMB.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.registerEntityRenderer(ModRegistry.THROWABLE_BRICK.get(), context -> new ThrownItemRenderer<>(context, 1, false));
        event.registerEntityRenderer(ModRegistry.SLINGSHOT_PROJECTILE.get(), SlingshotProjectileRenderer::new);
        event.registerEntityRenderer(ModRegistry.RED_MERCHANT_TYPE.get(), RedMerchantRenderer::new);
        event.registerEntityRenderer(ModRegistry.ROPE_ARROW.get(), RopeArrowRenderer::new);

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
        ResourceLocation loc = event.getMap().location();

        if (loc.equals(TextureAtlas.LOCATION_BLOCKS)) {
            for (ResourceLocation r : Textures.getTexturesToStitch()) {
                event.addSprite(r);
            }
        } else if (loc.equals(Sheets.BANNER_SHEET)) {
            try {
                Textures.FLAG_TEXTURES.values().stream().filter(r -> !MissingTextureAtlasSprite.getLocation().equals(r))
                        .forEach(event::addSprite);
            } catch (Exception ignored) {
            }
        } else if (loc.equals(Sheets.SHULKER_SHEET)) {
            event.addSprite(Textures.BOOK_ENCHANTED_TEXTURES);
            event.addSprite(Textures.BOOK_TOME_TEXTURES);
            Textures.BOOK_TEXTURES.values().forEach(event::addSprite);
        }

        OptifineHandler.refresh();
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

        registerStaticBlockState(Supplementaries.res("jar_boat_ship"), Blocks.AIR);

        FlowerPotHandler.registerCustomModels(n -> registerStaticBlockState(new ResourceLocation(n), Blocks.AIR));
    }

    private static void registerStaticBlockState(ResourceLocation name, Block parent, String... booleanProperties) {
        Map<ResourceLocation, StateDefinition<Block, BlockState>> mapCopy = new HashMap<>(ModelBakery.STATIC_DEFINITIONS);

        StateDefinition.Builder<Block, BlockState> builder = (new StateDefinition.Builder<>(parent));

        for (String p : booleanProperties) builder.add(BooleanProperty.create(p));

        mapCopy.put(name, builder.create(Block::defaultBlockState, BlockState::new));

        ModelBakery.STATIC_DEFINITIONS = mapCopy;
    }

}
