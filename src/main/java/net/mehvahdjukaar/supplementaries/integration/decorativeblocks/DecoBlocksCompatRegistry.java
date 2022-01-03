package net.mehvahdjukaar.supplementaries.integration.decorativeblocks;

import lilypuree.decorative_blocks.blocks.BrazierBlock;
import lilypuree.decorative_blocks.blocks.PalisadeBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;


public class DecoBlocksCompatRegistry {

    public static final String CHANDELIER_ROPE_NAME = "rope_chandelier";
    public static final RegistryObject<Block> CHANDELIER_ROPE;

    public static final String SOUL_CHANDELIER_ROPE_NAME = "rope_soul_chandelier";
    public static final RegistryObject<Block> SOUL_CHANDELIER_ROPE;

    public static final String ENDER_CHANDELIER_ROPE_NAME = "rope_ender_chandelier";
    public static final RegistryObject<Block> ENDER_CHANDELIER_ROPE;

    public static final String GLOW_CHANDELIER_ROPE_NAME = "rope_glow_chandelier";
    public static final RegistryObject<Block> GLOW_CHANDELIER_ROPE;

    static {
        CHANDELIER_ROPE = ModRegistry.BLOCKS.register(CHANDELIER_ROPE_NAME, () ->
                new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                        .strength(0.3F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .lightLevel((state) -> 15), CompatObjects.CHANDELIER, () -> ParticleTypes.FLAME));

        SOUL_CHANDELIER_ROPE = ModRegistry.BLOCKS.register(SOUL_CHANDELIER_ROPE_NAME, () ->
                new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                        .strength(0.3F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
                        .lightLevel((state) -> 11), CompatObjects.SOUL_CHANDELIER, () -> ParticleTypes.SOUL_FIRE_FLAME));

        if (CompatHandler.deco_blocks_abnormals) {
            ENDER_CHANDELIER_ROPE = ModRegistry.BLOCKS.register(ENDER_CHANDELIER_ROPE_NAME, () ->
                    new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                            .strength(0.3F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .lightLevel((state) -> 15), CompatObjects.ENDER_CHANDELIER,
                            CompatObjects.ENDER_FLAME)
                            .setRegistryName(ENDER_CHANDELIER_ROPE_NAME));
        } else ENDER_CHANDELIER_ROPE = null;

        if (CompatHandler.much_more_mod_compat) {
            GLOW_CHANDELIER_ROPE = ModRegistry.BLOCKS.register(GLOW_CHANDELIER_ROPE_NAME, () ->
                    new RopeChandelierBlock(BlockBehaviour.Properties.of(Material.DECORATION)
                            .strength(0.3F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .lightLevel((state) -> 15), CompatObjects.GLOW_CHANDELIER,
                            CompatObjects.GLOW_FLAME));
        } else GLOW_CHANDELIER_ROPE = null;

    }

    public static void registerStuff() {
        //I just need to load this class to register all the needed stuff
    }

    public static boolean isBrazier(Block b) {
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
