package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.TntBlock;

public class FireBehaviorsManager {

    public static void registerBehaviors() {

        IFireItemBehavior tnt = new TntBehavior();
        IFireItemBehavior spawnEgg = new SpawnEggBehavior();
        IFireItemBehavior firework = new FireworkBehavior();
        IFireItemBehavior enderPearl = new EnderPearlBehavior();
        IFireItemBehavior popper = new PopperBehavior();

        for (Item i : BuiltInRegistries.ITEM) {
            if (i instanceof BlockItem bi && bi.getBlock() instanceof TntBlock) {
                TrappedPresentBlock.registerBehavior(i, tnt);
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

        TrappedPresentBlock.registerBehavior(Items.FIREWORK_ROCKET, firework);
        CannonBlock.registerBehavior(Items.FIREWORK_ROCKET, firework);

        TrappedPresentBlock.registerBehavior(ModRegistry.HAT_STAND.get(), new SkibidiBehavior());

        var nuke = CompatObjects.NUKE_BLOCK.get();
        if (nuke != null) {
            TrappedPresentBlock.registerBehavior(nuke, tnt);
        }
    }

}



