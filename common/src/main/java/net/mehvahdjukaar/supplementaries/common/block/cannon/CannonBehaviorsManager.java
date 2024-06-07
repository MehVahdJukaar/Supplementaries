package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.Items;

public class CannonBehaviorsManager {

    public static void registerBehaviors() {
        CannonBlockTile.registerBehavior(Items.ENDER_PEARL, EnderPearlBehavior::new);
        PopperBehavior popper = new PopperBehavior();
        CannonBlockTile.registerBehavior(ModRegistry.CONFETTI_POPPER.get(), (level, stack) -> popper);
    }
}
