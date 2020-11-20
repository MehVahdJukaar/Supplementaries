package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.*;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.renderers.FireflyJarItemRenderer;
import net.mehvahdjukaar.supplementaries.renderers.JarItemRenderer;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Registry {

    @SubscribeEvent
    public static void registerSpawnEggs(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(FIREFLY_SPAWN_EGG_ITEM);
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event){
        event.getRegistry().register(FIREFLY_TYPE);
        GlobalEntityTypeAttributes.put(Registry.FIREFLY_TYPE, FireflyEntity.setCustomAttributes().create());
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Supplementaries.MOD_ID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Supplementaries.MOD_ID);
    //public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Supplementaries.MOD_ID);


    public static void init(){
        //ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    //entities
    public static final String FIREFLY_NAME = "firefly";
    public static final EntityType FIREFLY_TYPE = (EntityType.Builder.create(FireflyEntity::new, EntityClassification.AMBIENT)
            .setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)
            .size(0.3125f, 1f))
            .build(FIREFLY_NAME)
            .setRegistryName(FIREFLY_NAME);
    public static final Item FIREFLY_SPAWN_EGG_ITEM = new SpawnEggItem(FIREFLY_TYPE, -4784384, -16777216,
            new Item.Properties().group(ItemGroup.MISC)).setRegistryName(FIREFLY_NAME);


    //particles
    public static final RegistryObject<BasicParticleType> FIREFLY_GLOW = PARTICLES.register("firefly_glow", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> SPEAKER_SOUND = PARTICLES.register("speaker_sound", () -> new BasicParticleType(true));

    //blocks

    //planter
    public static final String PLANTER_NAME = "planter";
    public static final RegistryObject<Block> PLANTER = BLOCKS.register(PLANTER_NAME, () -> new PlanterBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<Item> PLANTER_ITEM = ITEMS.register(PLANTER_NAME, () -> new BlockItem(PLANTER.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //clock
    public static final String CLOCK_BLOCK_NAME = "clock_block";
    public static final RegistryObject<Block> CLOCK_BLOCK = BLOCKS.register(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 6f)
                    .harvestLevel(0)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
    ));
    public static final RegistryObject<TileEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = TILES.register(CLOCK_BLOCK_NAME,
            () -> TileEntityType.Builder.create(ClockBlockTile::new, CLOCK_BLOCK.get()).build(null));

    public static final RegistryObject<Item> CLOCK_BLOCK_ITEM = ITEMS.register(CLOCK_BLOCK_NAME, () -> new BlockItem(CLOCK_BLOCK.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //pedestal
    public static final String PEDESTAL_NAME = "pedestal";
    public static final RegistryObject<Block> PEDESTAL = BLOCKS.register(PEDESTAL_NAME, () -> new PedestalBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<PedestalBlockTile>> PEDESTAL_TILE = TILES.register(PEDESTAL_NAME,
            () -> TileEntityType.Builder.create(PedestalBlockTile::new, PEDESTAL.get()).build(null));

    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register(PEDESTAL_NAME, () -> new BlockItem(PEDESTAL.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //wind vane
    public static final String WIND_VANE_NAME = "wind_vane";
    public static final RegistryObject<Block> WIND_VANE = BLOCKS.register(WIND_VANE_NAME, () -> new WindVaneBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(5f, 6f)
                    .harvestLevel(2)
                    .setRequiresTool()
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<WindVaneBlockTile>> WIND_VANE_TILE = TILES.register(WIND_VANE_NAME,
            () -> TileEntityType.Builder.create(WindVaneBlockTile::new, WIND_VANE.get()).build(null));

    public static final RegistryObject<Item> WIND_VANE_ITEM = ITEMS.register(WIND_VANE_NAME, () -> new BlockItem(WIND_VANE.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //illuminator
    public static final String REDSTONE_ILLUMINATOR_NAME = "redstone_illuminator";
    public static final RegistryObject<Block> REDSTONE_ILLUMINATOR = BLOCKS.register(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            AbstractBlock.Properties.create(Material.REDSTONE_LIGHT, MaterialColor.QUARTZ)
                    .hardnessAndResistance(0.3f, 0.3f)
                    .sound(SoundType.GLASS)
                    .setLightLevel((state) -> 15)
    ));
    public static final RegistryObject<Item> REDSTONE_ILLUMINATOR_ITEM = ITEMS.register(REDSTONE_ILLUMINATOR_NAME, () -> new BlockItem(REDSTONE_ILLUMINATOR.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //notice board
    public static final String NOTICE_BOARD_NAME = "notice_board";
    public static final RegistryObject<Block> NOTICE_BOARD = BLOCKS.register(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)

    ));
    public static final RegistryObject<TileEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = TILES.register(NOTICE_BOARD_NAME,
            () -> TileEntityType.Builder.create(NoticeBoardBlockTile::new, NOTICE_BOARD.get()).build(null));

    public static final RegistryObject<Item> NOTICE_BOARD_ITEM = ITEMS.register(NOTICE_BOARD_NAME, () -> new BlockItem(NOTICE_BOARD.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    public static final RegistryObject<ContainerType<NoticeBoardContainer>> NOTICE_BOARD_CONTAINER = CONTAINERS.register(NOTICE_BOARD_NAME, () -> IForgeContainerType.create(NoticeBoardContainer::new));

    //crank
    public static final String CRANK_NAME = "crank";
    public static final RegistryObject<Block> CRANK = BLOCKS.register(CRANK_NAME, () -> new CrankBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(0.6f, 0.6f)
                    .harvestTool(ToolType.PICKAXE)
                    .doesNotBlockMovement()
                    .notSolid()
    ));
    public static final RegistryObject<Item> CRANK_ITEM = ITEMS.register(CRANK_NAME, () -> new BlockItem(CRANK.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //jar
    public static final String JAR_NAME = "jar";
    public static final RegistryObject<Block> JAR = BLOCKS.register(JAR_NAME, () -> new JarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<JarBlockTile>> JAR_TILE = TILES.register(JAR_NAME,
            () -> TileEntityType.Builder.create(JarBlockTile::new, JAR.get()).build(null));

    public static final RegistryObject<Item> JAR_ITEM = ITEMS.register(JAR_NAME, () -> new BlockItem(JAR.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(16).setISTER(()-> JarItemRenderer::new)));

    //faucet
    public static final String FAUCET_NAME = "faucet";
    public static final RegistryObject<Block> FAUCET = BLOCKS.register(FAUCET_NAME, () -> new FaucetBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3f, 4.8f)
                    .harvestLevel(2)
                    .setRequiresTool()
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<FaucetBlockTile>> FAUCET_TILE = TILES.register(FAUCET_NAME,
            () -> TileEntityType.Builder.create(FaucetBlockTile::new, FAUCET.get()).build(null));

    public static final RegistryObject<Item> FAUCET_ITEM = ITEMS.register(FAUCET_NAME, () -> new BlockItem(FAUCET.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //turn table
    public static final String TURN_TABLE_NAME = "turn_table";
    public static final RegistryObject<Block> TURN_TABLE = BLOCKS.register(TURN_TABLE_NAME, () -> new TurnTableBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(0.75f, 2f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(0)
                    .setRequiresTool()
    ));
    public static final RegistryObject<TileEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = TILES.register(TURN_TABLE_NAME,
            () -> TileEntityType.Builder.create(TurnTableBlockTile::new, TURN_TABLE.get()).build(null));

    public static final RegistryObject<Item> TURN_TABLE_ITEM = ITEMS.register(TURN_TABLE_NAME, () -> new BlockItem(TURN_TABLE.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //piston launcher base
    static AbstractBlock.IPositionPredicate abstractblock$ipositionpredicate = (state, reader, pos) -> {
        return !state.get(PistonBlock.EXTENDED);
    };

    public static final String PISTON_LAUNCHER_NAME = "piston_launcher";
    public static final RegistryObject<Block> PISTON_LAUNCHER = BLOCKS.register(PISTON_LAUNCHER_NAME, () -> new PistonLauncherBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .setOpaque((BlockState state, IBlockReader reader, BlockPos pos)->false)
                    .setSuffocates(abstractblock$ipositionpredicate)
                    .setBlocksVision(abstractblock$ipositionpredicate)

    ));
    public static final RegistryObject<Item> PISTON_LAUNCHER_ITEM = ITEMS.register(PISTON_LAUNCHER_NAME, () -> new BlockItem(PISTON_LAUNCHER.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    public static final String PISTON_LAUNCHER_HEAD_NAME = "piston_launcher_head";
    public static final RegistryObject<Block> PISTON_LAUNCHER_HEAD = BLOCKS.register(PISTON_LAUNCHER_HEAD_NAME, () -> new PistonLauncherHeadBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .noDrops()
                    .jumpFactor(1.18f)
    ));
    public static final String PISTON_LAUNCHER_ARM_NAME = "piston_launcher_arm";
    public static final RegistryObject<Block> PISTON_LAUNCHER_ARM = BLOCKS.register(PISTON_LAUNCHER_ARM_NAME, () -> new PistonLauncherArmBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
                    .noDrops()
    ));
    public static final RegistryObject<TileEntityType<PistonLauncherArmBlockTile>> PISTON_LAUNCHER_ARM_TILE = TILES.register(PISTON_LAUNCHER_ARM_NAME,
            () -> TileEntityType.Builder.create(PistonLauncherArmBlockTile::new, PISTON_LAUNCHER_ARM.get()).build(null));

    //speaker Block
    public static final String SPEAKER_BLOCK_NAME = "speaker_block";
    public static final RegistryObject<Block> SPEAKER_BLOCK = BLOCKS.register(SPEAKER_BLOCK_NAME, () -> new SpeakerBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(1f, 2f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
    ));
    public static final RegistryObject<TileEntityType<SpeakerBlockTile>> SPEAKER_BLOCK_TILE = TILES.register(SPEAKER_BLOCK_NAME,
            () -> TileEntityType.Builder.create(SpeakerBlockTile::new, SPEAKER_BLOCK.get()).build(null));

    public static final RegistryObject<Item> SPEAKER_BLOCK_ITEM = ITEMS.register(SPEAKER_BLOCK_NAME, () -> new BlockItem(SPEAKER_BLOCK.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //sign post
    public static final String SIGN_POST_NAME = "sign_post";
    public static final RegistryObject<Block> SIGN_POST = BLOCKS.register(SIGN_POST_NAME, () -> new SignPostBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<SignPostBlockTile>> SIGN_POST_TILE = TILES.register(SIGN_POST_NAME,
            () -> TileEntityType.Builder.create(SignPostBlockTile::new, SIGN_POST.get()).build(null));

    //items
    //oak
    public static final String SIGN_POST_NAME_OAK = "sign_post_oak";
    public static final RegistryObject<Item> SIGN_POST_ITEM_OAK = ITEMS.register(SIGN_POST_NAME_OAK, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //birch
    public static final String SIGN_POST_NAME_BIRCH = "sign_post_birch";
    public static final RegistryObject<Item> SIGN_POST_ITEM_BIRCH = ITEMS.register(SIGN_POST_NAME_BIRCH, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //spruce
    public static final String SIGN_POST_NAME_SPRUCE = "sign_post_spruce";
    public static final RegistryObject<Item> SIGN_POST_ITEM_SPRUCE = ITEMS.register(SIGN_POST_NAME_SPRUCE, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //jungle
    public static final String SIGN_POST_NAME_JUNGLE = "sign_post_jungle";
    public static final RegistryObject<Item> SIGN_POST_ITEM_JUNGLE = ITEMS.register(SIGN_POST_NAME_JUNGLE, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //acacia
    public static final String SIGN_POST_NAME_ACACIA = "sign_post_acacia";
    public static final RegistryObject<Item> SIGN_POST_ITEM_ACACIA = ITEMS.register(SIGN_POST_NAME_ACACIA, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //dark oak
    public static final String SIGN_POST_NAME_DARK_OAK = "sign_post_dark_oak";
    public static final RegistryObject<Item> SIGN_POST_ITEM__DARK_OAK = ITEMS.register(SIGN_POST_NAME_DARK_OAK, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //crimson
    public static final String SIGN_POST_NAME_CRIMSON = "sign_post_crimson";
    public static final RegistryObject<Item> SIGN_POST_ITEM_CRIMSON = ITEMS.register(SIGN_POST_NAME_CRIMSON, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));
    //warped
    public static final String SIGN_POST_NAME_WARPED = "sign_post_warped";
    public static final RegistryObject<Item> SIGN_POST_ITEM_WARPED = ITEMS.register(SIGN_POST_NAME_WARPED, () -> new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)));

    //hanging sing
    //oak

    public static final String HANGING_SIGN_NAME_OAK = "hanging_sign_oak";
    public static final RegistryObject<Block> HANGING_SIGN_OAK = BLOCKS.register(HANGING_SIGN_NAME_OAK, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.OAK_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_OAK = ITEMS.register(HANGING_SIGN_NAME_OAK, () -> new BlockItem(HANGING_SIGN_OAK.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //birch
    public static final String HANGING_SIGN_NAME_BIRCH = "hanging_sign_birch";
    public static final RegistryObject<Block> HANGING_SIGN_BIRCH = BLOCKS.register(HANGING_SIGN_NAME_BIRCH, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.BIRCH_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_BIRCH = ITEMS.register(HANGING_SIGN_NAME_BIRCH, () -> new BlockItem(HANGING_SIGN_BIRCH.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));


    //spruce
    public static final String HANGING_SIGN_NAME_SPRUCE = "hanging_sign_spruce";
    public static final RegistryObject<Block> HANGING_SIGN_SPRUCE = BLOCKS.register(HANGING_SIGN_NAME_SPRUCE, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.SPRUCE_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_SPRUCE = ITEMS.register(HANGING_SIGN_NAME_SPRUCE, () -> new BlockItem(HANGING_SIGN_SPRUCE.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS)));

    //jungle
    public static final String HANGING_SIGN_NAME_JUNGLE = "hanging_sign_jungle";
    public static final RegistryObject<Block> HANGING_SIGN_JUNGLE = BLOCKS.register(HANGING_SIGN_NAME_JUNGLE, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.JUNGLE_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_JUNGLE = ITEMS.register(HANGING_SIGN_NAME_JUNGLE, () -> new BlockItem(HANGING_SIGN_JUNGLE.get(),
          new Item.Properties().group(ItemGroup.DECORATIONS)));

    //acacia
    public static final String HANGING_SIGN_NAME_ACACIA = "hanging_sign_acacia";
    public static final RegistryObject<Block> HANGING_SIGN_ACACIA = BLOCKS.register(HANGING_SIGN_NAME_ACACIA, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.ACACIA_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_ACACIA = ITEMS.register(HANGING_SIGN_NAME_ACACIA, () -> new BlockItem(HANGING_SIGN_ACACIA.get(),
          new Item.Properties().group(ItemGroup.DECORATIONS)));

    //dark oak
    public static final String HANGING_SIGN_NAME_DARK_OAK = "hanging_sign_dark_oak";
    public static final RegistryObject<Block> HANGING_SIGN_DARK_OAK = BLOCKS.register(HANGING_SIGN_NAME_DARK_OAK, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.DARK_OAK_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_DARK_OAK = ITEMS.register(HANGING_SIGN_NAME_DARK_OAK, () -> new BlockItem(HANGING_SIGN_DARK_OAK.get(),
          new Item.Properties().group(ItemGroup.DECORATIONS)));

    //crimson
    public static final String HANGING_SIGN_NAME_CRIMSON = "hanging_sign_crimson";
    public static final RegistryObject<Block> HANGING_SIGN_CRIMSON = BLOCKS.register(HANGING_SIGN_NAME_CRIMSON, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.CRIMSON_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_CRIMSON = ITEMS.register(HANGING_SIGN_NAME_CRIMSON, () -> new BlockItem(HANGING_SIGN_CRIMSON.get(),
          new Item.Properties().group(ItemGroup.DECORATIONS)));

    //warped
    public static final String HANGING_SIGN_NAME_WARPED = "hanging_sign_warped";
    public static final RegistryObject<Block> HANGING_SIGN_WARPED = BLOCKS.register(HANGING_SIGN_NAME_WARPED, () -> new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.CRIMSON_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM_WARPED = ITEMS.register(HANGING_SIGN_NAME_WARPED, () -> new BlockItem(HANGING_SIGN_WARPED.get(),
          new Item.Properties().group(ItemGroup.DECORATIONS)));





    //tile
    public static final RegistryObject<TileEntityType<HangingSignBlockTile>> HANGING_SIGN_TILE = TILES.register("hanging_sign",
            () -> TileEntityType.Builder.create(HangingSignBlockTile::new,
                    HANGING_SIGN_OAK.get(), HANGING_SIGN_BIRCH.get(), HANGING_SIGN_SPRUCE.get(),HANGING_SIGN_JUNGLE.get(),
                    HANGING_SIGN_ACACIA.get(),HANGING_SIGN_DARK_OAK.get(),HANGING_SIGN_CRIMSON.get(),HANGING_SIGN_WARPED.get()
                    ).build(null));

    //wall lantern
    public static final String WALL_LANTERN_NAME = "wall_lantern";
    public static final RegistryObject<Block> WALL_LANTERN = BLOCKS.register(WALL_LANTERN_NAME, () -> new WallLanternBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3.5f, 3.5f)
                    .sound(SoundType.LANTERN)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .harvestLevel(1)
                    .setLightLevel((state) -> 15)
                    .notSolid()
                    .noDrops()
    ));
    public static final RegistryObject<TileEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = TILES.register(WALL_LANTERN_NAME,
            () -> TileEntityType.Builder.create(WallLanternBlockTile::new, WALL_LANTERN.get()).build(null));
    public static final RegistryObject<Item> WALL_LANTERN_ITEM = ITEMS.register(WALL_LANTERN_NAME, () -> new BlockItem(WALL_LANTERN.get(),
            new Item.Properties().group(null)));

    //bellows
    public static final String BELLOWS_NAME = "bellows";
    public static final RegistryObject<Block> BELLOWS = BLOCKS.register(BELLOWS_NAME, () -> new BellowsBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<BellowsBlockTile>> BELLOWS_TILE = TILES.register(BELLOWS_NAME,
            () -> TileEntityType.Builder.create(BellowsBlockTile::new, BELLOWS.get()).build(null));
    public static final RegistryObject<Item> BELLOWS_ITEM = ITEMS.register(BELLOWS_NAME, () -> new BlockItem(BELLOWS.get(),
            new Item.Properties().group(ItemGroup.REDSTONE)));

    //laser
    public static final String LASER_NAME = "laser_block";
    public static final RegistryObject<Block> LASER_BLOCK = BLOCKS.register(LASER_NAME, () -> new LaserBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(3.5f, 3.5f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<LaserBlockTile>> LASER_BLOCK_TILE = TILES.register(LASER_NAME,
            () -> TileEntityType.Builder.create(LaserBlockTile::new, LASER_BLOCK.get()).build(null));
    public static final RegistryObject<Item> LASER_BLOCK_ITEM = ITEMS.register(LASER_NAME, () -> new BlockItem(LASER_BLOCK.get(),
            new Item.Properties().group(null)));

    //clover
    /*
    public static final RegistryObject<Block> CLOVER_BLOCK = BLOCKS.register("clover", () -> new FlowerBlock(
            Effects.LUCK, 0, Block.Properties.create(Material.PLANTS).doesNotBlockMovement().sound(SoundType.PLANT)
            .hardnessAndResistance(0f, 0f)
    ));*/

    //flag
    public static final String FLAG_NAME = "flag";
    public static final RegistryObject<Block> FLAG = BLOCKS.register(FLAG_NAME, () -> new FlagBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<FlagBlockTile>> FLAG_TILE = TILES.register(FLAG_NAME,
            () -> TileEntityType.Builder.create(FlagBlockTile::new, FLAG.get()).build(null));
    public static final RegistryObject<Item> FLAG_ITEM = ITEMS.register(FLAG_NAME, () -> new BlockItem(FLAG.get(),
            new Item.Properties().group(null)));


    //drawers
    /*
    public static final String DRAWERS_NAME = "drawers";
    public static final RegistryObject<Block> DRAWERS = BLOCKS.register(DRAWERS_NAME, () -> new DrawersBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                    .hardnessAndResistance(2f, 2f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<DrawersBlockTile>> DRAWERS_TILE = TILES.register(DRAWERS_NAME,
            () -> TileEntityType.Builder.create(DrawersBlockTile::new, DRAWERS.get()).build(null));
    public static final RegistryObject<Item> DRAWERS_ITEM = ITEMS.register(DRAWERS_NAME, () -> new BlockItem(DRAWERS.get(),
            new Item.Properties().group(null)));
    */
    //sconce
    private static boolean needsPostProcessing(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    //normal
    public static final String SCONCE_NAME = "sconce";
    public static final RegistryObject<Block> SCONCE = BLOCKS.register(SCONCE_NAME, ()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .sound(SoundType.LANTERN), ParticleTypes.FLAME));
    public static final RegistryObject<Block> SCONCE_WALL = BLOCKS.register("sconce_wall", ()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .lootFrom(SCONCE.get())
                    .sound(SoundType.LANTERN), ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM = ITEMS.register(SCONCE_NAME, () ->new WallOrFloorItem(SCONCE.get(), SCONCE_WALL.get(), (new Item.Properties()).group(ItemGroup.DECORATIONS)));
    //soul
    public static final String SCONCE_NAME_SOUL = "sconce_soul";
    public static final RegistryObject<Block> SCONCE_SOUL = BLOCKS.register(SCONCE_NAME_SOUL, ()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .sound(SoundType.LANTERN), ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_SOUL = BLOCKS.register("sconce_wall_soul", ()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .lootFrom(SCONCE_SOUL.get())
                    .sound(SoundType.LANTERN), ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_SOUL = ITEMS.register(SCONCE_NAME_SOUL, () ->new WallOrFloorItem(SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(), (new Item.Properties()).group(ItemGroup.DECORATIONS)));

    //firefly & jar
    public static final String FIREFLY_JAR_NAME = "firefly_jar";
    public static final RegistryObject<Block> FIREFLY_JAR = BLOCKS.register(FIREFLY_JAR_NAME, () -> new FireflyJarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.GRASS)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
                    .setLightLevel((state) -> 8)
    ));
    public static final RegistryObject<TileEntityType<FireflyJarBlockTile>> FIREFLY_JAR_TILE = TILES.register(FIREFLY_JAR_NAME,
            () -> TileEntityType.Builder.create(FireflyJarBlockTile::new, FIREFLY_JAR.get()).build(null));
    public static final RegistryObject<Item> FIREFLY_JAR_ITEM = ITEMS.register(FIREFLY_JAR_NAME, () -> new BlockItem(FIREFLY_JAR.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(16).setISTER(()-> FireflyJarItemRenderer::new)));

    //candelabra
    public static final String CANDELABRA_NAME = "candelabra";
    public static final RegistryObject<Block> CANDELABRA = BLOCKS.register(CANDELABRA_NAME, () -> new CandelabraBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.GRASS)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
                    .setLightLevel((state) -> 14)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM = ITEMS.register(CANDELABRA_NAME, () -> new BlockItem(CANDELABRA.get(),
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(16)));


}
