package net.mehvahdjukaar.supplementaries.setup.registration;

import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.WoodTypes;
import net.mehvahdjukaar.supplementaries.items.BurnableBlockItem;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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

    public static boolean hasWood(IWoodType wood){
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
                    AbstractBlock.Properties.create(wood.getMaterial(), wood.getColor())
                            .hardnessAndResistance(2f, 3f)
                            .sound(SoundType.WOOD)
                            .harvestTool(ToolType.AXE)
                            .notSolid()
                            .doesNotBlockMovement()
            )));
        }
        return map;
    }

    public static Map<IWoodType, RegistryObject<Item>> makeHangingSignsItems(){
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String name = getHangingSignName(wood);
            map.put(wood, Registry.ITEMS.register(name, ()-> new BurnableBlockItem(Registry.HANGING_SIGNS.get(wood).get(),
                    new Item.Properties().group(Registry.getTab(!hasWood(wood)?null:
                            ItemGroup.DECORATIONS,Registry.HANGING_SIGN_NAME)),200
            )));
        }
        return map;
    }

    //sign posts
    public static Map<IWoodType, RegistryObject<Item>> makeSignPostItems(){
        Map<IWoodType, RegistryObject<Item>> map = new HashMap<>();

        for(IWoodType wood : WoodTypes.TYPES.values()){
            String name = getSignPostName(wood);
            map.put(wood, Registry.ITEMS.register(name, ()-> new SignPostItem(
                    new Item.Properties().group(!hasWood(wood)?null:
                            Registry.getTab(ItemGroup.DECORATIONS,Registry.SIGN_POST_NAME)),wood
            )));
        }
        return map;
    }


    public static String getHangingSignName(IWoodType type){
        return Registry.HANGING_SIGN_NAME+"_"+type.toString();
    }


    public static String getSignPostName(IWoodType type){
        return Registry.SIGN_POST_NAME+"_"+type.toString();
    }


}
