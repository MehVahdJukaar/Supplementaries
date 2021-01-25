package net.mehvahdjukaar.supplementaries.setup.registration;

import net.mehvahdjukaar.supplementaries.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.Supplier;

public class Variants {

    public static<T extends IForgeRegistryEntry<T>> Map<IWoodType, RegistryObject<T>> makeVariants(DeferredRegister<T> registry, String name, Supplier<T> supplier){
        Map<IWoodType, RegistryObject<T>> map = new HashMap<>();

        for(IWoodType wood : VanillaWoodTypes.values()){
            String s = name+"_"+wood;
            map.put(wood, registry.register(s, supplier));
        }
        return map;
    }

    //hanging signs
    public static Map<IWoodType, RegistryObject<Block>> makeHangingSingsBlocks(){
        Map<IWoodType, RegistryObject<Block>> map = new HashMap<>();

        for(IWoodType wood : VanillaWoodTypes.values()){
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

        for(IWoodType wood : VanillaWoodTypes.values()){
            String name = getHangingSignName(wood);
            map.put(wood, Registry.ITEMS.register(name, ()-> new BlockItem(Registry.HANGING_SIGNS.get(wood).get(),
                    new Item.Properties().group(Registry.getTab(ItemGroup.DECORATIONS,Registry.HANGING_SIGN_NAME))
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
