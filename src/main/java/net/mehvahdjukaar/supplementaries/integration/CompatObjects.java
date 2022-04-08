package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class CompatObjects {
    //object holders

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

    public static final Supplier<Block> RICH_SOUL_SOIL = makeCompatObject("nethers_delight:rich_soul_soil", ForgeRegistries.BLOCKS);

    public static final Supplier<ParticleType<?>> SHARPNEL = makeCompatObject("oreganized:lead_shrapnel", ForgeRegistries.PARTICLE_TYPES);

    public static final Supplier<MobEffect> STUNNED_EFFECT = makeCompatObject("oreganized:stunned", ForgeRegistries.MOB_EFFECTS);


    //public static final RegistryObject<Block> ENDER_CHANDELIER2 = getCompatObject()

    private static <T extends IForgeRegistryEntry<T>, U extends T> NullableRegistryObject<T, U> makeCompatObject(String name, IForgeRegistry<T> registry) {
        return new NullableRegistryObject<>(getRegistryObject(name, registry));
    }

    private static <T extends IForgeRegistryEntry<T>, U extends T> RegistryObject<U> getRegistryObject(String name, IForgeRegistry<T> registry) {
        return RegistryObject.create(new ResourceLocation(name), registry);
    }


    private record NullableRegistryObject<T extends IForgeRegistryEntry<T>, U extends T>(
            RegistryObject<U> obj) implements Supplier<T> {

        @Nullable
        @Override
        public T get() {
            return obj.orElse(null);
        }
    }
}
