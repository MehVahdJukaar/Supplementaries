package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;

public class FireBehaviorsManager {

    public static void registerBehaviors(RegistryAccess access) {
        CannonBlock.clearBehaviors();
        TrappedPresentBlock.clearBehaviors();

        IFireItemBehavior tnt = new TntBehavior();
        IFireItemBehavior spawnEgg = new SpawnEggBehavior();
        IFireItemBehavior firework = new FireworkBehavior();
        IFireItemBehavior enderPearl = new EnderPearlBehavior();
        IFireItemBehavior popper = new PopperBehavior();
        IFireItemBehavior fireBall = new SimpleProjectileBehavior<>(EntityType.SMALL_FIREBALL, ProjectileStats.FIREBALL_SPEED);
        IFireItemBehavior cannonBall = new SimpleProjectileBehavior<>(ModEntities.CANNONBALL.get(), ProjectileStats.CANNONBALL_SPEED);

        for (Item i : BuiltInRegistries.ITEM) {
            if (i instanceof BlockItem bi && TntBehavior.isTNTLikeBlock(bi.getBlock().defaultBlockState())) {
                TrappedPresentBlock.registerBehavior(i, tnt);
                if (CommonConfigs.Functional.CANNON_EXPLODE_TNT.get() == CommonConfigs.TNTMode.IGNITE) {
                    CannonBlock.registerBehavior(i, tnt);
                }
            }

            if (i instanceof SpawnEggItem sp) {
                TrappedPresentBlock.registerBehavior(sp, spawnEgg);
                CannonBlock.registerBehavior(sp, spawnEgg);
            }
        }

        TrappedPresentBlock.registerBehavior(ModRegistry.CONFETTI_POPPER.get(), popper);
        CannonBlock.registerBehavior(ModRegistry.CONFETTI_POPPER.get(), popper);

        TrappedPresentBlock.registerBehavior(Items.ENDER_PEARL, enderPearl);
        CannonBlock.registerBehavior(Items.ENDER_PEARL, enderPearl);

        if (!CompatHandler.AMENDMENTS || !AmendmentsCompat.hasThrowableFireCharge()) {
            TrappedPresentBlock.registerBehavior(Items.FIRE_CHARGE, fireBall);
            CannonBlock.registerBehavior(Items.FIRE_CHARGE, fireBall);
        }

        CannonBlock.registerBehavior(ModRegistry.CANNONBALL.get(), cannonBall);

        TrappedPresentBlock.registerBehavior(Items.FIREWORK_ROCKET, firework);
        CannonBlock.registerBehavior(Items.FIREWORK_ROCKET, firework);

        TrappedPresentBlock.registerBehavior(ModRegistry.HAT_STAND.get(), new SkibidiBehavior());

    }

}



