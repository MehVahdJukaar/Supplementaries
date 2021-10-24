package net.mehvahdjukaar.supplementaries.compat.decorativeblocks;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;


public class DecoBlocksCompatRegistry {

    public static final String CHANDELIER_ROPE_NAME = "rope_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + CHANDELIER_ROPE_NAME)
    public static final Block CHANDELIER_ROPE = null;

    public static final String SOUL_CHANDELIER_ROPE_NAME = "rope_soul_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + SOUL_CHANDELIER_ROPE_NAME)
    public static final Block SOUL_CHANDELIER_ROPE = null;

    public static final String ENDER_CHANDELIER_ROPE_NAME = "rope_ender_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + ENDER_CHANDELIER_ROPE_NAME)
    public static final Block ENDER_CHANDELIER_ROPE = null;

    public static final String GLOW_CHANDELIER_ROPE_NAME = "rope_glow_chandelier";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + GLOW_CHANDELIER_ROPE_NAME)
    public static final Block GLOW_CHANDELIER_ROPE = null;
/*

    public static void registerBlocks(RegistryEvent.Register<Block> event){
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 15), CompatObjects.CHANDELIER, ()->ParticleTypes.FLAME)
                .setRegistryName(CHANDELIER_ROPE_NAME));

        reg.register(new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                .strength(0.3F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .lightLevel((state) -> 11), CompatObjects.SOUL_CHANDELIER, ()-> ParticleTypes.SOUL_FIRE_FLAME)
                .setRegistryName(SOUL_CHANDELIER_ROPE_NAME));

        if(CompatHandler.deco_blocks_abnormals){
            reg.register(new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                    .strength(0.3F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel((state) -> 15), CompatObjects.ENDER_CHANDELIER,
                    CompatObjects.ENDER_FLAME)
                    .setRegistryName(ENDER_CHANDELIER_ROPE_NAME));
        }
        if(CompatHandler.much_more_mod_compat){
            reg.register(new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                    .strength(0.3F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .lightLevel((state) -> 15), CompatObjects.GLOW_CHANDELIER,
                    CompatObjects.GLOW_FLAME)
                    .setRegistryName(GLOW_CHANDELIER_ROPE_NAME));
        }

    }
    */
    public static boolean isBrazier(Block b){
        return false;
        //return b instanceof BrazierBlock;
    }

    public static boolean canLightBrazier(BlockState state) {
        return false;
        //return isBrazier(state.getBlock()) && !state.getValue(BlockStateProperties.WATERLOGGED)
        //        && !state.getValue(BlockStateProperties.LIT);
    }

    public static boolean isPalisade(BlockState state) {
        return false;
        //return state.getBlock() instanceof PalisadeBlock;
    }


}
