package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.entity.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.*;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.minecraft.world.entity.*;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.*;

public class ModEntities {

    public static void init() {
        RegHelper.addAttributeRegistration(ModEntities::registerEntityAttributes);
    }

    @EventCalled
    public static void registerEntityAttributes(RegHelper.AttributeEvent event) {
        event.register(ModEntities.RED_MERCHANT.get(), Mob.createMobAttributes());
        event.register(ModEntities.HAT_STAND.get(), LivingEntity.createLivingAttributes());
    }

    //entities
    public static final Supplier<EntityType<PearlMarker>> PEARL_MARKER = regEntity("pearl_marker",
            PearlMarker::new, MobCategory.MISC, 0.999F, 0.999F, 4, false, -1);

    //dispenser minecart
    public static final Supplier<EntityType<DispenserMinecartEntity>> DISPENSER_MINECART = regEntity(DISPENSER_MINECART_NAME, () ->
            EntityType.Builder.<DispenserMinecartEntity>of(DispenserMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F).clientTrackingRange(8));

    //hat stand
    public static final Supplier<EntityType<HatStandEntity>> HAT_STAND = regEntity(HAT_STAND_NAME,
            HatStandEntity::new, MobCategory.MISC, 10 / 16f, 0.75f, 10, false, 3);

    //red trader
    public static final Supplier<EntityType<RedMerchantEntity>> RED_MERCHANT = regEntity(RED_MERCHANT_NAME,
            RedMerchantEntity::new, MobCategory.CREATURE, 0.6F, 1.95F, 10, true, 3);


    //urn
    public static final Supplier<EntityType<FallingUrnEntity>> FALLING_URN = regEntity(FALLING_URN_NAME, () ->
            EntityType.Builder.<FallingUrnEntity>of(FallingUrnEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //ash
    public static final Supplier<EntityType<FallingAshEntity>> FALLING_ASH = regEntity(FALLING_ASH_NAME, () ->
            EntityType.Builder.<FallingAshEntity>of(FallingAshEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //sack
    public static final Supplier<EntityType<ImprovedFallingBlockEntity>> FALLING_SACK = regEntity(FALLING_SACK_NAME, () ->
            EntityType.Builder.<ImprovedFallingBlockEntity>of(ImprovedFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //TODO:check update interval and client tracking range
    //brick
    public static final Supplier<EntityType<ThrowableBrickEntity>> THROWABLE_BRICK = regEntity(THROWABLE_BRICK_NAME, () ->
            EntityType.Builder.<ThrowableBrickEntity>of(ThrowableBrickEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(5));

    //slimeball
    public static final Supplier<EntityType<SlimeBallEntity>> THROWABLE_SLIMEBALL = regEntity(THROWABLE_SLIMEBALL_NAME, () ->
            EntityType.Builder.<SlimeBallEntity>of(SlimeBallEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(5));

    //bomb
    public static final Supplier<EntityType<BombEntity>> BOMB = regEntity(BOMB_NAME, () ->
            EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(5));

    //cannonball
    public static final Supplier<EntityType<CannonBallEntity>> CANNONBALL = regEntity(CANNONBALL_NAME, () ->
            EntityType.Builder.<CannonBallEntity>of(CannonBallEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(1000));

    //rope arrow
    public static final Supplier<EntityType<RopeArrowEntity>> ROPE_ARROW = regEntity(ROPE_ARROW_NAME, () ->
            EntityType.Builder.<RopeArrowEntity>of(RopeArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(20));

    //slingshot projectile
    public static final Supplier<EntityType<SlingshotProjectileEntity>> SLINGSHOT_PROJECTILE = regEntity(SLINGSHOT_PROJECTILE_NAME, () ->
            EntityType.Builder.<SlingshotProjectileEntity>of(SlingshotProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(1)); //STOP GETTING IT OUT OF SYNC! either 1 or infinity aparently

    public static final Supplier<EntityType<CannonBoatEntity>> CANNON_BOAT = regEntity(CANNON_BOAT_NAME, () ->
            EntityType.Builder.<CannonBoatEntity>of(CannonBoatEntity::new, MobCategory.MISC)
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
    );

    //firefly

//    public static final String FIREFLY_NAME = "firefly";
//    private static final EntityType<FireflyEntity> FIREFLY_TYPE_RAW = (EntityType.Builder.of(FireflyEntity::new, MobCategory.AMBIENT)
//            .setShouldReceiveVelocityUpdates(true).setTrackingRange(12).setUpdateInterval(3)
//            .sized(0.3125f, 1f))
//            .build(FIREFLY_NAME);
//
//    public static final Supplier<EntityType<FireflyEntity>> FIREFLY_TYPE = ENTITIES.register(FIREFLY_NAME, () -> FIREFLY_TYPE_RAW);
//
//    public static final Supplier<Item> FIREFLY_SPAWN_EGG_ITEM = regItem(FIREFLY_NAME + "_spawn_egg", () ->
//            new ForgeSpawnEggItem(FIREFLY_TYPE, -5048018, -14409439, //-4784384, -16777216,
//                    new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, FIREFLY_NAME))));


    public static <T extends Entity> Supplier<EntityType<T>> regEntity(String name, Supplier<EntityType.Builder<T>> builder) {
        return RegHelper.registerEntityType(Supplementaries.res(name), () -> builder.get().build(name));
    }

    public static <T extends Entity> Supplier<EntityType<T>> regEntity(
            String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height,
            int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return RegHelper.registerEntityType(Supplementaries.res(name), () ->
                PlatHelper.newEntityType(name, factory, category, width, height,
                        clientTrackingRange, velocityUpdates, updateInterval));
    }


}
