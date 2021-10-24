package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.supplementaries.block.blocks.CeilingBannerBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.items.BurnableBlockItem;
import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Variants {

    public static boolean hasWoodInstalled(IWoodType wood) {
        return ModList.get().isLoaded(wood.getNamespace());
    }

    public static <T extends IForgeRegistryEntry<T>> Map<IWoodType, RegistryObject<T>> makeVariants(DeferredRegister<T> registry, String name, Supplier<T> supplier) {
        Map<IWoodType, RegistryObject<T>> map = new HashMap<>();

        for (IWoodType wood : WoodTypes.TYPES.values()) {
            String s = name + "_" + wood;
            map.put(wood, registry.register(s, supplier));
        }
        return map;
    }

    //hanging signs
    public static Map<IWoodType, RegistryObject<Block>> makeHangingSingsBlocks() {
        Map<IWoodType, RegistryObject<Block>> map = new HashMap<>();

        for (IWoodType wood : WoodTypes.TYPES.values()) {
            String name = getHangingSignName(wood);
            map.put(wood, ModRegistry.BLOCKS.register(name, () -> new HangingSignBlock(
                    BlockBehaviour.Properties.of(wood.getMaterial(), wood.getColor())
                            .strength(2f, 3f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .noCollission()
            )));
        }
        return map;
    }

    public static Map<IWoodType, RegistryObject<Item>> makeHangingSignsItems() {
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for (IWoodType wood : WoodTypes.TYPES.values()) {
            String name = getHangingSignName(wood);
            map.put(wood, ModRegistry.ITEMS.register(name, () -> new BurnableBlockItem(ModRegistry.HANGING_SIGNS.get(wood).get(),
                    new Item.Properties().tab(!hasWoodInstalled(wood) ? null :
                            ModRegistry.getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.HANGING_SIGN_NAME)), 200
            )));
        }
        return map;
    }

    public static String getHangingSignName(IWoodType type) {
        return ModRegistry.HANGING_SIGN_NAME + "_" + type.getRegName();
    }

    //sign posts
    public static Map<IWoodType, RegistryObject<Item>> makeSignPostItems() {
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for (IWoodType wood : WoodTypes.TYPES.values()) {
            String name = getSignPostName(wood);
            map.put(wood, ModRegistry.ITEMS.register(name, () -> new SignPostItem(
                    new Item.Properties().tab(!hasWoodInstalled(wood) ? null :
                            ModRegistry.getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.SIGN_POST_NAME)), wood
            )));
        }
        return map;
    }

    public static String getSignPostName(IWoodType type) {
        return ModRegistry.SIGN_POST_NAME + "_" + type.getRegName();
    }

    //flags

    public static Map<DyeColor, RegistryObject<Block>> makeFlagBlocks(String baseName) {
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, ModRegistry.BLOCKS.register(name, () -> new FlagBlock(color,
                    BlockBehaviour.Properties.of(Material.WOOD, color.getMaterialColor())
                            .strength(1.0F)
                            .noOcclusion()
                            .sound(SoundType.WOOD))
            ));
        }
        return map;
    }

    public static Map<DyeColor, RegistryObject<Item>> makeFlagItems(String baseName) {
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for (DyeColor color : DyeColor.values()) {
            String name = baseName + "_" + color.getName();
            map.put(color, ModRegistry.ITEMS.register(name, () -> new FlagItem(ModRegistry.FLAGS.get(color).get(),
                    new Item.Properties()
                            .stacksTo(16)
                            .tab(ModRegistry.getTab(CreativeModeTab.TAB_DECORATIONS, ModRegistry.FLAG_NAME))
            )));
        }
        return map;
    }

    //ceiling banners
    public static Map<DyeColor, RegistryObject<Block>> makeCeilingBanners(String baseName) {
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

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


    //ceiling banners
    public static Map<DyeColor, RegistryObject<Item>> makeCeilingBannersItems() {
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for (DyeColor color : DyeColor.values()) {
            map.put(color, ModRegistry.regBlockItem(ModRegistry.CEILING_BANNERS.get(color), null));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, RegistryObject<Block>> makePresents(String baseName) {
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

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
    public static Map<DyeColor, RegistryObject<Item>> makePresentsItems(){
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        /*
        for(DyeColor color : DyeColor.values()){
            //ModRegistry.getTab(ItemGroup.TAB_DECORATIONS, ModRegistry.PRESENT_NAME)
            map.put(color, ModRegistry.regBlockItem(ModRegistry.PRESENTS.get(color), ModRegistry.getTab(ItemGroup.TAB_DECORATIONS, ModRegistry.PRESENT_NAME)));
        }
        */
        map.put(null, ModRegistry.regBlockItem(ModRegistry.PRESENTS.get(null), null));

        return map;
    }

}