package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.item.Item;

/**
 * implement this in your entity class
 */
public interface ICatchableMob {

    /**
     * generic method. Implement if needed
     * @param item catching item
     * @return can be caught
     */
    default boolean canBeCaughtWithItem(Item item){
        switch (item.getRegistryName().toString()){
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
     * @return HitBox width increment
     */
    default float getHitBoxWidthIncrement(){return 0;};

    /**
     * the scale of a mob inside a jar/cage is determined by its HitBox. <br>
     * Here you can specify a value that will be summed to the mob HitBox width to create a new HitBox that better represents the mob actual render shape
     * @return Height width increment
     */
    default float getHitBoxHeightIncrement(){return 0;};

    default int getLightLevel(){return 0;}

    /**
     * Called each tick while inside a cage or jar. You can add here particles, sounds or animations
     */
    default void tickInsideCageOrJar(){}

    /**
     * animation category used by the mob. Can be the following types:
     * DEFAULT for default behavior
     * AIR to make it stand in mid air like a flying animal (note that such mobs are set to this value by default)
     * LAND to force it to stand on the ground even if it is a flying animal
     * FLOATING to to make it stand in mid air and wobble up and down
     * don't use FISH category. Not yet properly implemented
     * @return animation category
     */
    default AnimationCategory getAnimationCategory(){return AnimationCategory.DEFAULT;};

    enum AnimationCategory {
        DEFAULT,
        FISH,
        LAND,
        AIR,
        FLOATING;

        public boolean isFlying() {
            return this == AIR||this == FLOATING;
        }
        public boolean isLand() {
            return this == LAND;
        }
        public boolean isFloating(){
            return this == FLOATING;
        }
        public boolean isFish() {
            return this == FISH;
        }
    }
}
