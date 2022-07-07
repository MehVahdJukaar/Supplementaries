package net.mehvahdjukaar.supplementaries.api;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

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
        return switch (Utils.getID(item).toString()) {
            case "supplementaries:jar" -> canBeCaughtWithJar();
            case "supplementaries:cage" -> canBeCaughtWithCage();
            default -> false;
        };
    }

    default boolean canBeCaughtWithJar(){
        return false;
    }

    default  boolean canBeCaughtWithCage(){
        return true;
    }

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
    default void tickInsideContainer(Level world, BlockPos pos, float entityScale, CompoundTag entityData) {
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
    default InteractionResult onPlayerInteract(Level world, BlockPos pos, Player player, InteractionHand hand, CompoundTag entityData) {
        return InteractionResult.PASS;
    }

    /**
     * @param waterlogged if the container is waterlogged. return true for fish it waterlogged
     * @return true if this mob should not touch the ground when in a container
     */
    default boolean isFlyingMob(boolean waterlogged) {
        return isFlyingMob();
    }

    default boolean isFlyingMob(){
        Entity entity = getEntity();
        return entity.isNoGravity() || entity instanceof FlyingAnimal ||
                entity.isIgnoringBlockTriggers() || entity instanceof WaterAnimal;
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
    default void onContainerWaterlogged(boolean waterlogged) {

    }

    /**
     * this is called every time a container block is created. Store these in your cap
     * @param width container width
     * @param height container height
     */
    default void setContainerDimensions(float width, float height){}

}
