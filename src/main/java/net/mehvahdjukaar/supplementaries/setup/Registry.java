package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.*;
import net.mehvahdjukaar.supplementaries.block.tiles.*;
import net.mehvahdjukaar.supplementaries.client.renderers.items.BlackboardItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.CageItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FireflyJarItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.entities.FireflyEntity;
import net.mehvahdjukaar.supplementaries.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.inventories.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.inventories.SackContainer;
import net.mehvahdjukaar.supplementaries.items.*;
import net.mehvahdjukaar.supplementaries.items.crafting.*;
import net.mehvahdjukaar.supplementaries.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.setup.registration.Variants;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class Registry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Supplementaries.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Supplementaries.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Supplementaries.MOD_ID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Supplementaries.MOD_ID);

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        CONTAINERS.register(bus);
        ENTITIES.register(bus);
        PARTICLES.register(bus);
        SOUNDS.register(bus);
        RECIPES.register(bus);
    }

    //creative tab
    private static final boolean tab = RegistryConfigs.reg.CREATIVE_TAB.get();
    private static final boolean jar_tab = RegistryConfigs.reg.JAR_TAB.get();
    public static final ItemGroup MOD_TAB = !tab?null:
            new ItemGroup("supplementaries") {
                @Override
                public ItemStack createIcon() {
                    return new ItemStack(Registry.GLOBE_ITEM.get());
                }
                public boolean hasSearchBar() {
                    return false;
                }
            };
    public static final ItemGroup JAR_TAB = !jar_tab?null:
            new ItemGroup("jars") {
                @Override
                public ItemStack createIcon() {
                    return JarTab.getIcon();
                }
                public boolean hasSearchBar() {
                    return true;
                }
            };

    public static ItemGroup getTab(ItemGroup g,String reg_name){
        if(RegistryConfigs.reg.isEnabled(reg_name)) {
            return tab ? MOD_TAB : g;
        }
        return null;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(TOM_SOUND_EVENT);
        event.getRegistry().register(TICK_SOUND_EVENT);
        event.getRegistry().register(TICK_2_SOUND_EVENT);

    }
    //TODO: figure out sounds, use deferred registries
    //these are the names in sound.json. not actual location
    public static final SoundEvent TICK_SOUND_EVENT = makeSoundEvent("block.tick_1");
    public static final SoundEvent TICK_2_SOUND_EVENT = makeSoundEvent("block.tick_2");
    public static final SoundEvent TOM_SOUND_EVENT = makeSoundEvent("block.tom");

    public static SoundEvent makeSoundEvent(String name){
        ResourceLocation loc = new ResourceLocation(Supplementaries.MOD_ID,name);
        return new SoundEvent(loc).setRegistryName(name);
    }

    //TODO: use deferred reg
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event){
        event.getRegistry().register(FIREFLY_TYPE);
        GlobalEntityTypeAttributes.put((EntityType<? extends LivingEntity>) FIREFLY_TYPE, FireflyEntity.setCustomAttributes().create());
    }

    //entities

    //firefly
    public static final String FIREFLY_NAME = "firefly";
    public static final EntityType<?> FIREFLY_TYPE = (EntityType.Builder.create(FireflyEntity::new, EntityClassification.AMBIENT)
            .setShouldReceiveVelocityUpdates(true).setTrackingRange(128).setUpdateInterval(3)
            .size(0.3125f, 1f))
            .build(FIREFLY_NAME)
            .setRegistryName(FIREFLY_NAME);
    public static final RegistryObject<Item> FIREFLY_SPAWN_EGG_ITEM = ITEMS.register(FIREFLY_NAME+"_spawn_egg",()-> new SpawnEggItem(FIREFLY_TYPE,  -5048018, -14409439, //-4784384, -16777216,
            new Item.Properties().group(getTab(ItemGroup.MISC,FIREFLY_NAME))));


    //brick
    public static final String THROWABLE_BRICK_NAME = "brick_projectile";
    public static final RegistryObject<EntityType<ThrowableBrickEntity>> THROWABLE_BRICK = ENTITIES.register(THROWABLE_BRICK_NAME,()->(
            EntityType.Builder.<ThrowableBrickEntity>create(ThrowableBrickEntity::new, EntityClassification.MISC)
            .setShouldReceiveVelocityUpdates(true).setCustomClientFactory(ThrowableBrickEntity::new)
            .setTrackingRange(64).setUpdateInterval(1).size(0.5f, 0.5f))//.size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10))
            .build(THROWABLE_BRICK_NAME));

    //TODO: add enable config
    //rope arrow
    public static final String ROPE_ARROW_NAME = "rope_arrow";
    public static final RegistryObject<EntityType<RopeArrowEntity>> ROPE_ARROW = ENTITIES.register(ROPE_ARROW_NAME,()->(
            EntityType.Builder.<RopeArrowEntity>create(RopeArrowEntity::new, EntityClassification.MISC)
            .setShouldReceiveVelocityUpdates(true).setCustomClientFactory(RopeArrowEntity::new)
            .setTrackingRange(64).setUpdateInterval(1).size(0.5f, 0.5f))//.size(0.25F, 0.25F).trackingRange(4).func_233608_b_(10))
            .build(ROPE_ARROW_NAME));
    public static final RegistryObject<Item> ROPE_ARROW_ITEM = ITEMS.register(ROPE_ARROW_NAME,()-> new RopeArrowItem(
            new Item.Properties().group(getTab(ItemGroup.MISC,ROPE_ARROW_NAME))
                    .defaultMaxDamage(16).setNoRepair()));


    //particles
    public static final RegistryObject<BasicParticleType> ENDERGETIC_FLAME = PARTICLES
            .register("endergetic_flame", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> FIREFLY_GLOW = PARTICLES
            .register("firefly_glow", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> SPEAKER_SOUND = PARTICLES
            .register("speaker_sound", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> GREEN_FLAME = PARTICLES
            .register("green_flame", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> DRIPPING_LIQUID = PARTICLES
            .register("dripping_liquid", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> FALLING_LIQUID = PARTICLES
            .register("falling_liquid", ()-> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> SPLASHING_LIQUID = PARTICLES
            .register("splashing_liquid", ()-> new BasicParticleType(true));



    //recipes
    public static final RegistryObject<IRecipeSerializer<?>> BLACKBOARD_DUPLICATE_RECIPE = RECIPES.register("blackboard_duplicate_recipe", ()->
            new SpecialRecipeSerializer<>(BlackboardDuplicateRecipe::new));
    public static final RegistryObject<IRecipeSerializer<?>> BLACKBOARD_CLEAR_RECIPE = RECIPES.register("blackboard_clear_recipe", ()->
            new SpecialRecipeSerializer<>(BlackboardClearRecipe::new));
    public static final RegistryObject<IRecipeSerializer<?>> BAMBOO_SPIKES_TIPPED_RECIPE = RECIPES.register("bamboo_spikes_tipped_recipe", ()->
            new SpecialRecipeSerializer<>(TippedBambooSpikesRecipe::new));
    public static final RegistryObject<IRecipeSerializer<?>> ROPE_ARROW_CREATE_RECIPE = RECIPES.register("rope_arrow_create_recipe", ()->
            new SpecialRecipeSerializer<>(RopeArrowCreateRecipe::new));
    public static final RegistryObject<IRecipeSerializer<?>> ROPE_ARROW_ADD_RECIPE = RECIPES.register("rope_arrow_add_recipe", ()->
            new SpecialRecipeSerializer<>(RopeArrowAddRecipe::new));

    //blocks

    //variants:

    //hanging signs
    public static final String HANGING_SIGN_NAME = "hanging_sign";
    public static final Map<IWoodType, RegistryObject<Block>> HANGING_SIGNS = Variants.makeHangingSingsBlocks();
    public static final Map<IWoodType, RegistryObject<Item>> HANGING_SIGNS_ITEMS = Variants.makeHangingSignsItems();

    //keeping "hanging_sign_oak" for compatibility even if it should be just hanging_sign
    public static final RegistryObject<TileEntityType<HangingSignBlockTile>> HANGING_SIGN_TILE = TILES
            .register(HANGING_SIGN_NAME+"_oak", ()-> TileEntityType.Builder.create(HangingSignBlockTile::new,
            HANGING_SIGNS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //sign posts
    public static final String SIGN_POST_NAME = "sign_post";
    public static final RegistryObject<Block> SIGN_POST = BLOCKS.register(SIGN_POST_NAME,()-> new SignPostBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<SignPostBlockTile>> SIGN_POST_TILE = TILES.register(SIGN_POST_NAME,()-> TileEntityType.Builder.create(
            SignPostBlockTile::new, SIGN_POST.get()).build(null));

    public static final Map<IWoodType, RegistryObject<Item>> SIGN_POST_ITEMS = Variants.makeSignPostItems();



    //planter
    public static final String PLANTER_NAME = "planter";
    public static final RegistryObject<Block> PLANTER = BLOCKS.register(PLANTER_NAME, ()-> new PlanterBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.RED_TERRACOTTA)
                    .hardnessAndResistance(2f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<Item> PLANTER_ITEM = ITEMS.register(PLANTER_NAME,()-> new BlockItem(PLANTER.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,PLANTER_NAME))
    ));


    //clock
    public static final String CLOCK_BLOCK_NAME = "clock_block";
    public static final RegistryObject<Block> CLOCK_BLOCK = BLOCKS.register(CLOCK_BLOCK_NAME,()-> new ClockBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 6f)
                    .harvestLevel(0)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .setLightLevel((state)->1)
    ));
    public static final RegistryObject<TileEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = TILES.register(CLOCK_BLOCK_NAME,()->  TileEntityType.Builder.create(
            ClockBlockTile::new, CLOCK_BLOCK.get()).build(null));

    public static final RegistryObject<Item> CLOCK_BLOCK_ITEM = ITEMS.register(CLOCK_BLOCK_NAME,()-> new BlockItem(CLOCK_BLOCK.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,CLOCK_BLOCK_NAME))
    ));

    //pedestal
    public static final String PEDESTAL_NAME = "pedestal";
    public static final RegistryObject<Block> PEDESTAL = BLOCKS.register(PEDESTAL_NAME,()-> new PedestalBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(2f, 6f)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<PedestalBlockTile>> PEDESTAL_TILE = TILES.register(PEDESTAL_NAME,()-> TileEntityType.Builder.create(
            PedestalBlockTile::new, PEDESTAL.get()).build(null));

    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register(PEDESTAL_NAME,()-> new BlockItem(PEDESTAL.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,PEDESTAL_NAME))
    ));

    //wind vane
    public static final String WIND_VANE_NAME = "wind_vane";
    public static final RegistryObject<Block> WIND_VANE = BLOCKS.register(WIND_VANE_NAME,()-> new WindVaneBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(5f, 6f)
                    .harvestLevel(1)
                    .setRequiresTool()
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<WindVaneBlockTile>> WIND_VANE_TILE = TILES.register(WIND_VANE_NAME,()->  TileEntityType.Builder.create(
            WindVaneBlockTile::new, WIND_VANE.get()).build(null));

    public static final RegistryObject<Item> WIND_VANE_ITEM = ITEMS.register(WIND_VANE_NAME,()-> new BlockItem(WIND_VANE.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,WIND_VANE_NAME))
    ));

    //illuminator
    public static final String REDSTONE_ILLUMINATOR_NAME = "redstone_illuminator";
    public static final RegistryObject<Block> REDSTONE_ILLUMINATOR = BLOCKS.register(REDSTONE_ILLUMINATOR_NAME,()-> new RedstoneIlluminatorBlock(
            AbstractBlock.Properties.create(Material.REDSTONE_LIGHT, MaterialColor.QUARTZ)
                    .hardnessAndResistance(0.3f, 0.3f)
                    .sound(SoundType.GLASS)
                    .setLightLevel((state) -> 15)
    ));
    public static final RegistryObject<Item> REDSTONE_ILLUMINATOR_ITEM = ITEMS.register(REDSTONE_ILLUMINATOR_NAME,()-> new BlockItem(REDSTONE_ILLUMINATOR.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,REDSTONE_ILLUMINATOR_NAME))
    ));

    //notice board
    public static final String NOTICE_BOARD_NAME = "notice_board";
    public static final RegistryObject<Block> NOTICE_BOARD = BLOCKS.register(NOTICE_BOARD_NAME,()-> new NoticeBoardBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(2.5f, 2.5f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()

    ));
    public static final RegistryObject<TileEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = TILES.register(NOTICE_BOARD_NAME,()-> TileEntityType.Builder.create(
            NoticeBoardBlockTile::new, NOTICE_BOARD.get()).build(null));

    public static final RegistryObject<Item> NOTICE_BOARD_ITEM = ITEMS.register(NOTICE_BOARD_NAME,()-> new BurnableBlockItem(NOTICE_BOARD.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,NOTICE_BOARD_NAME)),300
    ));

    public static final RegistryObject<ContainerType<NoticeBoardContainer>> NOTICE_BOARD_CONTAINER = CONTAINERS
            .register(NOTICE_BOARD_NAME,()-> IForgeContainerType.create(NoticeBoardContainer::new));

    //crank
    public static final String CRANK_NAME = "crank";
    public static final RegistryObject<Block> CRANK = BLOCKS.register(CRANK_NAME,()-> new CrankBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(0.6f, 0.6f)
                    .harvestTool(ToolType.PICKAXE)
                    .doesNotBlockMovement()
                    .notSolid()
    ));
    public static final RegistryObject<Item> CRANK_ITEM = ITEMS.register(CRANK_NAME,()->   new BlockItem(CRANK.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,CRANK_NAME))
    ));

    //jar
    public static final String JAR_NAME = "jar";
    public static final RegistryObject<Block> JAR = BLOCKS.register(JAR_NAME,()-> new JarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
    ));

    public static final String JAR_NAME_TINTED = "jar_tinted";
    public static final RegistryObject<Block> JAR_TINTED = BLOCKS.register(JAR_NAME_TINTED,()-> new JarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
    ));

    public static final RegistryObject<TileEntityType<JarBlockTile>> JAR_TILE = TILES.register(JAR_NAME,()->  TileEntityType.Builder.create(
            JarBlockTile::new, JAR.get(),JAR_TINTED.get()).build(null));

    public static final RegistryObject<Item> JAR_ITEM = ITEMS.register("jar_full",()-> new JarItem(JAR.get(), new Item.Properties()
            .group(jar_tab?JAR_TAB:null)
            .maxStackSize(1).setISTER(()-> JarItemRenderer::new), Registry.EMPTY_JAR_ITEM));

    public static final RegistryObject<Item> JAR_ITEM_TINTED = ITEMS.register("jar_full_tinted",()-> new JarItem(JAR_TINTED.get(), new Item.Properties().group(null)
            .maxStackSize(1).setISTER(()-> JarItemRenderer::new), Registry.EMPTY_JAR_ITEM_TINTED));


    public static final RegistryObject<Item> EMPTY_JAR_ITEM = ITEMS.register(JAR_NAME,()-> new EmptyJarItem(JAR.get(), new Item.Properties().group(
            getTab(ItemGroup.DECORATIONS,JAR_NAME)).maxStackSize(16), Registry.JAR_ITEM, EmptyCageItem.CageWhitelist.JAR));

    public static final RegistryObject<Item> EMPTY_JAR_ITEM_TINTED = ITEMS.register(JAR_NAME_TINTED,()-> new EmptyJarItem(JAR_TINTED.get(), new Item.Properties().group(
            getTab(ItemGroup.DECORATIONS,JAR_NAME)).maxStackSize(16), Registry.JAR_ITEM_TINTED,EmptyCageItem.CageWhitelist.TINTED_JAR));


    //firefly jar
    public static final String FIREFLY_JAR_NAME = "firefly_jar";
    public static final RegistryObject<Block> FIREFLY_JAR = BLOCKS.register(FIREFLY_JAR_NAME,()-> new FireflyJarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
                    .setLightLevel((state) -> 8),false
    ));

    public static final RegistryObject<Item> FIREFLY_JAR_ITEM = ITEMS.register(FIREFLY_JAR_NAME,()-> new BlockItem(FIREFLY_JAR.get(), new Item.Properties()
            .group(getTab(ItemGroup.DECORATIONS,FIREFLY_JAR_NAME)).maxStackSize(16).setISTER(()-> FireflyJarItemRenderer::new))
    );

    //soul jar
    public static final String SOUL_JAR_NAME = "soul_jar";
    public static final RegistryObject<Block> SOUL_JAR = BLOCKS.register(SOUL_JAR_NAME,()-> new FireflyJarBlock(
            AbstractBlock.Properties.create(Material.GLASS, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.GLASS)
                    .notSolid()
                    .setLightLevel((state) -> 8),true
    ));
    public static final RegistryObject<Item> SOUL_JAR_ITEM = ITEMS.register(SOUL_JAR_NAME,()-> new BlockItem(SOUL_JAR.get(), new Item.Properties()
            .group(getTab(ItemGroup.DECORATIONS,SOUL_JAR_NAME)).maxStackSize(16))
    );

    public static final RegistryObject<TileEntityType<FireflyJarBlockTile>> FIREFLY_JAR_TILE = TILES.register(FIREFLY_JAR_NAME,()->  TileEntityType.Builder.create(
            FireflyJarBlockTile::new, FIREFLY_JAR.get(),SOUL_JAR.get()).build(null));


    //faucet
    public static final String FAUCET_NAME = "faucet";
    public static final RegistryObject<Block> FAUCET = BLOCKS.register(FAUCET_NAME,()-> new FaucetBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3f, 4.8f)
                    .harvestLevel(1)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<FaucetBlockTile>> FAUCET_TILE = TILES.register(FAUCET_NAME,()->  TileEntityType.Builder.create(
            FaucetBlockTile::new, FAUCET.get()).build(null));

    public static final RegistryObject<Item> FAUCET_ITEM = ITEMS.register(FAUCET_NAME,()-> new BlockItem(FAUCET.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,FAUCET_NAME))
    ));

    //turn table
    public static final String TURN_TABLE_NAME = "turn_table";
    public static final RegistryObject<Block> TURN_TABLE = BLOCKS.register(TURN_TABLE_NAME,()-> new TurnTableBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(0.75f, 2f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(0)
                    .setRequiresTool()
    ));
    public static final RegistryObject<TileEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = TILES.register(TURN_TABLE_NAME,()->  TileEntityType.Builder.create(
            TurnTableBlockTile::new, TURN_TABLE.get()).build(null));

    public static final RegistryObject<Item> TURN_TABLE_ITEM = ITEMS.register(TURN_TABLE_NAME,()-> new BlockItem(TURN_TABLE.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,TURN_TABLE_NAME))
    ));

    //piston launcher base
    public static final String PISTON_LAUNCHER_NAME = "piston_launcher";
    public static final RegistryObject<Block> PISTON_LAUNCHER = BLOCKS.register(PISTON_LAUNCHER_NAME,()-> new PistonLauncherBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(1)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .setOpaque((state, reader, pos)-> !state.get(PistonLauncherBlock.EXTENDED))
                    .setSuffocates((state, reader, pos) -> !state.get(PistonLauncherBlock.EXTENDED))
                    .setBlocksVision((state, reader, pos) -> !state.get(PistonLauncherBlock.EXTENDED))

    ));
    public static final RegistryObject<Item> PISTON_LAUNCHER_ITEM = ITEMS.register(PISTON_LAUNCHER_NAME,()-> new BlockItem(PISTON_LAUNCHER.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,PISTON_LAUNCHER_NAME))
    ));

    public static final String PISTON_LAUNCHER_HEAD_NAME = "piston_launcher_head";
    public static final RegistryObject<Block> PISTON_LAUNCHER_HEAD = BLOCKS.register(PISTON_LAUNCHER_HEAD_NAME,()-> new PistonLauncherHeadBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(1)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .noDrops()
                    .jumpFactor(1.18f)
    ));
    public static final String PISTON_LAUNCHER_ARM_NAME = "piston_launcher_arm";
    public static final RegistryObject<Block> PISTON_LAUNCHER_ARM = BLOCKS.register(PISTON_LAUNCHER_ARM_NAME,()-> new PistonLauncherArmBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .harvestLevel(1)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .notSolid()
                    .noDrops()
    ));
    public static final RegistryObject<TileEntityType<PistonLauncherArmBlockTile>> PISTON_LAUNCHER_ARM_TILE = TILES.register(PISTON_LAUNCHER_ARM_NAME,()-> TileEntityType.Builder.create(
            PistonLauncherArmBlockTile::new, PISTON_LAUNCHER_ARM.get()).build(null));

    //speaker Block
    public static final String SPEAKER_BLOCK_NAME = "speaker_block";
    public static final RegistryObject<Block> SPEAKER_BLOCK = BLOCKS.register(SPEAKER_BLOCK_NAME,()-> new SpeakerBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(1f, 2f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
    ));
    public static final RegistryObject<TileEntityType<?>> SPEAKER_BLOCK_TILE = TILES.register(SPEAKER_BLOCK_NAME,()-> TileEntityType.Builder.create(
            SpeakerBlockTile::new, SPEAKER_BLOCK.get()).build(null));

    public static final RegistryObject<Item> SPEAKER_BLOCK_ITEM = ITEMS.register(SPEAKER_BLOCK_NAME,()-> new BurnableBlockItem(SPEAKER_BLOCK.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,SPEAKER_BLOCK_NAME)),300
    ));


    //wall lantern
    public static final String WALL_LANTERN_NAME = "wall_lantern";
    public static final RegistryObject<Block> WALL_LANTERN = BLOCKS.register(WALL_LANTERN_NAME,()-> new WallLanternBlock(
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
    public static final RegistryObject<TileEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = TILES.register(WALL_LANTERN_NAME,()->  TileEntityType.Builder.create(
            WallLanternBlockTile::new, WALL_LANTERN.get()).build(null));
    public static final RegistryObject<Item> WALL_LANTERN_ITEM = ITEMS.register(WALL_LANTERN_NAME,()-> new BlockHolderItem(WALL_LANTERN.get(),
            new Item.Properties().group(null)));

    //bellows
    public static final String BELLOWS_NAME = "bellows";
    public static final RegistryObject<Block> BELLOWS = BLOCKS.register(BELLOWS_NAME,()-> new BellowsBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.BROWN)
                    .hardnessAndResistance(3f, 3f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<BellowsBlockTile>> BELLOWS_TILE = TILES.register(BELLOWS_NAME,()->  TileEntityType.Builder.create(
            BellowsBlockTile::new, BELLOWS.get()).build(null));
    public static final RegistryObject<Item> BELLOWS_ITEM = ITEMS.register(BELLOWS_NAME,()-> new BurnableBlockItem(BELLOWS.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,BELLOWS_NAME)),300
    ));

    //laser
    public static final String LASER_NAME = "laser_block";
    public static final RegistryObject<Block> LASER_BLOCK = BLOCKS.register(LASER_NAME,()-> new LaserBlock(
            AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(3.5f, 3.5f)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<LaserBlockTile>> LASER_BLOCK_TILE = TILES.register(LASER_NAME,()->  TileEntityType.Builder.create(
            LaserBlockTile::new, LASER_BLOCK.get()).build(null));
    public static final RegistryObject<Item> LASER_BLOCK_ITEM = ITEMS.register(LASER_NAME,()-> new BlockItem(LASER_BLOCK.get(),
            new Item.Properties().group(null)
    )); //getTab(ItemGroup.REDSTONE,LASER_NAME)

    //flag
    public static final String FLAG_NAME = "flag";
    public static final RegistryObject<Block> FLAG = BLOCKS.register(FLAG_NAME,()-> new FlagBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.AIR)
                    .hardnessAndResistance(1f, 1f)
                    .sound(SoundType.WOOD)
                    .harvestTool(ToolType.AXE)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<FlagBlockTile>> FLAG_TILE = TILES.register(FLAG_NAME,()->  TileEntityType.Builder.create(
            FlagBlockTile::new, FLAG.get()).build(null));
    public static final RegistryObject<Item> FLAG_ITEM = ITEMS.register(FLAG_NAME,()-> new BlockItem(FLAG.get(),
            new Item.Properties().group(null)
    ));//getTab(ItemGroup.DECORATIONS,FLAG_NAME)


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
    public static final RegistryObject<TileEntityType<?>> DRAWERS_TILE = TILES.register(ITEM_SHELF_NAME,()->  TileEntityType.Builder.create(DrawersBlockTile::new, DRAWERS).build(null));
    public static final RegistryObject<Item> DRAWERS_ITEM = ITEMS.register(SCONCE_NAME,()-> new BlockItem(DRAWERS.get(),
            new Item.Properties().group(null)));
    */
    //sconce
    //normal
    public static final String SCONCE_NAME = "sconce";
    public static final RegistryObject<Block> SCONCE = BLOCKS.register(SCONCE_NAME,()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .sound(SoundType.LANTERN), ()->ParticleTypes.FLAME));
    public static final RegistryObject<Block> SCONCE_WALL = BLOCKS.register("sconce_wall",()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .lootFrom(SCONCE.get())
                    .sound(SoundType.LANTERN), ()->ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM = ITEMS.register(SCONCE_NAME, ()-> new WallOrFloorItem(SCONCE.get(), SCONCE_WALL.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,SCONCE_NAME))));

    //soul
    public static final String SCONCE_NAME_SOUL = "sconce_soul";
    public static final RegistryObject<Block> SCONCE_SOUL = BLOCKS.register(SCONCE_NAME_SOUL,()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .sound(SoundType.LANTERN), ()->ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_SOUL = BLOCKS.register("sconce_wall_soul",()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 10 : 0)
                    .lootFrom(SCONCE_SOUL.get())
                    .sound(SoundType.LANTERN), ()->ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_SOUL = ITEMS.register(SCONCE_NAME_SOUL,()-> new WallOrFloorItem(SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,SCONCE_NAME_SOUL))
    ));
    //TODO: add config. also add burn times for wood stuff
    //optional: endergetic
    public static final String SCONCE_NAME_ENDER = "sconce_ender";
    public static final RegistryObject<Block> SCONCE_ENDER = BLOCKS.register(SCONCE_NAME_ENDER,()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .sound(SoundType.LANTERN), ENDERGETIC_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_ENDER = BLOCKS.register("sconce_wall_ender",()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .lootFrom(SCONCE_ENDER.get())
                    .sound(SoundType.LANTERN), ENDERGETIC_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_ENDER = ITEMS.register(SCONCE_NAME_ENDER,()-> new WallOrFloorItem(SCONCE_ENDER.get(), SCONCE_WALL_ENDER.get(),
            (new Item.Properties()).group(ModList.get().isLoaded("endergetic")?(getTab(ItemGroup.DECORATIONS,SCONCE_NAME_ENDER)):null)
    ));

    //green
    public static final String SCONCE_NAME_GREEN = "sconce_green";
    public static final RegistryObject<Block> SCONCE_GREEN = BLOCKS.register(SCONCE_NAME_GREEN,()-> new SconceBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .doesNotBlockMovement()
                    .zeroHardnessAndResistance()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .sound(SoundType.LANTERN), GREEN_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_GREEN = BLOCKS.register("sconce_wall_green",()-> new SconceWallBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 13 : 0)
                    .lootFrom(SCONCE_GREEN.get())
                    .sound(SoundType.LANTERN), GREEN_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_GREEN = ITEMS.register(SCONCE_NAME_GREEN,()-> new WallOrFloorItem(SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,SCONCE_NAME_GREEN))));


    //candelabra
    public static final String CANDELABRA_NAME = "candelabra";
    public static final RegistryObject<Block> CANDELABRA = BLOCKS.register(CANDELABRA_NAME,()-> new CandelabraBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.GOLD)
                    .hardnessAndResistance(4f, 5f)
                    .sound(SoundType.METAL)
                    .notSolid()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM = ITEMS.register(CANDELABRA_NAME,()-> new BlockItem(CANDELABRA.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,CANDELABRA_NAME))));
    //silver
    public static final String CANDELABRA_NAME_SILVER = "candelabra_silver";
    public static final RegistryObject<Block> CANDELABRA_SILVER = BLOCKS.register(CANDELABRA_NAME_SILVER,()-> new CandelabraBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(4f, 5f)
                    .sound(SoundType.METAL)
                    .notSolid()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM_SILVER = ITEMS.register(CANDELABRA_NAME_SILVER,()-> new BlockItem(CANDELABRA_SILVER.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,CANDELABRA_NAME_SILVER))
    ));

    //item shelf
    public static final String ITEM_SHELF_NAME = "item_shelf";
    public static final RegistryObject<Block> ITEM_SHELF = BLOCKS.register(ITEM_SHELF_NAME,()-> new ItemShelfBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .hardnessAndResistance(0.75f, 0.1f)
                    .notSolid()
                    .doesNotBlockMovement()
                    .harvestTool(ToolType.AXE)
    ));
    public static final RegistryObject<TileEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = TILES.register(ITEM_SHELF_NAME,()-> TileEntityType.Builder.create(
            ItemShelfBlockTile::new, ITEM_SHELF.get()).build(null));
    public static final RegistryObject<Item> ITEM_SHELF_ITEM = ITEMS.register(ITEM_SHELF_NAME,()-> new BurnableBlockItem(ITEM_SHELF.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,ITEM_SHELF_NAME)),100
    ));

    //cog block
    public static final String COG_BLOCK_NAME = "cog_block";
    public static final RegistryObject<Block> COG_BLOCK = BLOCKS.register(COG_BLOCK_NAME,()-> new CogBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3f, 6f)
                    .sound(SoundType.METAL)
                    .setRequiresTool()
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)
    ));
    public static final RegistryObject<Item> COG_BLOCK_ITEM = ITEMS.register(COG_BLOCK_NAME,()-> new BlockItem(COG_BLOCK.get(),
            new Item.Properties().group(getTab(ItemGroup.REDSTONE,COG_BLOCK_NAME))
    ));

    //stone lamp

    public static final String STONE_LAMP_NAME = "stone_lamp";
    public static final RegistryObject<Block> STONE_LAMP = BLOCKS.register(STONE_LAMP_NAME,()-> new Block(
            AbstractBlock.Properties.create(Material.ROCK,MaterialColor.YELLOW)
                    .hardnessAndResistance(1.5f, 6f)
                    .setLightLevel((s) -> 15)
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Item> STONE_LAMP_ITEM = ITEMS.register(STONE_LAMP_NAME,()-> new BlockItem(STONE_LAMP.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,STONE_LAMP_NAME))
    ));

    //cage
    public static final String CAGE_NAME = "cage";
    public static final RegistryObject<Block> CAGE = BLOCKS.register(CAGE_NAME,()-> new CageBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(3f, 6f)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<CageBlockTile>> CAGE_TILE = TILES.register(CAGE_NAME,()->  TileEntityType.Builder.create(
            CageBlockTile::new, CAGE.get()).build(null));
    public static final RegistryObject<Item> CAGE_ITEM = ITEMS.register("cage_full",()-> new CageItem(CAGE.get(),
            new Item.Properties().maxStackSize(1).setISTER(()-> CageItemRenderer::new)
                    .group(null), Registry.EMPTY_CAGE_ITEM));
    public static final RegistryObject<Item> EMPTY_CAGE_ITEM = ITEMS.register(CAGE_NAME,()-> new EmptyCageItem(CAGE.get(),
            new Item.Properties().maxStackSize(16).group(getTab(ItemGroup.DECORATIONS,CAGE_NAME)), Registry.CAGE_ITEM, EmptyCageItem.CageWhitelist.CAGE
    ));

    //sconce lever
    public static final String SCONCE_LEVER_NAME = "sconce_lever";
    public static final RegistryObject<Block> SCONCE_LEVER = BLOCKS.register(SCONCE_LEVER_NAME,()-> new SconceLeverBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .sound(SoundType.LANTERN), ()->ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_LEVER_ITEM = ITEMS.register(SCONCE_LEVER_NAME,()-> new BlockItem(SCONCE_LEVER.get(),
            (new Item.Properties()).group(getTab(ItemGroup.REDSTONE,SCONCE_LEVER_NAME))
    ));

    //globe
    public static final String GLOBE_NAME = "globe";
    public static final RegistryObject<Block> GLOBE = BLOCKS.register(GLOBE_NAME,()-> new GlobeBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.ORANGE_TERRACOTTA)
                    .sound(SoundType.METAL)
                    .hardnessAndResistance(2, 4)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .harvestLevel(1)
    ));
    public static final RegistryObject<TileEntityType<GlobeBlockTile>> GLOBE_TILE = TILES.register(GLOBE_NAME,()->  TileEntityType.Builder.create(
            GlobeBlockTile::new, GLOBE.get()).build(null));
    public static final RegistryObject<Item> GLOBE_ITEM = ITEMS.register(GLOBE_NAME,()-> new BlockItem(GLOBE.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,GLOBE_NAME)).rarity(Rarity.RARE)
    ));

    //hourglass
    public static final String HOURGLASS_NAME = "hourglass";
    public static final RegistryObject<Block> HOURGLASS = BLOCKS.register(HOURGLASS_NAME,()-> new HourGlassBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.GOLD)
                    .sound(SoundType.METAL)
                    .hardnessAndResistance(2, 4)
                    .harvestTool(ToolType.PICKAXE)
                    .setRequiresTool()
                    .harvestLevel(1)
    ));
    public static final RegistryObject<TileEntityType<HourGlassBlockTile>> HOURGLASS_TILE = TILES.register(HOURGLASS_NAME,()->  TileEntityType.Builder.create(
            HourGlassBlockTile::new, HOURGLASS.get()).build(null));
    public static final RegistryObject<Item> HOURGLASS_ITEM = ITEMS.register(HOURGLASS_NAME,()-> new BlockItem(HOURGLASS.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,HOURGLASS_NAME))
    ));


    //sack
    public static final String SACK_NAME = "sack";
    public static final RegistryObject<Block> SACK = BLOCKS.register(SACK_NAME,()-> new SackBlock(
            AbstractBlock.Properties.create(Material.WOOL, MaterialColor.WOOD)
                    .hardnessAndResistance(1F)
                    .sound(SoundType.CLOTH)
    ));
    public static final RegistryObject<TileEntityType<SackBlockTile>> SACK_TILE = TILES.register(SACK_NAME,()->  TileEntityType.Builder.create(
            SackBlockTile::new, SACK.get()).build(null));

    public static final RegistryObject<ContainerType<SackContainer>> SACK_CONTAINER = CONTAINERS.register(SACK_NAME,()-> IForgeContainerType.create(
            SackContainer::new));

    public static final RegistryObject<Item> SACK_ITEM = ITEMS.register(SACK_NAME,()-> new SackItem(SACK.get(),
            new Item.Properties().group(getTab(ItemGroup.DECORATIONS,SACK_NAME)).maxStackSize(1)
    ));

    //candle holder
    public static final String CANDLE_HOLDER_NAME = "candle_holder";
    public static final RegistryObject<Block> CANDLE_HOLDER = BLOCKS.register(CANDLE_HOLDER_NAME,()-> new CandleHolderBlock(
            AbstractBlock.Properties.create(Material.MISCELLANEOUS)
                    .zeroHardnessAndResistance()
                    .doesNotBlockMovement()
                    .setLightLevel((state) -> state.get(BlockStateProperties.LIT)? 14 : 0)
                    .sound(SoundType.LANTERN), ()->ParticleTypes.FLAME));
    public static final RegistryObject<Item> CANDLE_HOLDER_ITEM = ITEMS.register(CANDLE_HOLDER_NAME,()-> new BlockItem(CANDLE_HOLDER.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,CANDLE_HOLDER_NAME))
    ));

    //blackboard
    public static final String BLACKBOARD_NAME = "blackboard";
    public static final RegistryObject<Block> BLACKBOARD = BLOCKS.register(BLACKBOARD_NAME,()-> new BlackboardBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.IRON)
                    .hardnessAndResistance(2,3)
                    .setRequiresTool()
                    .harvestLevel(0)
                    .harvestTool(ToolType.PICKAXE)
    ));
    public static final RegistryObject<TileEntityType<BlackboardBlockTile>> BLACKBOARD_TILE = TILES.register(BLACKBOARD_NAME,()-> TileEntityType.Builder.create(
            BlackboardBlockTile::new, BLACKBOARD.get()).build(null));
    public static final RegistryObject<Item> BLACKBOARD_ITEM = ITEMS.register(BLACKBOARD_NAME,()-> new BlockItem(BLACKBOARD.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,BLACKBOARD_NAME)).setISTER(()-> BlackboardItemRenderer::new)
    ));

    //safe
    public static final String SAFE_NAME = "safe";
    public static final RegistryObject<Block> SAFE = BLOCKS.register(SAFE_NAME,()-> new SafeBlock(
            AbstractBlock.Properties.from(Blocks.NETHERITE_BLOCK)
                    .harvestLevel(3)
    ));
    public static final RegistryObject<TileEntityType<SafeBlockTile>> SAFE_TILE = TILES.register(SAFE_NAME,()-> TileEntityType.Builder.create(
            SafeBlockTile::new, SAFE.get()).build(null));
    public static final RegistryObject<Item> SAFE_ITEM = ITEMS.register(SAFE_NAME,()-> new BlockItem(SAFE.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,SAFE_NAME)).maxStackSize(1).isImmuneToFire()
    ));


    //flute
    public static final String FLUTE_NAME = "flute";
    public static final RegistryObject<Item> FLUTE_ITEM = ITEMS.register(FLUTE_NAME,()-> new Flute((new Item.Properties())
            .group(getTab(ItemGroup.TOOLS,FLUTE_NAME)).maxStackSize(1).maxDamage(32)));

    //copper lantern
    public static final String COPPER_LANTERN_NAME = "copper_lantern";
    public static final RegistryObject<Block> COPPER_LANTERN = BLOCKS.register(COPPER_LANTERN_NAME,()-> new OilLanternBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.ORANGE_TERRACOTTA)
                    .hardnessAndResistance(3.5f)
                    .setRequiresTool()
                    .setLightLevel((state)->state.get(OilLanternBlock.LIT)?15:0)
                    .sound(SoundType.LANTERN)
                    .notSolid()
    ));

    public static final RegistryObject<Item> COPPER_LANTERN_ITEM = ITEMS.register(COPPER_LANTERN_NAME,()-> new BlockItem(COPPER_LANTERN.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,COPPER_LANTERN_NAME))
    ));

    //crimson lantern
    public static final String CRIMSON_LANTERN_NAME = "crimson_lantern";
    public static final RegistryObject<Block> CRIMSON_LANTERN = BLOCKS.register(CRIMSON_LANTERN_NAME,()-> new CrimsonLanternBlock(
            AbstractBlock.Properties.create(Material.IRON, MaterialColor.RED)
                    .hardnessAndResistance(1.5f)
                    .sound(SoundType.CLOTH)
                    .setLightLevel((state)->15)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<OilLanternBlockTile>> CRIMSON_LANTERN_TILE = TILES.register(CRIMSON_LANTERN_NAME,()-> TileEntityType.Builder.create(
            OilLanternBlockTile::new, CRIMSON_LANTERN.get()).build(null));
    public static final RegistryObject<Item> CRIMSON_LANTERN_ITEM = ITEMS.register(CRIMSON_LANTERN_NAME,()-> new BlockItem(CRIMSON_LANTERN.get(),
            (new Item.Properties()).group(null)
    ));

    public static final RegistryObject<TileEntityType<OilLanternBlockTile>> COPPER_LANTERN_TILE = TILES.register(COPPER_LANTERN_NAME,()-> TileEntityType.Builder.create(
            OilLanternBlockTile::new, COPPER_LANTERN.get()).build(null));

    //doormat
    public static final String DOORMAT_NAME = "doormat";
    public static final RegistryObject<Block> DOORMAT = BLOCKS.register(DOORMAT_NAME,()-> new DoormatBlock(
            AbstractBlock.Properties.create(Material.CARPET, MaterialColor.YELLOW)
                    .hardnessAndResistance(0.1F)
                    .sound(SoundType.CLOTH)
                    .notSolid()
    ));
    public static final RegistryObject<TileEntityType<DoormatBlockTile>> DOORMAT_TILE = TILES.register(DOORMAT_NAME,()-> TileEntityType.Builder.create(
            DoormatBlockTile::new, DOORMAT.get()).build(null));
    public static final RegistryObject<Item> DOORMAT_ITEM = ITEMS.register(DOORMAT_NAME,()-> new BurnableBlockItem(DOORMAT.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,DOORMAT_NAME)),134
    ));

    //hanging flower pot
    public static final String HANGING_FLOWER_POT_NAME = "hanging_flower_pot";
    public static final RegistryObject<Block> HANGING_FLOWER_POT = BLOCKS.register(HANGING_FLOWER_POT_NAME,()-> new HangingFlowerPotBlock(
            AbstractBlock.Properties.from(Blocks.FLOWER_POT)
    ));
    public static final RegistryObject<TileEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = TILES.register(HANGING_FLOWER_POT_NAME,()-> TileEntityType.Builder.create(
            HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()).build(null));
    public static final RegistryObject<Item> HANGING_FLOWER_POT_ITEM = ITEMS.register(HANGING_FLOWER_POT_NAME,()-> new BlockItem(HANGING_FLOWER_POT.get(),
            (new Item.Properties()).group(null)
    ));

    //double cake
    public static final String DOUBLE_CAKE_NAME = "double_cake";
    public static final RegistryObject<Block> DOUBLE_CAKE = BLOCKS.register(DOUBLE_CAKE_NAME,()-> new DoubleCakeBlock(
            AbstractBlock.Properties.from(Blocks.CAKE)
    ));
    //directional cake
    public static final String DIRECTIONAL_CAKE_NAME = "directional_cake";
    public static final RegistryObject<Block> DIRECTIONAL_CAKE = BLOCKS.register(DIRECTIONAL_CAKE_NAME,()-> new DirectionalCakeBlock(
            AbstractBlock.Properties.from(Blocks.CAKE)
            .lootFrom(Blocks.CAKE)
    ));
    public static final RegistryObject<Item> DIRECTIONAL_CAKE_ITEM = ITEMS.register(DIRECTIONAL_CAKE_NAME,()-> new BlockItem(DIRECTIONAL_CAKE.get(),
            (new Item.Properties()).group(null)
    ));

    //gold door
    public static final String GOLD_DOOR_NAME = "gold_door";
    public static final RegistryObject<Block> GOLD_DOOR = BLOCKS.register(GOLD_DOOR_NAME, ()-> new GoldDoorBlock(
            AbstractBlock.Properties.from(Blocks.GOLD_BLOCK)
            .harvestLevel(2)
            .notSolid()));
    public static final RegistryObject<Item> GOLD_DOOR_ITEM = ITEMS.register(GOLD_DOOR_NAME,()-> new BlockItem(GOLD_DOOR.get(),
            (new Item.Properties()).group(getTab(ItemGroup.REDSTONE,GOLD_DOOR_NAME))
    ));
    //gold trapdoor
    public static final String GOLD_TRAPDOOR_NAME = "gold_trapdoor";
    public static final RegistryObject<Block> GOLD_TRAPDOOR = BLOCKS.register(GOLD_TRAPDOOR_NAME,()-> new GoldTrapdoorBlock(
            AbstractBlock.Properties.from(Blocks.GOLD_BLOCK)
            .notSolid()
            .harvestLevel(2)
            .setAllowsSpawn((a,b,c,d)->false)));
    public static final RegistryObject<Item> GOLD_TRAPDOOR_ITEM = ITEMS.register(GOLD_TRAPDOOR_NAME,()-> new BlockItem(GOLD_TRAPDOOR.get(),
            (new Item.Properties()).group(getTab(ItemGroup.REDSTONE,GOLD_TRAPDOOR_NAME))
    ));


    //rope
    public static final String ROPE_NAME = "rope";
    public static final RegistryObject<Block> ROPE = BLOCKS.register(ROPE_NAME,()-> new RopeBlock(
            AbstractBlock.Properties.create(Material.WOOL)
                    .sound(SoundType.CLOTH)
                    .zeroHardnessAndResistance()
                    //.speedFactor(0.8f)
                    .notSolid()));
    public static final RegistryObject<Item> ROPE_ITEM = ITEMS.register(ROPE_NAME,()-> new BlockItem(ROPE.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,ROPE_NAME))
    ));

    //spikes
    public static final String BAMBOO_SPIKES_NAME = "bamboo_spikes";
    public static final RegistryObject<Block> BAMBOO_SPIKES = BLOCKS.register(BAMBOO_SPIKES_NAME,()-> new BambooSpikesBlock(
            AbstractBlock.Properties.create(Material.WOOD, MaterialColor.SAND)
                    .sound(SoundType.SCAFFOLDING)
                    .harvestTool(ToolType.AXE)
                    .setOpaque((a,b,c)->false)
                    .hardnessAndResistance(2)
                    .notSolid()));
    public static final RegistryObject<TileEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = TILES.register(BAMBOO_SPIKES_NAME,()-> TileEntityType.Builder.create(
            BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()).build(null));

    public static final RegistryObject<Item> BAMBOO_SPIKES_ITEM = ITEMS.register(BAMBOO_SPIKES_NAME,()-> new BurnableBlockItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).group(null),150
    ));
    public static final RegistryObject<Item> BAMBOO_SPIKES_TIPPED_ITEM = ITEMS.register("bamboo_spikes_tipped",()-> new BambooSpikesTippedItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).defaultMaxDamage(BambooSpikesBlockTile.MAX_CHARGES).setNoRepair().group(getTab(ItemGroup.BREWING,BAMBOO_SPIKES_NAME))
    ));

    //key
    public static final String KEY_NAME = "key";
    public static final RegistryObject<Item> KEY_ITEM = ITEMS.register(KEY_NAME,()-> new KeyItem(
            (new Item.Properties()).group(getTab(ItemGroup.TOOLS,KEY_NAME))
    ));

    //netherite doors
    public static final String NETHERITE_DOOR_NAME = "netherite_door";
    public static final RegistryObject<Block> NETHERITE_DOOR = BLOCKS.register(NETHERITE_DOOR_NAME,()-> new NetheriteDoorBlock(
            AbstractBlock.Properties.from(Blocks.NETHERITE_BLOCK)
                    .notSolid()
                    .harvestLevel(3)
    ));
    public static final RegistryObject<Item> NETHERITE_DOOR_ITEM = ITEMS.register(NETHERITE_DOOR_NAME,()-> new BlockItem(NETHERITE_DOOR.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,NETHERITE_DOOR_NAME)).isImmuneToFire()
    ));
    public static final String NETHERITE_TRAPDOOR_NAME = "netherite_trapdoor";
    public static final RegistryObject<Block> NETHERITE_TRAPDOOR = BLOCKS.register(NETHERITE_TRAPDOOR_NAME,()-> new NetheriteTrapdoorBlock(
            AbstractBlock.Properties.from(Blocks.NETHERITE_BLOCK)
                    .notSolid()
                    .setAllowsSpawn((a,b,c,d)->false)
                    .harvestLevel(3)
    ));
    public static final RegistryObject<Item> NETHERITE_TRAPDOOR_ITEM = ITEMS.register(NETHERITE_TRAPDOOR_NAME,()-> new BlockItem(NETHERITE_TRAPDOOR.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,NETHERITE_TRAPDOOR_NAME)).isImmuneToFire()
    ));

    //lock block
    public static final String LOCK_BLOCK_NAME = "lock_block";
    public static final RegistryObject<Block> LOCK_BLOCK = BLOCKS.register(LOCK_BLOCK_NAME,()-> new LockBlock(
            AbstractBlock.Properties.create(Material.IRON,MaterialColor.IRON)
                    .setRequiresTool()
                    .hardnessAndResistance(5.0F)
                    .sound(SoundType.METAL))
    );
    public static final RegistryObject<Item> LOCK_BLOCK_ITEM = ITEMS.register(LOCK_BLOCK_NAME,()-> new BlockItem(LOCK_BLOCK.get(),
            (new Item.Properties()).group(getTab(ItemGroup.REDSTONE,LOCK_BLOCK_NAME))
    ));
    public static final RegistryObject<TileEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = TILES.register("key_lockable_tile",()-> TileEntityType.Builder.create(
            KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()).build(null));

    //checker block
    public static final String CHECKER_BLOCK_NAME = "checker_block";
    public static final RegistryObject<Block> CHECKER_BLOCK = BLOCKS.register(CHECKER_BLOCK_NAME,()-> new Block(
            AbstractBlock.Properties.create(Material.ROCK)
                    .setRequiresTool()
                    .hardnessAndResistance(1.5F, 6.0F))
    );
    public static final RegistryObject<Item> CHECKER_ITEM = ITEMS.register(CHECKER_BLOCK_NAME,()-> new BlockItem(CHECKER_BLOCK.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS, CHECKER_BLOCK_NAME))
    ));

    //pancakes
    public static final String PANCAKE_NAME = "pancake";
    public static final RegistryObject<Block> PANCAKE = BLOCKS.register(PANCAKE_NAME,()-> new PancakeBlock(
            AbstractBlock.Properties.create(Material.CAKE,MaterialColor.ORANGE_TERRACOTTA)
                    .hardnessAndResistance(0.5F)
                    .sound(SoundType.CLOTH))
    );
    public static final RegistryObject<Item> PANCAKE_ITEM = ITEMS.register(PANCAKE_NAME,()-> new BlockItem(PANCAKE.get(),
            (new Item.Properties()).group(getTab(ItemGroup.FOOD,PANCAKE_NAME))
    ));

    //flax
    public static final String FLAX_NAME = "flax";
    public static final RegistryObject<Block> FLAX = BLOCKS.register(FLAX_NAME,()-> new FlaxBlock(
            AbstractBlock.Properties.from(Blocks.ROSE_BUSH)
                    .tickRandomly()
                    .zeroHardnessAndResistance()
                    .sound(SoundType.CROP))
    );
    public static final RegistryObject<Item> FLAX_ITEM = ITEMS.register(FLAX_NAME,()-> new Item(
            (new Item.Properties()).group(getTab(ItemGroup.MISC,FLAX_NAME))));
    public static final RegistryObject<Item> FLAX_SEEDS_ITEM = ITEMS.register("flax_seeds",()-> new BlockNamedItem(FLAX.get(),
            (new Item.Properties()).group(getTab(ItemGroup.MISC,FLAX_NAME))));
    //pot
    public static final RegistryObject<Block> FLAX_POT = BLOCKS.register("potted_flax",()-> new FlowerPotBlock(
            () -> (FlowerPotBlock)Blocks.FLOWER_POT, FLAX, AbstractBlock.Properties.from(Blocks.FLOWER_POT)));

    /*
    //statue
    public static final String STATUE_NAME = "statue";
    public static final RegistryObject<Block> STATUE = BLOCKS.register(STATUE_NAME,()-> new StatueBlock(
            AbstractBlock.Properties.create(Material.ROCK)
                    .hardnessAndResistance(2)
                    .notSolid()));
    public static final RegistryObject<Item> STATUE_ITEM = ITEMS.register(STATUE_NAME,()-> new BlockItem(STATUE.get(),
            (new Item.Properties()).group(getTab(ItemGroup.DECORATIONS,STATUE_NAME))
    ));
    */


}
