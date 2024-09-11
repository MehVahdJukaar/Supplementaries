package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

public class ProjectileStats {

    // same as ender pearl & egg
    IBallistic.Data SNOWBALL = new IBallistic.Data(0.99F, 0.03F, 1.5F);
    ExtraData SNOWBALL_EXTRA = new ExtraData(1, 6, 1.1f);

    // same as xp bottle
    IBallistic.Data POTION = new IBallistic.Data(0.99F, 0.05F, 0.5f);
    ExtraData POTION_EXTRA = new ExtraData(1, 3, 1.375f);

    IBallistic.Data XP_BOTTLE = new IBallistic.Data(0.99F, 0.07F, 0.7f);
    ExtraData XP_BOTTLE_EXTRA = new ExtraData(1, 3, 1.375f);

    IBallistic.Data ARROW = new IBallistic.Data(0.99F, 0.05F, 0); //3.15F for crossbow
    ExtraData ARROW_EXTRA = new ExtraData(0, 6, 1.1f);


    record ExtraData(float inaccuracy, float dispenserInaccuracy, float dispenserInitialSpeed) {
    }

    public static float CANNON_SPEED_MULT = 1;
    public static float CANON_INACCURACY = 0;

    public static float PRESENT_SPEED_MULT = 0.25f;
    public static float PRESENT_INACCURACY = 1;

    // same as snowballs. actually a bit slower
    public static float SLIMEBALL_GRAVITY = 0.03f;
    public static float SLIMEBALL_SPEED = 1.25f;

    public static float BOMB_GRAVITY = 0.04f;
    public static float BOMB_SPEED = 1.25f;

    public static float BRICKS_GRAVITY = 0.05f;
    public static float BRICKS_SPEED = 1;

    public static float CANNONBALL_GRAVITY = 0.035f;
    public static float CANNONBALL_SPEED = 0.9f;
    // throwable bricks
    //
}
