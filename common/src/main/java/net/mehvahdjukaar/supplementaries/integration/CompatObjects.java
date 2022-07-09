package net.mehvahdjukaar.supplementaries.integration;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class CompatObjects {

    //these can return null on get instead of throwing

    public static final Supplier<Block> CHANDELIER = makeCompatObject("decorative_blocks:chandelier", Registry.BLOCK);

    public static final Supplier<Block> SOUL_CHANDELIER = makeCompatObject("decorative_blocks:soul_chandelier", Registry.BLOCK);

    public static final Supplier<Block> GLOW_CHANDELIER = makeCompatObject("muchmoremodcompat:glow_chandelier", Registry.BLOCK);

    public static final Supplier<Block> ENDER_CHANDELIER = makeCompatObject("decorative_blocks_abnormals:ender_chandelier", Registry.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_LOG = makeCompatObject("autumnity:sappy_maple_log", Registry.BLOCK);

    public static final Supplier<Block> SAPPY_MAPLE_WOOD = makeCompatObject("autumnity:sappy_maple_wood", Registry.BLOCK);

    public static final Supplier<ParticleType<?>> ENDER_FLAME = makeCompatObject("endergetic:ender_flame", Registry.PARTICLE_TYPE);

    public static final Supplier<ParticleType<?>> GLOW_FLAME = makeCompatObject("infernalexp:glowstone_sparkle", Registry.PARTICLE_TYPE);

    public static final Supplier<Block> RICH_SOIL = makeCompatObject("farmersdelight:rich_soil", Registry.BLOCK);

    public static final Supplier<Block> RICH_SOUL_SOIL = makeCompatObject("nethers_delight:rich_soul_soil", Registry.BLOCK);

    public static final Supplier<ParticleType<?>> SHARPNEL = makeCompatObject("oreganized:lead_shrapnel", Registry.PARTICLE_TYPE);

    public static final Supplier<MobEffect> STUNNED_EFFECT = makeCompatObject("oreganized:stunned", Registry.MOB_EFFECT);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T> Supplier<T> makeCompatObject(String name, Registry<T> registry) {
        return Suppliers.memoize(() -> registry.getOptional(new ResourceLocation(name)).orElse(null));
    }

}
