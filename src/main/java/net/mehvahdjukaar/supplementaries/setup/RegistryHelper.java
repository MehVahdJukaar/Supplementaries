package net.mehvahdjukaar.supplementaries.setup;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.common.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
//this is just stuff that could have been in registry class. split to make classes smaller
public class RegistryHelper {

    public static void initDynamicRegistry() {
        BlockSetManager.addBlockSetRegistrationCallback(RegistryHelper::registerHangingSignBlocks, Block.class, WoodType.class);
        BlockSetManager.addBlockSetRegistrationCallback(RegistryHelper::registerHangingSignItems, Item.class, WoodType.class);
        BlockSetManager.addBlockSetRegistrationCallback(RegistryHelper::registerSignPostItems, Item.class, WoodType.class);
    }

    public static CreativeModeTab getTab(CreativeModeTab g, String regName) {
        if (RegistryConfigs.reg.isEnabled(regName)) {
            return ModRegistry.MOD_TAB == null ? g : ModRegistry.MOD_TAB;
        }
        return null;
    }

    public static CreativeModeTab getTab(String modId, CreativeModeTab g, String regName) {
        return ModList.get().isLoaded(modId) ? getTab(g, regName) : null;
    }


    public static RegistryObject<Block> regPlaceableItem(
            String name, Supplier<? extends Block> sup, String itemLocation, ForgeConfigSpec.BooleanValue config) {
        Supplier<Item> itemSupp = () -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemLocation));
        return regPlaceableItem(name, sup, itemSupp, config);
    }

    public static RegistryObject<Block> regPlaceableItem(
            String name, Supplier<? extends Block> sup, Supplier<? extends Item> itemSupplier, ForgeConfigSpec.BooleanValue config) {
        Supplier<Block> newSupp = () -> {
            Block b = sup.get();
            BlockPlacerItem.registerPlaceableItem(b, itemSupplier,config);
            return b;
        };
        return ModRegistry.BLOCKS.register(name, newSupp);
    }

    public static RegistryObject<Item> regItem(String name, Supplier<? extends Item> sup) {
        return ModRegistry.ITEMS.register(name, sup);
    }

    public static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group) {
        return regItem(blockSup.getId().getPath(), () -> new BlockItem(blockSup.get(), (new Item.Properties()).tab(group)));
    }

    public static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group, int burnTime) {
        return regItem(blockSup.getId().getPath(), () -> new WoodBasedBlockItem(blockSup.get(), (new Item.Properties()).tab(group), burnTime));
    }

    public static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group, String tagKey) {
        return regItem(blockSup.getId().getPath(), () -> new OptionalTagBlockItem(blockSup.get(), (new Item.Properties()).tab(group), tagKey));
    }

    public static RegistryObject<SimpleParticleType> regParticle(String name) {
        return ModRegistry.PARTICLES.register(name, () -> new SimpleParticleType(true));
    }

    public static RegistryObject<SoundEvent> makeSoundEvent(String name) {
        return ModRegistry.SOUNDS.register(name, () -> new SoundEvent(Supplementaries.res(name)));
    }

    public static<T extends Entity> RegistryObject<EntityType<T>> regEntity(String name, EntityType.Builder<T> builder) {
        return ModRegistry.ENTITIES.register(name, ()->builder.build(name));
    }

    //flags
    public static Map<DyeColor, RegistryObject<Block>> makeFlagBlocks(String baseName) {
        ImmutableMap.Builder<DyeColor, RegistryObject<Block>> builder = new ImmutableMap.Builder<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            builder.put(color, ModRegistry.BLOCKS.register(name, () -> new FlagBlock(color,
                    BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                            .strength(1.0F)
                            .noOcclusion()
                            .sound(SoundType.WOOD))
            ));
        }
        return builder.build();
    }


    public static Map<DyeColor, RegistryObject<Item>> makeFlagItems(String baseName) {
        ImmutableMap.Builder<DyeColor, RegistryObject<Item>> builder = new ImmutableMap.Builder<>();

        for (var entry : ModRegistry.FLAGS.entrySet()) {
            DyeColor color = entry.getKey();
            var regObj = entry.getValue();
            builder.put(color, ModRegistry.ITEMS.register(regObj.getId().getPath(),
                    () -> new FlagItem(regObj.get(),
                            new Item.Properties()
                                    .stacksTo(16)
                                    .tab(getTab(CreativeModeTab.TAB_DECORATIONS, baseName))
                    )));
        }
        return builder.build();
    }

    //ceiling banners
    public static Map<DyeColor, RegistryObject<Block>> makeCeilingBanners(String baseName) {
        Map<DyeColor, RegistryObject<Block>> map = new LinkedHashMap<>();
        //TODO: fix this not working
        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, regPlaceableItem(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                                    .lootFrom(() -> BannerBlock.byColor(color))
                    ), color.getName() + "_banner", ServerConfigs.tweaks.CEILING_BANNERS
            ));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, RegistryObject<Block>> makePresents(String baseName, BiFunction<DyeColor, BlockBehaviour.Properties, Block> presentFactory) {
        Map<DyeColor, RegistryObject<Block>> map = new LinkedHashMap<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, ModRegistry.BLOCKS.register(name, () -> presentFactory.apply(color,
                    BlockBehaviour.Properties.of(Material.WOOL, color.getMaterialColor())
                            .strength(1.0F)
                            .sound(SoundType.WOOL))
            ));
        }
        map.put(null, ModRegistry.BLOCKS.register(baseName, () -> presentFactory.apply(null,
                BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                        .strength(1.0F)
                        .sound(SoundType.WOOL))
        ));
        return map;
    }


    //presents
    public static Map<DyeColor, RegistryObject<Item>> makePresentsItems(Map<DyeColor, RegistryObject<Block>> presents,
                                                                        String name, CreativeModeTab tab) {
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for (var entry : presents.entrySet()) {
            DyeColor color = entry.getKey();
            var regObj = entry.getValue();
            map.put(color, ModRegistry.ITEMS.register(regObj.getId().getPath(),
                    () -> new PresentItem(regObj.get(), (new Item.Properties())
                            .tab(getTab(tab, name)), map)));
        }
        return map;
    }


    //hanging signs
    private static void registerHangingSignBlocks(RegistryEvent.Register<Block> event, Collection<WoodType> woodTypes) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(RegistryConstants.HANGING_SIGN_NAME);
            Block block = new HangingSignBlock(
                    BlockBehaviour.Properties.of(wood.material, wood.material.getColor())
                            .strength(2f, 3f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .noCollission(),
                    wood
            ).setRegistryName(Supplementaries.res(name));
            registry.register(block);
            ModRegistry.HANGING_SIGNS.put(wood, (HangingSignBlock) block);
        }
    }

    public static void registerHangingSignItems(RegistryEvent.Register<Item> event, Collection<WoodType> woodTypes) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (var entry : ModRegistry.HANGING_SIGNS.entrySet()) {
            WoodType wood = entry.getKey();
            //should be there already since this is fired after block reg
            Block block = entry.getValue();
            Item item = new WoodBasedBlockItem(block,
                    new Item.Properties().stacksTo(16).tab(
                            getTab(CreativeModeTab.TAB_DECORATIONS, RegistryConstants.HANGING_SIGN_NAME)),
                    200, wood
            ).setRegistryName(block.getRegistryName());
            registry.register(item);
            ModRegistry.HANGING_SIGNS_ITEMS.put(wood, item);
        }

    }

    //sign posts
    public static void registerSignPostItems(RegistryEvent.Register<Item> event, Collection<WoodType> woodTypes) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (WoodType wood : woodTypes) {
            String name = wood.getVariantId(RegistryConstants.SIGN_POST_NAME);
            Item item = new SignPostItem(
                    new Item.Properties().stacksTo(16).tab(
                            getTab(CreativeModeTab.TAB_DECORATIONS, RegistryConstants.SIGN_POST_NAME)),
                    wood
            ).setRegistryName(Supplementaries.res(name));
            registry.register(item);
            ModRegistry.SIGN_POST_ITEMS.put(wood, (SignPostItem) item);
        }
    }


}