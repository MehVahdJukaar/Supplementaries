package net.mehvahdjukaar.supplementaries;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class SupplementariesClient {

    public static void initClient() {

    }

    @FunctionalInterface
    public interface EntityRendererReg {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    public static void onRegisterEntityRenderTypes(EntityRendererReg event) {

    }

    @FunctionalInterface
    public interface ParticleRendererReg {
        <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> particleFactory);
    }

    public static void onRegisterParticles(ParticleRendererReg event) {

    }


    @FunctionalInterface
    public interface BlockColorReg {
        void register(BlockColor color, Block block);
    }

    public static void onRegisterBlockColors(BlockColorReg event) {

    }

    @FunctionalInterface
    public interface ItemColorReg {
        void register(ItemColor color, ItemLike... block);
    }

    public static void onRegisterItemColors(ItemColorReg event) {

    }

}
