package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.*;
import net.mehvahdjukaar.supplementaries.block.tiles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.items.BlackboardItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.CageItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.entities.*;
import net.mehvahdjukaar.supplementaries.inventories.*;
import net.mehvahdjukaar.supplementaries.items.*;
import net.mehvahdjukaar.supplementaries.items.crafting.*;
import net.mehvahdjukaar.supplementaries.items.enchantment.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.items.tabs.SupplementariesTab;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Supplementaries.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Supplementaries.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Supplementaries.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Motive> PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_TYPES, Supplementaries.MOD_ID);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Supplementaries.MOD_ID);


    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        CONTAINERS.register(bus);
        ENTITIES.register(bus);
        PARTICLES.register(bus);
        SOUNDS.register(bus);
        RECIPES.register(bus);
        PAINTINGS.register(bus);
        ENCHANTMENTS.register(bus);
    }

    //creative tab
    private static final boolean tab = RegistryConfigs.reg.CREATIVE_TAB.get();

    public static final CreativeModeTab MOD_TAB = !tab ? null : new SupplementariesTab("supplementaries");
    public static final CreativeModeTab JAR_TAB = !RegistryConfigs.reg.JAR_TAB.get() ? null : new JarTab("jars");

    public static CreativeModeTab getTab(CreativeModeTab g, String regName) {
        if (RegistryConfigs.reg.isEnabled(regName)) {
            return tab ? MOD_TAB : g;
        }
        return null;
    }

    private static CreativeModeTab getTab(String modId, CreativeModeTab g, String regName) {
        return ModList.get().isLoaded(modId) ? getTab(g, regName) : null;
    }

    private static RegistryObject<Item> regItem(String name, Supplier<? extends Item> sup) {
        return ITEMS.register(name, sup);
    }

    protected static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group) {
        return regItem(blockSup.getId().getPath(), () -> new BlockItem(blockSup.get(), (new Item.Properties()).tab(group)));
    }

    protected static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group, int burnTime) {
        return regItem(blockSup.getId().getPath(), () -> new BurnableBlockItem(blockSup.get(), (new Item.Properties()).tab(group), burnTime));
    }

    private static RegistryObject<SimpleParticleType> regParticle(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(true));
    }

    private static RegistryObject<SoundEvent> makeSoundEvent(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(Supplementaries.res(name)));
    }

    //these are the names in sound.json. not actual location. this is so a sound event can play multiple sounds
    public static final RegistryObject<SoundEvent> TOM_SOUND = makeSoundEvent("block.tom");
    public static final RegistryObject<SoundEvent> TICK_SOUND = makeSoundEvent("block.tick_1");
    public static final RegistryObject<SoundEvent> TICK_2_SOUND = makeSoundEvent("block.tick_2");
    public static final RegistryObject<SoundEvent> BOMB_SOUND = makeSoundEvent("item.bomb");
    public static final RegistryObject<SoundEvent> PANCAKE_MUSIC = makeSoundEvent("music.pancake");
    public static final RegistryObject<SoundEvent> GUNPOWDER_IGNITE = makeSoundEvent("block.gunpowder.ignite");


    //dynamic registration so I can use their classes
    //TODO: use deferred regiries
    @SubscribeEvent
    public static void registerCompatBlocks(final RegistryEvent.Register<Block> event) {
        //CompatHandler.registerOptionalBlocks(event);
    }

    @SubscribeEvent
    public static void registerCompatItems(final RegistryEvent.Register<Item> event) {
        //CompatHandler.registerOptionalItems(event);
        //shulker shell

        if (RegistryConfigs.reg.SHULKER_HELMET_ENABLED.get()) {
            event.getRegistry().register(new ShulkerShellItem(new Item.Properties()
                    .stacksTo(64)
                    .tab(CreativeModeTab.TAB_MATERIALS)).setRegistryName("minecraft:shulker_shell"));
        }

    }

    @SubscribeEvent
    public static void registerCompatRecipes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        CompatHandler.registerOptionalRecipes(event);
    }

    //entities
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistry.RED_MERCHANT_TYPE.get(), Mob.createMobAttributes().build());
      //  event.put(ModRegistry.FIREFLY_TYPE.get(), FireflyEntity.setCustomAttributes().build());
    }

    //paintings
    public static final RegistryObject<Motive> BOMB_PAINTING = PAINTINGS.register("bombs", () -> new Motive(32, 32));

    //enchantment
    public static final RegistryObject<Enchantment> STASIS_ENCHANTMENT = ENCHANTMENTS.register("stasis", () -> new StasisEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW, EquipmentSlot.MAINHAND));

    //particles
    public static final RegistryObject<SimpleParticleType> FIREFLY_GLOW = regParticle("firefly_glow");
    public static final RegistryObject<SimpleParticleType> SPEAKER_SOUND = regParticle("speaker_sound");
    public static final RegistryObject<SimpleParticleType> GREEN_FLAME = regParticle("green_flame");
    public static final RegistryObject<SimpleParticleType> DRIPPING_LIQUID = regParticle("dripping_liquid");
    public static final RegistryObject<SimpleParticleType> FALLING_LIQUID = regParticle("falling_liquid");
    public static final RegistryObject<SimpleParticleType> SPLASHING_LIQUID = regParticle("splashing_liquid");
    public static final RegistryObject<SimpleParticleType> BOMB_EXPLOSION_PARTICLE = regParticle("bomb_explosion");
    public static final RegistryObject<SimpleParticleType> BOMB_EXPLOSION_PARTICLE_EMITTER = regParticle("bomb_explosion_emitter");
    public static final RegistryObject<SimpleParticleType> BOMB_SMOKE_PARTICLE = regParticle("bomb_smoke");
    public static final RegistryObject<SimpleParticleType> BOTTLING_XP_PARTICLE = regParticle("bottling_xp");
    public static final RegistryObject<SimpleParticleType> FEATHER_PARTICLE = regParticle("feather");
    public static final RegistryObject<SimpleParticleType> SLINGSHOT_PARTICLE = regParticle("air_burst");
    public static final RegistryObject<SimpleParticleType> STASIS_PARTICLE = regParticle("stasis");
    public static final RegistryObject<SimpleParticleType> CONFETTI_PARTICLE = regParticle("confetti");


    //recipes
    public static final RegistryObject<RecipeSerializer<?>> BLACKBOARD_DUPLICATE_RECIPE = RECIPES.register("blackboard_duplicate_recipe", () ->
            new SimpleRecipeSerializer<>(BlackboardDuplicateRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> BAMBOO_SPIKES_TIPPED_RECIPE = RECIPES.register("bamboo_spikes_tipped_recipe", () ->
            new SimpleRecipeSerializer<>(TippedBambooSpikesRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> ROPE_ARROW_CREATE_RECIPE = RECIPES.register("rope_arrow_create_recipe", () ->
            new SimpleRecipeSerializer<>(RopeArrowCreateRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> ROPE_ARROW_ADD_RECIPE = RECIPES.register("rope_arrow_add_recipe", () ->
            new SimpleRecipeSerializer<>(RopeArrowAddRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> FLAG_FROM_BANNER_RECIPE = RECIPES.register("flag_from_banner_recipe", () ->
            new SimpleRecipeSerializer<>(FlagFromBannerRecipe::new));


    //orange trader
    public static final String RED_MERCHANT_NAME = "red_merchant";
    private static final EntityType<RedMerchantEntity> RED_MERCHANT_TYPE_RAW =
            EntityType.Builder.<RedMerchantEntity>of(RedMerchantEntity::new, MobCategory.CREATURE)
                    .setShouldReceiveVelocityUpdates(true)
                    .clientTrackingRange(10)
                    .setUpdateInterval(3)
                    .sized(0.6F, 1.95F)
                    .build(RED_MERCHANT_NAME);

    public static final RegistryObject<EntityType<RedMerchantEntity>> RED_MERCHANT_TYPE = ENTITIES.register(RED_MERCHANT_NAME, () -> RED_MERCHANT_TYPE_RAW);

    public static final RegistryObject<MenuType<RedMerchantContainer>> RED_MERCHANT_CONTAINER = CONTAINERS
            .register(RED_MERCHANT_NAME, () -> IForgeContainerType.create(RedMerchantContainer::new));

    public static final RegistryObject<Item> RED_MERCHANT_SPAWN_EGG_ITEM = ITEMS.register(RED_MERCHANT_NAME + "_spawn_egg", () ->
            new SpawnEggItem(RED_MERCHANT_TYPE_RAW, 0x7A090F, 0xF4f1e0,
                    new Item.Properties().tab(tab ? MOD_TAB : null)));

    //firefly

//    public static final String FIREFLY_NAME = "firefly";
//    private static final EntityType<FireflyEntity> FIREFLY_TYPE_RAW = (EntityType.Builder.of(FireflyEntity::new, MobCategory.AMBIENT)
//            .setShouldReceiveVelocityUpdates(true).setTrackingRange(12).setUpdateInterval(3)
//            .sized(0.3125f, 1f))
//            .build(FIREFLY_NAME);
//
//    public static final RegistryObject<EntityType<FireflyEntity>> FIREFLY_TYPE = ENTITIES.register(FIREFLY_NAME, () -> FIREFLY_TYPE_RAW);
//
//    public static final RegistryObject<Item> FIREFLY_SPAWN_EGG_ITEM = ITEMS.register(FIREFLY_NAME + "_spawn_egg", () ->
//            new SpawnEggItem(FIREFLY_TYPE_RAW, -5048018, -14409439, //-4784384, -16777216,
//                    new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, FIREFLY_NAME))));

    //brick
    public static final String THROWABLE_BRICK_NAME = "brick_projectile";
    public static final RegistryObject<EntityType<ThrowableBrickEntity>> THROWABLE_BRICK = ENTITIES.register(THROWABLE_BRICK_NAME, () -> (
            EntityType.Builder.<ThrowableBrickEntity>of(ThrowableBrickEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(ThrowableBrickEntity::new)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10))//.size(0.25F, 0.25F).trackingRange(4).updateInterval(10))
            .build(THROWABLE_BRICK_NAME));

    //bomb
    public static final String BOMB_NAME = "bomb";
    public static final RegistryObject<EntityType<BombEntity>> BOMB = ENTITIES.register(BOMB_NAME, () -> (
            EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(BombEntity::new)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(10))
            .build(BOMB_NAME));

    public static final RegistryObject<Item> BOMB_ITEM = ITEMS.register(BOMB_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME))));
    public static final RegistryObject<Item> BOMB_ITEM_ON = ITEMS.register("bomb_projectile", () -> new BombItem(new Item.Properties()
            .tab(null)));

    public static final String BOMB_BLUE_NAME = "bomb_blue";
    public static final RegistryObject<Item> BOMB_BLUE_ITEM = ITEMS.register(BOMB_BLUE_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME)), true, true));
    public static final RegistryObject<Item> BOMB_BLUE_ITEM_ON = ITEMS.register("bomb_blue_projectile", () -> new BombItem(new Item.Properties()
            .tab(null), true, false));

    //rope arrow
    public static final String ROPE_ARROW_NAME = "rope_arrow";
    public static final RegistryObject<EntityType<RopeArrowEntity>> ROPE_ARROW = ENTITIES.register(ROPE_ARROW_NAME, () -> (
            EntityType.Builder.<RopeArrowEntity>of(RopeArrowEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(RopeArrowEntity::new)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .build(ROPE_ARROW_NAME));
    public static final RegistryObject<Item> ROPE_ARROW_ITEM = ITEMS.register(ROPE_ARROW_NAME, () -> new RopeArrowItem(
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, ROPE_ARROW_NAME)).defaultDurability(24).setNoRepair()));

    //slingshot projectile
    public static final String SLINGSHOT_PROJECTILE_NAME = "slingshot_projectile";
    public static final RegistryObject<EntityType<SlingshotProjectileEntity>> SLINGSHOT_PROJECTILE = ENTITIES.register(SLINGSHOT_PROJECTILE_NAME, () -> (
            EntityType.Builder.<SlingshotProjectileEntity>of(SlingshotProjectileEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(SlingshotProjectileEntity::new)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .build(SLINGSHOT_PROJECTILE_NAME));

    //slingshot
    public static final String SLINGSHOT_NAME = "slingshot";
    public static final RegistryObject<Item> SLINGSHOT_ITEM = regItem(SLINGSHOT_NAME, () -> new SlingshotItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, SLINGSHOT_NAME))
            .stacksTo(1).durability(192))); //setISTER(() -> SlingshotItemRenderer::new)


    //blocks

    //variants:

    //TODO: datagen signs tags
    //hanging signs
    public static final String HANGING_SIGN_NAME = "hanging_sign";
    public static final Map<IWoodType, RegistryObject<Block>> HANGING_SIGNS = Variants.makeHangingSingsBlocks();
    public static final Map<IWoodType, RegistryObject<Item>> HANGING_SIGNS_ITEMS = Variants.makeHangingSignsItems();

    //keeping "hanging_sign_oak" for compatibility even if it should be just hanging_sign
    public static final RegistryObject<BlockEntityType<HangingSignBlockTile>> HANGING_SIGN_TILE = TILES
            .register(HANGING_SIGN_NAME + "_oak", () -> BlockEntityType.Builder.of(HangingSignBlockTile::new,
                    HANGING_SIGNS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //sign posts
    public static final String SIGN_POST_NAME = "sign_post";
    public static final RegistryObject<Block> SIGN_POST = BLOCKS.register(SIGN_POST_NAME, () -> new SignPostBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(2f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<SignPostBlockTile>> SIGN_POST_TILE = TILES.register(SIGN_POST_NAME, () -> BlockEntityType.Builder.of(
            SignPostBlockTile::new, SIGN_POST.get()).build(null));

    public static final Map<IWoodType, RegistryObject<Item>> SIGN_POST_ITEMS = Variants.makeSignPostItems();

    //flags
    public static final String FLAG_NAME = "flag";
    public static final Map<DyeColor, RegistryObject<Block>> FLAGS = Variants.makeFlagBlocks(FLAG_NAME);
    public static final Map<DyeColor, RegistryObject<Item>> FLAGS_ITEMS = Variants.makeFlagItems(FLAG_NAME);

    public static final RegistryObject<BlockEntityType<FlagBlockTile>> FLAG_TILE = TILES
            .register(FLAG_NAME, () -> BlockEntityType.Builder.of(FlagBlockTile::new,
                    FLAGS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //ceiling banner
    public static final String CEILING_BANNER_NAME = "ceiling_banner";
    public static final Map<DyeColor, RegistryObject<Block>> CEILING_BANNERS = Variants.makeCeilingBanners(CEILING_BANNER_NAME);

    public static final RegistryObject<BlockEntityType<CeilingBannerBlockTile>> CEILING_BANNER_TILE = TILES
            .register(CEILING_BANNER_NAME, () -> BlockEntityType.Builder.of(CeilingBannerBlockTile::new,
                    CEILING_BANNERS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //presents
    public static final String PRESENT_NAME = "present";

    public static final Map<DyeColor, RegistryObject<Block>> PRESENTS = Variants.makePresents(PRESENT_NAME);

    public static final RegistryObject<BlockEntityType<PresentBlockTile>> PRESENT_TILE = TILES
            .register(PRESENT_NAME, () -> BlockEntityType.Builder.of(PresentBlockTile::new,
                    PRESENTS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static final Map<DyeColor, RegistryObject<Item>> PRESENTS_ITEMS = Variants.makePresentsItems();

    public static final RegistryObject<MenuType<PresentContainer>> PRESENT_BLOCK_CONTAINER = CONTAINERS
            .register(PRESENT_NAME, () -> IForgeContainerType.create(PresentContainer::new));


    //key
    public static final String KEY_NAME = "key";
    public static final RegistryObject<Item> KEY_ITEM = regItem(KEY_NAME, () -> new KeyItem(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_TOOLS, KEY_NAME))));
    //flute
    public static final String FLUTE_NAME = "flute";
    public static final RegistryObject<Item> FLUTE_ITEM = regItem(FLUTE_NAME, () -> new Flute((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, FLUTE_NAME)).stacksTo(1).durability(32)));

    //candy
    public static final String CANDY_NAME = "candy";
    public static final RegistryObject<Item> CANDY_ITEM = regItem(CANDY_NAME, () -> new CandyItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_FOOD, CANDY_NAME))));
    //speedometer
    /*
    public static final String SPEEDOMETER_NAME = "speedometer";
    public static final RegistryObject<Item> SPEEDOMETER_ITEM = regItem(SPEEDOMETER_NAME,()-> new SpeedometerItem(
            (new Item.Properties()).tab(null)));
    */

    //decoration blocks

    //planter
    public static final String PLANTER_NAME = "planter";
    public static final RegistryObject<Block> PLANTER = BLOCKS.register(PLANTER_NAME, () -> new PlanterBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED)
                    .strength(2f, 6f)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<Item> PLANTER_ITEM = regBlockItem(PLANTER, getTab(CreativeModeTab.TAB_DECORATIONS, PLANTER_NAME));

    //pedestal
    public static final String PEDESTAL_NAME = "pedestal";
    public static final RegistryObject<Block> PEDESTAL = BLOCKS.register(PEDESTAL_NAME, () -> new PedestalBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<BlockEntityType<PedestalBlockTile>> PEDESTAL_TILE = TILES.register(PEDESTAL_NAME, () -> BlockEntityType.Builder.of(
            PedestalBlockTile::new, PEDESTAL.get()).build(null));
    public static final RegistryObject<Item> PEDESTAL_ITEM = regBlockItem(PEDESTAL, getTab(CreativeModeTab.TAB_DECORATIONS, PEDESTAL_NAME));

    //notice board
    public static final String NOTICE_BOARD_NAME = "notice_board";
    public static final RegistryObject<Block> NOTICE_BOARD = BLOCKS.register(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<BlockEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = TILES.register(NOTICE_BOARD_NAME, () -> BlockEntityType.Builder.of(
            NoticeBoardBlockTile::new, NOTICE_BOARD.get()).build(null));

    public static final RegistryObject<Item> NOTICE_BOARD_ITEM = ITEMS.register(NOTICE_BOARD_NAME, () -> new BurnableBlockItem(NOTICE_BOARD.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, NOTICE_BOARD_NAME)), 300));

    public static final RegistryObject<MenuType<NoticeBoardContainer>> NOTICE_BOARD_CONTAINER = CONTAINERS
            .register(NOTICE_BOARD_NAME, () -> IForgeContainerType.create(NoticeBoardContainer::new));

    //safe
    public static final String SAFE_NAME = "safe";
    public static final RegistryObject<Block> SAFE = BLOCKS.register(SAFE_NAME, () -> new SafeBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
    ));
    public static final RegistryObject<BlockEntityType<SafeBlockTile>> SAFE_TILE = TILES.register(SAFE_NAME, () -> BlockEntityType.Builder.of(
            SafeBlockTile::new, SAFE.get()).build(null));
    public static final RegistryObject<Item> SAFE_ITEM = ITEMS.register(SAFE_NAME, () -> new SafeItem(SAFE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SAFE_NAME)).stacksTo(1).fireResistant()));

    //cage
    public static final String CAGE_NAME = "cage";
    public static final RegistryObject<Block> CAGE = BLOCKS.register(CAGE_NAME, () -> new CageBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .sound(SoundType.METAL)
    ));
    public static final RegistryObject<BlockEntityType<CageBlockTile>> CAGE_TILE = TILES.register(CAGE_NAME, () -> BlockEntityType.Builder.of(
            CageBlockTile::new, CAGE.get()).build(null));

    public static final RegistryObject<Item> CAGE_ITEM = ITEMS.register(CAGE_NAME, () -> new CageItem(CAGE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, CAGE_NAME))
                    .stacksTo(16)));

    //jar
    public static final String JAR_NAME = "jar";
    public static final RegistryObject<Block> JAR = BLOCKS.register(JAR_NAME, () -> new JarBlock(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                    .strength(1f, 1f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    ));

    public static final String JAR_NAME_TINTED = "jar_tinted";
    public static final RegistryObject<Block> JAR_TINTED = BLOCKS.register(JAR_NAME_TINTED, () -> new JarBlock(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.COLOR_BLACK)
                    .strength(1f, 1f)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
    ));

    public static final RegistryObject<BlockEntityType<JarBlockTile>> JAR_TILE = TILES.register(JAR_NAME, () -> BlockEntityType.Builder.of(
            JarBlockTile::new, JAR.get(), JAR_TINTED.get()).build(null));

    public static final RegistryObject<Item> JAR_ITEM = ITEMS.register(JAR_NAME, () -> new JarItem(JAR.get(), new Item.Properties().tab(
            getTab(CreativeModeTab.TAB_DECORATIONS, JAR_NAME)).stacksTo(16)));

    public static final RegistryObject<Item> JAR_ITEM_TINTED = ITEMS.register(JAR_NAME_TINTED, () -> new TintedJarItem(JAR_TINTED.get(), new Item.Properties().tab(
            getTab(CreativeModeTab.TAB_DECORATIONS, JAR_NAME)).stacksTo(16)));


    //sack
    public static final String SACK_NAME = "sack";
    public static final RegistryObject<Block> SACK = BLOCKS.register(SACK_NAME, () -> new SackBlock(
            BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                    .strength(1F)
                    .sound(SoundType.WOOL)
    ));
    public static final RegistryObject<BlockEntityType<SackBlockTile>> SACK_TILE = TILES.register(SACK_NAME, () -> BlockEntityType.Builder.of(
            SackBlockTile::new, SACK.get()).build(null));

    public static final RegistryObject<MenuType<SackContainer>> SACK_CONTAINER = CONTAINERS.register(SACK_NAME, () -> IForgeContainerType.create(
            SackContainer::new));

    public static final RegistryObject<Item> SACK_ITEM = regItem(SACK_NAME, () -> new SackItem(SACK.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, SACK_NAME)).stacksTo(1)));

    //blackboard
    public static final String BLACKBOARD_NAME = "blackboard";
    public static final RegistryObject<Block> BLACKBOARD = BLOCKS.register(BLACKBOARD_NAME, () -> new BlackboardBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2, 3)
    ));
    public static final RegistryObject<BlockEntityType<BlackboardBlockTile>> BLACKBOARD_TILE = TILES.register(BLACKBOARD_NAME, () -> BlockEntityType.Builder.of(
            BlackboardBlockTile::new, BLACKBOARD.get()).build(null));
    public static final RegistryObject<Item> BLACKBOARD_ITEM = ITEMS.register(BLACKBOARD_NAME, () -> new BlackboardItem(BLACKBOARD.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BLACKBOARD_NAME))));

    //globe
    public static final String GLOBE_NAME = "globe";
    public static final RegistryObject<Block> GLOBE = BLOCKS.register(GLOBE_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<BlockEntityType<GlobeBlockTile>> GLOBE_TILE = TILES.register(GLOBE_NAME, () -> BlockEntityType.Builder.of(
            GlobeBlockTile::new, GLOBE.get()).build(null));
    public static final RegistryObject<Item> GLOBE_ITEM = ITEMS.register(GLOBE_NAME, () -> new BlockItem(GLOBE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, GLOBE_NAME)).rarity(Rarity.RARE)));
    /*
    //candle holder
    public static final String CANDLE_HOLDER_NAME = "candle_holder";
    public static final RegistryObject<Block> CANDLE_HOLDER = BLOCKS.register(CANDLE_HOLDER_NAME, () -> new CandleHolderBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .instabreak()
                    .noCollission()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 14 : 0)
                    .sound(SoundType.LANTERN), () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> CANDLE_HOLDER_ITEM = regBlockItem(CANDLE_HOLDER, getTab(CreativeModeTab.TAB_DECORATIONS, CANDLE_HOLDER_NAME));


    //candelabra
    public static final String CANDELABRA_NAME = "candelabra";
    public static final RegistryObject<Block> CANDELABRA = BLOCKS.register(CANDELABRA_NAME, () -> new CandelabraBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 14 : 0)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM = regBlockItem(CANDELABRA, getTab(CreativeModeTab.TAB_DECORATIONS, CANDELABRA_NAME));

    //silver
    public static final String CANDELABRA_NAME_SILVER = "candelabra_silver";
    public static final RegistryObject<Block> CANDELABRA_SILVER = BLOCKS.register(CANDELABRA_NAME_SILVER, () -> new CandelabraBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 14 : 0)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM_SILVER = regBlockItem(CANDELABRA_SILVER, getTab(CreativeModeTab.TAB_DECORATIONS, CANDELABRA_NAME_SILVER));
    */

    //sconce
    //normal
    public static final String SCONCE_NAME = "sconce";
    public static final RegistryObject<Block> SCONCE = BLOCKS.register(SCONCE_NAME, () -> new SconceBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .noCollission()
                    .instabreak()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 14 : 0)
                    .sound(SoundType.LANTERN), () -> ParticleTypes.FLAME));
    public static final RegistryObject<Block> SCONCE_WALL = BLOCKS.register("sconce_wall", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE.get()), () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM = ITEMS.register(SCONCE_NAME, () -> new StandingAndWallBlockItem(SCONCE.get(), SCONCE_WALL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //soul
    public static final String SCONCE_NAME_SOUL = "sconce_soul";
    public static final RegistryObject<Block> SCONCE_SOUL = BLOCKS.register(SCONCE_NAME_SOUL, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 10 : 0),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_SOUL = BLOCKS.register("sconce_wall_soul", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_SOUL.get())
                    .dropsLike(SCONCE_SOUL.get()),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_SOUL = ITEMS.register(SCONCE_NAME_SOUL, () -> new StandingAndWallBlockItem(SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_SOUL))));

    //optional: endergetic
    public static final String SCONCE_NAME_ENDER = "sconce_ender";
    public static final RegistryObject<Block> SCONCE_ENDER = BLOCKS.register(SCONCE_NAME_ENDER, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 13 : 0),
            CompatObjects.ENDER_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_ENDER = BLOCKS.register("sconce_wall_ender", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_ENDER.get()),
            CompatObjects.ENDER_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_ENDER = ITEMS.register(SCONCE_NAME_ENDER, () -> new StandingAndWallBlockItem(SCONCE_ENDER.get(), SCONCE_WALL_ENDER.get(),
            (new Item.Properties()).tab(getTab("endergetic", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_ENDER))));

    //optional: infernal expansion
    public static final String SCONCE_NAME_GLOW = "sconce_glow";
    public static final RegistryObject<Block> SCONCE_GLOW = BLOCKS.register(SCONCE_NAME_GLOW, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 13 : 0),
            CompatObjects.GLOW_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_GLOW = BLOCKS.register("sconce_wall_glow", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE_GLOW.get()),
            CompatObjects.GLOW_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_GLOW = ITEMS.register(SCONCE_NAME_GLOW, () -> new StandingAndWallBlockItem(SCONCE_GLOW.get(), SCONCE_WALL_GLOW.get(),
            (new Item.Properties()).tab(getTab("infernalexp", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_GLOW))));

    //green
    public static final String SCONCE_NAME_GREEN = "sconce_green";
    public static final RegistryObject<Block> SCONCE_GREEN = BLOCKS.register(SCONCE_NAME_GREEN, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get()), GREEN_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_GREEN = BLOCKS.register("sconce_wall_green", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_GREEN.get()), GREEN_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_GREEN = ITEMS.register(SCONCE_NAME_GREEN, () -> new StandingAndWallBlockItem(SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_GREEN))));

    //copper lantern
    public static final String COPPER_LANTERN_NAME = "copper_lantern";
    public static final RegistryObject<Block> COPPER_LANTERN = BLOCKS.register(COPPER_LANTERN_NAME, () -> new CopperLanternBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> state.getValue(CopperLanternBlock.LIT) ? 15 : 0)
                    //TODO: add custom sound mixed
                    .sound(SoundType.COPPER)
                    .noOcclusion()
    ));
    public static final RegistryObject<Item> COPPER_LANTERN_ITEM = regBlockItem(COPPER_LANTERN, getTab(CreativeModeTab.TAB_DECORATIONS, COPPER_LANTERN_NAME));

    //brass lantern
    public static final String BRASS_LANTERN_NAME = "brass_lantern";
    public static final RegistryObject<Block> BRASS_LANTERN = BLOCKS.register(BRASS_LANTERN_NAME, () -> new CopperLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get())));

    public static final RegistryObject<Item> BRASS_LANTERN_ITEM = regBlockItem(BRASS_LANTERN, getTab(CreativeModeTab.TAB_DECORATIONS, BRASS_LANTERN_NAME));

    public static final RegistryObject<BlockEntityType<OilLanternBlockTile>> COPPER_LANTERN_TILE = TILES.register(COPPER_LANTERN_NAME, () -> BlockEntityType.Builder.of(
            OilLanternBlockTile::new, COPPER_LANTERN.get(), BRASS_LANTERN.get()).build(null));

    //crimson lantern
    public static final String CRIMSON_LANTERN_NAME = "crimson_lantern";
    public static final RegistryObject<Block> CRIMSON_LANTERN = BLOCKS.register(CRIMSON_LANTERN_NAME, () -> new CrimsonLanternBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_RED)
                    .strength(1.5f)
                    .sound(SoundType.WOOL)
                    .lightLevel((state) -> 15)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<OilLanternBlockTile>> CRIMSON_LANTERN_TILE = TILES.register(CRIMSON_LANTERN_NAME, () -> BlockEntityType.Builder.of(
            OilLanternBlockTile::new, CRIMSON_LANTERN.get()).build(null));
    public static final RegistryObject<Item> CRIMSON_LANTERN_ITEM = regBlockItem(CRIMSON_LANTERN, getTab(CreativeModeTab.TAB_DECORATIONS, CRIMSON_LANTERN_NAME));


    //rope
    public static final String ROPE_NAME = "rope";
    public static final RegistryObject<Block> ROPE = BLOCKS.register(ROPE_NAME, () -> new RopeBlock(
            BlockBehaviour.Properties.of(Material.WOOL)
                    .sound(SoundType.WOOL)
                    .instabreak()
                    .speedFactor(0.7f)
                    .noOcclusion()));
    public static final RegistryObject<Item> ROPE_ITEM = ITEMS.register(ROPE_NAME, () -> new RopeItem(ROPE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, ROPE_NAME))));

    public static final String ROPE_KNOT_NAME = "rope_knot";
    public static final RegistryObject<Block> ROPE_KNOT = BLOCKS.register(ROPE_KNOT_NAME, () -> new RopeKnotBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));

    public static final RegistryObject<BlockEntityType<RopeKnotBlockTile>> ROPE_KNOT_TILE = TILES.register(ROPE_KNOT_NAME, () -> BlockEntityType.Builder.of(
            RopeKnotBlockTile::new, ROPE_KNOT.get()).build(null));

    //spikes
    public static final String BAMBOO_SPIKES_NAME = "bamboo_spikes";
    public static final RegistryObject<Block> BAMBOO_SPIKES = BLOCKS.register(BAMBOO_SPIKES_NAME, () -> new BambooSpikesBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                    .sound(SoundType.SCAFFOLDING)
                    .isRedstoneConductor((a, b, c) -> false)
                    .strength(2)
                    .noOcclusion()));
    public static final RegistryObject<BlockEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = TILES.register(BAMBOO_SPIKES_NAME, () -> BlockEntityType.Builder.of(
            BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()).build(null));

    public static final RegistryObject<Item> BAMBOO_SPIKES_ITEM = ITEMS.register(BAMBOO_SPIKES_NAME, () -> new BambooSpikesItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BAMBOO_SPIKES_NAME))));
    public static final String TIPPED_SPIKES_NAME = "bamboo_spikes_tipped";
    public static final RegistryObject<Item> BAMBOO_SPIKES_TIPPED_ITEM = ITEMS.register(TIPPED_SPIKES_NAME, () -> new BambooSpikesTippedItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).defaultDurability(BambooSpikesBlockTile.MAX_CHARGES).setNoRepair().tab(getTab(CreativeModeTab.TAB_BREWING, TIPPED_SPIKES_NAME))));

    //goblet
    public static final String GOBLET_NAME = "goblet";
    public static final RegistryObject<Block> GOBLET = BLOCKS.register(GOBLET_NAME, () -> new GobletBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(1.5f, 2f)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Item> GOBLET_ITEM = regBlockItem(GOBLET, getTab(CreativeModeTab.TAB_DECORATIONS, GOBLET_NAME));

    public static final RegistryObject<BlockEntityType<GobletBlockTile>> GOBLET_TILE = TILES.register(GOBLET_NAME, () -> BlockEntityType.Builder.of(
            GobletBlockTile::new, GOBLET.get()).build(null));

    //hourglass
    public static final String HOURGLASS_NAME = "hourglass";
    public static final RegistryObject<Block> HOURGLASS = BLOCKS.register(HOURGLASS_NAME, () -> new HourGlassBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<BlockEntityType<HourGlassBlockTile>> HOURGLASS_TILE = TILES.register(HOURGLASS_NAME, () -> BlockEntityType.Builder.of(
            HourGlassBlockTile::new, HOURGLASS.get()).build(null));
    public static final RegistryObject<Item> HOURGLASS_ITEM = regBlockItem(HOURGLASS, getTab(CreativeModeTab.TAB_DECORATIONS, HOURGLASS_NAME));

    //item shelf
    public static final String ITEM_SHELF_NAME = "item_shelf";
    public static final RegistryObject<Block> ITEM_SHELF = BLOCKS.register(ITEM_SHELF_NAME, () -> new ItemShelfBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(0.75f, 0.1f)
                    .noOcclusion()
                    .noCollission()
    ));
    public static final RegistryObject<BlockEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = TILES.register(ITEM_SHELF_NAME, () -> BlockEntityType.Builder.of(
            ItemShelfBlockTile::new, ITEM_SHELF.get()).build(null));
    public static final RegistryObject<Item> ITEM_SHELF_ITEM = ITEMS.register(ITEM_SHELF_NAME, () -> new BurnableBlockItem(ITEM_SHELF.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, ITEM_SHELF_NAME)), 100));

    //doormat
    public static final String DOORMAT_NAME = "doormat";
    public static final RegistryObject<Block> DOORMAT = BLOCKS.register(DOORMAT_NAME, () -> new DoormatBlock(
            BlockBehaviour.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_YELLOW)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<DoormatBlockTile>> DOORMAT_TILE = TILES.register(DOORMAT_NAME, () -> BlockEntityType.Builder.of(
            DoormatBlockTile::new, DOORMAT.get()).build(null));
    public static final RegistryObject<Item> DOORMAT_ITEM = ITEMS.register(DOORMAT_NAME, () -> new BurnableBlockItem(DOORMAT.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, DOORMAT_NAME)), 134));

    //magma cream block
    public static final String MAGMA_CREAM_BLOCK_NAME = "magma_cream_block";
    public static final RegistryObject<Block> MAGMA_CREAM_BLOCK = BLOCKS.register(MAGMA_CREAM_BLOCK_NAME, () -> new MagmaCreamBlock(
            BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)));
    public static final RegistryObject<Item> MAGMA_CREAM_BLOCK_ITEM = ITEMS.register(MAGMA_CREAM_BLOCK_NAME, () -> new BlockItem(MAGMA_CREAM_BLOCK.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, MAGMA_CREAM_BLOCK_NAME))));

    //raked gravel
    public static final String RAKED_GRAVEL_NAME = "raked_gravel";
    public static final RegistryObject<Block> RAKED_GRAVEL = BLOCKS.register(RAKED_GRAVEL_NAME, () -> new RakedGravelBlock(
            BlockBehaviour.Properties.copy(Blocks.GRAVEL)
                    .isViewBlocking((w, s, p) -> true)
                    .isSuffocating((w, s, p) -> true)));

    public static final RegistryObject<Item> RAKED_GRAVEL_ITEM = regBlockItem(RAKED_GRAVEL, getTab(CreativeModeTab.TAB_DECORATIONS, RAKED_GRAVEL_NAME));


    //redstone blocks

    //cog block
    public static final String COG_BLOCK_NAME = "cog_block";
    public static final RegistryObject<Block> COG_BLOCK = BLOCKS.register(COG_BLOCK_NAME, () -> new CogBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<Item> COG_BLOCK_ITEM = regBlockItem(COG_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, COG_BLOCK_NAME));

    //piston launcher base
    public static final String SPRING_LAUNCHER_NAME = "spring_launcher";
    public static final RegistryObject<Block> SPRING_LAUNCHER = BLOCKS.register(SPRING_LAUNCHER_NAME, () -> new SpringLauncherBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isSuffocating((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isViewBlocking((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
    ));
    public static final RegistryObject<Item> PISTON_LAUNCHER_ITEM = regBlockItem(SPRING_LAUNCHER, getTab(CreativeModeTab.TAB_REDSTONE, SPRING_LAUNCHER_NAME));

    public static final String PISTON_LAUNCHER_HEAD_NAME = "spring_launcher_head";
    public static final RegistryObject<Block> SPRING_LAUNCHER_HEAD = BLOCKS.register(PISTON_LAUNCHER_HEAD_NAME, () -> new SpringLauncherHeadBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .noDrops()
                    .jumpFactor(1.18f)
    ));
    public static final String PISTON_LAUNCHER_ARM_NAME = "spring_launcher_arm";
    public static final RegistryObject<Block> SPRING_LAUNCHER_ARM = BLOCKS.register(PISTON_LAUNCHER_ARM_NAME, () -> new SpringLauncherArmBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(50f, 50f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noDrops()
    ));
    public static final RegistryObject<BlockEntityType<SpringLauncherArmBlockTile>> SPRING_LAUNCHER_ARM_TILE = TILES.register(PISTON_LAUNCHER_ARM_NAME, () -> BlockEntityType.Builder.of(
            SpringLauncherArmBlockTile::new, SPRING_LAUNCHER_ARM.get()).build(null));

    //speaker Block
    public static final String SPEAKER_BLOCK_NAME = "speaker_block";
    public static final RegistryObject<Block> SPEAKER_BLOCK = BLOCKS.register(SPEAKER_BLOCK_NAME, () -> new SpeakerBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(1f, 2f)
                    .sound(SoundType.WOOD)
    ));
    public static final RegistryObject<BlockEntityType<?>> SPEAKER_BLOCK_TILE = TILES.register(SPEAKER_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            SpeakerBlockTile::new, SPEAKER_BLOCK.get()).build(null));

    public static final RegistryObject<Item> SPEAKER_BLOCK_ITEM = ITEMS.register(SPEAKER_BLOCK_NAME, () -> new BurnableBlockItem(SPEAKER_BLOCK.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_REDSTONE, SPEAKER_BLOCK_NAME)), 300));

    //turn table
    public static final String TURN_TABLE_NAME = "turn_table";
    public static final RegistryObject<Block> TURN_TABLE = BLOCKS.register(TURN_TABLE_NAME, () -> new TurnTableBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
                    .strength(0.75f, 2f)
                    .sound(SoundType.STONE)
    ));
    public static final RegistryObject<BlockEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = TILES.register(TURN_TABLE_NAME, () -> BlockEntityType.Builder.of(
            TurnTableBlockTile::new, TURN_TABLE.get()).build(null));

    public static final RegistryObject<Item> TURN_TABLE_ITEM = regBlockItem(TURN_TABLE, getTab(CreativeModeTab.TAB_REDSTONE, TURN_TABLE_NAME));

    //illuminator
    public static final String REDSTONE_ILLUMINATOR_NAME = "redstone_illuminator";
    public static final RegistryObject<Block> REDSTONE_ILLUMINATOR = BLOCKS.register(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS, MaterialColor.QUARTZ)
                    .strength(0.3f, 0.3f)
                    .sound(SoundType.GLASS)
                    .lightLevel((state) -> 15)
    ));
    public static final RegistryObject<Item> REDSTONE_ILLUMINATOR_ITEM = regBlockItem(REDSTONE_ILLUMINATOR, getTab(CreativeModeTab.TAB_REDSTONE, REDSTONE_ILLUMINATOR_NAME));

    //pulley
    public static final String PULLEY_BLOCK_NAME = "pulley_block";
    public static final RegistryObject<Block> PULLEY_BLOCK = BLOCKS.register(PULLEY_BLOCK_NAME, () -> new PulleyBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> PULLEY_BLOCK_ITEM = regBlockItem(PULLEY_BLOCK, getTab(CreativeModeTab.TAB_DECORATIONS, PULLEY_BLOCK_NAME), 300);

    public static final RegistryObject<MenuType<PulleyBlockContainer>> PULLEY_BLOCK_CONTAINER = CONTAINERS
            .register(PULLEY_BLOCK_NAME, () -> IForgeContainerType.create(PulleyBlockContainer::new));
    public static final RegistryObject<BlockEntityType<PulleyBlockTile>> PULLEY_BLOCK_TILE = TILES.register(PULLEY_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            PulleyBlockTile::new, PULLEY_BLOCK.get()).build(null));

    //lock block
    public static final String LOCK_BLOCK_NAME = "lock_block";
    public static final RegistryObject<Block> LOCK_BLOCK = BLOCKS.register(LOCK_BLOCK_NAME, () -> new LockBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL))
    );
    public static final RegistryObject<Item> LOCK_BLOCK_ITEM = regBlockItem(LOCK_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, LOCK_BLOCK_NAME));

    //bellows
    public static final String BELLOWS_NAME = "bellows";
    public static final RegistryObject<Block> BELLOWS = BLOCKS.register(BELLOWS_NAME, () -> new BellowsBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<BellowsBlockTile>> BELLOWS_TILE = TILES.register(BELLOWS_NAME, () -> BlockEntityType.Builder.of(
            BellowsBlockTile::new, BELLOWS.get()).build(null));
    public static final RegistryObject<Item> BELLOWS_ITEM = ITEMS.register(BELLOWS_NAME, () -> new BurnableBlockItem(BELLOWS.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_REDSTONE, BELLOWS_NAME)), 300));

    //clock
    public static final String CLOCK_BLOCK_NAME = "clock_block";
    public static final RegistryObject<Block> CLOCK_BLOCK = BLOCKS.register(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 6f)
                    .sound(SoundType.WOOD)
                    .lightLevel((state) -> 1)
    ));
    public static final RegistryObject<BlockEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = TILES.register(CLOCK_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            ClockBlockTile::new, CLOCK_BLOCK.get()).build(null));

    public static final RegistryObject<Item> CLOCK_BLOCK_ITEM = regBlockItem(CLOCK_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, CLOCK_BLOCK_NAME));

    //sconce lever
    public static final String SCONCE_LEVER_NAME = "sconce_lever";
    public static final RegistryObject<Block> SCONCE_LEVER = BLOCKS.register(SCONCE_LEVER_NAME, () -> new SconceLeverBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()),
            () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_LEVER_ITEM = regBlockItem(SCONCE_LEVER, getTab(CreativeModeTab.TAB_REDSTONE, SCONCE_LEVER_NAME));

    //crank
    public static final String CRANK_NAME = "crank";
    public static final RegistryObject<Block> CRANK = BLOCKS.register(CRANK_NAME, () -> new CrankBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.NONE)
                    .strength(0.6f, 0.6f)
                    .noCollission()
                    .noOcclusion()
    ));
    public static final RegistryObject<Item> CRANK_ITEM = regBlockItem(CRANK, getTab(CreativeModeTab.TAB_REDSTONE, CRANK_NAME));

    //wind vane
    public static final String WIND_VANE_NAME = "wind_vane";
    public static final RegistryObject<Block> WIND_VANE = BLOCKS.register(WIND_VANE_NAME, () -> new WindVaneBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<WindVaneBlockTile>> WIND_VANE_TILE = TILES.register(WIND_VANE_NAME, () -> BlockEntityType.Builder.of(
            WindVaneBlockTile::new, WIND_VANE.get()).build(null));

    public static final RegistryObject<Item> WIND_VANE_ITEM = regBlockItem(WIND_VANE, getTab(CreativeModeTab.TAB_REDSTONE, WIND_VANE_NAME));

    //faucet
    public static final String FAUCET_NAME = "faucet";
    public static final RegistryObject<Block> FAUCET = BLOCKS.register(FAUCET_NAME, () -> new FaucetBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 4.8f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<FaucetBlockTile>> FAUCET_TILE = TILES.register(FAUCET_NAME, () -> BlockEntityType.Builder.of(
            FaucetBlockTile::new, FAUCET.get()).build(null));

    public static final RegistryObject<Item> FAUCET_ITEM = regBlockItem(FAUCET, getTab(CreativeModeTab.TAB_REDSTONE, FAUCET_NAME));

    //gold door
    public static final String GOLD_DOOR_NAME = "gold_door";
    public static final RegistryObject<Block> GOLD_DOOR = BLOCKS.register(GOLD_DOOR_NAME, () -> new GoldDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()));
    public static final RegistryObject<Item> GOLD_DOOR_ITEM = regBlockItem(GOLD_DOOR, getTab(CreativeModeTab.TAB_REDSTONE, GOLD_DOOR_NAME));

    //gold trapdoor
    public static final String GOLD_TRAPDOOR_NAME = "gold_trapdoor";
    public static final RegistryObject<Block> GOLD_TRAPDOOR = BLOCKS.register(GOLD_TRAPDOOR_NAME, () -> new GoldTrapdoorBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final RegistryObject<Item> GOLD_TRAPDOOR_ITEM = regBlockItem(GOLD_TRAPDOOR, getTab(CreativeModeTab.TAB_REDSTONE, GOLD_TRAPDOOR_NAME));

    //netherite doors
    public static final String NETHERITE_DOOR_NAME = "netherite_door";
    public static final RegistryObject<Block> NETHERITE_DOOR = BLOCKS.register(NETHERITE_DOOR_NAME, () -> new NetheriteDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .noOcclusion()
    ));
    public static final RegistryObject<Item> NETHERITE_DOOR_ITEM = ITEMS.register(NETHERITE_DOOR_NAME, () -> new BlockItem(NETHERITE_DOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_DOOR_NAME)).fireResistant()));

    //netherite trapdoor
    public static final String NETHERITE_TRAPDOOR_NAME = "netherite_trapdoor";
    public static final RegistryObject<Block> NETHERITE_TRAPDOOR = BLOCKS.register(NETHERITE_TRAPDOOR_NAME, () -> new NetheriteTrapdoorBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)
    ));
    public static final RegistryObject<Item> NETHERITE_TRAPDOOR_ITEM = ITEMS.register(NETHERITE_TRAPDOOR_NAME, () -> new BlockItem(NETHERITE_TRAPDOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_TRAPDOOR_NAME)).fireResistant()));

    public static final RegistryObject<BlockEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = TILES.register("key_lockable_tile", () -> BlockEntityType.Builder.of(
            KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()).build(null));

    //iron gate
    public static final String IRON_GATE_NAME = "iron_gate";
    public static final RegistryObject<Block> IRON_GATE = BLOCKS.register(IRON_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), false));
    public static final RegistryObject<Item> IRON_GATE_ITEM = regBlockItem(IRON_GATE, getTab(CreativeModeTab.TAB_REDSTONE, IRON_GATE_NAME));

    //gold gate
    public static final String GOLD_GATE_NAME = "gold_gate";
    public static final RegistryObject<Block> GOLD_GATE = BLOCKS.register(GOLD_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), true));
    public static final RegistryObject<Item> GOLD_GATE_ITEM = regBlockItem(GOLD_GATE, getTab("quark", CreativeModeTab.TAB_REDSTONE, IRON_GATE_NAME));


    //technical blocks

    //wall lantern
    public static final String WALL_LANTERN_NAME = "wall_lantern";
    public static final RegistryObject<Block> WALL_LANTERN = BLOCKS.register(WALL_LANTERN_NAME, () -> new WallLanternBlock(
            BlockBehaviour.Properties.copy(Blocks.LANTERN)
                    .lightLevel((state) -> 15)
                    .noDrops()
    ));
    public static final RegistryObject<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = TILES.register(WALL_LANTERN_NAME, () -> BlockEntityType.Builder.of(
            WallLanternBlockTile::new, WALL_LANTERN.get()).build(null));


    //hanging flower pot
    public static final String HANGING_FLOWER_POT_NAME = "hanging_flower_pot";
    public static final RegistryObject<Block> HANGING_FLOWER_POT = BLOCKS.register(HANGING_FLOWER_POT_NAME, () -> new HangingFlowerPotBlock(
            BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)
    ));
    public static final RegistryObject<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = TILES.register(HANGING_FLOWER_POT_NAME, () -> BlockEntityType.Builder.of(
            HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()).build(null));


    //double cake
    public static final String DOUBLE_CAKE_NAME = "double_cake";
    public static final RegistryObject<Block> DOUBLE_CAKE = BLOCKS.register(DOUBLE_CAKE_NAME, () -> new DoubleCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
    ));
    //directional cake
    public static final String DIRECTIONAL_CAKE_NAME = "directional_cake";
    public static final RegistryObject<Block> DIRECTIONAL_CAKE = BLOCKS.register(DIRECTIONAL_CAKE_NAME, () -> new DirectionalCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
                    .dropsLike(Blocks.CAKE)
    ));

    //checker block
    public static final String CHECKER_BLOCK_NAME = "checker_block";
    public static final RegistryObject<Block> CHECKER_BLOCK = BLOCKS.register(CHECKER_BLOCK_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F))
    );
    public static final RegistryObject<Item> CHECKER_BLOCK_ITEM = ITEMS.register(CHECKER_BLOCK_NAME, () -> new BlockItem(CHECKER_BLOCK.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));
    //slab
    public static final String CHECKER_SLAB_NAME = "checker_slab";
    public static final RegistryObject<Block> CHECKER_SLAB = BLOCKS.register(CHECKER_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get()))
    );
    public static final RegistryObject<Item> CHECKER_SLAB_ITEM = ITEMS.register(CHECKER_SLAB_NAME, () -> new BlockItem(CHECKER_SLAB.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));
    //vertical slab
    public static final String CHECKER_VERTICAL_SLAB_NAME = "checker_vertical_slab";
    public static final RegistryObject<Block> CHECKER_VERTICAL_SLAB = BLOCKS.register(CHECKER_VERTICAL_SLAB_NAME, () -> new VerticalSlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get()))
    );
    public static final RegistryObject<Item> CHECKER_VERTICAL_SLAB_ITEM = ITEMS.register(CHECKER_VERTICAL_SLAB_NAME, () -> new BlockItem(CHECKER_VERTICAL_SLAB.get(),
            (new Item.Properties()).tab(getTab("quark", CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));

    //pancakes
    public static final String PANCAKE_NAME = "pancake";
    public static final RegistryObject<Block> PANCAKE = BLOCKS.register(PANCAKE_NAME, () -> new PancakeBlock(
            BlockBehaviour.Properties.of(Material.CAKE, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(0.5F)
                    .sound(SoundType.WOOL))
    );
    public static final RegistryObject<Item> PANCAKE_ITEM = ITEMS.register(PANCAKE_NAME, () -> new PancakeItem(PANCAKE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_FOOD, PANCAKE_NAME))
    ));
    public static final RegistryObject<Item> PANCAKE_DISC = ITEMS.register("pancake_disc",
            () -> new RecordItem(15, PANCAKE_MUSIC, new Item.Properties().tab(null)
            ));

    //flax
    public static final String FLAX_NAME = "flax";
    public static final RegistryObject<Block> FLAX = BLOCKS.register(FLAX_NAME, () -> new FlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.ROSE_BUSH)
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP))
    );
    public static final RegistryObject<Item> FLAX_ITEM = ITEMS.register(FLAX_NAME, () -> new Item(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final RegistryObject<Item> FLAX_SEEDS_ITEM = ITEMS.register("flax_seeds", () -> new ItemNameBlockItem(FLAX.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final String FLAX_WILD_NAME = "wild_flax";
    public static final RegistryObject<Block> FLAX_WILD = BLOCKS.register(FLAX_WILD_NAME, () -> new WildFlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.TALL_GRASS))
    );
    public static final RegistryObject<Item> FLAX_WILD_ITEM = ITEMS.register(FLAX_WILD_NAME, () -> new BlockItem(FLAX_WILD.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, FLAX_WILD_NAME))));

    //pot
    public static final RegistryObject<Block> FLAX_POT = BLOCKS.register("potted_flax", () -> new FlowerPotBlock(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT, FLAX, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));

    //fodder
    public static final String FODDER_NAME = "fodder";
    public static final RegistryObject<Block> FODDER = BLOCKS.register(FODDER_NAME, () -> new FodderBlock(
            BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)));
    public static final RegistryObject<Item> FODDER_ITEM = ITEMS.register(FODDER_NAME, () -> new BlockItem(FODDER.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FODDER_NAME))));


    //flax block
    public static final String FLAX_BLOCK_NAME = "flax_block";
    public static final RegistryObject<Block> FLAX_BLOCK = BLOCKS.register(FLAX_BLOCK_NAME, () -> new FlaxBaleBlock(
            BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_LIGHT_GREEN)
                    .strength(0.5F)
                    .sound(SoundType.GRASS)));
    public static final RegistryObject<Item> FLAX_BLOCK_ITEM = ITEMS.register(FLAX_BLOCK_NAME, () -> new BlockItem(FLAX_BLOCK.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FLAX_NAME))));

    //boat in a jar
    public static final String JAR_BOAT_NAME = "jar_boat";
    public static final RegistryObject<Block> JAR_BOAT = BLOCKS.register(JAR_BOAT_NAME, () -> new JarBoatBlock(
            BlockBehaviour.Properties.copy(ModRegistry.JAR.get())));
    public static final RegistryObject<BlockEntityType<JarBoatTile>> JAR_BOAT_TILE = TILES.register(JAR_BOAT_NAME, () -> BlockEntityType.Builder.of(
            JarBoatTile::new, JAR_BOAT.get()).build(null));
    public static final RegistryObject<Item> JAR_BOAT_ITEM = ITEMS.register(JAR_BOAT_NAME, () -> new BlockItem(JAR_BOAT.get(),
            (new Item.Properties()).tab(null)));

    //block generator
    public static final String STRUCTURE_TEMP_NAME = "structure_temp";
    public static final RegistryObject<Block> STRUCTURE_TEMP = BLOCKS.register(STRUCTURE_TEMP_NAME, () -> new StructureTempBlock(
            BlockBehaviour.Properties.of(Material.STONE).strength(0).noDrops().noCollission().noOcclusion()));
    public static final RegistryObject<BlockEntityType<StructureTempBlockTile>> STRUCTURE_TEMP_TILE = TILES.register(STRUCTURE_TEMP_NAME, () -> BlockEntityType.Builder.of(
            StructureTempBlockTile::new, STRUCTURE_TEMP.get()).build(null));

    public static final String BLOCK_GENERATOR_NAME = "block_generator";
    public static final RegistryObject<Block> BLOCK_GENERATOR = BLOCKS.register(BLOCK_GENERATOR_NAME, () -> new BlockGeneratorBlock(
            BlockBehaviour.Properties.copy(STRUCTURE_TEMP.get())));
    public static final RegistryObject<BlockEntityType<BlockGeneratorBlockTile>> BLOCK_GENERATOR_TILE = TILES.register(BLOCK_GENERATOR_NAME, () -> BlockEntityType.Builder.of(
            BlockGeneratorBlockTile::new, BLOCK_GENERATOR.get()).build(null));

    //sticks
    public static final String STICK_NAME = "stick";
    public static final RegistryObject<Block> STICK_BLOCK = BLOCKS.register(STICK_NAME, () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD))
    );

    //blaze rod
    public static final String BLAZE_ROD_NAME = "blaze_rod";
    //TODO: blaze sound
    public static final RegistryObject<Block> BLAZE_ROD_BLOCK = BLOCKS.register(BLAZE_ROD_NAME, () -> new BlazeRodBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(0.25F, 0F)
                    .lightLevel(state -> 12)
                    .emissiveRendering((p, w, s) -> true)
                    .sound(SoundType.GILDED_BLACKSTONE))
    );

    //daub
    public static final String DAUB_NAME = "daub";
    public static final RegistryObject<Block> DAUB = BLOCKS.register(DAUB_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                    .strength(1.5f, 3f)
    ));
    public static final RegistryObject<Item> DAUB_ITEM = ITEMS.register(DAUB_NAME, () -> new BlockItem(DAUB.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))
    ));
    //wattle and daub
    //frame
    public static final String DAUB_FRAME_NAME = "daub_frame";
    public static final RegistryObject<Block> DAUB_FRAME = BLOCKS.register(DAUB_FRAME_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_FRAME_ITEM = ITEMS.register(DAUB_FRAME_NAME, () -> new BlockItem(DAUB_FRAME.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //brace
    public static final String DAUB_BRACE_NAME = "daub_brace";
    public static final RegistryObject<Block> DAUB_BRACE = BLOCKS.register(DAUB_BRACE_NAME, () -> new FlippedBlock(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_BRACE_ITEM = ITEMS.register(DAUB_BRACE_NAME, () -> new BlockItem(DAUB_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //cross brace
    public static final String DAUB_CROSS_BRACE_NAME = "daub_cross_brace";
    public static final RegistryObject<Block> DAUB_CROSS_BRACE = BLOCKS.register(DAUB_CROSS_BRACE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_CROSS_BRACE_ITEM = ITEMS.register(DAUB_CROSS_BRACE_NAME, () -> new BlockItem(DAUB_CROSS_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //timber frame
    public static final String TIMBER_FRAME_NAME = "timber_frame";
    public static final RegistryObject<Block> TIMBER_FRAME = BLOCKS.register(TIMBER_FRAME_NAME, () -> new FrameBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .strength(0f, 0f)
                    .dynamicShape()
                    .sound(SoundType.SCAFFOLDING), DAUB_FRAME));
    public static final RegistryObject<Item> TIMBER_FRAME_ITEM = ITEMS.register(TIMBER_FRAME_NAME, () -> new TimberFrameItem(TIMBER_FRAME.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber brace
    public static final String TIMBER_BRACE_NAME = "timber_brace";
    public static final RegistryObject<Block> TIMBER_BRACE = BLOCKS.register(TIMBER_BRACE_NAME, () -> new FrameBraceBlock(
            BlockBehaviour.Properties.copy(TIMBER_FRAME.get()), DAUB_BRACE));
    public static final RegistryObject<Item> TIMBER_BRACE_ITEM = ITEMS.register(TIMBER_BRACE_NAME, () -> new TimberFrameItem(TIMBER_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber cross brace
    public static final String TIMBER_CROSS_BRACE_NAME = "timber_cross_brace";
    public static final RegistryObject<Block> TIMBER_CROSS_BRACE = BLOCKS.register(TIMBER_CROSS_BRACE_NAME, () -> new FrameBlock(
            BlockBehaviour.Properties.copy(TIMBER_FRAME.get()), DAUB_CROSS_BRACE));
    public static final RegistryObject<Item> TIMBER_CROSS_BRACE_ITEM = ITEMS.register(TIMBER_CROSS_BRACE_NAME, () -> new TimberFrameItem(TIMBER_CROSS_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));
    public static final RegistryObject<BlockEntityType<FrameBlockTile>> TIMBER_FRAME_TILE = TILES.register(TIMBER_FRAME_NAME, () -> BlockEntityType.Builder.of(
            FrameBlockTile::new, TIMBER_FRAME.get(), TIMBER_CROSS_BRACE.get(), TIMBER_BRACE.get()).build(null));

    //stone lamp
    public static final String STONE_LAMP_NAME = "stone_lamp";
    public static final RegistryObject<Block> STONE_LAMP = BLOCKS.register(STONE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Item> STONE_LAMP_ITEM = ITEMS.register(STONE_LAMP_NAME, () -> new BlockItem(STONE_LAMP.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_LAMP_NAME))
    ));
    //stone tile
    public static final String STONE_TILE_NAME = "stone_tile";
    public static final RegistryObject<Block> STONE_TILE = BLOCKS.register(STONE_TILE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS))
    );
    public static final RegistryObject<Item> STONE_TILE_ITEM = ITEMS.register(STONE_TILE_NAME, () -> new BlockItem(STONE_TILE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME))
    ));
    //slab
    public static final String STONE_TILE_SLAB_NAME = "stone_tile_slab";
    public static final RegistryObject<Block> STONE_TILE_SLAB = BLOCKS.register(STONE_TILE_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(STONE_TILE.get()))
    );
    public static final RegistryObject<Item> STONE_TILE_SLAB_ITEM = ITEMS.register(STONE_TILE_SLAB_NAME, () -> new BlockItem(STONE_TILE_SLAB.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME))
    ));
    //vertical slab
    public static final String STONE_TILE_VERTICAL_SLAB_NAME = "stone_tile_vertical_slab";
    public static final RegistryObject<Block> STONE_TILE_VERTICAL_SLAB = BLOCKS.register(STONE_TILE_VERTICAL_SLAB_NAME, () -> new VerticalSlabBlock(
            BlockBehaviour.Properties.copy(STONE_TILE.get()))
    );
    public static final RegistryObject<Item> STONE_TILE_VERTICAL_SLAB_ITEM = regBlockItem(STONE_TILE_VERTICAL_SLAB,
            getTab("quark", CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME));

    //blackstone lamp
    public static final String BLACKSTONE_LAMP_NAME = "blackstone_lamp";
    public static final RegistryObject<Block> BLACKSTONE_LAMP = BLOCKS.register(BLACKSTONE_LAMP_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Item> BLACKSTONE_LAMP_ITEM = regBlockItem(BLACKSTONE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, BLACKSTONE_LAMP_NAME));

    //blackstone tile
    public static final String BLACKSTONE_TILE_NAME = "blackstone_tile";
    public static final RegistryObject<Block> BLACKSTONE_TILE = BLOCKS.register(BLACKSTONE_TILE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.BLACKSTONE))
    );
    public static final RegistryObject<Item> BLACKSTONE_TILE_ITEM = ITEMS.register(BLACKSTONE_TILE_NAME, () -> new BlockItem(BLACKSTONE_TILE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME))
    ));
    //slab
    public static final String BLACKSTONE_TILE_SLAB_NAME = "blackstone_tile_slab";
    public static final RegistryObject<Block> BLACKSTONE_TILE_SLAB = BLOCKS.register(BLACKSTONE_TILE_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(BLACKSTONE_TILE.get()))
    );
    public static final RegistryObject<Item> BLACKSTONE_TILE_SLAB_ITEM = regBlockItem(BLACKSTONE_TILE_SLAB,
            getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME));
    //vertical slab
    public static final String BLACKSTONE_TILE_VERTICAL_SLAB_NAME = "blackstone_tile_vertical_slab";
    public static final RegistryObject<Block> BLACKSTONE_TILE_VERTICAL_SLAB = BLOCKS.register(BLACKSTONE_TILE_VERTICAL_SLAB_NAME, () -> new VerticalSlabBlock(
            BlockBehaviour.Properties.copy(BLACKSTONE_TILE.get()))
    );
    public static final RegistryObject<Item> BLACKSTONE_TILE_VERTICAL_SLAB_ITEM = regBlockItem(BLACKSTONE_TILE_VERTICAL_SLAB,
            getTab("quark", CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_TILE_NAME));

    //deepslate lamp
    public static final String DEEPSLATE_LAMP_NAME = "deepslate_lamp";
    public static final RegistryObject<Block> DEEPSLATE_LAMP = BLOCKS.register(DEEPSLATE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).lightLevel(s -> 15)));
    public static final RegistryObject<Item> DEEPSLATE_LAMP_ITEM = regBlockItem(DEEPSLATE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DEEPSLATE_LAMP_NAME));


    //end_stone lamp
    public static final String END_STONE_LAMP_NAME = "end_stone_lamp";
    public static final RegistryObject<Block> END_STONE_LAMP = BLOCKS.register(END_STONE_LAMP_NAME, () -> new EndLampBlock(
            BlockBehaviour.Properties.copy(Blocks.END_STONE).lightLevel(s -> 15)));
    public static final RegistryObject<Item> END_STONE_LAMP_ITEM = regBlockItem(END_STONE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, END_STONE_LAMP_NAME));


    //flower box
    public static final String FLOWER_BOX_NAME = "flower_box";
    public static final RegistryObject<Block> FLOWER_BOX = BLOCKS.register(FLOWER_BOX_NAME, () -> new FlowerBoxBlock(
            BlockBehaviour.Properties.of(Material.WOOD)
                    .strength(0.5F)
                    .sound(SoundType.WOOD))
    );

    public static final RegistryObject<Item> FLOWER_BOX_ITEM = regBlockItem(FLOWER_BOX, getTab(CreativeModeTab.TAB_DECORATIONS, FLOWER_BOX_NAME), 300);

    public static final RegistryObject<BlockEntityType<FlowerBoxBlockTile>> FLOWER_BOX_TILE = TILES.register(FLOWER_BOX_NAME, () -> BlockEntityType.Builder.of(
            FlowerBoxBlockTile::new, FLOWER_BOX.get()).build(null));

    //statue
    public static final String STATUE_NAME = "statue";
    public static final RegistryObject<Block> STATUE = BLOCKS.register(STATUE_NAME, () -> new StatueBlock(
            BlockBehaviour.Properties.of(Material.STONE)
                    .strength(2)));
    public static final RegistryObject<Item> STATUE_ITEM = regBlockItem(STATUE, getTab(CreativeModeTab.TAB_DECORATIONS, STATUE_NAME));

    public static final RegistryObject<BlockEntityType<StatueBlockTile>> STATUE_TILE = TILES.register(STATUE_NAME, () -> BlockEntityType.Builder.of(
            StatueBlockTile::new, STATUE.get()).build(null));

    //feather block
    public static final String FEATHER_BLOCK_NAME = "feather_block";
    public static final RegistryObject<Block> FEATHER_BLOCK = BLOCKS.register(FEATHER_BLOCK_NAME, () -> new FeatherBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)
                    .noCollission()));
    public static final RegistryObject<Item> FEATHER_BLOCK_ITEM = regBlockItem(FEATHER_BLOCK, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FEATHER_BLOCK_NAME));

    //flint block
    public static final String FLINT_BLOCK_NAME = "flint_block";
    public static final RegistryObject<Block> FLINT_BLOCK = BLOCKS.register(FLINT_BLOCK_NAME, () -> new FlintBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE)));
    public static final RegistryObject<Item> FLINT_BLOCK_ITEM = regBlockItem(FLINT_BLOCK, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FLINT_BLOCK_NAME));


    //gunpowder block
    public static final String GUNPOWDER_BLOCK_NAME = "gunpowder";
    public static final RegistryObject<Block> GUNPOWDER_BLOCK = BLOCKS.register(GUNPOWDER_BLOCK_NAME, () -> new GunpowderBlock(
            BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));

    //placeable book
    public static final String BOOK_PILE_NAME = "book_pile";
    public static final RegistryObject<Block> BOOK_PILE = BLOCKS.register(BOOK_PILE_NAME, () -> new BookPileBlock(
            BlockBehaviour.Properties.of(Material.DECORATION).strength(0.5F).sound(SoundType.WOOD)));

    //placeable book
    public static final String BOOK_PILE_H_NAME = "book_pile_horizontal";
    public static final RegistryObject<Block> BOOK_PILE_H = BLOCKS.register(BOOK_PILE_H_NAME, () -> new BookPileHorizontalBlock(
            BlockBehaviour.Properties.copy(BOOK_PILE.get())));

    public static final RegistryObject<BlockEntityType<BookPileBlockTile>> BOOK_PILE_TILE = TILES.register(BOOK_PILE_NAME, () -> BlockEntityType.Builder.of(
            BookPileBlockTile::new, BOOK_PILE.get(), BOOK_PILE_H.get()).build(null));


    /*
    public static final String REDSTONE_DRIVER_NAME = "redstone_driver";
    public static final RegistryObject<Block> REDSTONE_DRIVER = BLOCKS.register(REDSTONE_DRIVER_NAME,()-> new RedstoneDriverBlock(
            AbstractBlock.Properties.copy(Blocks.REPEATER)));
    public static final RegistryObject<Item> REDSTONE_DRIVER_ITEM = ITEMS.register(REDSTONE_DRIVER_NAME,()-> new BlockItem(REDSTONE_DRIVER.get(),
            (new Item.Properties()).tab(getTab(ItemGroup.TAB_REDSTONE,REDSTONE_DRIVER_NAME))));

    */


}
