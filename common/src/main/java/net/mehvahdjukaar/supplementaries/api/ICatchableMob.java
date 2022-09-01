package net.mehvahdjukaar.supplementaries.api;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
/**
 * Author: MehVahdJukaar
 * You can copy this class into your mod and implement this interface into your entity class
 * or provide its capability, both work
 */
public interface ICatchableMob {

    //what all mobs that don't have it implemented use
    ICatchableMob DEFAULT = new ICatchableMob() {
    };

    /**
     * Override this to have more control over when your entity can be caught. Default implementation uses tags
     *
     * @param item catching item
     * @return can be caught
     */
    //2
    default boolean canBeCaughtWithItem(Entity self, Item item, Player player) {
        if (item instanceof AbstractMobContainerItem containerItem) {
            //we went full circle lol
            if (self instanceof Slime slime) {
                if (slime.getSize() != 1) return false;
            }
            return containerItem.canItemCatch(self);
        }
        return false;
    }


    /**
     * block light level that the container should have when this mob is inside
     *
     * @return light level
     */
    default int getLightLevel(Level world, BlockPos pos) {
        return 0;
    }


    /**
     * this is called every time a container item is created
     * Also used for extra interaction on capture
     */
    default void onCaptured(Entity entity, Player player, ItemStack stack) {
    }

    /**
     * the scale of a mob inside a jar/cage is determined by its hitbox. <br>
     * Here you can specify a value that will be summed to the mob hitbox width to create a new hitbox that better represents the mob actual render shape
     *
     * @return HitBox width increment
     */
    default float getHitBoxWidthIncrement(Entity self) {
        return 0;
    }

    /**
     * the scale of a mob inside a jar/cage is determined by its HitBox. <br>
     * Here you can specify a value that will be summed to the mob HitBox width to create a new HitBox that better represents the mob actual render shape
     *
     * @return Height width increment
     */
    default float getHitBoxHeightIncrement(Entity self) {
        return 0;
    }

    /**
     * @param waterlogged if the container is waterlogged. return true for fish it waterlogged
     * @return true if this mob should not touch the ground when in a container
     */
    default boolean shouldHover(Entity self, boolean waterlogged) {
        return self.isNoGravity() || self instanceof FlyingAnimal ||
                self.isIgnoringBlockTriggers() || self instanceof WaterAnimal;
    }


    /**
     * used by mobs that should always have water when in a jar
     *
     * @return should always render with water
     */
    default Optional<Holder<SoftFluid>> shouldRenderWithFluid() {
        return Optional.empty();
    }

    /**
     * If this should be rendered as a 2d fish returns an index that's available on the fishies.png texture
     *
     * @return -1 for normal mobs
     */
    default int getFishTextureIndex() {
        return -1;
    }

    default boolean renderAs2DFish() {
        return getFishTextureIndex() == -1;
    }

    /**
     * Here you can return a custom Behavior instance that will give you more control over the behavior in cages and jars
     */
    default <T extends Entity> CapturedMobInstance<T> createCapturedMobInstance(T self, float containerWidth, float containerHeight) {
        return new CapturedMobInstance.Default<>(self, containerWidth, containerHeight);
    }

}
