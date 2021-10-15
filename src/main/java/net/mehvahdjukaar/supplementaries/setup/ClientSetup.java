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
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.*;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.compat.CompatHandlerClient;
import net.mehvahdjukaar.supplementaries.compat.optifine.OptifineHandler;
import net.mehvahdjukaar.supplementaries.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.world.data.map.client.CMDclient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.resources.ResourceLocation;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
                renderManager -> new ThrownItemRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.THROWABLE_BRICK.get(),
                renderManager -> new ThrownItemRenderer<>(renderManager, itemRenderer));
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.LABEL.get(),
                renderManager -> new LabelEntityRenderer(renderManager, itemRenderer));

        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.AMETHYST_SHARD.get(),
                ShardProjectileRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.FLINT_SHARD.get(),
                ShardProjectileRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.SLINGSHOT_PROJECTILE.get(),
                SlingshotProjectileRenderer::new);


        //dynamic textures
        GlobeTextureManager.init(Minecraft.getInstance().textureManager);
        BlackboardTextureManager.init(Minecraft.getInstance().textureManager);


        //orange trader
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.RED_MERCHANT_TYPE.get(), OrangeTraderEntityRenderer::new);
        MenuScreens.register(ModRegistry.RED_MERCHANT_CONTAINER.get(), OrangeMerchantGui::new);

        //rope arrow
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.ROPE_ARROW.get(), RopeArrowRenderer::new);
        //amethyst arrow
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.AMETHYST_ARROW.get(), AmethystArrowRenderer::new);

        //firefly & jar
        RenderingRegistry.registerEntityRenderingHandler(ModRegistry.FIREFLY_TYPE.get(), FireflyEntityRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FIREFLY_JAR.get(), RenderType.cutout());
        //clock
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CLOCK_BLOCK_TILE.get(), ClockBlockTileRenderer::new);
        //pedestal
        ClientRegistry.bindTileEntityRenderer(ModRegistry.PEDESTAL_TILE.get(), PedestalBlockTileRenderer::new);
        //wind vane
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WIND_VANE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.WIND_VANE_TILE.get(), WindVaneBlockTileRenderer::new);
        //notice board
        ClientRegistry.bindTileEntityRenderer(ModRegistry.NOTICE_BOARD_TILE.get(), NoticeBoardBlockTileRenderer::new);
        MenuScreens.register(ModRegistry.NOTICE_BOARD_CONTAINER.get(), NoticeBoardGui::new);
        //crank
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRANK.get(), RenderType.cutout());
        //jar
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.JAR_TINTED.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.JAR_TILE.get(), JarBlockTileRenderer::new);
        //faucet
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.FAUCET.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FAUCET_TILE.get(), FaucetBlockTileRenderer::new);
        //piston launcher
        ClientRegistry.bindTileEntityRenderer(ModRegistry.PISTON_LAUNCHER_ARM_TILE.get(), PistonLauncherArmBlockTileRenderer::new);
        //sign post
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SIGN_POST.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.SIGN_POST_TILE.get(), SignPostBlockTileRenderer::new);
        //hanging sign
        ModRegistry.HANGING_SIGNS.values().forEach(s -> ItemBlockRenderTypes.setRenderLayer(s.get(), RenderType.translucent()));
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HANGING_SIGN_TILE.get(), HangingSignBlockTileRenderer::new);
        //wall lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.WALL_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.WALL_LANTERN_TILE.get(), WallLanternBlockTileRenderer::new);
        //bellows
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BELLOWS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.BELLOWS_TILE.get(), BellowsBlockTileRenderer::new);
        //laser
        ClientRegistry.bindTileEntityRenderer(ModRegistry.LASER_BLOCK_TILE.get(), LaserBlockTileRenderer::new);
        //flag
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FLAG_TILE.get(), FlagBlockTileRenderer::new);
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
        //candelabra
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CANDELABRA.get(), RenderType.cutout());
        //item shelf
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ITEM_SHELF.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.ITEM_SHELF_TILE.get(), ItemShelfBlockTileRenderer::new);
        //cage
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CAGE.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CAGE_TILE.get(), CageBlockTileRenderer::new);
        //sconce lever
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SCONCE_LEVER.get(), RenderType.cutout());
        //globe
        ClientRegistry.bindTileEntityRenderer(ModRegistry.GLOBE_TILE.get(), GlobeBlockTileRenderer::new);
        //hourglass
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HOURGLASS.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HOURGLASS_TILE.get(), HourGlassBlockTileRenderer::new);
        //sack
        MenuScreens.register(ModRegistry.SACK_CONTAINER.get(), SackGui::new);
        //blackboard
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BLACKBOARD.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.BLACKBOARD_TILE.get(), BlackboardBlockTileRenderer::new);
        //soul jar
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.SOUL_JAR.get(), RenderType.translucent());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.FIREFLY_JAR_TILE.get(), SoulJarBlockTileRenderer::new);
        //copper lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COPPER_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.COPPER_LANTERN_TILE.get(), OilLanternBlockTileRenderer::new);
        //brass lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.BRASS_LANTERN.get(), RenderType.cutout());
        //crimson lantern
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.CRIMSON_LANTERN.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CRIMSON_LANTERN_TILE.get(), CrimsonLanternBlockTileRenderer::new);
        //doormat
        ClientRegistry.bindTileEntityRenderer(ModRegistry.DOORMAT_TILE.get(), DoormatBlockTileRenderer::new);
        //hanging flower pot
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.HANGING_FLOWER_POT.get(), RenderType.cutout());
        ClientRegistry.bindTileEntityRenderer(ModRegistry.HANGING_FLOWER_POT_TILE.get(), HangingFlowerPotBlockTileRenderer::new);
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
        //goblet
        ClientRegistry.bindTileEntityRenderer(ModRegistry.GOBLET_TILE.get(), GobletBlockTileRenderer::new);
        //cog block
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.COG_BLOCK.get(), RenderType.cutout());
        //ceiling banner
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CEILING_BANNER_TILE.get(), CeilingBannerBlockTileRenderer::new);
        //statue
        ClientRegistry.bindTileEntityRenderer(ModRegistry.STATUE_TILE.get(), StatueBlockTileRenderer::new);
        //iron gate
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.IRON_GATE.get(), RenderType.cutout());
        //gold gate
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GOLD_GATE.get(), RenderType.cutout());
        //cracked bell
        ClientRegistry.bindTileEntityRenderer(ModRegistry.CRACKED_BELL_TILE.get(), CrackedBellTileEntityRenderer::new);
        //present
        MenuScreens.register(ModRegistry.PRESENT_BLOCK_CONTAINER.get(), PresentBlockGui.GUI_FACTORY);
        //gunpowder
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.GUNPOWDER_BLOCK.get(), RenderType.cutout());
        //rope knot
        ItemBlockRenderTypes.setRenderLayer(ModRegistry.ROPE_KNOT.get(), RenderType.cutout());
        //book pile
        ClientRegistry.bindTileEntityRenderer(ModRegistry.BOOK_PILE_TILE.get(), r -> new BookPileBlockTileRenderer(r, false));

        //jar boat
        ClientRegistry.bindTileEntityRenderer(ModRegistry.JAR_BOAT_TILE.get(), JarBoatTileRenderer::new);


        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("rope_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ItemProperties.register(Items.CROSSBOW, new ResourceLocation("amethyst_arrow"),
                new CrossbowProperty(ModRegistry.ROPE_ARROW_ITEM.get()));

        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), new ResourceLocation("pull"),
                (stack, world, entity) -> {
                    if (entity == null || entity.getUseItem() != stack) {
                        return 0.0F;
                    } else {
                        return (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / SlingshotItem.getChargeDuration(stack);
                    }
                });
        ItemProperties.register(ModRegistry.SLINGSHOT_ITEM.get(), new ResourceLocation("pulling"),
                (stack, world, entity) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);


        ModRegistry.PRESENTS_ITEMS.values().forEach(i -> ItemProperties.register(i.get(), new ResourceLocation("packed"),
                (stack, world, entity) -> PresentBlockTile.isPacked(stack) ? 1.0F : 1.0F));

        ItemProperties.register(ModRegistry.CANDY_ITEM.get(), new ResourceLocation("wrapping"),
                (stack, world, entity) -> CommonUtil.FESTIVITY.getCandyWrappingIndex());

        //ItemModelsProperties.register(ModRegistry.SPEEDOMETER_ITEM.get(), new ResourceLocation("speed"),
        //       new SpeedometerItem.SpeedometerItemProperty());
    }

    public static class CrossbowProperty implements ItemPropertyFunction {

        private final Item projectile;

        private CrossbowProperty(Item projectile) {
            this.projectile = projectile;
        }

        @Override
        public float call(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity) {
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
        registerStaticBlockState(ModRegistry.LABEL.get().getRegistryName(), Blocks.AIR, "jar");

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
