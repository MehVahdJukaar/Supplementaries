package net.mehvahdjukaar.supplementaries.setup;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.blocks.*;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.items.EmptyJarItem;
import net.mehvahdjukaar.supplementaries.items.JarItem;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.renderers.FireflyJarItemRenderer;
import net.mehvahdjukaar.supplementaries.renderers.JarItemRenderer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Registry {

    private static final List<Block> BLOCKS = Lists.newArrayList();
    private static final List<TileEntityType<?>> TILES = Lists.newArrayList();
    private static final List<Item> ITEMS = Lists.newArrayList();
    private static final List<ContainerType<?>> CONTAINERS = Lists.newArrayList();
    private static final List<ParticleType<?>> PARTICLES = Lists.newArrayList();
    private static final List<EntityType<?>> ENTITIES = Lists.newArrayList();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block n : BLOCKS)
            event.getRegistry().register(n);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        for (TileEntityType<?> n : TILES)
            event.getRegistry().register(n);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (Item n : ITEMS)
            event.getRegistry().register(n);
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
        for (ContainerType<?> n : CONTAINERS)
            event.getRegistry().register(n);
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event){
        for (EntityType<?> n : ENTITIES)
            event.getRegistry().register(n);
        GlobalEntityTypeAttributes.put((EntityType<? extends LivingEntity>) FIREFLY_TYPE, FireflyEntity.setCustomAttributes().create());
    }

    @SubscribeEvent
    public static void registerParticles(final RegistryEvent.Register<ParticleType<?>> event){
        for (ParticleType<?> n : PARTICLES)
            event.getRegistry().register(n);
    }


    public static void init(){
        //firefly
        if(true) {
            ENTITIES.add(FIREFLY_TYPE);
            ITEMS.add(FIREFLY_SPAWN_EGG_ITEM);
        }
        //planter
        if(true) {
            BLOCKS.add(PLANTER);
            ITEMS.add(PLANTER_ITEM);
        }
        //clock
        if(true) {
            BLOCKS.add(CLOCK_BLOCK);
            TILES.add(CLOCK_BLOCK_TILE);
            ITEMS.add(CLOCK_BLOCK_ITEM);
        }
        //pedestal
        if(true) {
            BLOCKS.add(PEDESTAL);
            TILES.add(PEDESTAL_TILE);
            ITEMS.add(PEDESTAL_ITEM);
        }
        //wind vane
        if(true) {
            BLOCKS.add(WIND_VANE);
            TILES.add(WIND_VANE_TILE);
            ITEMS.add(WIND_VANE_ITEM);
        }
        //illuminator
        if(true) {
            BLOCKS.add(REDSTONE_ILLUMINATOR);
            ITEMS.add(REDSTONE_ILLUMINATOR_ITEM);
        }
        //notice board
        if(true) {
            BLOCKS.add(NOTICE_BOARD);
            TILES.add(NOTICE_BOARD_TILE);
            ITEMS.add(NOTICE_BOARD_ITEM);
            CONTAINERS.add(NOTICE_BOARD_CONTAINER);
        }
        //crank
        if(true) {
            BLOCKS.add(CRANK);
            ITEMS.add(CRANK_ITEM);
        }
        //jar
        if(true) {
            BLOCKS.add(JAR);
            TILES.add(JAR_TILE);
            ITEMS.add(JAR_ITEM);
            ITEMS.add(EMPTY_JAR_ITEM);

            BLOCKS.add(JAR_TINTED);
            ITEMS.add(JAR_ITEM_TINTED);
            ITEMS.add(EMPTY_JAR_ITEM_TINTED);
        }
        //faucet
        if(true) {
            BLOCKS.add(FAUCET);
            TILES.add(FAUCET_TILE);
            ITEMS.add(FAUCET_ITEM);
        }
        //turn table
        if(true) {
            BLOCKS.add(TURN_TABLE);
            TILES.add(TURN_TABLE_TILE);
            ITEMS.add(TURN_TABLE_ITEM);
        }
        //piston launcher
        if(true) {
            BLOCKS.add(PISTON_LAUNCHER);
            BLOCKS.add(PISTON_LAUNCHER_ARM);
            BLOCKS.add(PISTON_LAUNCHER_HEAD);
            TILES.add(PISTON_LAUNCHER_ARM_TILE);
            ITEMS.add(PISTON_LAUNCHER_ITEM);
        }
        //speaker block
        if(true) {
            PARTICLES.add(SPEAKER_SOUND);
            BLOCKS.add(SPEAKER_BLOCK);
            TILES.add(SPEAKER_BLOCK_TILE);
            ITEMS.add(SPEAKER_BLOCK_ITEM);
        }
        //sign post
        if(true) {
            BLOCKS.add(SIGN_POST);
            TILES.add(SIGN_POST_TILE);
            ITEMS.add(SIGN_POST_ITEM_OAK);
            ITEMS.add(SIGN_POST_ITEM_SPRUCE);
            ITEMS.add(SIGN_POST_ITEM_BIRCH);
            ITEMS.add(SIGN_POST_ITEM_JUNGLE);
            ITEMS.add(SIGN_POST_ITEM_ACACIA);
            ITEMS.add(SIGN_POST_ITEM_DARK_OAK);
            ITEMS.add(SIGN_POST_ITEM_CRIMSON);
            ITEMS.add(SIGN_POST_ITEM_WARPED);
        }
        //hanging sign
        if(true) {
            BLOCKS.add(HANGING_SIGN_OAK);
            BLOCKS.add(HANGING_SIGN_SPRUCE);
            BLOCKS.add(HANGING_SIGN_BIRCH);
            BLOCKS.add(HANGING_SIGN_JUNGLE);
            BLOCKS.add(HANGING_SIGN_ACACIA);
            BLOCKS.add(HANGING_SIGN_DARK_OAK);
            BLOCKS.add(HANGING_SIGN_CRIMSON);
            BLOCKS.add(HANGING_SIGN_WARPED);
            TILES.add(HANGING_SIGN_TILE);
            ITEMS.add(HANGING_SIGN_ITEM_OAK);
            ITEMS.add(HANGING_SIGN_ITEM_SPRUCE);
            ITEMS.add(HANGING_SIGN_ITEM_BIRCH);
            ITEMS.add(HANGING_SIGN_ITEM_JUNGLE);
            ITEMS.add(HANGING_SIGN_ITEM_ACACIA);
            ITEMS.add(HANGING_SIGN_ITEM_DARK_OAK);
            ITEMS.add(HANGING_SIGN_ITEM_CRIMSON);
            ITEMS.add(HANGING_SIGN_ITEM_WARPED);
        }
        //wall lantern
        if(true) {
            BLOCKS.add(WALL_LANTERN);
            TILES.add(WALL_LANTERN_TILE);
            ITEMS.add(WALL_LANTERN_ITEM);
        }
        //bellows
        if(true) {
            BLOCKS.add(BELLOWS);
            TILES.add(BELLOWS_TILE);
            ITEMS.add(BELLOWS_ITEM);
        }
        //sconce
        if(true) { //true
            BLOCKS.add(SCONCE);
            BLOCKS.add(SCONCE_WALL);
            ITEMS.add(SCONCE_ITEM);

            BLOCKS.add(SCONCE_SOUL);
            BLOCKS.add(SCONCE_WALL_SOUL);
            ITEMS.add(SCONCE_ITEM_SOUL);

            PARTICLES.add(ENDERGETIC_FLAME);
            BLOCKS.add(SCONCE_ENDER);
            BLOCKS.add(SCONCE_WALL_ENDER);
            ITEMS.add(SCONCE_ITEM_ENDER);

            PARTICLES.add(GREEN_FLAME);
            BLOCKS.add(SCONCE_GREEN);
            BLOCKS.add(SCONCE_WALL_GREEN);
            ITEMS.add(SCONCE_ITEM_GREEN);
        }
        //candelabra
        if(true) {
            BLOCKS.add(CANDELABRA);
            ITEMS.add(CANDELABRA_ITEM);

            BLOCKS.add(CANDELABRA_SILVER);
            ITEMS.add(CANDELABRA_ITEM_SILVER);
        }
        //firefly jar
        if(true) {
            PARTICLES.add(FIREFLY_GLOW);
            BLOCKS.add(FIREFLY_JAR);
            TILES.add(FIREFLY_JAR_TILE);
            ITEMS.add(FIREFLY_JAR_ITEM);
        }
        if(true) {
            BLOCKS.add(ITEM_SHELF);
            TILES.add(ITEM_SHELF_TILE);
            ITEMS.add(ITEM_SHELF_ITEM);
        }


        //cog block
        if(true){
            BLOCKS.add(COG_BLOCK);
            ITEMS.add(COG_BLOCK_ITEM);
        }

        //flag
        if(true) {
            BLOCKS.add(FLAG);
            TILES.add(FLAG_TILE);
            ITEMS.add(FLAG_ITEM);
        }
        //laser
        if(true) {
            BLOCKS.add(LASER_BLOCK);
            TILES.add(LASER_BLOCK_TILE);
            ITEMS.add(LASER_BLOCK_ITEM);
        }
    }

    //entities
    public static final String FIREFLY_NAME = "firefly";
    public static final EntityType<?> FIREFLY_TYPE = (EntityType.Builder.create(FireflyEntity::new, EntityClassification.AMBIENT)
            .setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)
            .size(0.3125f, 1f))
            .build(FIREFLY_NAME)
            .setRegistryName(FIREFLY_NAME);
    public static final Item FIREFLY_SPAWN_EGG_ITEM = new SpawnEggItem(FIREFLY_TYPE, -4784384, -16777216,
            new Item.Properties().group(ItemGroup.MISC)).setRegistryName(FIREFLY_NAME);


    //particles
    public static final BasicParticleType ENDERGETIC_FLAME = (BasicParticleType) new BasicParticleType(true).setRegistryName("endergetic_flame");
    public static final BasicParticleType FIREFLY_GLOW = (BasicParticleType) new BasicParticleType(true).setRegistryName("firefly_glow");
    public static final BasicParticleType SPEAKER_SOUND = (BasicParticleType) new BasicParticleType(true).setRegistryName("speaker_sound");
    public static final BasicParticleType GREEN_FLAME = (BasicParticleType) new BasicParticleType(true).setRegistryName("green_flame");


    //blocks

    //planter
    public static final String PLANTER_NAME = "planter";
    public static final Block PLANTER = new PlanterBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
    ).setRegistryName(PLANTER_NAME);
    public static final Item PLANTER_ITEM = new BlockItem(PLANTER,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(PLANTER_NAME);

    //clock
    public static final String CLOCK_BLOCK_NAME = "clock_block";
    public static final Block CLOCK_BLOCK = new ClockBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 6f)
                    .harvestLevel(0)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .setLightLevel((state)->1)
    ).setRegistryName(CLOCK_BLOCK_NAME);
    public static final TileEntityType<?> CLOCK_BLOCK_TILE =  TileEntityType.Builder.create(
            ClockBlockTile::new, CLOCK_BLOCK).build(null).setRegistryName(CLOCK_BLOCK_NAME);

    public static final Item CLOCK_BLOCK_ITEM = new BlockItem(CLOCK_BLOCK,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(CLOCK_BLOCK_NAME);

    //pedestal
    public static final String PEDESTAL_NAME = "pedestal";
    public static final Block PEDESTAL = new PedestalBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
    ).setRegistryName(PEDESTAL_NAME);
    public static final TileEntityType<?> PEDESTAL_TILE =  TileEntityType.Builder.create(
            PedestalBlockTile::new, PEDESTAL).build(null).setRegistryName(PEDESTAL_NAME);

    public static final Item PEDESTAL_ITEM = new BlockItem(PEDESTAL,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(PEDESTAL_NAME);

    //wind vane
    public static final String WIND_VANE_NAME = "wind_vane";
    public static final Block WIND_VANE = new WindVaneBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(5f, 6f)
                    .harvestLevel(2)
                    .setRequiresTool()
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ).setRegistryName(WIND_VANE_NAME);
    public static final TileEntityType<?> WIND_VANE_TILE =  TileEntityType.Builder.create(
            WindVaneBlockTile::new, WIND_VANE).build(null).setRegistryName(WIND_VANE_NAME);

    public static final Item WIND_VANE_ITEM = new BlockItem(WIND_VANE,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(WIND_VANE_NAME);

    //illuminator
    public static final String REDSTONE_ILLUMINATOR_NAME = "redstone_illuminator";
    public static final Block REDSTONE_ILLUMINATOR = new RedstoneIlluminatorBlock(
            AbstractBlock.Properties.create(Material.REDSTONE_LIGHT, MaterialColor.QUARTZ)
                    .hardnessAndResistance(0.3f, 0.3f)
                    .sound(SoundType.GLASS)
                    .setLightLevel((state) -> 15)
    ).setRegistryName(REDSTONE_ILLUMINATOR_NAME);
    public static final Item REDSTONE_ILLUMINATOR_ITEM = new BlockItem(REDSTONE_ILLUMINATOR,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(REDSTONE_ILLUMINATOR_NAME);

    //notice board
    public static final String NOTICE_BOARD_NAME = "notice_board";
    public static final Block NOTICE_BOARD = new NoticeBoardBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()

    ).setRegistryName(NOTICE_BOARD_NAME);
    public static final TileEntityType<?> NOTICE_BOARD_TILE =  TileEntityType.Builder.create(
            NoticeBoardBlockTile::new, NOTICE_BOARD).build(null).setRegistryName(NOTICE_BOARD_NAME);

    public static final Item NOTICE_BOARD_ITEM = new BlockItem(NOTICE_BOARD,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(NOTICE_BOARD_NAME);

    public static final ContainerType<?> NOTICE_BOARD_CONTAINER = IForgeContainerType.create(
            NoticeBoardContainer::new).setRegistryName(NOTICE_BOARD_NAME);

    //crank
    public static final String CRANK_NAME = "crank";
    public static final Block CRANK = new CrankBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(0.6f, 0.6f)
                    .harvestTool(ToolType.PICKAXE)
                    .doesNotBlockMovement()
                    .notSolid()
    ).setRegistryName(CRANK_NAME);
    public static final Item CRANK_ITEM =   new BlockItem(CRANK,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(CRANK_NAME);

    //jar
    public static final String JAR_NAME = "jar";
    public static final Block JAR = new JarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
    ).setRegistryName(JAR_NAME);

    public static final String JAR_NAME_TINTED = "jar_tinted";
    public static final Block JAR_TINTED = new JarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
    ).setRegistryName(JAR_NAME_TINTED);

    public static final TileEntityType<?> JAR_TILE =  TileEntityType.Builder.create(
            JarBlockTile::new, JAR,JAR_TINTED).build(null).setRegistryName(JAR_NAME);

    public static final Item JAR_ITEM = new JarItem(JAR, new Item.Properties().group(null)
            .maxStackSize(1).setISTER(()-> JarItemRenderer::new)).setRegistryName("jar_full");

    public static final Item JAR_ITEM_TINTED = new JarItem(JAR_TINTED, new Item.Properties().group(null)
            .maxStackSize(1).setISTER(()-> JarItemRenderer::new)).setRegistryName("jar_full_tinted");


    public static final Item EMPTY_JAR_ITEM = new EmptyJarItem(JAR, new Item.Properties().group(
            ItemGroup.DECORATIONS).maxStackSize(16)).setRegistryName(JAR_NAME);

    public static final Item EMPTY_JAR_ITEM_TINTED = new EmptyJarItem(JAR_TINTED, new Item.Properties().group(
            ItemGroup.DECORATIONS).maxStackSize(16)).setRegistryName(JAR_NAME_TINTED);




    //faucet
    public static final String FAUCET_NAME = "faucet";
    public static final Block FAUCET = new FaucetBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3f, 4.8f)
                    .harvestLevel(2)
                    .setRequiresTool()
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ).setRegistryName(FAUCET_NAME);
    public static final TileEntityType<?> FAUCET_TILE =  TileEntityType.Builder.create(
            FaucetBlockTile::new, FAUCET).build(null).setRegistryName(FAUCET_NAME);

    public static final Item FAUCET_ITEM = new BlockItem(FAUCET,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(FAUCET_NAME);

    //turn table
    public static final String TURN_TABLE_NAME = "turn_table";
    public static final Block TURN_TABLE = new TurnTableBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(0.75f, 2f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(0)
                    .setRequiresTool()
    ).setRegistryName(TURN_TABLE_NAME);
    public static final TileEntityType<?> TURN_TABLE_TILE =  TileEntityType.Builder.create(
            TurnTableBlockTile::new, TURN_TABLE).build(null).setRegistryName(TURN_TABLE_NAME);

    public static final Item TURN_TABLE_ITEM = new BlockItem(TURN_TABLE,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(TURN_TABLE_NAME);

    //piston launcher base
    public static final String PISTON_LAUNCHER_NAME = "piston_launcher";
    public static final Block PISTON_LAUNCHER = new PistonLauncherBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .setOpaque((state, reader, pos)-> !state.get(PistonLauncherBlock.EXTENDED))
                    .setSuffocates((state, reader, pos) -> !state.get(PistonLauncherBlock.EXTENDED))
                    .setBlocksVision((state, reader, pos) -> !state.get(PistonLauncherBlock.EXTENDED))

    ).setRegistryName(PISTON_LAUNCHER_NAME);
    public static final Item PISTON_LAUNCHER_ITEM = new BlockItem(PISTON_LAUNCHER,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(PISTON_LAUNCHER_NAME);

    public static final String PISTON_LAUNCHER_HEAD_NAME = "piston_launcher_head";
    public static final Block PISTON_LAUNCHER_HEAD = new PistonLauncherHeadBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .noDrops()
                    .jumpFactor(1.18f)
    ).setRegistryName(PISTON_LAUNCHER_HEAD_NAME);
    public static final String PISTON_LAUNCHER_ARM_NAME = "piston_launcher_arm";
    public static final Block PISTON_LAUNCHER_ARM = new PistonLauncherArmBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(2)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
                    .noDrops()
    ).setRegistryName(PISTON_LAUNCHER_ARM_NAME);
    public static final TileEntityType<?> PISTON_LAUNCHER_ARM_TILE =  TileEntityType.Builder.create(
            PistonLauncherArmBlockTile::new, PISTON_LAUNCHER_ARM).build(null).setRegistryName(PISTON_LAUNCHER_ARM_NAME);

    //speaker Block
    public static final String SPEAKER_BLOCK_NAME = "speaker_block";
    public static final Block SPEAKER_BLOCK = new SpeakerBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(1f, 2f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
    ).setRegistryName(SPEAKER_BLOCK_NAME);
    public static final TileEntityType<?> SPEAKER_BLOCK_TILE =  TileEntityType.Builder.create(
            SpeakerBlockTile::new, SPEAKER_BLOCK).build(null).setRegistryName(SPEAKER_BLOCK_NAME);

    public static final Item SPEAKER_BLOCK_ITEM = new BlockItem(SPEAKER_BLOCK,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(SPEAKER_BLOCK_NAME);

    //sign post
    public static final String SIGN_POST_NAME = "sign_post";
    public static final Block SIGN_POST = new SignPostBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ).setRegistryName(SIGN_POST_NAME);
    public static final TileEntityType<?> SIGN_POST_TILE =  TileEntityType.Builder.create(
            SignPostBlockTile::new, SIGN_POST).build(null).setRegistryName(SIGN_POST_NAME);

    //items
    //oak
    public static final Item SIGN_POST_ITEM_OAK = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_oak");
    //spruce
    public static final Item SIGN_POST_ITEM_SPRUCE = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_spruce");
    //birch
    public static final Item SIGN_POST_ITEM_BIRCH = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_birch");
    //jungle
    public static final Item SIGN_POST_ITEM_JUNGLE = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_jungle");
    //acacia
    public static final Item SIGN_POST_ITEM_ACACIA = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_acacia");
    //dark oak
    public static final Item SIGN_POST_ITEM_DARK_OAK = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_dark_oak");
    //crimson
    public static final Item SIGN_POST_ITEM_CRIMSON = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_crimson");
    //warped
    public static final Item SIGN_POST_ITEM_WARPED = new SignPostItem(
            new Item.Properties().group(ItemGroup.DECORATIONS).maxStackSize(64)).setRegistryName("sign_post_warped");

    //hanging sing
    //oak
    public static final String HANGING_SIGN_NAME_OAK = "hanging_sign_oak";
    public static final Block HANGING_SIGN_OAK = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.OAK_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_OAK);
    public static final Item HANGING_SIGN_ITEM_OAK = new BlockItem(HANGING_SIGN_OAK,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_OAK);

    //spruce
    public static final String HANGING_SIGN_NAME_SPRUCE = "hanging_sign_spruce";
    public static final Block HANGING_SIGN_SPRUCE = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.SPRUCE_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_SPRUCE);
    public static final Item HANGING_SIGN_ITEM_SPRUCE = new BlockItem(HANGING_SIGN_SPRUCE,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_SPRUCE);

    //birch
    public static final String HANGING_SIGN_NAME_BIRCH = "hanging_sign_birch";
    public static final Block HANGING_SIGN_BIRCH = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.BIRCH_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_BIRCH);
    public static final Item HANGING_SIGN_ITEM_BIRCH = new BlockItem(HANGING_SIGN_BIRCH,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_BIRCH);

    //jungle
    public static final String HANGING_SIGN_NAME_JUNGLE = "hanging_sign_jungle";
    public static final Block HANGING_SIGN_JUNGLE = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.JUNGLE_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_JUNGLE);
    public static final Item HANGING_SIGN_ITEM_JUNGLE = new BlockItem(HANGING_SIGN_JUNGLE,
          new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_JUNGLE);

    //acacia
    public static final String HANGING_SIGN_NAME_ACACIA = "hanging_sign_acacia";
    public static final Block HANGING_SIGN_ACACIA = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.ACACIA_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_ACACIA);
    public static final Item HANGING_SIGN_ITEM_ACACIA = new BlockItem(HANGING_SIGN_ACACIA,
          new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_ACACIA);

    //dark oak
    public static final String HANGING_SIGN_NAME_DARK_OAK = "hanging_sign_dark_oak";
    public static final Block HANGING_SIGN_DARK_OAK = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.DARK_OAK_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_DARK_OAK);
    public static final Item HANGING_SIGN_ITEM_DARK_OAK = new BlockItem(HANGING_SIGN_DARK_OAK,
          new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_DARK_OAK);

    //crimson
    public static final String HANGING_SIGN_NAME_CRIMSON = "hanging_sign_crimson";
    public static final Block HANGING_SIGN_CRIMSON = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.CRIMSON_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_CRIMSON);
    public static final Item HANGING_SIGN_ITEM_CRIMSON = new BlockItem(HANGING_SIGN_CRIMSON,
          new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_CRIMSON);

    //warped
    public static final String HANGING_SIGN_NAME_WARPED = "hanging_sign_warped";
    public static final Block HANGING_SIGN_WARPED = new HangingSignBlock(
            AbstractBlock.Properties.create(Material.WOOD, Blocks.CRIMSON_PLANKS.getMaterialColor())
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
                    .doesNotBlockMovement()
    ).setRegistryName(HANGING_SIGN_NAME_WARPED);
    public static final Item HANGING_SIGN_ITEM_WARPED = new BlockItem(HANGING_SIGN_WARPED,
          new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(HANGING_SIGN_NAME_WARPED);


    //tile
    public static final TileEntityType<?> HANGING_SIGN_TILE =  TileEntityType.Builder.create(HangingSignBlockTile::new,
                    HANGING_SIGN_OAK,HANGING_SIGN_BIRCH,HANGING_SIGN_SPRUCE,HANGING_SIGN_JUNGLE,
                    HANGING_SIGN_ACACIA,HANGING_SIGN_DARK_OAK,HANGING_SIGN_CRIMSON,HANGING_SIGN_WARPED
                    ).build(null).setRegistryName(HANGING_SIGN_NAME_OAK);

    //wall lantern
    public static final String WALL_LANTERN_NAME = "wall_lantern";
    public static final Block WALL_LANTERN = new WallLanternBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3.5f, 3.5f)
                    .sound(SoundType.LANTERN)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .harvestLevel(1)
                    .setLightLevel((state) -> 15)
                    .notSolid()
                    .noDrops()
    ).setRegistryName(WALL_LANTERN_NAME);
    public static final TileEntityType<?> WALL_LANTERN_TILE =  TileEntityType.Builder.create(
            WallLanternBlockTile::new, WALL_LANTERN).build(null).setRegistryName(WALL_LANTERN_NAME);
    public static final Item WALL_LANTERN_ITEM = new BlockItem(WALL_LANTERN,
            new Item.Properties().group(null)).setRegistryName(WALL_LANTERN_NAME);

    //bellows
    public static final String BELLOWS_NAME = "bellows";
    public static final Block BELLOWS = new BellowsBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ).setRegistryName(BELLOWS_NAME);
    public static final TileEntityType<?> BELLOWS_TILE =  TileEntityType.Builder.create(
            BellowsBlockTile::new, BELLOWS).build(null).setRegistryName(BELLOWS_NAME);
    public static final Item BELLOWS_ITEM = new BlockItem(BELLOWS,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(BELLOWS_NAME);

    //laser
    public static final String LASER_NAME = "laser_block";
    public static final Block LASER_BLOCK = new LaserBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(3.5f, 3.5f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
    ).setRegistryName(LASER_NAME);
    public static final TileEntityType<?> LASER_BLOCK_TILE =  TileEntityType.Builder.create(
            LaserBlockTile::new, LASER_BLOCK).build(null).setRegistryName(LASER_NAME);
    public static final Item LASER_BLOCK_ITEM = new BlockItem(LASER_BLOCK,
            new Item.Properties().group(null)).setRegistryName(LASER_NAME);
    
    //flag
    public static final String FLAG_NAME = "flag";
    public static final Block FLAG = new FlagBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ).setRegistryName(FLAG_NAME);
    public static final TileEntityType<?> FLAG_TILE =  TileEntityType.Builder.create(
            FlagBlockTile::new, FLAG).build(null).setRegistryName(FLAG_NAME);
    public static final Item FLAG_ITEM = new BlockItem(FLAG,
            new Item.Properties().group(null)).setRegistryName(FLAG_NAME);


    //drawers
    /*
    public static final String DRAWERS_NAME = "drawers";
    public static final Block> DRAWERS = new DrawersBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                    .hardnessAndResistance(2f, 2f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final TileEntityType<?> DRAWERS_TILE =  TileEntityType.Builder.create(DrawersBlockTile::new, DRAWERS).build(null));
    public static final Item DRAWERS_ITEM = new BlockItem(DRAWERS,
            new Item.Properties().group(null)));
    */
    //sconce
    //normal
    public static final String SCONCE_NAME = "sconce";
    public static final Block SCONCE = new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .sound(SoundType.LANTERN), ParticleTypes.FLAME).setRegistryName(SCONCE_NAME);
    public static final Block SCONCE_WALL = new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .lootFrom(SCONCE)
                    .sound(SoundType.LANTERN), ParticleTypes.FLAME).setRegistryName("sconce_wall");
    public static final Item SCONCE_ITEM = new WallOrFloorItem(SCONCE, SCONCE_WALL,
            (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName(SCONCE_NAME);

    //soul
    public static final String SCONCE_NAME_SOUL = "sconce_soul";
    public static final Block SCONCE_SOUL = new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .sound(SoundType.LANTERN), ParticleTypes.SOUL_FIRE_FLAME).setRegistryName(SCONCE_NAME_SOUL);
    public static final Block SCONCE_WALL_SOUL = new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .lootFrom(SCONCE_SOUL)
                    .sound(SoundType.LANTERN), ParticleTypes.SOUL_FIRE_FLAME).setRegistryName("sconce_wall_soul");
    public static final Item SCONCE_ITEM_SOUL = new WallOrFloorItem(SCONCE_SOUL, SCONCE_WALL_SOUL,
            (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName(SCONCE_NAME_SOUL);
    //TODO: add config. also add burn times for wood stuff
    //optional: endergetic
    public static final String SCONCE_NAME_ENDER = "sconce_ender";
    public static final Block SCONCE_ENDER = new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .sound(SoundType.LANTERN), ENDERGETIC_FLAME).setRegistryName(SCONCE_NAME_ENDER);
    public static final Block SCONCE_WALL_ENDER = new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .lootFrom(SCONCE_ENDER)
                    .sound(SoundType.LANTERN), ENDERGETIC_FLAME).setRegistryName("sconce_wall_ender");
    public static final Item SCONCE_ITEM_ENDER = new WallOrFloorItem(SCONCE_ENDER, SCONCE_WALL_ENDER,
            (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName(SCONCE_NAME_ENDER);
    //green
    public static final String SCONCE_NAME_GREEN = "sconce_green";
    public static final Block SCONCE_GREEN = new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .sound(SoundType.LANTERN), GREEN_FLAME).setRegistryName(SCONCE_NAME_GREEN);
    public static final Block SCONCE_WALL_GREEN = new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .lootFrom(SCONCE_GREEN)
                    .sound(SoundType.LANTERN), GREEN_FLAME).setRegistryName("sconce_wall_green");
    public static final Item SCONCE_ITEM_GREEN = new WallOrFloorItem(SCONCE_GREEN, SCONCE_WALL_GREEN,
            (new Item.Properties()).group(ItemGroup.DECORATIONS)).setRegistryName(SCONCE_NAME_GREEN);


    //firefly & jar
    public static final String FIREFLY_JAR_NAME = "firefly_jar";
    public static final Block FIREFLY_JAR = new FireflyJarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
                    .setLightLevel((state) -> 8)
    ).setRegistryName(FIREFLY_JAR_NAME);
    public static final TileEntityType<?> FIREFLY_JAR_TILE =  TileEntityType.Builder.create(
            FireflyJarBlockTile::new, FIREFLY_JAR).build(null).setRegistryName(FIREFLY_JAR_NAME);
    public static final Item FIREFLY_JAR_ITEM = new BlockItem(FIREFLY_JAR, new Item.Properties().group(
            ItemGroup.DECORATIONS).maxStackSize(16).setISTER(()-> FireflyJarItemRenderer::new)).setRegistryName(FIREFLY_JAR_NAME);

    //candelabra
    public static final String CANDELABRA_NAME = "candelabra";
    public static final Block CANDELABRA = new CandelabraBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.GOLD)
                    .hardnessAndResistance(4f, 5f)
                    .sound(SoundType.METAL)
                    .notSolid()
                    .setLightLevel((state) -> 14)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(2)
    ).setRegistryName(CANDELABRA_NAME);
    public static final Item CANDELABRA_ITEM = new BlockItem(CANDELABRA,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(CANDELABRA_NAME);
    //silver
    public static final String CANDELABRA_NAME_SILVER = "candelabra_silver";
    public static final Block CANDELABRA_SILVER = new CandelabraBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .sound(SoundType.METAL)
                    .notSolid()
                    .setLightLevel((state) -> 14)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(2)
    ).setRegistryName(CANDELABRA_NAME_SILVER);
    public static final Item CANDELABRA_ITEM_SILVER = new BlockItem(CANDELABRA_SILVER,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(CANDELABRA_NAME_SILVER);

    //item shelf
    public static final String ITEM_SHELF_NAME = "item_shelf";
    public static final Block ITEM_SHELF = new ItemShelfBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .hardnessAndResistance(0.75f, 0.1f)
                    .notSolid()
                    .doesNotBlockMovement()
                    .harvestTool(ToolType.AXE)
    ).setRegistryName(ITEM_SHELF_NAME);
    public static final TileEntityType<?> ITEM_SHELF_TILE =  TileEntityType.Builder.create(
            ItemShelfBlockTile::new, ITEM_SHELF).build(null).setRegistryName(ITEM_SHELF_NAME);
    public static final Item ITEM_SHELF_ITEM = new BlockItem(ITEM_SHELF,
            new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName(ITEM_SHELF_NAME);

    //cog block
    public static final String COG_BLOCK_NAME = "cog_block";
    public static final Block COG_BLOCK = new CogBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(5f, 6f)
                    .sound(SoundType.METAL)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(2)
    ).setRegistryName(COG_BLOCK_NAME);
    public static final Item COG_BLOCK_ITEM = new BlockItem(COG_BLOCK,
            new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(COG_BLOCK_NAME);

}
