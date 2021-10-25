package net.mehvahdjukaar.supplementaries.compat;

import net.minecraft.block.Block;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CompatObjects {
    //object holders

    /*
    @ObjectHolder("decorative_blocks:chandelier")
    public static final Block CHANDELIER = null;

    @ObjectHolder("decorative_blocks:soul_chandelier")

    public static final Block SOUL_CHANDELIER = null;

    @ObjectHolder("decorative_blocks_abnormals:ender_chandelier")
    public static final Block ENDER_CHANDELIER = null;

    @ObjectHolder("muchmoremodcompat:glow_chandelier")
    public static final Block GLOW_CHANDELIER = null;

    @ObjectHolder("infernalexp:glowstone_sparkle")
    public static final ParticleType<?> GLOW_FLAME = null;

    @ObjectHolder("endergetic:ender_flame")
    public static final ParticleType<?> ENDER_FLAME = null;

    */

    //these can return null on get instead of throwing

    public static final Supplier<Block> CHANDELIER = makeCompatObject("decorative_blocks:chandelier", ForgeRegistries.BLOCKS);

    public static final Supplier<Block> SOUL_CHANDELIER = makeCompatObject("decorative_blocks:soul_chandelier", ForgeRegistries.BLOCKS);

    public static final Supplier<Block> GLOW_CHANDELIER = makeCompatObject("muchmoremodcompat:glow_chandelier", ForgeRegistries.BLOCKS);

    public static final Supplier<Block> ENDER_CHANDELIER = makeCompatObject("decorative_blocks_abnormals:ender_chandelier", ForgeRegistries.BLOCKS);

    public static final Supplier<Block> SAPPY_MAPLE_LOG = makeCompatObject("autumnity:sappy_maple_log", ForgeRegistries.BLOCKS);

    public static final Supplier<Block> SAPPY_MAPLE_WOOD = makeCompatObject("autumnity:sappy_maple_wood", ForgeRegistries.BLOCKS);

    public static final Supplier<ParticleType<?>> ENDER_FLAME = makeCompatObject("endergetic:ender_flame", ForgeRegistries.PARTICLE_TYPES);

    public static final Supplier<ParticleType<?>> GLOW_FLAME = makeCompatObject("infernalexp:glowstone_sparkle", ForgeRegistries.PARTICLE_TYPES);

    public static final Supplier<Block> RICH_SOIL = makeCompatObject("farmersdelight:rich_soil", ForgeRegistries.BLOCKS);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T extends IForgeRegistryEntry<T>, U extends T> NullableRegistryObject<T,U> makeCompatObject(String name, IForgeRegistry<T> registry){
        return new NullableRegistryObject<>(getRegistryObject(name, registry));
    }

    private static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> getRegistryObject(String name, IForgeRegistry<T> registry){
        return RegistryObject.of(new ResourceLocation(name), registry);
    }


    private static class NullableRegistryObject<T extends IForgeRegistryEntry<T>, U extends T> implements Supplier<T>{

        private final RegistryObject<U> obj;

        private NullableRegistryObject(RegistryObject<U> obj) {
            this.obj = obj;
        }

        @Override
        public @Nullable T get() {
            return obj.orElse(null);
        }
    }
}
