package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.AmendmentsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

public class FireBehaviorsManager {
    private static final Map<Item, IFireItemBehavior> CANNON_FIRE_BEHAVIORS = new Object2ObjectOpenHashMap<>();
    private static final IFireItemBehavior CANNON_DEFAULT = new AlternativeBehavior(
            new GenericProjectileBehavior(), new SlingshotBehavior());

    private static final Map<Item, IFireItemBehavior> PRESENT_FIRE_BEHAVIORS = new IdentityHashMap<>();
    private static final IFireItemBehavior PRESENT_DEFAULT =
            new AlternativeBehavior(new GenericProjectileBehavior(), new SpitItemBehavior());


    synchronized public static void registerCannonBehavior(ItemLike pItem, IFireItemBehavior pBehavior) {
        CANNON_FIRE_BEHAVIORS.put(pItem.asItem(), pBehavior);
    }

    public static IFireItemBehavior getCannonBehavior(ItemLike item) {
        return CANNON_FIRE_BEHAVIORS.getOrDefault(item, CANNON_DEFAULT);
    }

    synchronized public static void registerPresentBehavior(ItemLike pItem, IFireItemBehavior pBehavior) {
        PRESENT_FIRE_BEHAVIORS.put(pItem.asItem(), pBehavior);
    }

    public static IFireItemBehavior getPresentBehavior(ItemLike item) {
        return PRESENT_FIRE_BEHAVIORS.getOrDefault(item, PRESENT_DEFAULT);
    }

    @ApiStatus.Internal
    public static void registerBehaviors(RegistryAccess access) {
        CANNON_FIRE_BEHAVIORS.clear();
        PRESENT_FIRE_BEHAVIORS.clear();

        IFireItemBehavior tnt = new TntBehavior();
        IFireItemBehavior spawnEgg = new SpawnEggBehavior();
        IFireItemBehavior spawnArmorStand = new SpawnMobBehavior(EntityType.ARMOR_STAND);
        IFireItemBehavior firework = new FireworkBehavior();
        IFireItemBehavior enderPearl = new EnderPearlBehavior();
        IFireItemBehavior popper = new PopperBehavior();
        IFireItemBehavior fireBall = new SimpleProjectileBehavior<>(EntityType.SMALL_FIREBALL, ProjectileStats.FIREBALL_SPEED);
        IFireItemBehavior cannonBall = new SimpleProjectileBehavior<>(ModEntities.CANNONBALL.get(), ProjectileStats.CANNONBALL_SPEED);

        for (Item i : BuiltInRegistries.ITEM) {
            if (i instanceof BlockItem bi && TntBehavior.isTNTLikeBlock(bi.getBlock().defaultBlockState())) {
                registerPresentBehavior(i, tnt);
                if (CommonConfigs.Functional.CANNON_EXPLODE_TNT.get() == CommonConfigs.TNTMode.IGNITE) {
                    registerCannonBehavior(i, tnt);
                }
            }

            if (i instanceof SpawnEggItem sp) {
                registerPresentBehavior(sp, spawnEgg);
                registerCannonBehavior(sp, spawnEgg);
            }
        }

        registerPresentBehavior(ModRegistry.CONFETTI_POPPER.get(), popper);
        registerCannonBehavior(ModRegistry.CONFETTI_POPPER.get(), popper);

        registerPresentBehavior(Items.ENDER_PEARL, enderPearl);
        registerCannonBehavior(Items.ENDER_PEARL, enderPearl);

        if (!CompatHandler.AMENDMENTS || !AmendmentsCompat.hasThrowableFireCharge()) {
            registerPresentBehavior(Items.FIRE_CHARGE, fireBall);
            registerCannonBehavior(Items.FIRE_CHARGE, fireBall);
        }

        registerCannonBehavior(ModRegistry.CANNONBALL.get(), cannonBall);

        registerPresentBehavior(Items.FIREWORK_ROCKET, firework);
        registerCannonBehavior(Items.FIREWORK_ROCKET, firework);

        registerPresentBehavior(ModRegistry.HAT_STAND.get(), new SkibidiBehavior());
        registerCannonBehavior(ModRegistry.HAT_STAND.get(), new SpawnMobBehavior(ModEntities.HAT_STAND.get()));

        registerPresentBehavior(Items.ARMOR_STAND, spawnArmorStand);
        registerCannonBehavior(Items.ARMOR_STAND, spawnArmorStand);
    }

}



