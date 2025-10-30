package net.mehvahdjukaar.supplementaries.reg;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.ModStairBlock;
import net.mehvahdjukaar.moonlight.api.misc.IAttachmentType;
import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.components.LunchBaskedContent;
import net.mehvahdjukaar.supplementaries.common.items.components.QuiverContent;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomArrowFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.RandomEnchantFunction;
import net.mehvahdjukaar.supplementaries.common.items.loot.SetChargesFunction;
import net.mehvahdjukaar.supplementaries.common.misc.effects.FlammableEffect;
import net.mehvahdjukaar.supplementaries.common.misc.effects.OverencumberedEffect;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.ArrayList;
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
    public static final Supplier<LootItemFunctionType<RandomEnchantFunction>> CURSE_LOOT_FUNCTION = RegHelper.register(res("curse_loot"),
            () -> new LootItemFunctionType<>(RandomEnchantFunction.CODEC), Registries.LOOT_FUNCTION_TYPE);
    public static final Supplier<LootItemFunctionType<RandomArrowFunction>> RANDOM_ARROW_FUNCTION = RegHelper.register(res("random_arrows"),
            () -> new LootItemFunctionType<>(RandomArrowFunction.CODEC), Registries.LOOT_FUNCTION_TYPE);
    public static final Supplier<LootItemFunctionType<SetChargesFunction>> SET_CHARGES_FUNCTION = RegHelper.register(res("set_charges"),
            () -> new LootItemFunctionType<>(SetChargesFunction.CODEC), Registries.LOOT_FUNCTION_TYPE);

    //data
    public static final WorldSavedDataType<GlobeData> GLOBE_DATA = RegHelper.registerWorldSavedData(
            res("globe_data"), GlobeData::createFromLevel, GlobeData.CODEC, GlobeData.STREAM_CODEC
    );

    public static final IAttachmentType<SlimedData, LivingEntity> SLIMED_DATA = RegHelper.registerDataAttachment(
            res("slimed_data"),
            () -> RegHelper.AttachmentBuilder.create(SlimedData::new)
                    .syncWith(SlimedData.STREAM_CODEC)
                    .persistent(SlimedData.CODEC),
            LivingEntity.class
    );

    //effects
    public static final RegSupplier<MobEffect> OVERENCUMBERED = RegHelper.registerEffect(
            res("overencumbered"), OverencumberedEffect::new);

    public static final RegSupplier<MobEffect> FLAMMABLE = RegHelper.registerEffect(
            res("flammable"), FlammableEffect::new);


    //red merchant
    //public static final Supplier<Item> RED_MERCHANT_SPAWN_EGG_ITEM = regItem(RED_MERCHANT_NAME + "_spawn_egg", () ->
    //        PlatHelper.newSpawnEgg(ModEntities.RED_MERCHANT, 0x7A090F, 0xF4f1e0, new Item.Properties()));


    //dispenser minecart
    public static final Supplier<Item> DISPENSER_MINECART_ITEM = regItem(DISPENSER_MINECART_NAME, () -> new DispenserMinecartItem(new Item.Properties()
            .stacksTo(1)));


    public static final Supplier<Item> BOMB_ITEM = regItem(BOMB_NAME, () -> new BombItem(new Item.Properties()));

    public static final Supplier<Item> BOMB_BLUE_ITEM = regItem(BOMB_BLUE_NAME, () -> new BombItem(new Item.Properties()
            .rarity(Rarity.RARE),
            BombEntity.BombType.BLUE, true));

    //sharpnel bomb
    public static final Supplier<Item> BOMB_SPIKY_ITEM = regItem(BOMB_SPIKY_NAME, () -> new BombItem(new Item.Properties(),
            BombEntity.BombType.SPIKY, false));

    //rope arrow
    public static final Supplier<RopeArrowItem> ROPE_ARROW_ITEM = regItem(ROPE_ARROW_NAME, () -> new RopeArrowItem(
            CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get(),
            new Item.Properties()
                    .component(ModComponents.CHARGES.get(), CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get())
                    .component(ModComponents.MAX_CHARGES.get(), CommonConfigs.Tools.ROPE_ARROW_CAPACITY.get())));

    //bubble blower
    public static final Supplier<Item> BUBBLE_BLOWER = regItem(BUBBLE_BLOWER_NAME, () -> new BubbleBlowerItem(
            new Item.Properties()
                    .component(ModComponents.CHARGES.get(), 0)
                    .component(ModComponents.MAX_CHARGES.get(), CommonConfigs.Tools.BUBBLE_BLOWER_MAX_CHARGES.get())
                    .stacksTo(1)
    ));

    //slingshot
    public static final Supplier<Item> SLINGSHOT_ITEM = regItem(SLINGSHOT_NAME, () -> new SlingshotItem(new Item.Properties()
            .stacksTo(1)
            .durability(192)));

    //confetti
    public static final Supplier<Block> CONFETTI_LITTER = regBlock(CONFETTI_LITTER_NAME, () -> new ConfettiLitterBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .sound(SoundType.AZALEA_LEAVES)
                    .strength(0, 0)
                    .forceSolidOff()
                    .noOcclusion()
    ));

    public static final Supplier<Item> CONFETTI_POPPER = regItem(CONFETTI_POPPER_NAME, () ->
            new ConfettiPopperItem(CONFETTI_LITTER.get(), new Item.Properties()));

    //flute
    public static final Supplier<Item> FLUTE_ITEM = regItem(FLUTE_NAME, () -> new FluteItem(new Item.Properties()
            .stacksTo(1)
            .durability(64)));

    //key
    public static final Supplier<KeyItem> KEY_ITEM = regItem(KEY_NAME, () -> new KeyItem(new Item.Properties()));

    //candy
    public static final Supplier<Item> CANDY_ITEM = regItem(CANDY_NAME, () -> new CandyItem(new Item.Properties()));

    //antique ink
    public static final Supplier<Item> ANTIQUE_INK = regItem(ANTIQUE_INK_NAME, () -> new AntiqueInkItem(new Item.Properties()));

    //wrench
    public static final Supplier<Item> WRENCH = regItem(WRENCH_NAME, () -> new WrenchItem(new Item.Properties()
            .stacksTo(1)
            .attributes(AxeItem.createAttributes(Tiers.WOOD, 2.5f, -2f))
            .durability(200)));

    //quiver
    public static final Supplier<QuiverItem> QUIVER_ITEM = regItem(QUIVER_NAME, () -> new QuiverItem((new Item.Properties())
            .stacksTo(1)
            .component(ModComponents.QUIVER_CONTENT.get(), QuiverContent.empty(CommonConfigs.Tools.QUIVER_SLOTS.get()))
            .rarity(Rarity.RARE)));

    //lunch basket
    public static final Supplier<LunchBoxItem> LUNCH_BASKET_ITEM = regItem(LUNCH_BASKET_NAME, () -> new LunchBoxItem((new Item.Properties())
            .component(ModComponents.LUNCH_BASKET_CONTENT.get(),
                    LunchBaskedContent.empty(CommonConfigs.Tools.LUNCH_BOX_SLOTS.get()))
            .stacksTo(1)));

    public static final Supplier<Block> LUNCH_BASKET = regBlock(LUNCH_BASKET_NAME, () -> new LunchBoxBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .sound(SoundType.BAMBOO)
                    .instabreak()));

    public static final Supplier<BlockEntityType<LunchBoxBlockTile>> LUNCH_BASKET_TILE = regTile(
            LUNCH_BASKET_NAME, () -> PlatHelper.newBlockEntityType(
                    LunchBoxBlockTile::new, LUNCH_BASKET.get()));

    public static final Supplier<Item> DRAGON_PATTERN = regItem(DRAGON_PATTERN_NAME,
            () -> new BannerPatternItem(ModTags.PATTERN_ITEM_DRAGON, (new Item.Properties())
                    .stacksTo(1).rarity(Rarity.RARE)));

    //speedometer
    //   public static final Supplier<Item> SPEEDOMETER_ITEM = regItem(SPEEDOMETER_NAME,()-> new SpeedometerItem(new Item.Properties()));


    //altimeter
    public static final Supplier<Item> ALTIMETER_ITEM = regItem(DEPTH_METER_NAME, () -> new AltimeterItem(new Item.Properties()));

    public static final Supplier<Item> SLICE_MAP = regItem(SLICE_MAP_NAME, () -> new EmptySliceMapItem(new Item.Properties()));


    //blocks

    //sign posts
    public static final Supplier<Block> WAY_SIGN = regBlock(WAY_SIGN_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
        return new SignPostBlock(p);
    });

    public static final Supplier<Block> WAY_SIGN_WALL = regBlock(WAY_SIGN_NAME + "_wall", () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN);
        return new SignPostWallBlock(p);
    });

    public static final Supplier<BlockEntityType<SignPostBlockTile>> WAY_SIGN_TILE = regTile(
            WAY_SIGN_NAME, () -> PlatHelper.newBlockEntityType(
                    SignPostBlockTile::new, WAY_SIGN.get(), WAY_SIGN_WALL.get()));

    public static final Map<WoodType, SignPostItem> WAY_SIGN_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

    //flags
    public static final Map<DyeColor, Supplier<Block>> FLAGS = RegUtils.registerFlags(FLAG_NAME);

    public static final Supplier<BlockEntityType<FlagBlockTile>> FLAG_TILE = regTile(
            FLAG_NAME, () -> PlatHelper.newBlockEntityType(
                    FlagBlockTile::new, FLAGS.values().stream().map(Supplier::get).toArray(Block[]::new)));
    //presents

    public static final Map<DyeColor, Supplier<Block>> PRESENTS = RegUtils.registerPresents(
            PRESENT_NAME, PresentBlock::new, PresentItem::new);

    public static final Supplier<BlockEntityType<PresentBlockTile>> PRESENT_TILE = regTile(
            PRESENT_NAME, () -> PlatHelper.newBlockEntityType(
                    PresentBlockTile::new, PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));


    //trapped presents

    public static final Map<DyeColor, Supplier<Block>> TRAPPED_PRESENTS = RegUtils.registerPresents(
            TRAPPED_PRESENT_NAME, TrappedPresentBlock::new, TrappedPresentItem::new);

    public static final Supplier<BlockEntityType<TrappedPresentBlockTile>> TRAPPED_PRESENT_TILE = regTile(
            TRAPPED_PRESENT_NAME, () -> PlatHelper.newBlockEntityType(
                    TrappedPresentBlockTile::new, TRAPPED_PRESENTS.values().stream().map(Supplier::get).toArray(Block[]::new)));

    // awnings

    public static final Map<DyeColor, Supplier<Block>> AWNINGS = RegUtils.registerAwnings(AWNING_NAME);

    public static final Map<WoodType, CannonBoatItem> CANNON_BOAT_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

    //decoration blocks

    //planter
    public static final Supplier<PlanterBlock> PLANTER = regWithItem(PLANTER_NAME, () ->
            CompatHandler.FARMERS_DELIGHT ? FarmersDelightCompat.makePlanterRich() :
                    new PlanterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA)
                            .mapColor(MapColor.TERRACOTTA_RED)
                            .strength(2f, 6f)
                    ));

    //pedestal
    public static final Supplier<Block> PEDESTAL = regWithItem(PEDESTAL_NAME, () -> new PedestalBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));

    public static final Supplier<BlockEntityType<PedestalBlockTile>> PEDESTAL_TILE = regTile(
            PEDESTAL_NAME, () -> PlatHelper.newBlockEntityType(
                    PedestalBlockTile::new, PEDESTAL.get()));


    //notice board
    public static final Supplier<Block> NOTICE_BOARD = regWithItem(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));

    public static final Supplier<BlockEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = regTile(
            NOTICE_BOARD_NAME, () -> PlatHelper.newBlockEntityType(
                    NoticeBoardBlockTile::new, NOTICE_BOARD.get()));

    //fine wood
    public static final Supplier<Block> FINE_WOOD = regWithItem(FINE_WOOD_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_STAIRS)));

    public static final Supplier<Block> FINE_WOOD_STAIRS = regWithItem(FINE_WOOD_NAME + "_stairs", () -> new ModStairBlock(
            FINE_WOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_STAIRS)));

    public static final Supplier<Block> FINE_WOOD_SLAB = regWithItem(FINE_WOOD_NAME + "_slab", () -> new DirectionalSlabBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_STAIRS)));

    //safe
    public static final Supplier<Block> SAFE = regBlock(SAFE_NAME, () -> new SafeBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
                    .pushReaction(PushReaction.BLOCK)
    ));
    public static final Supplier<BlockEntityType<SafeBlockTile>> SAFE_TILE = regTile(
            SAFE_NAME, () -> PlatHelper.newBlockEntityType(
                    SafeBlockTile::new, SAFE.get()));


    public static final Supplier<Item> SAFE_ITEM = regItem(SAFE_NAME, () ->
            new SafeItem(SAFE.get(), new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                    .fireResistant()));

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
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
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
            new ColorRGBA(0xba8f6a),
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL)
                    .mapColor(MapColor.WOOD)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.8f)
                    .sound(ModSounds.SACK)
    ));
    public static final Supplier<BlockEntityType<SackBlockTile>> SACK_TILE = regTile(
            SACK_NAME, () -> PlatHelper.newBlockEntityType(
                    SackBlockTile::new, SACK.get()));

    public static final Supplier<Item> SACK_ITEM = regItem(SACK_NAME, () -> new SackItem(SACK.get(),
            new Item.Properties().stacksTo(1)
                    .component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
    ));

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
    ));
    public static final Supplier<Item> GLOBE_ITEM = regItem(GLOBE_NAME, () -> new BlockItem(GLOBE.get(),
            new Item.Properties().rarity(Rarity.RARE)));

    public static final Supplier<Block> GLOBE_SEPIA = regBlock(GLOBE_SEPIA_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.ofFullCopy(GLOBE.get())));
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
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .instabreak()
                    .sound(SoundType.LANTERN),
            14, () -> ParticleTypes.FLAME));
    public static final Supplier<Block> SCONCE_WALL = regBlock("sconce_wall", () -> new SconceWallBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE.get())
                    .dropsLike(SCONCE.get()), () -> ParticleTypes.FLAME));
    public static final Supplier<Item> SCONCE_ITEM = regItem(SCONCE_NAME, () ->
            new StandingAndWallBlockItem(SCONCE.get(), SCONCE_WALL.get(), new Item.Properties(), Direction.DOWN));

    //soul
    public static final Supplier<Block> SCONCE_SOUL = regBlock(SCONCE_NAME_SOUL, () -> new SconceBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE.get()), 10,
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Block> SCONCE_WALL_SOUL = regBlock("sconce_wall_soul", () -> new SconceWallBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE_SOUL.get())
                    .dropsLike(SCONCE_SOUL.get()),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_SOUL = regItem(SCONCE_NAME_SOUL, () -> new StandingAndWallBlockItem(
            SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(), new Item.Properties(), Direction.DOWN));


    //green
    public static final Supplier<Block> SCONCE_GREEN = regBlock(SCONCE_NAME_GREEN, () -> new SconceBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE.get()), 14, ModParticles.GREEN_FLAME));
    public static final Supplier<Block> SCONCE_WALL_GREEN = regBlock("sconce_wall_green", () -> new SconceWallBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE.get())
                    .dropsLike(SCONCE_GREEN.get()), ModParticles.GREEN_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_GREEN = regItem(SCONCE_NAME_GREEN, () -> new StandingAndWallBlockItem(
            SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(), new Item.Properties(), Direction.DOWN));


    //sconce lever
    public static final Supplier<Block> SCONCE_LEVER = regWithItem(SCONCE_LEVER_NAME, () -> new SconceLeverBlock(
            BlockBehaviour.Properties.ofFullCopy(SCONCE.get()),
            () -> ParticleTypes.FLAME
    ));

    public static final List<Supplier<Item>> SCONCES = new ArrayList<>(List.of(SCONCE_ITEM, SCONCE_ITEM_SOUL));

    //candle holder
    public static final List<Supplier<? extends Block>> ALL_CANDLE_HOLDERS = new ArrayList<>();
    public static final Map<DyeColor, Supplier<Block>> CANDLE_HOLDERS = RegUtils.registerCandleHolders(Supplementaries.res(CANDLE_HOLDER_NAME));

    //rope
    public static final Supplier<RopeBlock> ROPE = regBlock(ROPE_NAME, () -> new RopeBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL)
                    .sound(ModSounds.ROPE)
                    .strength(0.25f)
                    .speedFactor(0.7f)
                    .noOcclusion()));

    public static final Supplier<Block> ROPE_KNOT = regBlock(ROPE_KNOT_NAME, () -> new RopeKnotBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
                    .dynamicShape()));

    public static final Supplier<Item> ROPE_ITEM = regItem(ROPE_NAME, () -> new RopeItem(
            ROPE.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<RopeKnotBlockTile>> ROPE_KNOT_TILE = regTile(
            ROPE_KNOT_NAME, () -> PlatHelper.newBlockEntityType(
                    RopeKnotBlockTile::new, ROPE_KNOT.get()));

    //buntings
    public static final Supplier<Item> BUNTING_OLD = regItem(BUNTING_NAME, () -> new BuntingItemOld(new Item.Properties()
            .component(DataComponents.BASE_COLOR, DyeColor.WHITE)));

    public static final Map<DyeColor, Supplier<Block>> BUNTING_WALL_BLOCKS = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<DyeColor, Supplier<Block>> BUNTING_BLOCKS = RegUtils.registerBuntings(BUNTING_NAME);

    public static final Supplier<RopeBuntingBlock> BUNTING_ROPE_BLOCK = regBlock("rope_buntings", () -> new RopeBuntingBlock(
            BlockBehaviour.Properties.ofFullCopy(ROPE.get())
                    .dropsLike(ROPE.get())));

    public static final Supplier<BlockEntityType<BuntingBlockTile>> BUNTING_TILE = regTile(
            "rope_buntings", () -> PlatHelper.newBlockEntityType(
                    BuntingBlockTile::new, BUNTING_ROPE_BLOCK.get()));

    // wicker fence
    public static final Supplier<Block> WICKER_FENCE = regWithItem(WICKER_FENCE_NAME, () -> new WickerFenceBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
                    .mapColor(MapColor.WOOD)
                    .ignitedByLava()
                    .strength(0.5f)
                    .sound(SoundType.SCAFFOLDING)
                    .noOcclusion()
    ));

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

    public static final Supplier<Item> BAMBOO_SPIKES_ITEM = regItem(BAMBOO_SPIKES_NAME, () -> new BlockItem(
            BAMBOO_SPIKES.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = regTile(
            BAMBOO_SPIKES_NAME, () -> PlatHelper.newBlockEntityType(
                    BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()));

    public static final Supplier<Item> BAMBOO_SPIKES_TIPPED_ITEM = regItem(TIPPED_SPIKES_NAME, () -> new BambooSpikesTippedItem(
            BAMBOO_SPIKES.get(), new Item.Properties()
            .component(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON))
            .component(ModComponents.CHARGES.get(), BambooSpikesBlockTile.MAX_CHARGES)));

    //goblet
    public static final Supplier<Block> GOBLET = regWithItem(GOBLET_NAME, () -> new GobletBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(1.5f, 2f)
                    .sound(SoundType.METAL)));

    public static final Supplier<BlockEntityType<GobletBlockTile>> GOBLET_TILE = regTile(
            GOBLET_NAME, () -> PlatHelper.newBlockEntityType(
                    GobletBlockTile::new, GOBLET.get()));

    //hourglass
    public static final Supplier<Block> HOURGLASS = regWithItem(HOURGLASS_NAME, () -> new HourGlassBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));

    public static final Supplier<BlockEntityType<HourGlassBlockTile>> HOURGLASS_TILE = regTile(
            HOURGLASS_NAME, () -> PlatHelper.newBlockEntityType(
                    HourGlassBlockTile::new, HOURGLASS.get()));

    //item shelf
    public static final Supplier<Block> ITEM_SHELF = regWithItem(ITEM_SHELF_NAME, () -> new ItemShelfBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .sound(SoundType.WOOD)
                    .strength(0.75f, 0.1f)
                    .noOcclusion()
                    .noCollission()
    ));

    public static final Supplier<BlockEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = regTile(
            ITEM_SHELF_NAME, () -> PlatHelper.newBlockEntityType(
                    ItemShelfBlockTile::new, ITEM_SHELF.get()));

    //doormat
    public static final Supplier<Block> DOORMAT = regWithItem(DOORMAT_NAME, () -> new DoormatBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_CARPET)
                    .mapColor(MapColor.WOOD)
                    .strength(0.1F)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<DoormatBlockTile>> DOORMAT_TILE = regTile(
            DOORMAT_NAME, () -> PlatHelper.newBlockEntityType(
                    DoormatBlockTile::new, DOORMAT.get()));

    //magma cream block
    //public static final Supplier<Block> MAGMA_CREAM_BLOCK = regBlock(MAGMA_CREAM_BLOCK_NAME, () -> new MagmaCreamBlock(
    //        BlockBehaviour.Properties.ofFullCopy()(Blocks.SLIME_BLOCK)));
    //public static final Supplier<Item> MAGMA_CREAM_BLOCK_ITEM = regItem(MAGMA_CREAM_BLOCK_NAME, () -> new BlockItem(MAGMA_CREAM_BLOCK.get(),
    //        (new Item.Properties()).tab(getTab( MAGMA_CREAM_BLOCK_NAME))));

    //raked gravel
    public static final Supplier<Block> RAKED_GRAVEL = regWithItem(RAKED_GRAVEL_NAME, () -> new RakedGravelBlock(
            new ColorRGBA(-8356741),
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                    .isViewBlocking((w, s, p) -> true)
                    .isSuffocating((w, s, p) -> true)
    ));

    //redstone blocks

    //cog block
    public static final Supplier<Block> COG_BLOCK = regWithItem(COG_BLOCK_NAME, () -> new CogBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .strength(3f, 6f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
    ));

    //diode block
    public static final Supplier<Block> RELAYER = regWithItem(RELAYER_NAME, () -> new RelayerBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER).isRedstoneConductor((s, l, p) -> false)
    ));

    //piston launcher base
    public static final Supplier<Block> SPRING_LAUNCHER = regWithItem(SPRING_LAUNCHER_NAME, () -> new SpringLauncherBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .pushReaction(PushReaction.BLOCK)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isSuffocating((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
                    .isViewBlocking((state, reader, pos) -> !state.getValue(SpringLauncherBlock.EXTENDED))
    ));

    public static final Supplier<Block> SPRING_LAUNCHER_HEAD = regBlock(PISTON_LAUNCHER_HEAD_NAME, () -> new SpringLauncherHeadBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .strength(4f, 5f)
                    .pushReaction(PushReaction.BLOCK)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noLootTable()
                    .jumpFactor(1.18f)
    ));
    public static final Supplier<Block> SPRING_LAUNCHER_ARM = regBlock(PISTON_LAUNCHER_ARM_NAME, () -> new SpringLauncherArmBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
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
            new SpeakerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK)
                    .strength(1f, 2f)
                    .sound(SoundType.WOOD)));

    public static final Supplier<BlockEntityType<SpeakerBlockTile>> SPEAKER_BLOCK_TILE = regTile(
            SPEAKER_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    SpeakerBlockTile::new, SPEAKER_BLOCK.get()));

    //turn table
    public static final Supplier<Block> TURN_TABLE = regWithItem(TURN_TABLE_NAME, () -> new TurnTableBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(0.75f, 2f)
    ));

    public static final Supplier<BlockEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = regTile(
            TURN_TABLE_NAME, () -> PlatHelper.newBlockEntityType(
                    TurnTableBlockTile::new, TURN_TABLE.get()));

    //illuminator
    public static final Supplier<Block> REDSTONE_ILLUMINATOR = regWithItem(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.SEA_LANTERN)
                    .isRedstoneConductor(NEVER)
                    .isValidSpawn((s, w, p, g) -> true)
                    .strength(0.3f, 0.3f)
    ));


    //pulley
    public static final Supplier<Block> PULLEY_BLOCK = regWithItem(PULLEY_BLOCK_NAME, () -> new PulleyBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)
    ));

    public static final Supplier<BlockEntityType<PulleyBlockTile>> PULLEY_BLOCK_TILE = regTile(
            PULLEY_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    PulleyBlockTile::new, PULLEY_BLOCK.get()));

    //lock block
    public static final Supplier<Block> LOCK_BLOCK = regWithItem(LOCK_BLOCK_NAME, () -> new LockBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .isRedstoneConductor((blockState, blockGetter, blockPos) -> false)
                    .sound(SoundType.METAL)
    ));

    //bellows
    public static final Supplier<Block> BELLOWS = regWithItem(BELLOWS_NAME, () -> new BellowsBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_BROWN)
                    .isViewBlocking(NEVER)
                    .forceSolidOn()
                    .isRedstoneConductor(NEVER)
                    .dynamicShape()
                    .strength(3f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<BellowsBlockTile>> BELLOWS_TILE = regTile(
            BELLOWS_NAME, () -> PlatHelper.newBlockEntityType(
                    BellowsBlockTile::new, BELLOWS.get()));

    //clock
    public static final Supplier<Block> CLOCK_BLOCK = regWithItem(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS)
                    .strength(3f, 6f)
                    .lightLevel((state) -> 1)
    ));

    public static final Supplier<BlockEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = regTile(
            CLOCK_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    ClockBlockTile::new, CLOCK_BLOCK.get()));

    //crystal display
    public static final Supplier<Block> CRYSTAL_DISPLAY = regWithItem(CRYSTAL_DISPLAY_NAME, () -> new CrystalDisplayBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .sound(SoundType.POLISHED_DEEPSLATE)
                    .strength(0.5f, 0.5f)
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
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<WindVaneBlockTile>> WIND_VANE_TILE = regTile(
            WIND_VANE_NAME, () -> PlatHelper.newBlockEntityType(
                    WindVaneBlockTile::new, WIND_VANE.get()));

    //faucet
    public static final Supplier<Block> FAUCET = regWithItem(FAUCET_NAME, () -> new FaucetBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .strength(3f, 4.8f)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<FaucetBlockTile>> FAUCET_TILE = regTile(
            FAUCET_NAME, () -> PlatHelper.newBlockEntityType(
                    FaucetBlockTile::new, FAUCET.get()));

    //gold door
    public static final Supplier<Block> GOLD_DOOR = regWithItem(GOLD_DOOR_NAME, () -> new GoldDoorBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK)
                    .noOcclusion()
    ));

    //gold trapdoor
    public static final Supplier<Block> GOLD_TRAPDOOR = regWithItem(GOLD_TRAPDOOR_NAME, () -> new GoldTrapdoorBlock(
            BlockBehaviour.Properties.ofFullCopy(GOLD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)
    ));

    //netherite doors
    public static final Supplier<Block> NETHERITE_DOOR = regBlock(NETHERITE_DOOR_NAME, () -> new NetheriteDoorBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK)
                    .noOcclusion()
    ));
    public static final Supplier<Item> NETHERITE_DOOR_ITEM = regItem(NETHERITE_DOOR_NAME, () -> new BlockItem(
            NETHERITE_DOOR.get(), new Item.Properties()
            .fireResistant()));

    //netherite trapdoor
    public static final Supplier<Block> NETHERITE_TRAPDOOR = regBlock(NETHERITE_TRAPDOOR_NAME, () -> new NetheriteTrapdoorBlock(
            BlockBehaviour.Properties.ofFullCopy(NETHERITE_DOOR.get())
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)
    ));
    public static final Supplier<Item> NETHERITE_TRAPDOOR_ITEM = regItem(NETHERITE_TRAPDOOR_NAME, () -> new BlockItem(
            NETHERITE_TRAPDOOR.get(), new Item.Properties()
            .fireResistant()));

    public static final Supplier<BlockEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = regTile(
            "key_lockable_tile", () -> PlatHelper.newBlockEntityType(
                    KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()));

    //gate bars
    public static final Supplier<Block> GOLD_BARS = regWithItem(GOLD_BARS_NAME, () -> new IronBarsBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                    .sound(SoundType.METAL)
    ));

    //iron gate
    public static final Supplier<Block> IRON_GATE = regWithItem(IRON_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS), false
    ));

    //gold gate
    public static final Supplier<Block> GOLD_GATE = regWithItem(GOLD_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS), true));

    //checker block
    public static final Supplier<Block> CHECKER_BLOCK = regWithItem(CHECKER_BLOCK_NAME, () -> new Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.QUARTZ)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F)
    ));

    //slab
    public static final Supplier<Block> CHECKER_SLAB = regWithItem(CHECKER_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.ofFullCopy(CHECKER_BLOCK.get())
    ));

    //pancakes
    public static final Supplier<Item> PANCAKE_ITEM = regItem(PANCAKE_NAME,
            () -> new PancakeItem(new Item.Properties()
                    .component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(
                            new EitherHolder<>(ModSounds.PANCAKE_MUSIC_JUKEBOX.getKey()), false))));

    // cant be block item so we use extra placement stuff later
    public static final Supplier<Block> PANCAKE = regBlock(PANCAKE_NAME, () -> new PancakeBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(0.5F)
                    .sound(SoundType.WOOL))
    );

    public static final Supplier<Item> PIRATE_DISC = regItem(PIRATE_DISC_NAME, () -> new Item(new Item.Properties()
            .component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(
                    new EitherHolder<>(ModSounds.PIRATE_MUSIC_JUKEBOX.getKey()), true))
            .stacksTo(1)
            .rarity(Rarity.RARE)));

    //flax
    public static final Supplier<Block> FLAX = regBlock(FLAX_NAME, () -> new FlaxBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
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
            BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS).offsetType(BlockBehaviour.OffsetType.NONE)
    ));

    //pot
    public static final Supplier<Block> FLAX_POT = regBlock("potted_flax", () -> PlatHelper.newFlowerPot(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT, FLAX, BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT)));

    //fodder
    public static final Supplier<Block> FODDER = regWithItem(FODDER_NAME, () -> new FodderBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)
                    .pushReaction(PushReaction.NORMAL)
    ));

    //flax block
    public static final Supplier<Block> FLAX_BLOCK = regWithItem(FLAX_BLOCK_NAME, () -> new FlaxBaleBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK)
                    .mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
    ));

    //boat in a jar
    public static final Supplier<Block> JAR_BOAT = regWithItem(JAR_BOAT_NAME, () -> new JarBoatBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
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
            BlockBehaviour.Properties.ofFullCopy(STRUCTURE_TEMP.get()).lightLevel((s) -> 14)));

    public static final Supplier<BlockEntityType<BlockGeneratorBlockTile>> BLOCK_GENERATOR_TILE = regTile(
            BLOCK_GENERATOR_NAME, () -> PlatHelper.newBlockEntityType(
                    BlockGeneratorBlockTile::new, BLOCK_GENERATOR.get()));

    //sticks
    public static final Supplier<Block> STICK_BLOCK = regBlock(STICK_NAME, () -> new StickBlock(
            BlockBehaviour.Properties.of()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
                    .mapColor(MapColor.NONE)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD)));

    //blaze rod
    //TODO: blaze sound
    public static final Supplier<Block> BLAZE_ROD_BLOCK = regBlock(BLAZE_ROD_NAME, () -> new BlazeRodBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.25F, 0F)
                    .lightLevel(state -> 12)
                    .emissiveRendering((p, w, s) -> true)
                    .sound(SoundType.GILDED_BLACKSTONE))
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
            BlockBehaviour.Properties.ofFullCopy(DAUB.get())
    ));
    //brace
    public static final RegSupplier<Block> DAUB_BRACE = regWithItem(DAUB_BRACE_NAME, () -> new FlippedBlock(
            BlockBehaviour.Properties.ofFullCopy(DAUB.get())
    ));

    //cross brace
    public static final RegSupplier<Block> DAUB_CROSS_BRACE = regWithItem(DAUB_CROSS_BRACE_NAME, () -> new Block(
            BlockBehaviour.Properties.ofFullCopy(DAUB.get())
    ));

    //timber frame
    public static final RegSupplier<FrameBlock> TIMBER_FRAME = regBlock(TIMBER_FRAME_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                .strength(0.1f, 0f)
                .noCollission().instabreak()
                .sound(SoundType.SCAFFOLDING); //.dynamicShape()
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_FRAME) :*/ new FrameBlock(p);
    });
    public static final Supplier<Item> TIMBER_FRAME_ITEM = regItem(TIMBER_FRAME_NAME, () -> new TimberFrameItem(
            TIMBER_FRAME.get(), new Item.Properties()));

    //timber brace
    public static final Supplier<FrameBraceBlock> TIMBER_BRACE = regBlock(TIMBER_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFrameBraceBlock(p, DAUB_BRACE) :*/ new FrameBraceBlock(p);
    });
    public static final Supplier<Item> TIMBER_BRACE_ITEM = regItem(TIMBER_BRACE_NAME, () -> new TimberFrameItem(
            TIMBER_BRACE.get(), new Item.Properties()));

    //timber cross brace
    public static final Supplier<FrameBlock> TIMBER_CROSS_BRACE = regBlock(TIMBER_CROSS_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_CROSS_BRACE) :*/ new FrameBlock(p);
    });
    public static final Supplier<Item> TIMBER_CROSS_BRACE_ITEM = regItem(TIMBER_CROSS_BRACE_NAME, () -> new TimberFrameItem(
            TIMBER_CROSS_BRACE.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<FrameBlockTile>> TIMBER_FRAME_TILE = regTile(
            TIMBER_FRAME_NAME, () -> PlatHelper.newBlockEntityType(
                    FrameBlockTile::new, FrameBlock.FRAMED_BLOCKS.toArray(Block[]::new)));

    //lapis bricks
    public static final Map<RegHelper.VariantType, Supplier<Block>> LAPIS_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(res(LAPIS_BRICKS_NAME), BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_BLOCK)
                    .sound(SoundType.DEEPSLATE_TILES).strength(2.0F, 2.0F));

    //ashen bricks
    public static final Map<RegHelper.VariantType, Supplier<Block>> ASH_BRICKS_BLOCKS =
            RegHelper.registerFullBlockSet(res(ASH_BRICKS_NAME), Blocks.STONE_BRICKS);

    // gravel bricks
    public static final Supplier<Block> GRAVEL_BRICKS = regWithItem(GRAVEL_BRICKS_NAME, () -> new GravelBricksBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                    .sound(SoundType.STONE)
    ));

    public static final Supplier<Block> SUS_GRAVEL_BRICKS = regWithItem(SUS_GRAVEL_BRICKS_NAME,
            () -> new SusGravelBricksBlock(
                    BlockBehaviour.Properties.ofFullCopy(GRAVEL_BRICKS.get())
            ));

    public static final Supplier<BlockEntityType<SusGravelBricksTile>> SUS_GRAVEL_BRICKS_TILE = regTile(SUS_GRAVEL_BRICKS_NAME, () ->
            PlatHelper.newBlockEntityType(SusGravelBricksTile::new, SUS_GRAVEL_BRICKS.get()));


    public static final Supplier<Block> SLIDY_BLOCK = regWithItem(SLIDY_BLOCK_NAME, () -> new SlidyBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF).sound(ModSounds.SLIDY_BLOCK)
    ));

    public static final Supplier<Block> MOVING_SLIDY_BLOCK = regBlock("moving_slidy_block", () -> new MovingSlidyBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.MOVING_PISTON)
    ));

    public static final Supplier<BlockEntityType<MovingSlidyBlockEntity>> MOVING_SLIDY_BLOCK_TILE = regTile("moving_slidy_block", () ->
            PlatHelper.newBlockEntityType(MovingSlidyBlockEntity::new, MOVING_SLIDY_BLOCK.get()));


    public static final Supplier<Block> MOVING_SLIDY_BLOCK_SOURCE = regBlock("moving_slidy_block_source", () -> new MovingSlidyBlockSource(
            BlockBehaviour.Properties.of().noCollission()
                    .noOcclusion().mapColor(MapColor.NONE)
                    .noLootTable().pushReaction(PushReaction.BLOCK))
    );

    //stone tile
    public static final Map<RegHelper.VariantType, Supplier<Block>> STONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(res(STONE_TILE_NAME), Blocks.STONE_BRICKS);

    //blackstone tile
    public static final Map<RegHelper.VariantType, Supplier<Block>> BLACKSTONE_TILE_BLOCKS =
            RegHelper.registerFullBlockSet(res(BLACKSTONE_TILE_NAME), Blocks.BLACKSTONE);

    //stone lamp
    public static final Supplier<Block> STONE_LAMP = regWithItem(STONE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ));

    //blackstone lamp
    public static final Supplier<Block> BLACKSTONE_LAMP = regWithItem(BLACKSTONE_LAMP_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)
    ));

    //deepslate lamp
    public static final Supplier<Block> DEEPSLATE_LAMP = regWithItem(DEEPSLATE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_BRICKS).lightLevel(s -> 15)
    ));

    //end_stone lamp
    public static final Supplier<Block> END_STONE_LAMP = regWithItem(END_STONE_LAMP_NAME, () -> new EndLampBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.END_STONE).lightLevel(s -> 15)
    ));

    //flower box
    public static final Supplier<Block> FLOWER_BOX = regWithItem(FLOWER_BOX_NAME, () -> {
        var p = BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.5F);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFlowerBox(p) : */new FlowerBoxBlock(p);
    });

    public static final Supplier<BlockEntityType<FlowerBoxBlockTile>> FLOWER_BOX_TILE = regTile(FLOWER_BOX_NAME, () ->
            PlatHelper.newBlockEntityType(FlowerBoxBlockTile::new, FLOWER_BOX.get()));

    //statue
    public static final Supplier<Block> STATUE = regWithItem(STATUE_NAME, () -> new StatueBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .strength(2)
    ));

    public static final Supplier<BlockEntityType<StatueBlockTile>> STATUE_TILE = regTile(
            STATUE_NAME, () -> PlatHelper.newBlockEntityType(
                    StatueBlockTile::new, STATUE.get()));

    //feather block
    public static final Supplier<Block> FEATHER_BLOCK = regWithItem(FEATHER_BLOCK_NAME, () -> new FeatherBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).strength(0.5f)
                    .dynamicShape()
                    .noCollission()
    ));

    //flint block
    public static final Supplier<Block> FLINT_BLOCK = regWithItem(FLINT_BLOCK_NAME, () -> new FlintBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK).strength(2F, 7.5F)
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
    public static final Supplier<Block> GUNPOWDER_BLOCK = regBlock(GUNPOWDER_BLOCK_NAME, () -> new GunpowderBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).sound(SoundType.SAND)));

    //fire pit
    public static final Supplier<Block> FIRE_PIT = regWithItem(FIRE_PIT_NAME, () -> new FirePitBlock(1,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).sound(SoundType.COPPER)));

    //placeable book
    public static final Supplier<Block> BOOK_PILE = regBlock(BOOK_PILE_NAME, () -> new BookPileBlock(
            BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.NONE)
                    .strength(0.5F)
                    .sound(ModSounds.BOOKS)));

    //placeable book
    public static final Supplier<Block> BOOK_PILE_H = regBlock(BOOK_PILE_H_NAME, () -> new BookPileHorizontalBlock(
            BlockBehaviour.Properties.ofFullCopy(BOOK_PILE.get())));

    public static final Supplier<BlockEntityType<BookPileBlockTile>> BOOK_PILE_TILE = regTile(
            BOOK_PILE_NAME, () -> PlatHelper.newBlockEntityType(
                    BookPileBlockTile::new, BOOK_PILE.get(), BOOK_PILE_H.get()));

    //cannon
    public static final Supplier<Block> CANNON = regWithItem(CANNON_NAME, () -> new CannonBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)
                    .isSuffocating(NEVER)
                    //.forceSolidOff()
                    .forceSolidOn()
                    .isRedstoneConductor(NEVER)
                    .isViewBlocking(NEVER)
                    .noOcclusion()
    ));

    public static final Supplier<BlockEntityType<CannonBlockTile>> CANNON_TILE = regTile(
            CANNON_NAME, () -> PlatHelper.newBlockEntityType(
                    CannonBlockTile::new, CANNON.get()));

    //cannonball
    public static final Supplier<Block> CANNONBALL = regBlock(CANNONBALL_NAME, () -> new CannonBallBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)
                    .strength(5, 6)
                    .sound(SoundType.COPPER)
                    .isSuffocating(NEVER)
                    .isRedstoneConductor(NEVER)
                    .isViewBlocking(NEVER)
                    .noOcclusion()
    ));

    public static final Supplier<Item> CANNONBALL_ITEM = regItem(CANNONBALL_NAME, () ->
            new CannonBallItem(CANNONBALL.get(), new Item.Properties()));

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
            new ColorRGBA(0x9a9090),
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
    public static final Supplier<Item> BUBBLE_BLOCK_ITEM = regItem(BUBBLE_BLOCK_NAME, () -> new BlockItem(
            BUBBLE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<BubbleBlockTile>> BUBBLE_BLOCK_TILE = regTile(
            BUBBLE_BLOCK_NAME, () -> PlatHelper.newBlockEntityType(
                    BubbleBlockTile::new, BUBBLE_BLOCK.get()));


    //enderman skull
    public static final Supplier<EndermanSkullBlock> ENDERMAN_SKULL_BLOCK = regBlock(ENDERMAN_HEAD_NAME, () ->
            new EndermanSkullBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WITHER_SKELETON_SKULL)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<EndermanSkullWallBlock> ENDERMAN_SKULL_BLOCK_WALL = regBlock("enderman_wall_head", () ->
            new EndermanSkullWallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WITHER_SKELETON_SKULL)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<Item> ENDERMAN_SKULL_ITEM = regItem(ENDERMAN_HEAD_NAME, () ->
            new EndermanHeadItem(ENDERMAN_SKULL_BLOCK.get(), ENDERMAN_SKULL_BLOCK_WALL.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final Supplier<BlockEntityType<EndermanSkullBlockTile>> ENDERMAN_SKULL_TILE = regTile(
            ENDERMAN_HEAD_NAME, () -> PlatHelper.newBlockEntityType(
                    EndermanSkullBlockTile::new, ENDERMAN_SKULL_BLOCK.get(), ENDERMAN_SKULL_BLOCK_WALL.get()));

    //spider head

    public static final Supplier<SkullBlock> SPIDER_SKULL_BLOCK = regBlock(SPIDER_HEAD_NAME, () ->
            new SpiderSkullBlock( BlockBehaviour.Properties.ofFullCopy(Blocks.CREEPER_HEAD)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<WallSkullBlock> SPIDER_SKULL_BLOCK_WALL = regBlock("spider_wall_head", () ->
            new SpiderWallSkullBlock( BlockBehaviour.Properties.ofFullCopy(Blocks.CREEPER_HEAD)
                    .instrument(NoteBlockInstrument.CUSTOM_HEAD))
    );
    public static final Supplier<Item> SPIDER_SKULL_ITEM = regItem(SPIDER_HEAD_NAME, () ->
            new StandingAndWallBlockItem(SPIDER_SKULL_BLOCK.get(), SPIDER_SKULL_BLOCK_WALL.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON), Direction.UP));

    public static final Supplier<BlockEntityType<SkullBlockEntity>> SPIDER_SKULL_TILE = regTile(
            SPIDER_HEAD_NAME, () -> PlatHelper.newBlockEntityType(
                    SkullBlockEntity::new, SPIDER_SKULL_BLOCK.get(), SPIDER_SKULL_BLOCK_WALL.get()));


    //ash basalt
    public static final Supplier<Block> ASHEN_BASALT = regBlock("ashen_basalt", () ->
            new AshenBasaltBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT))
    );

    //hat stand
    public static final Supplier<Item> HAT_STAND = regItem(HAT_STAND_NAME, () -> new HatStandItem(new Item.Properties()));

}
