package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import com.lilypuree.decorative_blocks.setup.Registration;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
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
    private static final ResourceLocation ENDER_CHANDELIER_RES = Supplementaries.res(ENDER_CHANDELIER_ROPE_NAME);

    public static Block getEnderRopeChandelier(){
        return ForgeRegistries.BLOCKS.getValue(ENDER_CHANDELIER_RES);
    }
    //@ObjectHolder(Supplementaries.MOD_ID+ENDER_CHANDELIER_ROPE_NAME)
    //public static final Block ENDER_CHANDELIER_ROPE = null;



    public static void registerBlocks(RegistryEvent.Register<Block> event){
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 15), Registration.CHANDELIER.get(), ()->ParticleTypes.FLAME)
                .setRegistryName(CHANDELIER_ROPE_NAME));

        reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 11), Registration.SOUL_CHANDELIER.get(), ()-> ParticleTypes.SOUL_FIRE_FLAME)
                .setRegistryName(SOUL_CHANDELIER_ROPE_NAME));

        if(CompatHandler.endergetic){
            reg.register(new RopeChandelierBlock(AbstractBlock.Properties.of(Material.DECORATION)
                    .strength(0.3F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel((state) -> 15), ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation("decorative_blocks_abnormals:ender_chandelier")),()->
                    (BasicParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("endergetic:ender_flame")))
                    .setRegistryName(ENDER_CHANDELIER_ROPE_NAME));
        }

    }



}
