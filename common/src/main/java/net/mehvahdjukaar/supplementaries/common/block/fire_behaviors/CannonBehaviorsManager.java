package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CannonBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.Items;

public class CannonBehaviorsManager {

    public static void registerBehaviors() {
        CannonBlock.registerBehavior(Items.ENDER_PEARL, new EnderPearlBehavior());
        CannonBlock.registerBehavior(ModRegistry.CONFETTI_POPPER.get(), new PopperBehavior());
    }
}
