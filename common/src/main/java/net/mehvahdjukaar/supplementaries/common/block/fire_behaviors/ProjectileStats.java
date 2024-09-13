package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.misc.explosion.BombExplosion;

public class ProjectileStats {

    // same as ender pearl & egg
    public static final float SNOWBALL_GRAVITY = 0.03f;
    public static final float SNOWBALL_SPEED = 1.5f;
    public static final float SNOWBALL_DISPENSER_SPEED = 1.1f;
    public static final float SNOWBALL_DISPENSER_INACCURACY = 6;

    public static final float POTION_GRAVITY = 0.05f;
    public static final float POTION_SPEED = 0.5f;
    public static final float POTION_DISPENSER_SPEED = 1.375f;
    public static final float POTION_DISPENSER_INACCURACY = 3;

    public static final float XP_BOTTLE_GRAVITY = 0.07f;
    public static final float XP_BOTTLE_SPEED = 0.7f;
    public static final float XP_BOTTLE_DISPENSER_SPEED = 1.375f;
    public static final float XP_BOTTLE_DISPENSER_INACCURACY = 3;

    public static final float ARROW_GRAVITY = 0.05f;
    public static final float ARROW_SPEED = 3.15f;
    public static final float ARROW_DISPENSER_SPEED = 1.1f;
    public static final float ARROW_DISPENSER_INACCURACY = 6;


    public static float DISPENSER_INACCURACY = 6;
    // calculated from snowball speed
    public static float DISPENSER_SPEED_SCALE = 1.1f / 1.5f;

    public static float CANNON_SPEED_MULT = 1;
    public static float CANON_INACCURACY = 0;

    public static float PRESENT_SPEED_MULT = 0.25f;
    public static float PRESENT_INACCURACY = 1;

    // same as snowballs. actually a bit slower
    public static float SLIMEBALL_GRAVITY = 0.03f;
    public static float SLIMEBALL_SPEED = 1.25f;
    public static float SLIMEBALL_DISPENSER_SPEED = SLIMEBALL_SPEED * DISPENSER_SPEED_SCALE;
    public static float SLIMEBALL_DISPENSER_INACCURACY = 6;

    public static float BOMB_GRAVITY = 0.04f;
    public static float BOMB_SPEED = 1.25f;
    public static float BOMB_DISPENSER_SPEED = BOMB_SPEED * DISPENSER_SPEED_SCALE;
    public static float BOMB_DISPENSER_INACCURACY = 6;

    public static float BRICKS_GRAVITY = 0.05f;
    public static float BRICKS_SPEED = 1;
    public static float BRICKS_DISPENSER_SPEED = BRICKS_SPEED * DISPENSER_SPEED_SCALE;
    public static float BRICKS_DISPENSER_INACCURACY = 7.5f;

    public static float CANNONBALL_GRAVITY = 0.035f;
    public static float CANNONBALL_SPEED = 0.9f;

    // todo: check thee last
    public static float SLINGSHOT_GRAVITY = 0.03f;
    public static float SLINGSHOT_SPEED = 0.9f;

    public static final float FIREBALL_SPEED = 1;

}
