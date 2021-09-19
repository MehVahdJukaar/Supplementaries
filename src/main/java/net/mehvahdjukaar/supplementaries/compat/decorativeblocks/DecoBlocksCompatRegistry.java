package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import com.lilypuree.decorative_blocks.blocks.BrazierBlock;
import com.lilypuree.decorative_blocks.blocks.PalisadeBlock;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
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

    public static final String ENDER_CHANDELIER_ROPE_NAME = "rope_ender_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID+":"+ENDER_CHANDELIER_ROPE_NAME)
    public static final Block ENDER_CHANDELIER_ROPE = null;

    public static final String GLOW_CHANDELIER_ROPE_NAME = "rope_glow_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID+":"+GLOW_CHANDELIER_ROPE_NAME)
    public static final Block GLOW_CHANDELIER_ROPE = null;


    public static void registerBlocks(RegistryEvent.Register<Block> event){
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 15), CompatObjects.CHANDELIER, ()->ParticleTypes.FLAME)
                .setRegistryName(CHANDELIER_ROPE_NAME));

        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 11), CompatObjects.SOUL_CHANDELIER, ()-> ParticleTypes.SOUL_FIRE_FLAME)
                .setRegistryName(SOUL_CHANDELIER_ROPE_NAME));

        if(CompatHandler.deco_blocks_abnormals){
            reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                    .strength(0.3F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel((state) -> 15), CompatObjects.ENDER_CHANDELIER,
                    CompatObjects.ENDER_FLAME)
                    .setRegistryName(ENDER_CHANDELIER_ROPE_NAME));
        }
        if(CompatHandler.much_more_mod_compat){
            reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                    .strength(0.3F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel((state) -> 15), CompatObjects.GLOW_CHANDELIER,
                    CompatObjects.GLOW_FLAME)
                    .setRegistryName(GLOW_CHANDELIER_ROPE_NAME));
        }

    }

    public static boolean isBrazier(Block b){
        return b instanceof BrazierBlock;
    }

    public static boolean canLightBrazier(BlockState state) {
        return isBrazier(state.getBlock()) && !state.getValue(BlockStateProperties.WATERLOGGED)
                && !state.getValue(BlockStateProperties.LIT);
    }

    public static boolean isPalisade(BlockState state) {
        return state.getBlock() instanceof PalisadeBlock;
    }
}
