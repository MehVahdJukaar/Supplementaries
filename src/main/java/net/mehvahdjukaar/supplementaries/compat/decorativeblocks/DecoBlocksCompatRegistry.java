package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;


public class DecoBlocksCompatRegistry {

    public static final String CHANDELIER_ROPE_NAME = "rope_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID+":"+CHANDELIER_ROPE_NAME)
    public static final Block CHANDELIER_ROPE = null;
    public static final String SOUL_CHANDELIER_ROPE_NAME = "rope_soul_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID+":"+SOUL_CHANDELIER_ROPE_NAME)
    public static final Block SOUL_CHANDELIER_ROPE = null;
    /*
    public static final String ENDER_CHANDELIER_ROPE_NAME = "rope_ender_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID+ENDER_CHANDELIER_ROPE_NAME)
    public static final Block ENDER_CHANDELIER_ROPE = null;
    */


    public static void registerBlocks(RegistryEvent.Register<Block> event){
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 15), false)
                .setRegistryName(CHANDELIER_ROPE_NAME));

        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 11), true)
                .setRegistryName(SOUL_CHANDELIER_ROPE_NAME));

    }



}
