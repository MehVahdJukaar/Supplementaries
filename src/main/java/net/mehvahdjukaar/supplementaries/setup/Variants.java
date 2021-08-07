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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Variants {

    public static boolean hasWoodInstalled(IWoodType wood){
        return ModList.get().isLoaded(wood.getNamespace());
    }

    public static<T extends IForgeRegistryEntry<T>> Map<IWoodType, RegistryObject<T>> makeVariants(DeferredRegister<T> registry, String name, Supplier<T> supplier){
        Map<IWoodType, RegistryObject<T>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String s = name+"_"+wood;
            map.put(wood, registry.register(s, supplier));
        }
        return map;
    }

    //hanging signs
    public static Map<IWoodType, RegistryObject<Block>> makeHangingSingsBlocks(){
        Map<IWoodType, RegistryObject<Block>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String name = getHangingSignName(wood);
            map.put(wood, Registry.BLOCKS.register(name, ()-> new HangingSignBlock(
                    AbstractBlock.Properties.of(wood.getMaterial(), wood.getColor())
                            .strength(2f, 3f)
                            .sound(SoundType.WOOD)
                            .harvestTool(ToolType.AXE)
                            .noOcclusion()
                            .noCollission()
            )));
        }
        return map;
    }

    public static Map<IWoodType, RegistryObject<Item>> makeHangingSignsItems(){
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String name = getHangingSignName(wood);
            map.put(wood, Registry.ITEMS.register(name, ()-> new BurnableBlockItem(Registry.HANGING_SIGNS.get(wood).get(),
                    new Item.Properties().tab(!hasWoodInstalled(wood)?null:
                            Registry.getTab(ItemGroup.TAB_DECORATIONS,Registry.HANGING_SIGN_NAME)),200
            )));
        }
        return map;
    }

    public static String getHangingSignName(IWoodType type){
        return Registry.HANGING_SIGN_NAME+"_"+type.getRegName();
    }

    //sign posts
    public static Map<IWoodType, RegistryObject<Item>> makeSignPostItems(){
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String name = getSignPostName(wood);
            map.put(wood, Registry.ITEMS.register(name, ()-> new SignPostItem(
                    new Item.Properties().tab(!hasWoodInstalled(wood)?null:
                            Registry.getTab(ItemGroup.TAB_DECORATIONS,Registry.SIGN_POST_NAME)),wood
            )));
        }
        return map;
    }
    public static String getSignPostName(IWoodType type){
        return Registry.SIGN_POST_NAME+"_"+type.getRegName();
    }

    //flags

    public static Map<DyeColor, RegistryObject<Block>> makeFlagBlocks(String baseName){
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            String name = baseName+"_"+color.getName();
            map.put(color, Registry.BLOCKS.register(name, ()-> new FlagBlock(color,
                    AbstractBlock.Properties.of(Material.WOOD, color.getMaterialColor())
                            .strength(1.0F)
                            .noOcclusion()
                            .sound(SoundType.WOOD))
            ));
        }
        return map;
    }

    public static Map<DyeColor, RegistryObject<Item>> makeFlagItems(String baseName){
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            String name = baseName+"_"+color.getName();
            map.put(color, Registry.ITEMS.register(name, ()-> new FlagItem(Registry.FLAGS.get(color).get(),
                    new Item.Properties()
                            .stacksTo(16)
                            .setISTER(()-> FlagItemRenderer::new)
                            .tab(Registry.getTab(ItemGroup.TAB_DECORATIONS,Registry.FLAG_NAME))
            )));
        }
        return map;
    }

    //ceiling banners
    public static Map<DyeColor, RegistryObject<Block>> makeCeilingBanners(String baseName){
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            String name = baseName+"_"+color.getName();
            map.put(color, Registry.BLOCKS.register(name, ()-> new CeilingBannerBlock(color,
                    AbstractBlock.Properties.of(Material.WOOD, color.getMaterialColor())
                            .strength(1.0F)
                            .noCollission()
                            .sound(SoundType.WOOD)
                            .lootFrom(()->BannerBlock.byColor(color))
                    )
            ));
        }
        return map;
    }


    //ceiling banners
    public static Map<DyeColor, RegistryObject<Item>> makeCeilingBannersItems(){
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            map.put(color, Registry.regBlockItem(Registry.CEILING_BANNERS.get(color),null));
        }
        return map;
    }

    //presents
    public static Map<DyeColor, RegistryObject<Block>> makePresents(String baseName){
        Map<DyeColor, RegistryObject<Block>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            String name = baseName+"_"+color.getName();
            map.put(color, Registry.BLOCKS.register(name, ()-> new PresentBlock(color,
                            AbstractBlock.Properties.of(Material.WOOL, color.getMaterialColor())
                                    .strength(1.0F)
                                    .sound(SoundType.WOOL)
                    )
            ));
        }
        return map;
    }


    //presents
    public static Map<DyeColor, RegistryObject<Item>> makePresentsItems(){
        Map<DyeColor, RegistryObject<Item>> map = new HashMap<>();

        for(DyeColor color : DyeColor.values()){
            map.put(color, Registry.regBlockItem(Registry.PRESENTS.get(color),Registry.getTab(ItemGroup.TAB_DECORATIONS, Registry.PRESENT_NAME)));
        }
        return map;
    }









}
