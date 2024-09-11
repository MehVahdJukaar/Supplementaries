package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedProjectileEntity;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.TridentItem;

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

    float CANNON_SPEED_MULT = 1;
    float CANON_INACCURACY = 0;

    // same as snowballs
    float SLIMEBALL_GRAVITY = 0.03f;
    float SLIMEBALL_SPEED = 1.5f;

    float BOMB_GRAVITY = 0.04f;
    float BOMB_SPEED = 1.2f;

    float BRICKS_GRAVIY = 0.05f;
    float BRICKS_SPEED = 1;

    float CANNONBALL_GRAVITY = 0.1f;
    float CANNONBALL_SPEED = 1.2f;
    // throwable bricks
    //

}
