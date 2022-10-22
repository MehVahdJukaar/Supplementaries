package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.block.VerticalSlabBlock;
import net.mehvahdjukaar.moonlight.api.entity.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.commands.ModCommands;
import net.mehvahdjukaar.supplementaries.common.effects.OverencumberedEffect;
import net.mehvahdjukaar.supplementaries.common.effects.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.common.entities.*;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.mehvahdjukaar.supplementaries.common.entities.trades.VillagerTradesHandler;
import net.mehvahdjukaar.supplementaries.common.inventories.*;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.integration.CCCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.RegUtils.*;
import static net.mehvahdjukaar.supplementaries.reg.RegistryConstants.*;

@SuppressWarnings({"unused", "ConstantConditions"})
public class ModRegistry {

    //creative tab
    public static CreativeModeTab MOD_TAB = null;
    public static CreativeModeTab JAR_TAB = null;

    public static void init() {
        MOD_TAB = ModCreativeTabs.MOD_TAB;
        JAR_TAB = ModCreativeTabs.JAR_TAB;

        CompatHandler.initOptionalRegistries();
        RegUtils.initDynamicRegistry();
        RegHelper.addAttributeRegistration(ModRegistry::registerEntityAttributes);
        RegHelper.addCommandRegistration(ModCommands::register);
        VillagerTradesHandler.addTradesRegistration();
    }

    private static boolean isDisabled(String name) {
        return !RegistryConfigs.isEnabled(name);
    }

    @EventCalled
    public static void registerEntityAttributes(RegHelper.AttributeEvent event) {
        event.register(ModRegistry.RED_MERCHANT.get(), Mob.createMobAttributes());
    }

    //misc entries
    //loot
    public static final Supplier<LootItemFunctionType> CURSE_LOOT_FUNCTION = RegHelper.register(Supplementaries.res("curse_loot"),
            () -> new LootItemFunctionType(new CurseLootFunction.Serializer()), Registry.LOOT_FUNCTION_TYPE);
    public static final Supplier<LootItemFunctionType> RANDOM_ARROW_FUNCTION = RegHelper.register(Supplementaries.res("random_arrows"),
            () -> new LootItemFunctionType(new RandomArrowFunction.Serializer()), Registry.LOOT_FUNCTION_TYPE);

    //paintings
    public static final Supplier<PaintingVariant> BOMB_PAINTING = RegHelper.registerPainting(
            Supplementaries.res("bombs"), () -> new PaintingVariant(32, 32));

    //enchantment
    public static final Supplier<Enchantment> STASIS_ENCHANTMENT = RegHelper.registerAsync(
            Supplementaries.res(STASIS_NAME), StasisEnchantment::new, Registry.ENCHANTMENT);

    //effects
    public static final Supplier<MobEffect> OVERENCUMBERED = RegHelper.registerEffect(
            Supplementaries.res("overencumbered"), OverencumberedEffect::new);


    //entities
    public static final Supplier<EntityType<PearlMarker>> PEARL_MARKER = regEntity("pearl_marker",
            PearlMarker::new, MobCategory.MISC, 0.999F, 0.999F, 4, false, -1);

    //dispenser minecart
    public static final Supplier<EntityType<DispenserMinecartEntity>> DISPENSER_MINECART = regEntity(DISPENSER_MINECART_NAME, () ->
            EntityType.Builder.<DispenserMinecartEntity>of(DispenserMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F).clientTrackingRange(8));

    public static final Supplier<Item> DISPENSER_MINECART_ITEM = regItem(DISPENSER_MINECART_NAME, () -> new DispenserMinecartItem(new Item.Properties()
            .stacksTo(1).tab(getTab(CreativeModeTab.TAB_TRANSPORTATION, DISPENSER_MINECART_NAME))));

    //red trader
    public static final Supplier<EntityType<RedMerchantEntity>> RED_MERCHANT = regEntity(RED_MERCHANT_NAME,
            RedMerchantEntity::new, MobCategory.CREATURE, 0.6F, 1.95F, 10, true, 3);

    public static final Supplier<MenuType<RedMerchantContainerMenu>> RED_MERCHANT_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(RED_MERCHANT_NAME), RedMerchantContainerMenu::new);

    public static final Supplier<Item> RED_MERCHANT_SPAWN_EGG_ITEM = regItem(RED_MERCHANT_NAME + "_spawn_egg", () ->
            PlatformHelper.newSpawnEgg(RED_MERCHANT, 0x7A090F, 0xF4f1e0,
                    new Item.Properties().tab(getTab(null, RED_MERCHANT_NAME))));

    //urn
    public static final Supplier<EntityType<FallingUrnEntity>> FALLING_URN = regEntity(FALLING_URN_NAME, () ->
            EntityType.Builder.<FallingUrnEntity>of(FallingUrnEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //ash
    public static final Supplier<EntityType<FallingAshEntity>> FALLING_ASH = regEntity(FALLING_ASH_NAME, () ->
            EntityType.Builder.<FallingAshEntity>of(FallingAshEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //ash
    public static final Supplier<EntityType<FallingLanternEntity>> FALLING_LANTERN = regEntity(FALLING_LANTERN_NAME, () ->
            EntityType.Builder.<FallingLanternEntity>of(FallingLanternEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    public static final Supplier<EntityType<ImprovedFallingBlockEntity>> FALLING_SACK = regEntity(FALLING_SACK_NAME, () ->
            EntityType.Builder.<ImprovedFallingBlockEntity>of(ImprovedFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //firefly

//    public static final String FIREFLY_NAME = "firefly";
//    private static final EntityType<FireflyEntity> FIREFLY_TYPE_RAW = (EntityType.Builder.of(FireflyEntity::new, MobCategory.AMBIENT)
//            .setShouldReceiveVelocityUpdates(true).setTrackingRange(12).setUpdateInterval(3)
//            .sized(0.3125f, 1f))
//            .build(FIREFLY_NAME);
//
//    public static final Supplier<EntityType<FireflyEntity>> FIREFLY_TYPE = ENTITIES.register(FIREFLY_NAME, () -> FIREFLY_TYPE_RAW);
//
//    public static final Supplier<Item> FIREFLY_SPAWN_EGG_ITEM = regItem(FIREFLY_NAME + "_spawn_egg", () ->
//            new ForgeSpawnEggItem(FIREFLY_TYPE, -5048018, -14409439, //-4784384, -16777216,
//                    new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, FIREFLY_NAME))));

    //brick
    public static final Supplier<EntityType<ThrowableBrickEntity>> THROWABLE_BRICK = regEntity(THROWABLE_BRICK_NAME, () ->
            EntityType.Builder.<ThrowableBrickEntity>of(ThrowableBrickEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    //bomb
    public static final Supplier<EntityType<BombEntity>> BOMB = regEntity(BOMB_NAME, () ->
            EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(10));

    public static final Supplier<Item> BOMB_ITEM = regItem(BOMB_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME))));
    public static final Supplier<Item> BOMB_ITEM_ON = regItem("bomb_projectile", () -> new BombItem(new Item.Properties()
            .tab(null)));

    public static final Supplier<Item> BOMB_BLUE_ITEM = regItem(BOMB_BLUE_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME)), BombEntity.BombType.BLUE, true));
    public static final Supplier<Item> BOMB_BLUE_ITEM_ON = regItem("bomb_blue_projectile", () -> new BombItem(new Item.Properties()
            .tab(null), BombEntity.BombType.BLUE, false));

    //sharpnel bomb
    public static final Supplier<Item> BOMB_SPIKY_ITEM = regItem(BOMB_SPIKY_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_SPIKY_NAME)), BombEntity.BombType.SPIKY, false));
    public static final Supplier<Item> BOMB_SPIKY_ITEM_ON = regItem("bomb_spiky_projectile", () -> new BombItem(new Item.Properties()
            .tab(null), BombEntity.BombType.SPIKY, false));

    //rope arrow
    public static final Supplier<EntityType<RopeArrowEntity>> ROPE_ARROW = regEntity(ROPE_ARROW_NAME, () ->
            EntityType.Builder.<RopeArrowEntity>of(RopeArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20));

    public static final Supplier<Item> ROPE_ARROW_ITEM = regItem(ROPE_ARROW_NAME, () -> new RopeArrowItem(
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, ROPE_ARROW_NAME)).defaultDurability(32)));

    //slingshot projectile
    public static final Supplier<EntityType<SlingshotProjectileEntity>> SLINGSHOT_PROJECTILE = regEntity(SLINGSHOT_PROJECTILE_NAME, () ->
            EntityType.Builder.<SlingshotProjectileEntity>of(SlingshotProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20));


    //soap bubbler
    public static final Supplier<Item> BUBBLE_BLOWER = regItem(BUBBLE_BLOWER_NAME, () -> new BubbleBlower((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, BUBBLE_BLOWER_NAME))
            .stacksTo(1).durability(250)));

    //slingshot
    public static final Supplier<Item> SLINGSHOT_ITEM = regItem(SLINGSHOT_NAME, () -> new SlingshotItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, SLINGSHOT_NAME))
            .stacksTo(1).durability(192)));

    //flute
    public static final Supplier<Item> FLUTE_ITEM = regItem(FLUTE_NAME, () -> new FluteItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, FLUTE_NAME)).stacksTo(1).durability(64)));


    //key
    public static final Supplier<Item> KEY_ITEM = regItem(KEY_NAME, () -> new KeyItem(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_TOOLS, KEY_NAME))));

    //candy
    public static final Supplier<Item> CANDY_ITEM = regItem(CANDY_NAME, () -> new CandyItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_FOOD, CANDY_NAME))));

    //antique ink
    public static final Supplier<Item> ANTIQUE_INK = regItem(ANTIQUE_INK_NAME, () -> new Item((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_MISC, ANTIQUE_INK_NAME))));

    //wrench
    public static final Supplier<Item> WRENCH = regItem(WRENCH_NAME, () -> new WrenchItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, WRENCH_NAME)).stacksTo(1).durability(200)));

    //quiver
    public static final Supplier<QuiverItem> QUIVER_ITEM = regItem(QUIVER_NAME, () -> new QuiverItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, QUIVER_NAME)).stacksTo(1).rarity(Rarity.RARE)));


    //speedometer
    /*
    public static final String SPEEDOMETER_NAME = "speedometer";
    public static final Supplier<Item> SPEEDOMETER_ITEM = regItem(SPEEDOMETER_NAME,()-> new SpeedometerItem(
            (new Item.Properties()).tab(null)));
    */


    //blocks

    //variants:

    //dynamic. Handled by wood set handler
    public static final Map<WoodType, HangingSignBlock> HANGING_SIGNS = new LinkedHashMap<>();

    //keeping "hanging_sign_oak" for compatibility even if it should be just hanging_sign

    public static final Supplier<BlockEntityType<HangingSignBlockTile>> HANGING_SIGN_TILE = regTile(
            HANGING_SIGN_NAME + "_oak", () -> PlatformHelper.newBlockEntityType(
                    HangingSignBlockTile::new, HANGING_SIGNS.values().toArray(Block[]::new)));

    //sign posts
    public static final Supplier<Block> SIGN_POST = regBlock(SIGN_POST_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                .strength(2f, 3f)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return /*CompatHandler.create ? SchematicCannonStuff.makeSignPost(p) :*/ new SignPostBlock(p);
    });

    public static final Supplier<BlockEntityType<SignPostBlockTile>> SIGN_POST_TILE = regTile(
            SIGN_POST_NAME, () -> PlatformHelper.newBlockEntityType(
                    SignPostBlockTile::new, SIGN_POST.get()));

    public static final Map<WoodType, SignPostItem> SIGN_POST_ITEMS = new IdentityHashMap<>();

    //flags
    public static final Map<DyeColor, Supplier<Block>> FLAGS = RegUtils.registerFlags(FLAG_NAME);

    public static final Supplier<BlockEntityType<FlagBlockTile>> FLAG_TILE = regTile(
            FLAG_NAME, () -> PlatformHelper.newBlockEntityType(
                    FlagBlockTile::new, FLAGS.values().stream().map(Supplier::get).toArray(Block[]::new)));
    //ceiling banner
    public static final Map<DyeColor, Supplier<Block>> CEILING_BANNERS = RegUtils.registerCeilingBanners(CEILING_BANNER_NAME);

    public static final Supplier<BlockEntityType<CeilingBannerBlockTile>> CEILING_BANNER_TILE = regTile(
            CEILING_BANNER_NAME, () -> PlatformHelper.newBlockEntityType(
                    CeilingBannerBlockTile::new, CEILING_BANNERS.values().stream().map(Supplier::get).toArray(Block[]::new)));

    //presents

    public static final Map<DyeColor, Supplier<Block>> PRESENTS = RegUtils.registerPresents(PRESENT_NAME, PresentBlock::new);

    public static final Supplier<BlockEntityType<PresentBlockTile>> PRESENT_TILE = regTile(
            PRESENT_NAME, () -> PlatformHelper.newBlockEntityType(
                    PresentBlockTile::new, PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    public static final Supplier<MenuType<PresentContainerMenu>> PRESENT_BLOCK_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(PRESENT_NAME), PresentContainerMenu::new);

    //trapped presents

    public static final Map<DyeColor, Supplier<Block>> TRAPPED_PRESENTS = RegUtils.registerPresents(TRAPPED_PRESENT_NAME, TrappedPresentBlock::new);

    public static final Supplier<BlockEntityType<TrappedPresentBlockTile>> TRAPPED_PRESENT_TILE = regTile(
            TRAPPED_PRESENT_NAME, () -> PlatformHelper.newBlockEntityType(
                    TrappedPresentBlockTile::new, TRAPPED_PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    public static final Supplier<MenuType<TrappedPresentContainerMenu>> TRAPPED_PRESENT_BLOCK_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(TRAPPED_PRESENT_NAME), TrappedPresentContainerMenu::new);


    //decoration blocks

    //planter
    public static final Supplier<Block> PLANTER = regWithItem(PLANTER_NAME, () -> new PlanterBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED)
                    .strength(2f, 6f)
                    .requiresCorrectToolForDrops()
    ), CreativeModeTab.TAB_DECORATIONS);

    //pedestal
    public static final Supplier<Block> PEDESTAL = regWithItem(PEDESTAL_NAME, () -> new PedestalBlock(
                    BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<PedestalBlockTile>> PEDESTAL_TILE = regTile(
            PEDESTAL_NAME, () -> PlatformHelper.newBlockEntityType(
                    PedestalBlockTile::new, PEDESTAL.get()));


    //notice board
    public static final Supplier<Block> NOTICE_BOARD = regWithItem(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
                    BlockBehaviour.Properties.copy(Blocks.BARREL)),
            CreativeModeTab.TAB_DECORATIONS, 300);

    public static final Supplier<BlockEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = regTile(
            NOTICE_BOARD_NAME, () -> PlatformHelper.newBlockEntityType(
                    NoticeBoardBlockTile::new, NOTICE_BOARD.get()));

    public static final Supplier<MenuType<NoticeBoardContainerMenu>> NOTICE_BOARD_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(NOTICE_BOARD_NAME), NoticeBoardContainerMenu::new);

    //safe
    public static final Supplier<Block> SAFE = regBlock(SAFE_NAME, () -> new SafeBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
    ));
    public static final Supplier<BlockEntityType<SafeBlockTile>> SAFE_TILE = regTile(
            SAFE_NAME, () -> PlatformHelper.newBlockEntityType(
                    SafeBlockTile::new, SAFE.get()));


    public static final Supplier<Item> SAFE_ITEM = regItem(SAFE_NAME, () -> new SafeItem(SAFE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SAFE_NAME)).stacksTo(1).fireResistant()));

    //cage
    public static final Supplier<Block> CAGE = regBlock(CAGE_NAME, () -> new CageBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .sound(SoundType.METAL)
    ));

    public static final Supplier<BlockEntityType<CageBlockTile>> CAGE_TILE = regTile(
            CAGE_NAME, () -> PlatformHelper.newBlockEntityType(
                    CageBlockTile::new, CAGE.get()));

    public static final Supplier<Item> CAGE_ITEM = regItem(CAGE_NAME, () -> new CageItem(CAGE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, CAGE_NAME))
                    .stacksTo(16)));

    //jar
    public static final Supplier<Block> JAR = regBlock(JAR_NAME, () -> new JarBlock(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                    .strength(0.5f, 1f)
                    .sound(ModSounds.JAR)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<JarBlockTile>> JAR_TILE = regTile(
            JAR_NAME, () -> PlatformHelper.newBlockEntityType(
                    JarBlockTile::new, JAR.get()));

    public static final Supplier<Item> JAR_ITEM = regItem(JAR_NAME, () -> new JarItem(JAR.get(), new Item.Properties().tab(
            getTab(CreativeModeTab.TAB_DECORATIONS, JAR_NAME)).stacksTo(16)));


    //sack
    public static final Supplier<Block> SACK = regBlock(SACK_NAME, () -> new SackBlock(
            BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                    .strength(1F)
                    .sound(ModSounds.SACK)
    ));
    public static final Supplier<BlockEntityType<SackBlockTile>> SACK_TILE = regTile(
            SACK_NAME, () -> PlatformHelper.newBlockEntityType(
                    SackBlockTile::new, SACK.get()));

    public static final Supplier<MenuType<SackContainerMenu>> SACK_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(SACK_NAME), SackContainerMenu::new);

    public static final Supplier<Item> SACK_ITEM = regItem(SACK_NAME, () -> new SackItem(SACK.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, SACK_NAME)).stacksTo(1)));

    //blackboard
    public static final Supplier<Block> BLACKBOARD = regBlock(BLACKBOARD_NAME, () -> new BlackboardBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2, 3)
    ));
    public static final Supplier<BlockEntityType<BlackboardBlockTile>> BLACKBOARD_TILE = regTile(
            BLACKBOARD_NAME, () -> PlatformHelper.newBlockEntityType(
                    BlackboardBlockTile::new, BLACKBOARD.get()));

    public static final Supplier<Item> BLACKBOARD_ITEM = regItem(BLACKBOARD_NAME, () -> new BlackboardItem(BLACKBOARD.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BLACKBOARD_NAME))));

    //globe
    public static final Supplier<Block> GLOBE = regBlock(GLOBE_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final Supplier<Item> GLOBE_ITEM = regItem(GLOBE_NAME, () -> new BlockItem(GLOBE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, GLOBE_NAME)).rarity(Rarity.RARE)));

    public static final Supplier<Block> GLOBE_SEPIA = regBlock(GLOBE_SEPIA_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.copy(GLOBE.get())));
    public static final Supplier<Item> GLOBE_SEPIA_ITEM = regItem(GLOBE_SEPIA_NAME, () -> new BlockItem(GLOBE_SEPIA.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, GLOBE_SEPIA_NAME)).rarity(Rarity.RARE)));


    public static final Supplier<BlockEntityType<GlobeBlockTile>> GLOBE_TILE = regTile(
            GLOBE_NAME, () -> PlatformHelper.newBlockEntityType(
                    GlobeBlockTile::new, GLOBE.get(), GLOBE_SEPIA.get()));

    //sconce
    //normal
    public static final Supplier<Block> SCONCE = regBlock(SCONCE_NAME, () -> new SconceBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .noOcclusion()
                    .instabreak()
                    .sound(SoundType.LANTERN),
            14, () -> ParticleTypes.FLAME));
    public static final Supplier<Block> SCONCE_WALL = regBlock("sconce_wall", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE.get()), () -> ParticleTypes.FLAME));
    public static final Supplier<Item> SCONCE_ITEM = regItem(SCONCE_NAME, () -> new StandingAndWallBlockItem(SCONCE.get(), SCONCE_WALL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //soul
    public static final Supplier<Block> SCONCE_SOUL = regBlock(SCONCE_NAME_SOUL, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 10,
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Block> SCONCE_WALL_SOUL = regBlock("sconce_wall_soul", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_SOUL.get())
                    .dropsLike(SCONCE_SOUL.get()),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_SOUL = regItem(SCONCE_NAME_SOUL, () -> new StandingAndWallBlockItem(SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //optional: endergetic
    public static final Supplier<Block> SCONCE_ENDER = regBlock(SCONCE_NAME_ENDER, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 13,
            CompatObjects.ENDER_FLAME));
    public static final Supplier<Block> SCONCE_WALL_ENDER = regBlock("sconce_wall_ender", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_ENDER.get()),
            CompatObjects.ENDER_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_ENDER = regItem(SCONCE_NAME_ENDER, () -> new StandingAndWallBlockItem(SCONCE_ENDER.get(), SCONCE_WALL_ENDER.get(),
            (new Item.Properties()).tab(getTab("endergetic", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //glow
    public static final Supplier<Block> SCONCE_GLOW = regBlock(SCONCE_NAME_GLOW, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 13,
            CompatObjects.GLOW_FLAME));
    public static final Supplier<Block> SCONCE_WALL_GLOW = regBlock("sconce_wall_glow", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE_GLOW.get()),
            CompatObjects.GLOW_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_GLOW = regItem(SCONCE_NAME_GLOW, () -> new StandingAndWallBlockItem(SCONCE_GLOW.get(), SCONCE_WALL_GLOW.get(),
            (new Item.Properties()).tab(getTab("infernalexp", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //green
    public static final Supplier<Block> SCONCE_GREEN = regBlock(SCONCE_NAME_GREEN, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get()), 14, ModParticles.GREEN_FLAME));
    public static final Supplier<Block> SCONCE_WALL_GREEN = regBlock("sconce_wall_green", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_GREEN.get()), ModParticles.GREEN_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_GREEN = regItem(SCONCE_NAME_GREEN, () -> new StandingAndWallBlockItem(SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_GREEN))));

    //nether brass
    public static final Supplier<Block> SCONCE_NETHER_BRASS = regBlock(SCONCE_NAME_NETHER_BRASS, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 14,
            CompatObjects.NETHER_BRASS_FLAME));
    public static final Supplier<Block> SCONCE_WALL_NETHER_BRASS = regBlock("sconce_wall_nether_brass", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE_NETHER_BRASS.get()),
            CompatObjects.NETHER_BRASS_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_NETHER_BRASS = regItem(SCONCE_NAME_NETHER_BRASS, () -> new StandingAndWallBlockItem(SCONCE_NETHER_BRASS.get(), SCONCE_WALL_NETHER_BRASS.get(),
            (new Item.Properties()).tab(getTab("architects_palette", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));


    //candle holder
    public static final Map<DyeColor, Supplier<Block>> CANDLE_HOLDERS = RegUtils.registerCandleHolders(CANDLE_HOLDER_NAME);
    //soul candle holder
    public static final Supplier<Block> SOUL_CANDLE_HOLDER = regWithItem(CANDLE_HOLDER_NAME + "_soul", () -> new CandleHolderBlock(null,
                    BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()),
                    CompatHandler.buzzier_bees ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME),
            getTab(CreativeModeTab.TAB_DECORATIONS, CANDLE_HOLDER_NAME), "buzzier_bees");

    //copper lantern
    public static final Supplier<Block> COPPER_LANTERN = regWithItem(COPPER_LANTERN_NAME, () -> new CopperLanternBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> state.getValue(LightableLanternBlock.LIT) ? 15 : 0)
                    //TODO: add custom sound mixed
                    .sound(SoundType.COPPER)
    ), CreativeModeTab.TAB_DECORATIONS);

    //brass lantern
    public static final Supplier<Block> BRASS_LANTERN = regBlock(BRASS_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Shapes.or(Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D),
                    Block.box(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D),
                    Block.box(4.0D, 7.0D, 4.0D, 12.0D, 8.0D, 12.0D))));

    public static final Supplier<BlockItem> BRASS_LANTERN_ITEM = regBlockItem(BRASS_LANTERN_NAME, BRASS_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, BRASS_LANTERN_NAME), "forge:ingots/brass");

    //crimson lantern
    public static final Supplier<Block> CRIMSON_LANTERN = regWithItem(CRIMSON_LANTERN_NAME, () -> new CrimsonLanternBlock(
                    BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_RED)
                            .strength(1.5f)
                            .sound(SoundType.WOOL)
                            .lightLevel((state) -> 15)
                            .noOcclusion()),
            CreativeModeTab.TAB_DECORATIONS);

    //silver lantern
    public static final Supplier<Block> SILVER_LANTERN = regBlock(SILVER_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D)));

    public static final Supplier<BlockItem> SILVER_LANTERN_ITEM = regBlockItem(SILVER_LANTERN_NAME, SILVER_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, SILVER_LANTERN_NAME), "forge:ingots/silver");

    //lead lantern
    public static final Supplier<Block> LEAD_LANTERN = regBlock(LEAD_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Shapes.or(Block.box(4.0D, 4.0D, 4.0D, 12.0D, 7.0D, 12.0D),
                    Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D))));

    public static final Supplier<BlockItem> LEAD_LANTERN_ITEM = regBlockItem(LEAD_LANTERN_NAME, LEAD_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, LEAD_LANTERN_NAME), "forge:ingots/lead");


    //rope
    public static final Supplier<Block> ROPE = regBlock(ROPE_NAME, () -> new RopeBlock(
            BlockBehaviour.Properties.of(Material.WOOL)
                    .sound(ModSounds.ROPE)
                    .strength(0.25f)
                    .speedFactor(0.7f)
                    .noOcclusion()));

    public static final Supplier<Block> ROPE_KNOT = regBlock(ROPE_KNOT_NAME, () -> new RopeKnotBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));

    public static final Supplier<Item> ROPE_ITEM = regItem(ROPE_NAME, () -> new RopeItem(ROPE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, ROPE_NAME))));

    public static final Supplier<BlockEntityType<RopeKnotBlockTile>> ROPE_KNOT_TILE = regTile(
            ROPE_KNOT_NAME, () -> PlatformHelper.newBlockEntityType(
                    RopeKnotBlockTile::new, ROPE_KNOT.get()));

    //spikes
    public static final Supplier<Block> BAMBOO_SPIKES = regBlock(BAMBOO_SPIKES_NAME, () -> new BambooSpikesBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                    .sound(SoundType.SCAFFOLDING)
                    .isRedstoneConductor((a, b, c) -> false)
                    .strength(2)
                    .noOcclusion()));

    public static final Supplier<BlockEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = regTile(
            BAMBOO_SPIKES_NAME, () -> PlatformHelper.newBlockEntityType(
                    BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()));

    public static final Supplier<Item> BAMBOO_SPIKES_ITEM = regItem(BAMBOO_SPIKES_NAME, () -> new BambooSpikesItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BAMBOO_SPIKES_NAME))));

    public static final Supplier<Item> BAMBOO_SPIKES_TIPPED_ITEM = regItem(TIPPED_SPIKES_NAME, () -> new BambooSpikesTippedItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).defaultDurability(BambooSpikesBlockTile.MAX_CHARGES).tab(getTab(CreativeModeTab.TAB_BREWING, TIPPED_SPIKES_NAME))));

    //goblet
    public static final Supplier<Block> GOBLET = regWithItem(GOBLET_NAME, () -> new GobletBlock(
                    BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                            .strength(1.5f, 2f)
                            .sound(SoundType.METAL)),
            CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<GobletBlockTile>> GOBLET_TILE = regTile(
            GOBLET_NAME, () -> PlatformHelper.newBlockEntityType(
                    GobletBlockTile::new, GOBLET.get()));

    //hourglass
    public static final Supplier<Block> HOURGLASS = regWithItem(HOURGLASS_NAME, () -> new HourGlassBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ), CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<HourGlassBlockTile>> HOURGLASS_TILE = regTile(
            HOURGLASS_NAME, () -> PlatformHelper.newBlockEntityType(
                    HourGlassBlockTile::new, HOURGLASS.get()));

    //item shelf
    public static final Supplier<Block> ITEM_SHELF = regWithItem(ITEM_SHELF_NAME, () -> new ItemShelfBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(0.75f, 0.1f)
                    .noOcclusion()
                    .noCollission()
    ), CreativeModeTab.TAB_DECORATIONS, 100);

    public static final Supplier<BlockEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = regTile(
            ITEM_SHELF_NAME, () -> PlatformHelper.newBlockEntityType(
                    ItemShelfBlockTile::new, ITEM_SHELF.get()));

    //doormat
    public static final Supplier<Block> DOORMAT = regWithItem(DOORMAT_NAME, () -> new DoormatBlock(
            BlockBehaviour.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_YELLOW)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
    ), CreativeModeTab.TAB_DECORATIONS, 134);

    public static final Supplier<BlockEntityType<DoormatBlockTile>> DOORMAT_TILE = regTile(
            DOORMAT_NAME, () -> PlatformHelper.newBlockEntityType(
                    DoormatBlockTile::new, DOORMAT.get()));

    //magma cream block
    //public static final Supplier<Block> MAGMA_CREAM_BLOCK = regBlock(MAGMA_CREAM_BLOCK_NAME, () -> new MagmaCreamBlock(
    //        BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)));
    //public static final Supplier<Item> MAGMA_CREAM_BLOCK_ITEM = regItem(MAGMA_CREAM_BLOCK_NAME, () -> new BlockItem(MAGMA_CREAM_BLOCK.get(),
    //        (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, MAGMA_CREAM_BLOCK_NAME))));

    //raked gravel
    public static final Supplier<Block> RAKED_GRAVEL = regWithItem(RAKED_GRAVEL_NAME, () -> new RakedGravelBlock(
            BlockBehaviour.Properties.copy(Blocks.GRAVEL)
                    .isViewBlocking((w, s, p) -> true)
                    .isSuffocating((w, s, p) -> true)
    ), CreativeModeTab.TAB_DECORATIONS);

    //redstone blocks

    //cog block
    public static final Supplier<Block> COG_BLOCK = regWithItem(COG_BLOCK_NAME, () -> new CogBlock(
            BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                    .strength(3f, 6f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
    ), CreativeModeTab.TAB_REDSTONE);

    //diode block
    public static final Supplier<Block> RELAYER = regWithItem(RELAYER_NAME, () -> new RelayerBlock(
            BlockBehaviour.Properties.copy(Blocks.OBSERVER).isRedstoneConductor((s, l, p) -> false)
    ), CreativeModeTab.TAB_REDSTONE);

    //piston launcher base
    public static final Supplier<Block> SPRING_LAUNCHER = regWithItem(SPRING_LAUNCHER_NAME, () -> new SpringLauncherBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isSuffocating((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isViewBlocking((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
    ), CreativeModeTab.TAB_REDSTONE);

    public static final Supplier<Block> SPRING_LAUNCHER_HEAD = regBlock(PISTON_LAUNCHER_HEAD_NAME, () -> new SpringLauncherHeadBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
                    .jumpFactor(1.18f)
    ));
    public static final Supplier<Block> SPRING_LAUNCHER_ARM = regBlock(PISTON_LAUNCHER_ARM_NAME, () -> new SpringLauncherArmBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(50f, 50f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noLootTable()
    ));
    public static final Supplier<BlockEntityType<SpringLauncherArmBlockTile>> SPRING_LAUNCHER_ARM_TILE = regTile(
            PISTON_LAUNCHER_ARM_NAME, () -> PlatformHelper.newBlockEntityType(
                    SpringLauncherArmBlockTile::new, SPRING_LAUNCHER_ARM.get()));

    //speaker Block
    public static final Supplier<SpeakerBlock> SPEAKER_BLOCK = regWithItem(SPEAKER_BLOCK_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                .strength(1f, 2f)
                .sound(SoundType.WOOD);
        return CompatHandler.computercraft ? CCCompat.makeSpeaker(p) : new SpeakerBlock(p);
    }, CreativeModeTab.TAB_REDSTONE, 300);

    public static final Supplier<BlockEntityType<SpeakerBlockTile>> SPEAKER_BLOCK_TILE = regTile(
            SPEAKER_BLOCK_NAME, () -> PlatformHelper.newBlockEntityType(
                    SpeakerBlockTile::new, SPEAKER_BLOCK.get()));

    //turn table
    public static final Supplier<Block> TURN_TABLE = regWithItem(TURN_TABLE_NAME, () -> new TurnTableBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
                    .strength(0.75f, 2f)
                    .sound(SoundType.STONE)
    ), CreativeModeTab.TAB_REDSTONE);

    public static final Supplier<BlockEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = regTile(
            TURN_TABLE_NAME, () -> PlatformHelper.newBlockEntityType(
                    TurnTableBlockTile::new, TURN_TABLE.get()));

    //illuminator
    public static final Supplier<Block> REDSTONE_ILLUMINATOR = regWithItem(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.QUARTZ)
                    .strength(0.3f, 0.3f)
    ), CreativeModeTab.TAB_REDSTONE);


    //pulley
    public static final Supplier<Block> PULLEY_BLOCK = regWithItem(PULLEY_BLOCK_NAME, () -> new PulleyBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)
    ), CreativeModeTab.TAB_DECORATIONS, 300);

    public static final Supplier<MenuType<PulleyBlockContainerMenu>> PULLEY_BLOCK_CONTAINER = RegHelper.registerMenuType(
            Supplementaries.res(PULLEY_BLOCK_NAME), PulleyBlockContainerMenu::new);

    public static final Supplier<BlockEntityType<PulleyBlockTile>> PULLEY_BLOCK_TILE = regTile(
            PULLEY_BLOCK_NAME, () -> PlatformHelper.newBlockEntityType(
                    PulleyBlockTile::new, PULLEY_BLOCK.get()));

    //lock block
    public static final Supplier<Block> LOCK_BLOCK = regWithItem(LOCK_BLOCK_NAME, () -> new LockBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
    ), CreativeModeTab.TAB_REDSTONE);

    //bellows
    public static final Supplier<Block> BELLOWS = regWithItem(BELLOWS_NAME, () -> new BellowsBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ), CreativeModeTab.TAB_REDSTONE, 300);

    public static final Supplier<BlockEntityType<BellowsBlockTile>> BELLOWS_TILE = regTile(
            BELLOWS_NAME, () -> PlatformHelper.newBlockEntityType(
                    BellowsBlockTile::new, BELLOWS.get()));

    //clock
    public static final Supplier<Block> CLOCK_BLOCK = regWithItem(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 6f)
                    .sound(SoundType.WOOD)
                    .lightLevel((state) -> 1)
    ), CreativeModeTab.TAB_REDSTONE);

    public static final Supplier<BlockEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = regTile(
            CLOCK_BLOCK_NAME, () -> PlatformHelper.newBlockEntityType(
                    ClockBlockTile::new, CLOCK_BLOCK.get()));

    //crystal display
    public static final Supplier<Block> CRYSTAL_DISPLAY = regWithItem(CRYSTAL_DISPLAY_NAME, () -> new CrystalDisplayBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.DEEPSLATE)
                    .sound(SoundType.POLISHED_DEEPSLATE)
                    .strength(0.5f, 0.5f)
    ), CreativeModeTab.TAB_REDSTONE);

    //sconce lever
    public static final Supplier<Block> SCONCE_LEVER = regWithItem(SCONCE_LEVER_NAME, () -> new SconceLeverBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()),
            () -> ParticleTypes.FLAME
    ), CreativeModeTab.TAB_REDSTONE);

    //crank
    public static final Supplier<Block> CRANK = regWithItem(CRANK_NAME, () -> new CrankBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.NONE)
                    .strength(0.6f, 0.6f)
                    .noCollission()
                    .noOcclusion()
    ), CreativeModeTab.TAB_REDSTONE);

    //wind vane
    public static final Supplier<Block> WIND_VANE = regWithItem(WIND_VANE_NAME, () -> new WindVaneBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ), CreativeModeTab.TAB_REDSTONE);

    public static final Supplier<BlockEntityType<WindVaneBlockTile>> WIND_VANE_TILE = regTile(
            WIND_VANE_NAME, () -> PlatformHelper.newBlockEntityType(
                    WindVaneBlockTile::new, WIND_VANE.get()));

    //faucet
    public static final Supplier<Block> FAUCET = regWithItem(FAUCET_NAME, () -> new FaucetBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 4.8f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ), CreativeModeTab.TAB_REDSTONE);

    public static final Supplier<BlockEntityType<FaucetBlockTile>> FAUCET_TILE = regTile(
            FAUCET_NAME, () -> PlatformHelper.newBlockEntityType(
                    FaucetBlockTile::new, FAUCET.get()));

    //gold door
    public static final Supplier<Block> GOLD_DOOR = regWithItem(GOLD_DOOR_NAME, () -> new GoldDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()
    ), CreativeModeTab.TAB_REDSTONE);

    //gold trapdoor
    public static final Supplier<Block> GOLD_TRAPDOOR = regWithItem(GOLD_TRAPDOOR_NAME, () -> new GoldTrapdoorBlock(
            BlockBehaviour.Properties.copy(GOLD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)
    ), CreativeModeTab.TAB_REDSTONE);

    //silver door
    public static final Supplier<Block> SILVER_DOOR = regBlock(SILVER_DOOR_NAME, () -> new SilverDoorBlock(
            BlockBehaviour.Properties.of(Material.METAL)
                    .strength(4.0F, 5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    public static final Supplier<BlockItem> SILVER_DOOR_ITEM = regBlockItem(SILVER_DOOR_NAME, SILVER_DOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, SILVER_DOOR_NAME), "forge:ingots/silver");

    //silver trapdoor
    public static final Supplier<Block> SILVER_TRAPDOOR = regBlock(SILVER_TRAPDOOR_NAME, () -> new SilverTrapdoorBlock(
            BlockBehaviour.Properties.copy(SILVER_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final Supplier<BlockItem> SILVER_TRAPDOOR_ITEM = regBlockItem(SILVER_TRAPDOOR_NAME, SILVER_TRAPDOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, SILVER_TRAPDOOR_NAME), "forge:ingots/silver");

    //lead door
    public static final Supplier<Block> LEAD_DOOR = regBlock(LEAD_DOOR_NAME, () -> new LeadDoorBlock(
            BlockBehaviour.Properties.of(Material.METAL)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    public static final Supplier<BlockItem> LEAD_DOOR_ITEM = regBlockItem(LEAD_DOOR_NAME, LEAD_DOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, LEAD_DOOR_NAME), "forge:ingots/lead");

    //lead trapdoor
    public static final Supplier<Block> LEAD_TRAPDOOR = regBlock(LEAD_TRAPDOOR_NAME, () -> new LeadTrapdoorBlock(
            BlockBehaviour.Properties.copy(LEAD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final Supplier<BlockItem> LEAD_TRAPDOOR_ITEM = regBlockItem(LEAD_TRAPDOOR_NAME, LEAD_TRAPDOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, LEAD_TRAPDOOR_NAME), "forge:ingots/lead");


    //netherite doors
    public static final Supplier<Block> NETHERITE_DOOR = regBlock(NETHERITE_DOOR_NAME, () -> new NetheriteDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).noOcclusion()
    ));
    public static final Supplier<Item> NETHERITE_DOOR_ITEM = regItem(NETHERITE_DOOR_NAME, () -> new BlockItem(NETHERITE_DOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_DOOR_NAME)).fireResistant()));

    //netherite trapdoor
    public static final Supplier<Block> NETHERITE_TRAPDOOR = regBlock(NETHERITE_TRAPDOOR_NAME, () -> new NetheriteTrapdoorBlock(
            BlockBehaviour.Properties.copy(NETHERITE_DOOR.get())
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)
    ));
    public static final Supplier<Item> NETHERITE_TRAPDOOR_ITEM = regItem(NETHERITE_TRAPDOOR_NAME, () -> new BlockItem(NETHERITE_TRAPDOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_TRAPDOOR_NAME)).fireResistant()));

    public static final Supplier<BlockEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = regTile(
            "key_lockable_tile", () -> PlatformHelper.newBlockEntityType(
                    KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()));

    //iron gate
    public static final Supplier<Block> IRON_GATE = regWithItem(IRON_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), false
    ), CreativeModeTab.TAB_REDSTONE);

    //gold gate
    public static final Supplier<Block> GOLD_GATE = regWithItem(GOLD_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), true
    ), CreativeModeTab.TAB_REDSTONE, "quark");

    //wall lantern
    public static final Supplier<WallLanternBlock> WALL_LANTERN = regBlock(WALL_LANTERN_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.LANTERN)
                .lightLevel((state) -> 15).noLootTable();

        return /*CompatHandler.create ? SchematicCannonStuff.makeWallLantern(p):*/  new WallLanternBlock(p);
    });

    public static final Supplier<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = regTile(
            WALL_LANTERN_NAME, () -> PlatformHelper.newBlockEntityType(
                    WallLanternBlockTile::new, WALL_LANTERN.get()));


    //hanging flower pot
    public static final Supplier<Block> HANGING_FLOWER_POT = regPlaceableItem(HANGING_FLOWER_POT_NAME,
            () -> new HangingFlowerPotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)),
            () -> Items.FLOWER_POT, CommonConfigs.Tweaks.HANGING_POT_PLACEMENT);

    public static final Supplier<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = regTile(
            HANGING_FLOWER_POT_NAME, () -> PlatformHelper.newBlockEntityType(
                    HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()));

    //double cake
    public static final Supplier<Block> DOUBLE_CAKE = regBlock(DOUBLE_CAKE_NAME, () -> new DoubleCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
    ));
    //directional cake
    public static final Supplier<Block> DIRECTIONAL_CAKE = regBlock(DIRECTIONAL_CAKE_NAME, () -> new DirectionalCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
                    .dropsLike(Blocks.CAKE)
    ));

    //checker block
    public static final Supplier<Block> CHECKER_BLOCK = regWithItem(CHECKER_BLOCK_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //slab
    public static final Supplier<Block> CHECKER_SLAB = regWithItem(CHECKER_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get())
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //vertical slab
    public static final Supplier<Block> CHECKER_VERTICAL_SLAB = regWithItem(CHECKER_VERTICAL_SLAB_NAME, () -> new VerticalSlabBlock(
                    BlockBehaviour.Properties.copy(CHECKER_BLOCK.get())
            ), CreativeModeTab.TAB_BUILDING_BLOCKS, "quark"
    );

    //pancakes
    public static final Supplier<Block> PANCAKE = regBlock(PANCAKE_NAME, () -> new PancakeBlock(
            BlockBehaviour.Properties.of(Material.CAKE, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(0.5F)
                    .sound(SoundType.WOOL))
    );
    public static final Supplier<Item> PANCAKE_ITEM = regItem(PANCAKE_NAME, () -> new PancakeItem(PANCAKE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_FOOD, PANCAKE_NAME))
    ));
    public static final Supplier<Item> PANCAKE_DISC = regItem("pancake_disc",
            () -> PlatformHelper.newMusicDisc(15, ModSounds.PANCAKE_MUSIC, new Item.Properties().tab(null), 345));

    //flax
    public static final Supplier<Block> FLAX = regBlock(FLAX_NAME, () -> new FlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.ROSE_BUSH)
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP))
    );

    public static final Supplier<Item> FLAX_ITEM = regItem(FLAX_NAME, () -> new Item(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final Supplier<Item> FLAX_SEEDS_ITEM = regItem("flax_seeds", () -> new ItemNameBlockItem(FLAX.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final Supplier<Block> FLAX_WILD = regWithItem(FLAX_WILD_NAME, () -> new WildFlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.TALL_GRASS).offsetType(BlockBehaviour.OffsetType.NONE)
    ), CreativeModeTab.TAB_DECORATIONS);

    //pot
    public static final Supplier<Block> FLAX_POT = regBlock("potted_flax", () -> PlatformHelper.newFlowerPot(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT, FLAX, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));

    //fodder
    public static final Supplier<Block> FODDER = regWithItem(FODDER_NAME, () -> new FodderBlock(
            BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //flax block
    public static final Supplier<Block> FLAX_BLOCK = regWithItem(FLAX_BLOCK_NAME, () -> new FlaxBaleBlock(
            BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_LIGHT_GREEN)
                    .strength(0.5F)
                    .sound(SoundType.GRASS)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //boat in a jar
    public static final Supplier<Block> JAR_BOAT = regWithItem(JAR_BOAT_NAME, () -> new JarBoatBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS)
    ), (CreativeModeTab) null);

    public static final Supplier<BlockEntityType<JarBoatTile>> JAR_BOAT_TILE = regTile(
            JAR_BOAT_NAME, () -> PlatformHelper.newBlockEntityType(
                    JarBoatTile::new, JAR_BOAT.get()));

    //block generator
    public static final Supplier<Block> STRUCTURE_TEMP = regBlock(STRUCTURE_TEMP_NAME, () -> new StructureTempBlock(
            BlockBehaviour.Properties.of(Material.STONE).strength(0).noLootTable().noCollission().noOcclusion()));

    public static final Supplier<BlockEntityType<StructureTempBlockTile>> STRUCTURE_TEMP_TILE = regTile(
            STRUCTURE_TEMP_NAME, () -> PlatformHelper.newBlockEntityType(
                    StructureTempBlockTile::new, STRUCTURE_TEMP.get()));

    public static final Supplier<BlockPlacerItem> BLOCK_PLACER = regItem("placeable_item", () -> new BlockPlacerItem(STRUCTURE_TEMP.get(),
            (new Item.Properties()).tab(null)));

    public static final Supplier<Block> BLOCK_GENERATOR = regBlock(BLOCK_GENERATOR_NAME, () -> new BlockGeneratorBlock(
            BlockBehaviour.Properties.copy(STRUCTURE_TEMP.get()).lightLevel((s) -> 14)));

    public static final Supplier<BlockEntityType<BlockGeneratorBlockTile>> BLOCK_GENERATOR_TILE = regTile(
            BLOCK_GENERATOR_NAME, () -> PlatformHelper.newBlockEntityType(
                    BlockGeneratorBlockTile::new, BLOCK_GENERATOR.get()));

    //sticks
    public static final Supplier<Block> STICK_BLOCK = regPlaceableItem(STICK_NAME, () -> new StickBlock(
            BlockBehaviour.Properties.of(new Material.Builder(MaterialColor.WOOD).nonSolid().build())
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD), 60), () -> Items.STICK, CommonConfigs.Tweaks.PLACEABLE_STICKS);
    public static final Supplier<Block> EDELWOOD_STICK_BLOCK = regPlaceableItem("edelwood_stick", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD)), "forbidden_arcanus:edelwood_stick", CommonConfigs.Tweaks.PLACEABLE_STICKS);
    public static final Supplier<Block> PRISMARINE_ROD_BLOCK = regPlaceableItem("prismarine_rod", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN)
                    .strength(0.25F, 0F)
                    .sound(SoundType.STONE), 0), "upgrade_aquatic:prismarine_rod", CommonConfigs.Tweaks.PLACEABLE_STICKS);
    public static final Supplier<Block> PROPELPLANT_ROD_BLOCK = regPlaceableItem("propelplant_cane", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.CRIMSON_STEM)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD)), "nethers_delight:propelplant_cane", CommonConfigs.Tweaks.PLACEABLE_STICKS);

    //blaze rod
    //TODO: blaze sound
    public static final Supplier<Block> BLAZE_ROD_BLOCK = regPlaceableItem(BLAZE_ROD_NAME, () -> new BlazeRodBlock(
                    BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                            .strength(0.25F, 0F)
                            .lightLevel(state -> 12)
                            .emissiveRendering((p, w, s) -> true)
                            .sound(SoundType.GILDED_BLACKSTONE)),
            () -> Items.BLAZE_ROD, CommonConfigs.Tweaks.PLACEABLE_RODS
    );

    //daub
    public static final Supplier<Block> DAUB = regWithItem(DAUB_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                    .sound(SoundType.PACKED_MUD)
                    .strength(1.5f, 3f)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //wattle and daub
    //frame
    public static final Supplier<Block> DAUB_FRAME = regWithItem(DAUB_FRAME_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);
    //brace
    public static final Supplier<Block> DAUB_BRACE = regWithItem(DAUB_BRACE_NAME, () -> new FlippedBlock(
            BlockBehaviour.Properties.copy(DAUB.get())
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //cross brace
    public static final Supplier<Block> DAUB_CROSS_BRACE = regWithItem(DAUB_CROSS_BRACE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //timber frame
    public static final Supplier<Block> TIMBER_FRAME = regBlock(TIMBER_FRAME_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                .strength(0.1f, 0f)
                .noCollission().instabreak()
                .dynamicShape().sound(SoundType.SCAFFOLDING);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_FRAME) :*/ new FrameBlock(p, DAUB_FRAME);
    });
    public static final Supplier<Item> TIMBER_FRAME_ITEM = regItem(TIMBER_FRAME_NAME, () -> new TimberFrameItem(TIMBER_FRAME.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber brace
    public static final Supplier<Block> TIMBER_BRACE = regBlock(TIMBER_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFrameBraceBlock(p, DAUB_BRACE) :*/ new FrameBraceBlock(p, DAUB_BRACE);
    });
    public static final Supplier<Item> TIMBER_BRACE_ITEM = regItem(TIMBER_BRACE_NAME, () -> new TimberFrameItem(TIMBER_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber cross brace
    public static final Supplier<Block> TIMBER_CROSS_BRACE = regBlock(TIMBER_CROSS_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_CROSS_BRACE) :*/ new FrameBlock(p, DAUB_CROSS_BRACE);
    });
    public static final Supplier<Item> TIMBER_CROSS_BRACE_ITEM = regItem(TIMBER_CROSS_BRACE_NAME, () -> new TimberFrameItem(TIMBER_CROSS_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    public static final Supplier<BlockEntityType<FrameBlockTile>> TIMBER_FRAME_TILE = regTile(
            TIMBER_FRAME_NAME, () -> PlatformHelper.newBlockEntityType(
                    FrameBlockTile::new, TIMBER_FRAME.get(), TIMBER_CROSS_BRACE.get(), TIMBER_BRACE.get()));

    //lapis bricks
    public static final EnumMap<RegHelper.VariantType, Supplier<Block>> LAPIS_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(Supplementaries.res(LAPIS_BRICKS_NAME), BlockBehaviour.Properties.copy(Blocks.LAPIS_BLOCK)
                            .sound(SoundType.DEEPSLATE_TILES).strength(2.0F, 2.0F),
                    isDisabled(LAPIS_BRICKS_NAME));

    //ashen bricks
    public static final EnumMap<RegHelper.VariantType, Supplier<Block>> ASH_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(Supplementaries.res(ASH_BRICKS_NAME), Blocks.STONE_BRICKS, isDisabled(ASH_BRICKS_NAME));

    //stone tile
    public static final EnumMap<RegHelper.VariantType, Supplier<Block>> STONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(Supplementaries.res(STONE_TILE_NAME), Blocks.STONE_BRICKS, isDisabled(STONE_TILE_NAME));

    //blackstone tile
    public static final EnumMap<RegHelper.VariantType, Supplier<Block>> BLACKSTONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(Supplementaries.res(BLACKSTONE_TILE_NAME), Blocks.BLACKSTONE, isDisabled(BLACKSTONE_TILE_NAME));

    //stone lamp
    public static final Supplier<Block> STONE_LAMP = regWithItem(STONE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //blackstone lamp
    public static final Supplier<Block> BLACKSTONE_LAMP = regWithItem(BLACKSTONE_LAMP_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //deepslate lamp
    public static final Supplier<Block> DEEPSLATE_LAMP = regWithItem(DEEPSLATE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).lightLevel(s -> 15)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //end_stone lamp
    public static final Supplier<Block> END_STONE_LAMP = regWithItem(END_STONE_LAMP_NAME, () -> new EndLampBlock(
            BlockBehaviour.Properties.copy(Blocks.END_STONE).lightLevel(s -> 15)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //flower box
    public static final Supplier<Block> FLOWER_BOX = regWithItem(FLOWER_BOX_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.5F);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFlowerBox(p) : */new FlowerBoxBlock(p);
    }, CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<FlowerBoxBlockTile>> FLOWER_BOX_TILE = regTile(FLOWER_BOX_NAME, () ->
            PlatformHelper.newBlockEntityType(FlowerBoxBlockTile::new, FLOWER_BOX.get()));

    //statue
    public static final Supplier<Block> STATUE = regWithItem(STATUE_NAME, () -> new StatueBlock(
            BlockBehaviour.Properties.of(Material.STONE)
                    .strength(2)
    ), CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<StatueBlockTile>> STATUE_TILE = regTile(
            STATUE_NAME, () -> PlatformHelper.newBlockEntityType(
                    StatueBlockTile::new, STATUE.get()));

    //feather block
    public static final Supplier<Block> FEATHER_BLOCK = regWithItem(FEATHER_BLOCK_NAME, () -> new FeatherBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(0.5f)
                    .noCollission()
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //flint block
    public static final Supplier<Block> FLINT_BLOCK = regWithItem(FLINT_BLOCK_NAME, () -> new FlintBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2F, 7.5F)
    ), CreativeModeTab.TAB_BUILDING_BLOCKS);

    //sugar block
    public static final Supplier<Block> SUGAR_CUBE = regBlock(SUGAR_CUBE_NAME, () -> new SugarBlock(
            BlockBehaviour.Properties.of(Material.DECORATION).color(MaterialColor.SNOW).strength(0.5f).sound(SoundType.SAND)
    ));
    public static final Supplier<Item> SUGAR_CUBE_ITEM = regItem(SUGAR_CUBE_NAME, () -> new SugarCubeItem(
            SUGAR_CUBE.get(), new Item.Properties().tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, SUGAR_CUBE_NAME))
    ));

    //gunpowder block
    public static final Supplier<Block> GUNPOWDER_BLOCK = regPlaceableItem(GUNPOWDER_BLOCK_NAME, () -> new GunpowderBlock(
                    BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).sound(SoundType.SAND)),
            () -> Items.GUNPOWDER, CommonConfigs.Tweaks.PLACEABLE_GUNPOWDER);

    //placeable book
    public static final Supplier<Block> BOOK_PILE = regPlaceableItem(BOOK_PILE_NAME, () -> new BookPileBlock(
                    BlockBehaviour.Properties.of(Material.DECORATION).strength(0.5F).sound(SoundType.WOOD)),
            () -> Items.ENCHANTED_BOOK, CommonConfigs.Tweaks.PLACEABLE_BOOKS);

    //placeable book
    public static final Supplier<Block> BOOK_PILE_H = regPlaceableItem(BOOK_PILE_H_NAME, () -> new BookPileHorizontalBlock(
                    BlockBehaviour.Properties.copy(BOOK_PILE.get())),
            () -> Items.BOOK, CommonConfigs.Tweaks.PLACEABLE_BOOKS);

    public static final Supplier<BlockEntityType<BookPileBlockTile>> BOOK_PILE_TILE = regTile(
            BOOK_PILE_NAME, () -> PlatformHelper.newBlockEntityType(
                    BookPileBlockTile::new, BOOK_PILE.get(), BOOK_PILE_H.get()));

    //urn
    public static final Supplier<Block> URN = regWithItem(URN_NAME, () -> new UrnBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.WOOD)
                    .sound(SoundType.GLASS)
                    .strength(0.1f, 0)
    ), CreativeModeTab.TAB_DECORATIONS);

    public static final Supplier<BlockEntityType<UrnBlockTile>> URN_TILE = regTile(
            URN_NAME, () -> PlatformHelper.newBlockEntityType(
                    UrnBlockTile::new, URN.get()));

    //ash
    public static final Supplier<Block> ASH_BLOCK = regWithItem(ASH_NAME, () -> new AshLayerBlock(
            BlockBehaviour.Properties.of(Material.TOP_SNOW, MaterialColor.COLOR_GRAY)
                    .sound(SoundType.SAND).randomTicks().strength(0.1F).requiresCorrectToolForDrops()
    ), CreativeModeTab.TAB_MISC);


    //ash
    public static final Supplier<Item> ASH_BRICK_ITEM = regItem(ASH_BRICK_NAME, () -> new Item(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, ASH_BRICKS_NAME))));

    //soap
    public static final Supplier<Item> SOAP = regItem(SOAP_NAME, () -> new SoapItem(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, SOAP_NAME))));

    public static final Supplier<Block> SOAP_BLOCK = regWithItem(SOAP_BLOCK_NAME, () -> new SoapBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_PINK)
                    .friction(0.94f)
                    .strength(1.25F, 4.0F)
                    .sound(SoundType.CORAL_BLOCK)
    ), CreativeModeTab.TAB_DECORATIONS);

    //stackable skulls
    public static final Supplier<Block> SKULL_PILE = regBlock(SKULL_PILE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);

        return /*CompatHandler.create ? SchematicCannonStuff.makeDoubleSkull(p) :*/ new DoubleSkullBlock(p);
    });

    public static final Supplier<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = regTile(
            SKULL_PILE_NAME, () -> PlatformHelper.newBlockEntityType(
                    DoubleSkullBlockTile::new, SKULL_PILE.get()));

    //skulls candles
    public static final Supplier<Block> SKULL_CANDLE = regBlock(SKULL_CANDLE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);
        return /*CompatHandler.create ? SchematicCannonStuff.makeCandleSkull(p) :*/ new CandleSkullBlock(p);
    });

    public static final Supplier<Block> SKULL_CANDLE_SOUL = regBlock(SKULL_CANDLE_SOUL_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(SKULL_CANDLE.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeCandleSkull(p) :*/ new SoulCandleSkullBlock(p);
    });

    public static final Supplier<BlockEntityType<CandleSkullBlockTile>> SKULL_CANDLE_TILE = regTile(
            SKULL_CANDLE_NAME, () -> PlatformHelper.newBlockEntityType(
                    CandleSkullBlockTile::new, SKULL_CANDLE.get(), SKULL_CANDLE_SOUL.get()));

    //bubble
    public static final Supplier<BubbleBlock> BUBBLE_BLOCK = regBlock(BUBBLE_BLOCK_NAME, () ->
            new BubbleBlock(BlockBehaviour.Properties.of(Material.DECORATION, MaterialColor.COLOR_PINK)
                    .sound(ModSounds.BUBBLE_BLOCK)
                    .noOcclusion()
                    .isSuffocating((a, b, c) -> false)
                    .isViewBlocking((a, b, c) -> false)
                    .isRedstoneConductor((a, b, c) -> false)
                    .instabreak())
    );
    public static final Supplier<Item> BUBBLE_BLOCK_ITEM = regItem(BUBBLE_BLOCK_NAME, () -> new BubbleBlockItem(
            BUBBLE_BLOCK.get(), (new Item.Properties()).tab(null)));

    public static final Supplier<BlockEntityType<BubbleBlockTile>> BUBBLE_BLOCK_TILE = regTile(
            BUBBLE_BLOCK_NAME, () -> PlatformHelper.newBlockEntityType(
                    BubbleBlockTile::new, BUBBLE_BLOCK.get()));

    //public static final String CRE
    // ATIVE_WAND = "creative_wand";
    //public static final Supplier<Item> TELEPORT_WAND = regItem(CREATIVE_WAND, () ->
    //        new TeleportWand((new Item.Properties()).tab(null)));
    /* //magnetic driver?
    public static final String REDSTONE_DRIVER_NAME = "redstone_driver";
    public static final Supplier<Block> REDSTONE_DRIVER = regBlock(REDSTONE_DRIVER_NAME,()-> new RedstoneDriverBlock(
            AbstractBlock.Properties.copy(Blocks.REPEATER)));
    public static final Supplier<Item> REDSTONE_DRIVER_ITEM = regItem(REDSTONE_DRIVER_NAME,()-> new BlockItem(REDSTONE_DRIVER.get(),
            (new Item.Properties()).tab(getTab(ItemGroup.TAB_REDSTONE,REDSTONE_DRIVER_NAME))));



    */


}
