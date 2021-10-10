package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Author: MehVahdJukaar
 * You can copy this class into your mod and implement this interface into your entity class
 * or provide its capability, both work
 * <p>
 * note that all this class will need to store its entity instance to be able to work on it. Refer to BaseCatchableMobCap for a basic implementation
 * Such entity will only be visual tho since the actual entity that will be released is kept frozen and saved in nbt
 * The actual entity data that is stored inside the container is kept unaltered and isn't related to the entity passed here, which is only needed for display purposes
 * It is possible to access and modify such data in the two client/server sided methods here provided: tickInsideContainer() and onPlayerInteract().
 */
public interface ICatchableMob {

    /**
     * generic method. Implement if needed
     *
     * @param item catching item
     * @return can be caught
     */
    default boolean canBeCaughtWithItem(Item item) {
        switch (item.getRegistryName().toString()) {
            case "supplementaries:jar":
                return canBeCaughtWithJar();
            case "supplementaries:tinted_jar":
                return canBeCaughtWithTintedJar();
            case "supplementaries:cage":
                return canBeCaughtWithCage();
        }
        return false;
    }

    boolean canBeCaughtWithJar();

    boolean canBeCaughtWithTintedJar();

    boolean canBeCaughtWithCage();

    /**
     * the scale of a mob inside a jar/cage is determined by its hitbox. <br>
     * Here you can specify a value that will be summed to the mob hitbox width to create a new hitbox that better represents the mob actual render shape
     *
     * @return HitBox width increment
     */
    default float getHitBoxWidthIncrement() {
        return 0;
    }

    /**
     * the scale of a mob inside a jar/cage is determined by its HitBox. <br>
     * Here you can specify a value that will be summed to the mob HitBox width to create a new HitBox that better represents the mob actual render shape
     *
     * @return Height width increment
     */
    default float getHitBoxHeightIncrement() {
        return 0;
    }

    /**
     * block light level that the container should have when this mob is inside
     *
     * @return light level
     */
    default int getLightLevel() {
        return 0;
    }

    /**
     * called each tick while inside a container. You can add here particles, sounds or animations
     * Note: the mob tick counter shouldn't be touched as it's already incremented by default
     *
     * @param world       world
     * @param pos         world position
     * @param entityScale scale that the mob being rendered is at. useful for particles so they can be rendered accordingly
     * @param entityData  actual mob nbt that is stored in the container. Use this only if you want to store permanent data in your entity which will be kept when it will be released from the container.
     */
    default void tickInsideContainer(World world, BlockPos pos, float entityScale, CompoundNBT entityData) {
    }

    /**
     * called when a player right-clicks a container block that contains this mob. Use this to make the player interact with the mob itself.
     * can be used, for example, to make the player trade with the mob in case it was a villager
     *
     * @param world      world
     * @param pos        container block position
     * @param player     player that is interacting
     * @param entityData actual mob nbt that is stored in the container. Use this only if you want to store permanent data in your entity which will be kept when it will be released from the container.
     * @return Pass to do nothing. Success or Consume to prevent any further action
     */
    default ActionResultType onPlayerInteract(World world, BlockPos pos, PlayerEntity player, Hand hand, CompoundNBT entityData) {
        return ActionResultType.PASS;
    }

    /**
     * @return true if this mob should not touch the ground when in a container
     */
    default boolean isFlyingMob() {
        Entity entity = getEntity();
        return entity.isNoGravity() || entity instanceof IFlyingAnimal ||
                entity.isIgnoringBlockTriggers() || entity instanceof WaterMobEntity;
    }

    /**
     * @return the stored entity
     */
    Entity getEntity();

    /**
     * used by mobs that should always have water when in a jar
     *
     * @return should always render with water
     */
    default boolean shouldHaveWater() {
        return false;
    }

    /**
     * use this to react to the container being waterlogged. useful for water mobs to be set out of water
     *
     * @param waterlogged new block waterlogged state
     */
    //NYI
    default void onContainerWaterlogged(boolean waterlogged) {

    }

}
