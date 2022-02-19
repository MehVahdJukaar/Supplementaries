package net.mehvahdjukaar.supplementaries.setup;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.selene.util.BlockSetHandler;
import net.mehvahdjukaar.selene.util.WoodSetType;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.common.items.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
//this is just stuff that could have been in registry class. split to make classes smaller
public class RegistryHelper {

    public static void initDynamicRegistry() {
        BlockSetHandler.addWoodRegistrationCallback(RegistryHelper::registerHangingSignBlocks, Block.class);
        BlockSetHandler.addWoodRegistrationCallback(RegistryHelper::registerHangingSignItems, Item.class);
        BlockSetHandler.addWoodRegistrationCallback(RegistryHelper::registerSignPostItems, Item.class);
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

    public static RegistryObject<Item> regItem(String name, Supplier<? extends Item> sup) {
        return ModRegistry.ITEMS.register(name, sup);
    }

    public static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group) {
        return regItem(blockSup.getId().getPath(), () -> new BlockItem(blockSup.get(), (new Item.Properties()).tab(group)));
    }

    public static RegistryObject<Item> regBlockItem(RegistryObject<Block> blockSup, CreativeModeTab group, int burnTime) {
        return regItem(blockSup.getId().getPath(), () -> new WoodBasedBlockItem(blockSup.get(), (new Item.Properties()).tab(group), burnTime));
    }

    public static RegistryObject<SimpleParticleType> regParticle(String name) {
        return ModRegistry.PARTICLES.register(name, () -> new SimpleParticleType(true));
    }

    public static RegistryObject<SoundEvent> makeSoundEvent(String name) {
        return ModRegistry.SOUNDS.register(name, () -> new SoundEvent(Supplementaries.res(name)));
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

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, ModRegistry.BLOCKS.register(name, () -> new CeilingBannerBlock(color,
                            BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                                    .strength(1.0F)
                                    .noCollission()
                                    .sound(SoundType.WOOD)
                                    .lootFrom(() -> BannerBlock.byColor(color))
                    )
            ));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, RegistryObject<Block>> makePresents(String baseName) {
        Map<DyeColor, RegistryObject<Block>> map = new LinkedHashMap<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, ModRegistry.BLOCKS.register(name, () -> new PresentBlock(color,
                    BlockBehaviour.Properties.of(Material.WOOL, color.getMaterialColor())
                            .strength(1.0F)
                            .sound(SoundType.WOOL))
            ));
        }
        map.put(null, ModRegistry.BLOCKS.register(baseName, () -> new PresentBlock(null,
                BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                        .strength(1.0F)
                        .sound(SoundType.WOOL))
        ));
        return map;
    }


    //presents
    public static Map<DyeColor, RegistryObject<Item>> makePresentsItems() {
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for (var entry : ModRegistry.PRESENTS.entrySet()) {
            DyeColor color = entry.getKey();
            var regObj = entry.getValue();
            map.put(color, ModRegistry.ITEMS.register(regObj.getId().getPath(),
                    () -> new PresentItem(regObj.get(), (new Item.Properties())
                            .tab(getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.PRESENT_NAME)))));
        }
        return map;
    }


    //hanging signs
    private static void registerHangingSignBlocks(RegistryEvent.Register<Block> event, Collection<WoodSetType> woodTypes) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (WoodSetType wood : woodTypes) {
            if (wood.shouldHaveBlockSet()) {
                String name = wood.getVariantId(ModRegistry.HANGING_SIGN_NAME);
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
    }

    public static void registerHangingSignItems(RegistryEvent.Register<Item> event, Collection<WoodSetType> woodTypes) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (var entry : ModRegistry.HANGING_SIGNS.entrySet()) {
            WoodSetType wood = entry.getKey();
            //should be there already since this is fired after block reg
            Block block = entry.getValue();
            Item item = new WoodBasedBlockItem(block,
                    new Item.Properties().stacksTo(16).tab(
                            getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.HANGING_SIGN_NAME)),
                    200,wood
            ).setRegistryName(block.getRegistryName());
            registry.register(item);
            ModRegistry.HANGING_SIGNS_ITEMS.put(wood, item);
        }

    }

    //sign posts
    public static void registerSignPostItems(RegistryEvent.Register<Item> event, Collection<WoodSetType> woodTypes) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (WoodSetType wood : woodTypes) {
            if (wood.shouldHaveBlockSet()) {
                String name = wood.getVariantId(ModRegistry.SIGN_POST_NAME);
                Item item = new SignPostItem(
                        new Item.Properties().stacksTo(16).tab(
                                getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.SIGN_POST_NAME)),
                        wood
                ).setRegistryName(Supplementaries.res(name));
                registry.register(item);
                ModRegistry.SIGN_POST_ITEMS.put(wood, (SignPostItem) item);
            }
        }
    }


}