package net.mehvahdjukaar.supplementaries.reg;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.common.misc.CakeRegistry;
import net.mehvahdjukaar.supplementaries.common.misc.OverencumberedEffect;
import net.mehvahdjukaar.supplementaries.common.misc.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.Supplementaries.res;
import static net.mehvahdjukaar.supplementaries.reg.ModConstants.*;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.*;

@SuppressWarnings({"unused", "ConstantConditions"})
public class ModRegistry {

    public static void init() {
        CompatHandler.initOptionalRegistries();
        RegUtils.initDynamicRegistry();
    }

    private static boolean isDisabled(String name) {
        return !CommonConfigs.isEnabled(name);
    }

    //misc entries

    //loot
    public static final Supplier<LootItemFunctionType> CURSE_LOOT_FUNCTION = RegHelper.register(res("curse_loot"),
            () -> new LootItemFunctionType(new CurseLootFunction.Serializer()), Registries.LOOT_FUNCTION_TYPE);
    public static final Supplier<LootItemFunctionType> RANDOM_ARROW_FUNCTION = RegHelper.register(res("random_arrows"),
            () -> new LootItemFunctionType(new RandomArrowFunction.Serializer()), Registries.LOOT_FUNCTION_TYPE);

    //paintings
    public static final Supplier<PaintingVariant> BOMB_PAINTING = RegHelper.registerPainting(
            res("bombs"), () -> new PaintingVariant(32, 32));

    //enchantment
    public static final Supplier<Enchantment> STASIS_ENCHANTMENT = RegHelper.registerAsync(
            res(STASIS_NAME), StasisEnchantment::new, Registries.ENCHANTMENT);

    //effects
    public static final Supplier<MobEffect> OVERENCUMBERED = RegHelper.registerEffect(
            res("overencumbered"), OverencumberedEffect::new);


    //red merchant
    public static final Supplier<Item> RED_MERCHANT_SPAWN_EGG_ITEM = regItem(RED_MERCHANT_NAME + "_spawn_egg", () ->
            PlatHelper.newSpawnEgg(ModEntities.RED_MERCHANT, 0x7A090F, 0xF4f1e0, new Item.Properties()));


    //dispenser minecart
    public static final Supplier<Item> DISPENSER_MINECART_ITEM = regItem(DISPENSER_MINECART_NAME, () -> new DispenserMinecartItem(new Item.Properties()
            .stacksTo(1)));


    public static final Supplier<Item> BOMB_ITEM = regItem(BOMB_NAME, () -> new BombItem(new Item.Properties()));
    public static final Supplier<Item> BOMB_ITEM_ON = regItem("bomb_projectile", () -> new BombItem(new Item.Properties()));

    public static final Supplier<Item> BOMB_BLUE_ITEM = regItem(BOMB_BLUE_NAME, () -> new BombItem(new Item.Properties(),
            BombEntity.BombType.BLUE, true));
    public static final Supplier<Item> BOMB_BLUE_ITEM_ON = regItem("bomb_blue_projectile", () -> new BombItem(new Item.Properties(),
            BombEntity.BombType.BLUE, false));

    //sharpnel bomb
    public static final Supplier<Item> BOMB_SPIKY_ITEM = regItem(BOMB_SPIKY_NAME, () -> new BombItem(new Item.Properties(),
            BombEntity.BombType.SPIKY, false));
    public static final Supplier<Item> BOMB_SPIKY_ITEM_ON = regItem("bomb_spiky_projectile", () -> new BombItem(new Item.Properties(),
            BombEntity.BombType.SPIKY, false));

    //rope arrow
    public static final Supplier<Item> ROPE_ARROW_ITEM = regItem(ROPE_ARROW_NAME, () -> new RopeArrowItem(new Item.Properties()
            .defaultDurability(32)));

    //soap bubbler
    public static final Supplier<Item> BUBBLE_BLOWER = regItem(BUBBLE_BLOWER_NAME, () -> new BubbleBlowerItem(new Item.Properties()
            .stacksTo(1).durability(250)));

    //slingshot
    public static final Supplier<Item> SLINGSHOT_ITEM = regItem(SLINGSHOT_NAME, () -> new SlingshotItem(new Item.Properties()
            .stacksTo(1)
            .durability(192)));

    //flute
    public static final Supplier<Item> FLUTE_ITEM = regItem(FLUTE_NAME, () -> new FluteItem(new Item.Properties()
            .stacksTo(1)
            .durability(64)));

    //key
    public static final Supplier<KeyItem> KEY_ITEM = regItem(KEY_NAME, () -> new KeyItem(new Item.Properties()));

    //candy
    public static final Supplier<Item> CANDY_ITEM = regItem(CANDY_NAME, () -> new CandyItem(new Item.Properties()));

    //antique ink
    public static final Supplier<Item> ANTIQUE_INK = regItem(ANTIQUE_INK_NAME, () -> new Item(new Item.Properties()));

    //wrench
    public static final Supplier<Item> WRENCH = regItem(WRENCH_NAME, () -> new WrenchItem(new Item.Properties()
            .stacksTo(1)
            .durability(200)));

    //quiver
    public static final Supplier<QuiverItem> QUIVER_ITEM = regItem(QUIVER_NAME, () -> new QuiverItem((new Item.Properties())
            .stacksTo(1)
            .rarity(Rarity.RARE)));


    //speedometer
    //   public static final Supplier<Item> SPEEDOMETER_ITEM = regItem(SPEEDOMETER_NAME,()-> new SpeedometerItem(new Item.Properties()));


    //altimeter
    public static final Supplier<Item> DEPTH_METER_ITEM = regItem(DEPTH_METER_NAME, () -> new AltimeterItem(new Item.Properties()));

    public static final Supplier<Item> SLICE_MAP = regItem(SLICE_MAP_NAME, () -> new SliceMapItem(new Item.Properties()));


    //blocks

    //sign posts
    public static final Supplier<Block> SIGN_POST = regBlock(SIGN_POST_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                .strength(2f, 3f)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return /*CompatHandler.create ? SchematicCannonStuff.makeSignPost(p) :*/ new SignPostBlock(p);
    });

    public static final Supplier<BlockEntityType<SignPostBlockTile>> SIGN_POST_TILE = regTile(
            SIGN_POST_NAME, () -> PlatHelper.newBlockEntityType(
                    SignPostBlockTile::new, SIGN_POST.get()));

    public static final Map<WoodType, SignPostItem> SIGN_POST_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

    //flags
    public static final Map<DyeColor, Supplier<Block>> FLAGS = RegUtils.registerFlags(FLAG_NAME);

    public static final Supplier<BlockEntityType<FlagBlockTile>> FLAG_TILE = regTile(
            FLAG_NAME, () -> PlatHelper.newBlockEntityType(
                    FlagBlockTile::new, FLAGS.values().stream().map(Supplier::get).toArray(Block[]::new)));
    //ceiling banner
    public static final Map<DyeColor, Supplier<Block>> CEILING_BANNERS = RegUtils.registerCeilingBanners(CEILING_BANNER_NAME);

    public static final Supplier<BlockEntityType<CeilingBannerBlockTile>> CEILING_BANNER_TILE = regTile(
            CEILING_BANNER_NAME, () -> PlatHelper.newBlockEntityType(
                    CeilingBannerBlockTile::new, CEILING_BANNERS.values().stream().map(Supplier::get).toArray(Block[]::new)));

    //presents

    public static final Map<DyeColor, Supplier<Block>> PRESENTS = RegUtils.registerPresents(PRESENT_NAME, PresentBlock::new);

    public static final Supplier<BlockEntityType<PresentBlockTile>> PRESENT_TILE = regTile(
            PRESENT_NAME, () -> PlatHelper.newBlockEntityType(
                    PresentBlockTile::new, PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    //trapped presents

    public static final Map<DyeColor, Supplier<Block>> TRAPPED_PRESENTS = RegUtils.registerPresents(TRAPPED_PRESENT_NAME, TrappedPresentBlock::new);

    public static final Supplier<BlockEntityType<TrappedPresentBlockTile>> TRAPPED_PRESENT_TILE = regTile(
            TRAPPED_PRESENT_NAME, () -> PlatHelper.newBlockEntityType(
                    TrappedPresentBlockTile::new, TRAPPED_PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    //decoration blocks

    //planter
    public static final Supplier<PlanterBlock> PLANTER = regWithItem(PLANTER_NAME, () ->
            CompatHandler.FARMERS_DELIGHT ? FarmersDelightCompat.makePlanterRich() :
                    new PlanterBlock(BlockBehaviour.Properties.copy(Blocks.TERRACOTTA)
                            .mapColor(MapColor.TERRACOTTA_RED)
                            .strength(2f, 6f)
                    ));

    //pedestal
    public static final Supplier<Block> PEDESTAL = regWithItem(PEDESTAL_NAME, () -> new PedestalBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));

    public static final Supplier<BlockEntityType<PedestalBlockTile>> PEDESTAL_TILE = regTile(
            PEDESTAL_NAME, () -> PlatHelper.newBlockEntityType(
                    PedestalBlockTile::new, PEDESTAL.get()));


    //notice board
    public static final Supplier<Block> NOTICE_BOARD = regWithItem(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
                    BlockBehaviour.Properties.copy(Blocks.BARREL)),
            300);

    public static final Supplier<BlockEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = regTile(
            NOTICE_BOARD_NAME, () -> PlatHelper.newBlockEntityType(
                    NoticeBoardBlockTile::new, NOTICE_BOARD.get()));

    //safe
    public static final Supplier<Block> SAFE = regBlock(SAFE_NAME, () -> new SafeBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .pushReaction(PushReaction.BLOCK)
    ));
    public static final Supplier<BlockEntityType<SafeBlockTile>> SAFE_TILE = regTile(
            SAFE_NAME, () -> PlatHelper.newBlockEntityType(
                    SafeBlockTile::new, SAFE.get()));


    public static final Supplier<Item> SAFE_ITEM = regItem(SAFE_NAME, () ->
            new SafeItem(SAFE.get(), new Item.Properties()
                    .stacksTo(1).fireResistant()));

    //cage
    public static final Supplier<Block> CAGE = regBlock(CAGE_NAME, () -> new CageBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(3f, 6f)
                    .sound(SoundType.METAL)
    ));

    public static final Supplier<BlockEntityType<CageBlockTile>> CAGE_TILE = regTile(
            CAGE_NAME, () -> PlatHelper.newBlockEntityType(
                    CageBlockTile::new, CAGE.get()));

    public static final Supplier<Item> CAGE_ITEM = regItem(CAGE_NAME, () ->
            new CageItem(CAGE.get(), new Item.Properties()
                    .stacksTo(16)));

    //jar
    public static final Supplier<Block> JAR = regBlock(JAR_NAME, () -> new JarBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.5f, 1f)
                    .sound(ModSounds.JAR)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<JarBlockTile>> JAR_TILE = regTile(
            JAR_NAME, () -> PlatHelper.newBlockEntityType(
                    JarBlockTile::new, JAR.get()));

    public static final Supplier<Item> JAR_ITEM = regItem(JAR_NAME, () ->
            new JarItem(JAR.get(), new Item.Properties()
                    .stacksTo(16)));


    //sack
    public static final Supplier<Block> SACK = regBlock(SACK_NAME, () -> new SackBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)
                    .mapColor(MapColor.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.8f)
                    .sound(ModSounds.SACK)
    ));
    public static final Supplier<BlockEntityType<SackBlockTile>> SACK_TILE = regTile(
            SACK_NAME, () -> PlatHelper.newBlockEntityType(
                    SackBlockTile::new, SackBlock.SACK_BLOCKS.toArray(Block[]::new)));

    public static final Supplier<Item> SACK_ITEM = regItem(SACK_NAME, () -> new SackItem(SACK.get(),
            new Item.Properties().stacksTo(1)));

    //blackboard
    public static final Supplier<Block> BLACKBOARD = regBlock(BLACKBOARD_NAME, () -> new BlackboardBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2, 3)
    ));
    public static final Supplier<BlockEntityType<BlackboardBlockTile>> BLACKBOARD_TILE = regTile(
            BLACKBOARD_NAME, () -> PlatHelper.newBlockEntityType(
                    BlackboardBlockTile::new, BLACKBOARD.get()));

    public static final Supplier<Item> BLACKBOARD_ITEM = regItem(BLACKBOARD_NAME, () ->
            new BlackboardItem(BLACKBOARD.get(), new Item.Properties()));

    //globe
    public static final Supplier<Block> GLOBE = regBlock(GLOBE_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final Supplier<Item> GLOBE_ITEM = regItem(GLOBE_NAME, () -> new BlockItem(GLOBE.get(),
            new Item.Properties().rarity(Rarity.RARE)));

    public static final Supplier<Block> GLOBE_SEPIA = regBlock(GLOBE_SEPIA_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.copy(GLOBE.get())));
    public static final Supplier<Item> GLOBE_SEPIA_ITEM = regItem(GLOBE_SEPIA_NAME, () -> new BlockItem(GLOBE_SEPIA.get(),
            new Item.Properties().rarity(Rarity.RARE)));


    public static final Supplier<BlockEntityType<GlobeBlockTile>> GLOBE_TILE = regTile(
            GLOBE_NAME, () -> PlatHelper.newBlockEntityType(
                    GlobeBlockTile::new, GLOBE.get(), GLOBE_SEPIA.get()));

    //sconce
    //normal
    public static final Supplier<Block> SCONCE = regBlock(SCONCE_NAME, () -> new SconceBlock(
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .pushReaction(PushReaction.DESTROY)
                    .instabreak()
                    .sound(SoundType.LANTERN),
            14, () -> ParticleTypes.FLAME));
    public static final Supplier<Block> SCONCE_WALL = regBlock("sconce_wall", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE.get()), () -> ParticleTypes.FLAME));
    public static final Supplier<Item> SCONCE_ITEM = regItem(SCONCE_NAME, () ->
            new StandingAndWallBlockItem(SCONCE.get(), SCONCE_WALL.get(), new Item.Properties(), Direction.DOWN));

    //soul
    public static final Supplier<Block> SCONCE_SOUL = regBlock(SCONCE_NAME_SOUL, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 10,
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Block> SCONCE_WALL_SOUL = regBlock("sconce_wall_soul", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_SOUL.get())
                    .dropsLike(SCONCE_SOUL.get()),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_SOUL = regItem(SCONCE_NAME_SOUL, () -> new StandingAndWallBlockItem(
            SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(), new Item.Properties(), Direction.DOWN));

    public static final List<Supplier<Item>> SCONCES = new ArrayList<>(List.of(SCONCE_ITEM, SCONCE_ITEM_SOUL));

    //green
    public static final Supplier<Block> SCONCE_GREEN = regBlock(SCONCE_NAME_GREEN, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 14, ModParticles.GREEN_FLAME));
    public static final Supplier<Block> SCONCE_WALL_GREEN = regBlock("sconce_wall_green", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE_GREEN.get()), ModParticles.GREEN_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_GREEN = regItem(SCONCE_NAME_GREEN, () -> new StandingAndWallBlockItem(
            SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(), new Item.Properties(), Direction.DOWN));


    //candle holder
    public static final Map<DyeColor, Supplier<Block>> CANDLE_HOLDERS = RegUtils.registerCandleHolders(Supplementaries.res(CANDLE_HOLDER_NAME));


    //rope
    public static final Supplier<Block> ROPE = regBlock(ROPE_NAME, () -> new RopeBlock(
            BlockBehaviour.Properties.copy(Blocks.BROWN_WOOL)
                    .sound(ModSounds.ROPE)
                    .strength(0.25f)
                    .speedFactor(0.7f)
                    .noOcclusion()));

    public static final Supplier<Block> ROPE_KNOT = regBlock(ROPE_KNOT_NAME, () -> new RopeKnotBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)
                    .dynamicShape()));

    public static final Supplier<Item> ROPE_ITEM = regItem(ROPE_NAME, () -> new RopeItem(
            ROPE.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<RopeKnotBlockTile>> ROPE_KNOT_TILE = regTile(
            ROPE_KNOT_NAME, () -> PlatHelper.newBlockEntityType(
                    RopeKnotBlockTile::new, ROPE_KNOT.get()));

    //spikes
    public static final Supplier<Block> BAMBOO_SPIKES = regBlock(BAMBOO_SPIKES_NAME, () -> new BambooSpikesBlock(
            BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.NORMAL)
                    .mapColor(MapColor.SAND)
                    .ignitedByLava()
                    .sound(SoundType.SCAFFOLDING)
                    .isRedstoneConductor((a, b, c) -> false)
                    .strength(2)
                    .noOcclusion()));

    public static final Supplier<BlockEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = regTile(
            BAMBOO_SPIKES_NAME, () -> PlatHelper.newBlockEntityType(
                    BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()));

    public static final Supplier<Item> BAMBOO_SPIKES_ITEM = regItem(BAMBOO_SPIKES_NAME, () -> new WoodBasedBlockItem(
            BAMBOO_SPIKES.get(), new Item.Properties(), 150));

    public static final Supplier<Item> BAMBOO_SPIKES_TIPPED_ITEM = regItem(TIPPED_SPIKES_NAME, () -> new BambooSpikesTippedItem(
            BAMBOO_SPIKES.get(), new Item.Properties()
            .defaultDurability(BambooSpikesBlockTile.MAX_CHARGES)));

    //goblet
    public static final Supplier<Block> GOBLET = regWithItem(GOBLET_NAME, () -> new GobletBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(1.5f, 2f)
                    .sound(SoundType.METAL)));

    public static final Supplier<BlockEntityType<GobletBlockTile>> GOBLET_TILE = regTile(
            GOBLET_NAME, () -> PlatHelper.newBlockEntityType(
                    GobletBlockTile::new, GOBLET.get()));

    //hourglass
    public static final Supplier<Block> HOURGLASS = regWithItem(HOURGLASS_NAME, () -> new HourGlassBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));

    public static final Supplier<BlockEntityType<HourGlassBlockTile>> HOURGLASS_TILE = regTile(
            HOURGLASS_NAME, () -> PlatHelper.newBlockEntityType(
                    HourGlassBlockTile::new, HOURGLASS.get()));

    //item shelf
    public static final Supplier<Block> ITEM_SHELF = regWithItem(ITEM_SHELF_NAME, () -> new ItemShelfBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .sound(SoundType.WOOD)
                    .strength(0.75f, 0.1f)
                    .noOcclusion()
                    .noCollission()
    ), 100);

    public static final Supplier<BlockEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = regTile(
            ITEM_SHELF_NAME, () -> PlatHelper.newBlockEntityType(
                    ItemShelfBlockTile::new, ItemShelfBlock.ITEM_SHELF_BLOCKS.toArray(Block[]::new)));

    //doormat
    public static final Supplier<Block> DOORMAT = regWithItem(DOORMAT_NAME, () -> new DoormatBlock(
            BlockBehaviour.Properties.copy(Blocks.BROWN_CARPET)
                    .mapColor(MapColor.WOOD)
                    .strength(0.1F)
                    .noOcclusion()
    ), 134);

    public static final Supplier<BlockEntityType<DoormatBlockTile>> DOORMAT_TILE = regTile(
            DOORMAT_NAME, () -> PlatHelper.newBlockEntityType(
                    DoormatBlockTile::new, DOORMAT.get()));

    //magma cream block
    //public static final Supplier<Block> MAGMA_CREAM_BLOCK = regBlock(MAGMA_CREAM_BLOCK_NAME, () -> new MagmaCreamBlock(
    //        BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)));
    //public static final Supplier<Item> MAGMA_CREAM_BLOCK_ITEM = regItem(MAGMA_CREAM_BLOCK_NAME, () -> new BlockItem(MAGMA_CREAM_BLOCK.get(),
    //        (new Item.Properties()).tab(getTab( MAGMA_CREAM_BLOCK_NAME))));

    //raked gravel
    public static final Supplier<Block> RAKED_GRAVEL = regWithItem(RAKED_GRAVEL_NAME, () -> new RakedGravelBlock(
            BlockBehaviour.Properties.copy(Blocks.GRAVEL)
                    .isViewBlocking((w, s, p) -> true)
                    .isSuffocating((w, s, p) -> true)
    ));

    //redstone blocks

    //cog block
    public static final Supplier<Block> COG_BLOCK = regWithItem(COG_BLOCK_NAME, () -> new CogBlock(
            BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                    .strength(3f, 6f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
    ));

    //diode block
    public static final Supplier<Block> RELAYER = regWithItem(RELAYER_NAME, () -> new RelayerBlock(
            BlockBehaviour.Properties.copy(Blocks.OBSERVER).isRedstoneConductor((s, l, p) -> false)
    ));

    //piston launcher base
    public static final Supplier<Block> SPRING_LAUNCHER = regWithItem(SPRING_LAUNCHER_NAME, () -> new SpringLauncherBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isSuffocating((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isViewBlocking((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
    ));

    public static final Supplier<Block> SPRING_LAUNCHER_HEAD = regBlock(PISTON_LAUNCHER_HEAD_NAME, () -> new SpringLauncherHeadBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(4f, 5f)
                    .pushReaction(PushReaction.BLOCK)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
                    .jumpFactor(1.18f)
    ));
    public static final Supplier<Block> SPRING_LAUNCHER_ARM = regBlock(PISTON_LAUNCHER_ARM_NAME, () -> new SpringLauncherArmBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(50f, 50f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noLootTable()
    ));
    public static final Supplier<BlockEntityType<SpringLauncherArmBlockTile>> SPRING_LAUNCHER_ARM_TILE = regTile(
            PISTON_LAUNCHER_ARM_NAME, () -> PlatHelper.newBlockEntityType(
                    SpringLauncherArmBlockTile::new, SPRING_LAUNCHER_ARM.get()));

    //speaker Block
    public static final Supplier<SpeakerBlock> SPEAKER_BLOCK = regWithItem(SPEAKER_BLOCK_NAME, () ->
            new SpeakerBlock(BlockBehaviour.Properties.copy(Blocks.NOTE_BLOCK)
                    .strength(1f, 2f)
                    .sound(SoundType.WOOD)), 300);

    public static final Supplier<BlockEntityType<SpeakerBlockTile>> SPEAKER_BLOCK_TILE = regTile(
            SPEAKER_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    SpeakerBlockTile::new, SPEAKER_BLOCK.get()));

    //turn table
    public static final Supplier<Block> TURN_TABLE = regWithItem(TURN_TABLE_NAME, () -> new TurnTableBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(0.75f, 2f)
    ));

    public static final Supplier<BlockEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = regTile(
            TURN_TABLE_NAME, () -> PlatHelper.newBlockEntityType(
                    TurnTableBlockTile::new, TURN_TABLE.get()));

    //illuminator
    public static final Supplier<Block> REDSTONE_ILLUMINATOR = regWithItem(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            BlockBehaviour.Properties.copy(Blocks.SEA_LANTERN)
                    .isValidSpawn((s, w, p, g) -> true)
                    .strength(0.3f, 0.3f)
    ));


    //pulley
    public static final Supplier<Block> PULLEY_BLOCK = regWithItem(PULLEY_BLOCK_NAME, () -> new PulleyBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)
    ), 300);

    public static final Supplier<BlockEntityType<PulleyBlockTile>> PULLEY_BLOCK_TILE = regTile(
            PULLEY_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    PulleyBlockTile::new, PULLEY_BLOCK.get()));

    //lock block
    public static final Supplier<Block> LOCK_BLOCK = regWithItem(LOCK_BLOCK_NAME, () -> new LockBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .isRedstoneConductor((blockState, blockGetter, blockPos) -> false)
                    .sound(SoundType.METAL)
    ));

    //bellows
    public static final Supplier<Block> BELLOWS = regWithItem(BELLOWS_NAME, () -> new BellowsBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_BROWN)
                    .dynamicShape()
                    .strength(3f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ), 300);

    public static final Supplier<BlockEntityType<BellowsBlockTile>> BELLOWS_TILE = regTile(
            BELLOWS_NAME, () -> PlatHelper.newBlockEntityType(
                    BellowsBlockTile::new, BELLOWS.get()));

    //clock
    public static final Supplier<Block> CLOCK_BLOCK = regWithItem(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            BlockBehaviour.Properties.copy(Blocks.DARK_OAK_PLANKS)
                    .strength(3f, 6f)
                    .lightLevel((state) -> 1)
    ));

    public static final Supplier<BlockEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = regTile(
            CLOCK_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    ClockBlockTile::new, CLOCK_BLOCK.get()));

    //crystal display
    public static final Supplier<Block> CRYSTAL_DISPLAY = regWithItem(CRYSTAL_DISPLAY_NAME, () -> new CrystalDisplayBlock(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                    .sound(SoundType.POLISHED_DEEPSLATE)
                    .strength(0.5f, 0.5f)
    ));

    //sconce lever
    public static final Supplier<Block> SCONCE_LEVER = regWithItem(SCONCE_LEVER_NAME, () -> new SconceLeverBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()),
            () -> ParticleTypes.FLAME
    ));

    //crank
    public static final Supplier<Block> CRANK = regWithItem(CRANK_NAME, () -> new CrankBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.6f, 0.6f)
                    .noCollission()
                    .noOcclusion()
    ));

    //wind vane
    public static final Supplier<Block> WIND_VANE = regWithItem(WIND_VANE_NAME, () -> new WindVaneBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<WindVaneBlockTile>> WIND_VANE_TILE = regTile(
            WIND_VANE_NAME, () -> PlatHelper.newBlockEntityType(
                    WindVaneBlockTile::new, WIND_VANE.get()));

    //faucet
    public static final Supplier<Block> FAUCET = regWithItem(FAUCET_NAME, () -> new FaucetBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS)
                    .strength(3f, 4.8f)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<FaucetBlockTile>> FAUCET_TILE = regTile(
            FAUCET_NAME, () -> PlatHelper.newBlockEntityType(
                    FaucetBlockTile::new, FAUCET.get()));

    //gold door
    public static final Supplier<Block> GOLD_DOOR = regWithItem(GOLD_DOOR_NAME, () -> new GoldDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()
    ));

    //gold trapdoor
    public static final Supplier<Block> GOLD_TRAPDOOR = regWithItem(GOLD_TRAPDOOR_NAME, () -> new GoldTrapdoorBlock(
            BlockBehaviour.Properties.copy(GOLD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)
    ));

    //netherite doors
    public static final Supplier<Block> NETHERITE_DOOR = regBlock(NETHERITE_DOOR_NAME, () -> new NetheriteDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .noOcclusion()
    ));
    public static final Supplier<Item> NETHERITE_DOOR_ITEM = regItem(NETHERITE_DOOR_NAME, () -> new BlockItem(
            NETHERITE_DOOR.get(), new Item.Properties()
            .fireResistant()));

    //netherite trapdoor
    public static final Supplier<Block> NETHERITE_TRAPDOOR = regBlock(NETHERITE_TRAPDOOR_NAME, () -> new NetheriteTrapdoorBlock(
            BlockBehaviour.Properties.copy(NETHERITE_DOOR.get())
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)
    ));
    public static final Supplier<Item> NETHERITE_TRAPDOOR_ITEM = regItem(NETHERITE_TRAPDOOR_NAME, () -> new BlockItem(
            NETHERITE_TRAPDOOR.get(), new Item.Properties()
            .fireResistant()));

    public static final Supplier<BlockEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = regTile(
            "key_lockable_tile", () -> PlatHelper.newBlockEntityType(
                    KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()));

    //iron gate
    public static final Supplier<Block> IRON_GATE = regWithItem(IRON_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), false
    ));

    //gold gate
    public static final Supplier<Block> GOLD_GATE = regWithItem(GOLD_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), true));

    //wall lantern
    public static final Supplier<WallLanternBlock> WALL_LANTERN = regBlock(WALL_LANTERN_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.LANTERN)
                .pushReaction(PushReaction.DESTROY)
                .lightLevel((state) -> 15).noLootTable();

        return /*CompatHandler.create ? SchematicCannonStuff.makeWallLantern(p):*/  new WallLanternBlock(p);
    });

    public static final Supplier<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = regTile(
            WALL_LANTERN_NAME, () -> PlatHelper.newBlockEntityType(
                    WallLanternBlockTile::new, WALL_LANTERN.get()));


    //hanging flower pot
    public static final Supplier<Block> HANGING_FLOWER_POT = regPlaceableItem(HANGING_FLOWER_POT_NAME,
            () -> new HangingFlowerPotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)),
            () -> Items.FLOWER_POT, CommonConfigs.Tweaks.HANGING_POT_PLACEMENT);

    public static final Supplier<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = regTile(
            HANGING_FLOWER_POT_NAME, () -> PlatHelper.newBlockEntityType(
                    HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()));

    public static final Map<CakeRegistry.CakeType, DoubleCakeBlock> DOUBLE_CAKES = new LinkedHashMap<>();

    //directional cake
    public static final Supplier<Block> DIRECTIONAL_CAKE = regBlock(DIRECTIONAL_CAKE_NAME, () -> new DirectionalCakeBlock(
            CakeRegistry.VANILLA
    ));

    //checker block
    public static final Supplier<Block> CHECKER_BLOCK = regWithItem(CHECKER_BLOCK_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.QUARTZ)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
    ));

    //slab
    public static final Supplier<Block> CHECKER_SLAB = regWithItem(CHECKER_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get())
    ));

    //pancakes
    public static final Supplier<Item> PANCAKE_ITEM = regItem(PANCAKE_NAME, () -> new PancakeItem(
            15, ModSounds.PANCAKE_MUSIC.get(), new Item.Properties(), 3 * 60 + 48));

    public static final Supplier<Block> PANCAKE = regPlaceableItem(PANCAKE_NAME,
            () -> new PancakeBlock(
                    BlockBehaviour.Properties.copy(Blocks.CAKE)
                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                            .strength(0.5F)
                            .sound(SoundType.WOOL)),
            PANCAKE_ITEM,
            () -> true
    );
    //flax
    public static final Supplier<Block> FLAX = regBlock(FLAX_NAME, () -> new FlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.WHEAT)
                    .noCollission()
                    .randomTicks()
                    .offsetType(BlockBehaviour.OffsetType.NONE)
                    .instabreak()
                    .sound(SoundType.CROP))
    );

    public static final Supplier<Item> FLAX_ITEM = regItem(FLAX_NAME, () -> new Item(new Item.Properties()));

    public static final Supplier<Item> FLAX_SEEDS_ITEM = regItem("flax_seeds", () -> new ItemNameBlockItem(
            FLAX.get(), new Item.Properties()));

    public static final Supplier<Block> FLAX_WILD = regWithItem(FLAX_WILD_NAME, () -> new WildFlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.TALL_GRASS).offsetType(BlockBehaviour.OffsetType.NONE)
    ));

    //pot
    public static final Supplier<Block> FLAX_POT = regBlock("potted_flax", () -> PlatHelper.newFlowerPot(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT, FLAX, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));

    //fodder
    public static final Supplier<Block> FODDER = regWithItem(FODDER_NAME, () -> new FodderBlock(
            BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)
                    .pushReaction(PushReaction.NORMAL)
    ));

    //flax block
    public static final Supplier<Block> FLAX_BLOCK = regWithItem(FLAX_BLOCK_NAME, () -> new FlaxBaleBlock(
            BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)
                    .mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
    ));

    //boat in a jar
    public static final Supplier<Block> JAR_BOAT = regWithItem(JAR_BOAT_NAME, () -> new JarBoatBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Supplier<BlockEntityType<JarBoatTile>> JAR_BOAT_TILE = regTile(
            JAR_BOAT_NAME, () -> PlatHelper.newBlockEntityType(
                    JarBoatTile::new, JAR_BOAT.get()));

    //block generator
    public static final Supplier<Block> STRUCTURE_TEMP = regBlock(STRUCTURE_TEMP_NAME, () -> new StructureTempBlock(
            BlockBehaviour.Properties.of().strength(0).noLootTable().noCollission().noOcclusion()));

    public static final Supplier<BlockEntityType<StructureTempBlockTile>> STRUCTURE_TEMP_TILE = regTile(
            STRUCTURE_TEMP_NAME, () -> PlatHelper.newBlockEntityType(
                    StructureTempBlockTile::new, STRUCTURE_TEMP.get()));

    public static final Supplier<Block> BLOCK_GENERATOR = regBlock(BLOCK_GENERATOR_NAME, () -> new BlockGeneratorBlock(
            BlockBehaviour.Properties.copy(STRUCTURE_TEMP.get()).lightLevel((s) -> 14)));

    public static final Supplier<BlockEntityType<BlockGeneratorBlockTile>> BLOCK_GENERATOR_TILE = regTile(
            BLOCK_GENERATOR_NAME, () -> PlatHelper.newBlockEntityType(
                    BlockGeneratorBlockTile::new, BLOCK_GENERATOR.get()));

    //sticks
    public static final Supplier<Block> STICK_BLOCK = regPlaceableItem(STICK_NAME, () -> new StickBlock(
            BlockBehaviour.Properties.of()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.NONE)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD), 60), () -> Items.STICK, CommonConfigs.Tweaks.PLACEABLE_STICKS);

    //TODO: move these outta here
    /*
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
    */
    //blaze rod
    //TODO: blaze sound
    public static final Supplier<Block> BLAZE_ROD_BLOCK = regPlaceableItem(BLAZE_ROD_NAME, () -> new BlazeRodBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(0.25F, 0F)
                            .lightLevel(state -> 12)
                            .emissiveRendering((p, w, s) -> true)
                            .sound(SoundType.GILDED_BLACKSTONE)),
            () -> Items.BLAZE_ROD, CommonConfigs.Tweaks.PLACEABLE_RODS
    );

    //daub
    public static final RegSupplier<Block> DAUB = regWithItem(DAUB_NAME, () -> new Block(
            BlockBehaviour.Properties.of()
                    .sound(SoundType.PACKED_MUD)
                    .mapColor(DyeColor.WHITE)
                    .strength(1.5f, 3f)
    ));

    //wattle and daub
    //frame
    public static final RegSupplier<Block> DAUB_FRAME = regWithItem(DAUB_FRAME_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())
    ));
    //brace
    public static final RegSupplier<Block> DAUB_BRACE = regWithItem(DAUB_BRACE_NAME, () -> new FlippedBlock(
            BlockBehaviour.Properties.copy(DAUB.get())
    ));

    //cross brace
    public static final RegSupplier<Block> DAUB_CROSS_BRACE = regWithItem(DAUB_CROSS_BRACE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())
    ));

    //timber frame
    public static final RegSupplier<FrameBlock> TIMBER_FRAME = regBlock(TIMBER_FRAME_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                .strength(0.1f, 0f)
                .noCollission().instabreak()
                .sound(SoundType.SCAFFOLDING); //.dynamicShape()
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_FRAME) :*/ new FrameBlock(p);
    });
    public static final Supplier<Item> TIMBER_FRAME_ITEM = regItem(TIMBER_FRAME_NAME, () -> new TimberFrameItem(
            TIMBER_FRAME.get(), new Item.Properties(), 200));

    //timber brace
    public static final Supplier<FrameBraceBlock> TIMBER_BRACE = regBlock(TIMBER_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFrameBraceBlock(p, DAUB_BRACE) :*/ new FrameBraceBlock(p);
    });
    public static final Supplier<Item> TIMBER_BRACE_ITEM = regItem(TIMBER_BRACE_NAME, () -> new TimberFrameItem(
            TIMBER_BRACE.get(), new Item.Properties(), 200));

    //timber cross brace
    public static final Supplier<FrameBlock> TIMBER_CROSS_BRACE = regBlock(TIMBER_CROSS_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_CROSS_BRACE) :*/ new FrameBlock(p);
    });
    public static final Supplier<Item> TIMBER_CROSS_BRACE_ITEM = regItem(TIMBER_CROSS_BRACE_NAME, () -> new TimberFrameItem(
            TIMBER_CROSS_BRACE.get(), new Item.Properties(), 200));

    public static final Supplier<BlockEntityType<FrameBlockTile>> TIMBER_FRAME_TILE = regTile(
            TIMBER_FRAME_NAME, () -> PlatHelper.newBlockEntityType(
                    FrameBlockTile::new, FrameBlock.FRAMED_BLOCKS.toArray(Block[]::new)));

    //lapis bricks
    public static final Map<RegHelper.VariantType, Supplier<Block>> LAPIS_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(res(LAPIS_BRICKS_NAME), BlockBehaviour.Properties.copy(Blocks.LAPIS_BLOCK)
                    .sound(SoundType.DEEPSLATE_TILES).strength(2.0F, 2.0F));

    //ashen bricks
    public static final Map<RegHelper.VariantType, Supplier<Block>> ASH_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(res(ASH_BRICKS_NAME), Blocks.STONE_BRICKS);

    //stone tile
    public static final Map<RegHelper.VariantType, Supplier<Block>> STONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(res(STONE_TILE_NAME), Blocks.STONE_BRICKS);

    //blackstone tile
    public static final Map<RegHelper.VariantType, Supplier<Block>> BLACKSTONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(res(BLACKSTONE_TILE_NAME), Blocks.BLACKSTONE);

    //stone lamp
    public static final Supplier<Block> STONE_LAMP = regWithItem(STONE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ));

    //blackstone lamp
    public static final Supplier<Block> BLACKSTONE_LAMP = regWithItem(BLACKSTONE_LAMP_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ));

    //deepslate lamp
    public static final Supplier<Block> DEEPSLATE_LAMP = regWithItem(DEEPSLATE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).lightLevel(s -> 15)
    ));

    //end_stone lamp
    public static final Supplier<Block> END_STONE_LAMP = regWithItem(END_STONE_LAMP_NAME, () -> new EndLampBlock(
            BlockBehaviour.Properties.copy(Blocks.END_STONE).lightLevel(s -> 15)
    ));

    //flower box
    public static final Supplier<Block> FLOWER_BOX = regWithItem(FLOWER_BOX_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(0.5F);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFlowerBox(p) : */new FlowerBoxBlock(p);
    });

    public static final Supplier<BlockEntityType<FlowerBoxBlockTile>> FLOWER_BOX_TILE = regTile(FLOWER_BOX_NAME, () ->
            PlatHelper.newBlockEntityType(FlowerBoxBlockTile::new, FLOWER_BOX.get()));

    //statue
    public static final Supplier<Block> STATUE = regWithItem(STATUE_NAME, () -> new StatueBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2)
    ));

    public static final Supplier<BlockEntityType<StatueBlockTile>> STATUE_TILE = regTile(
            STATUE_NAME, () -> PlatHelper.newBlockEntityType(
                    StatueBlockTile::new, STATUE.get()));

    //feather block
    public static final Supplier<Block> FEATHER_BLOCK = regWithItem(FEATHER_BLOCK_NAME, () -> new FeatherBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(0.5f)
                    .dynamicShape()
                    .noCollission()
    ));

    //flint block
    public static final Supplier<Block> FLINT_BLOCK = regWithItem(FLINT_BLOCK_NAME, () -> new FlintBlock(
            BlockBehaviour.Properties.copy(Blocks.COAL_BLOCK).strength(2F, 7.5F)
    ));

    //sugar block
    public static final Supplier<Block> SUGAR_CUBE = regBlock(SUGAR_CUBE_NAME, () -> new SugarBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SNOW)
                    .strength(0.5f)
                    .sound(SoundType.SAND)
    ));
    public static final Supplier<Item> SUGAR_CUBE_ITEM = regItem(SUGAR_CUBE_NAME, () -> new SugarCubeItem(
            SUGAR_CUBE.get(), new Item.Properties())
    );

    //gunpowder block
    public static final Supplier<Block> GUNPOWDER_BLOCK = regPlaceableItem(GUNPOWDER_BLOCK_NAME, () -> new GunpowderBlock(
                    BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).sound(SoundType.SAND)),
            () -> Items.GUNPOWDER, CommonConfigs.Tweaks.PLACEABLE_GUNPOWDER);

    //placeable book
    public static final Supplier<Block> BOOK_PILE = regPlaceableItem(BOOK_PILE_NAME, () -> new BookPileBlock(
                    BlockBehaviour.Properties.of()
                            .noOcclusion()
                            .mapColor(MapColor.NONE)
                            .strength(0.5F)
                            .sound(ModSounds.BOOKS)),
            () -> Items.ENCHANTED_BOOK, CommonConfigs.Tweaks.PLACEABLE_BOOKS);

    //placeable book
    public static final Supplier<Block> BOOK_PILE_H = regPlaceableItem(BOOK_PILE_H_NAME, () -> new BookPileHorizontalBlock(
                    BlockBehaviour.Properties.copy(BOOK_PILE.get())),
            () -> Items.BOOK, CommonConfigs.Tweaks.PLACEABLE_BOOKS);

    public static final Supplier<BlockEntityType<BookPileBlockTile>> BOOK_PILE_TILE = regTile(
            BOOK_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    BookPileBlockTile::new, BOOK_PILE.get(), BOOK_PILE_H.get()));

    //urn
    public static final Supplier<Block> URN = regWithItem(URN_NAME, () -> new UrnBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .sound(SoundType.DECORATED_POT_CRACKED)
                    .strength(0.1f, 0)
    ));

    public static final Supplier<BlockEntityType<UrnBlockTile>> URN_TILE = regTile(
            URN_NAME, () -> PlatHelper.newBlockEntityType(
                    UrnBlockTile::new, URN.get()));

    //ash
    public static final Supplier<Block> ASH_BLOCK = regWithItem(ASH_NAME, () -> new AshLayerBlock(
            BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
                    .replaceable()
                    .mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.SAND)
                    .randomTicks()
                    .strength(0.1F)
                    .forceSolidOff()
                    .isViewBlocking((state, l, p) -> state.getValue(AshLayerBlock.LAYERS) >= 8)
                    .requiresCorrectToolForDrops()
    ));


    //ash
    public static final Supplier<Item> ASH_BRICK_ITEM = regItem(ASH_BRICK_NAME, () -> new Item(new Item.Properties()));

    //soap
    public static final Supplier<Item> SOAP = regItem(SOAP_NAME, () -> new SoapItem(new Item.Properties()));

    public static final Supplier<Block> SOAP_BLOCK = regWithItem(SOAP_BLOCK_NAME, () -> new SoapBlock(
            BlockBehaviour.Properties.of()
                    .friction(0.94f)
                    .instrument(NoteBlockInstrument.DIDGERIDOO)
                    .mapColor(DyeColor.PINK)
                    .pushReaction(PushReaction.PUSH_ONLY)
                    .strength(1.25F, 4.0F)
                    .sound(SoundType.CORAL_BLOCK)
    ));

    //stackable skulls
    public static final Supplier<Block> SKULL_PILE = regBlock(SKULL_PILE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);

        return /*CompatHandler.create ? SchematicCannonStuff.makeDoubleSkull(p) :*/ new DoubleSkullBlock(p);
    });

    public static final Supplier<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = regTile(
            SKULL_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    DoubleSkullBlockTile::new, SKULL_PILE.get()));

    //skulls candles
    public static final Supplier<Block> SKULL_CANDLE = regBlock(SKULL_CANDLE_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK)));

    public static final Supplier<Block> SKULL_CANDLE_WALL = regBlock(SKULL_CANDLE_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get())));


    //needed for tag so it can repel piglins
    public static final Supplier<Block> SKULL_CANDLE_SOUL = regBlock(SKULL_CANDLE_SOUL_NAME, () ->
            new FloorCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get()),
                    CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME));

    public static final Supplier<Block> SKULL_CANDLE_SOUL_WALL = regBlock(SKULL_CANDLE_SOUL_NAME + "_wall", () ->
            new WallCandleSkullBlock(BlockBehaviour.Properties.copy(SKULL_CANDLE.get()),
                    CompatHandler.BUZZIER_BEES ? CompatObjects.SMALL_SOUL_FLAME : () -> ParticleTypes.SOUL_FIRE_FLAME));


    public static final Supplier<BlockEntityType<CandleSkullBlockTile>> SKULL_CANDLE_TILE = regTile(
            SKULL_CANDLE_NAME, () -> PlatHelper.newBlockEntityType(
                    CandleSkullBlockTile::new, SKULL_CANDLE.get(), SKULL_CANDLE_WALL.get(),
                    SKULL_CANDLE_SOUL.get(), SKULL_CANDLE_SOUL_WALL.get()));

    //bubble
    public static final Supplier<BubbleBlock> BUBBLE_BLOCK = regBlock(BUBBLE_BLOCK_NAME, () ->
            new BubbleBlock(BlockBehaviour.Properties.of()
                    .sound(ModSounds.BUBBLE_BLOCK)
                    .mapColor(MapColor.COLOR_PINK)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .forceSolidOff()
                    .isSuffocating((a, b, c) -> false)
                    .isViewBlocking((a, b, c) -> false)
                    .isRedstoneConductor((a, b, c) -> false)
                    .instabreak())
    );
    public static final Supplier<Item> BUBBLE_BLOCK_ITEM = regItem(BUBBLE_BLOCK_NAME, () -> new BubbleBlockItem(
            BUBBLE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<BubbleBlockTile>> BUBBLE_BLOCK_TILE = regTile(
            BUBBLE_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    BubbleBlockTile::new, BUBBLE_BLOCK.get()));

    //enderman skull
    public static final Supplier<EndermanSkullBlock> ENDERMAN_SKULL_BLOCK = regBlock(ENDERMAN_HEAD_NAME, () ->
            new EndermanSkullBlock(BlockBehaviour.Properties.copy(Blocks.WITHER_SKELETON_SKULL)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<EndermanSkullWallBlock> ENDERMAN_SKULL_BLOCK_WALL = regBlock("enderman_wall_head", () ->
            new EndermanSkullWallBlock(BlockBehaviour.Properties.copy(Blocks.WITHER_SKELETON_SKULL)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<Item> ENDERMAN_SKULL_ITEM = regItem(ENDERMAN_HEAD_NAME, () ->
            new EndermanHeadItem(ENDERMAN_SKULL_BLOCK.get(), ENDERMAN_SKULL_BLOCK_WALL.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockEntityType<EndermanSkullBlockTile>> ENDERMAN_SKULL_TILE = regTile(
            ENDERMAN_HEAD_NAME, () -> PlatHelper.newBlockEntityType(
                    EndermanSkullBlockTile::new, ENDERMAN_SKULL_BLOCK.get(), ENDERMAN_SKULL_BLOCK_WALL.get()));

    //ash basalt
    public static final Supplier<Block> ASHEN_BASALT = regBlock("ashen_basalt", () ->
            new AshenBasaltBlock(BlockBehaviour.Properties.copy(Blocks.BASALT))
    );

    //hat stand
    public static final Supplier<Item> HAT_STAND = regItem(HAT_STAND_NAME, () -> new HatStandItem(new Item.Properties()));

}
