package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

public class EndermanHeadItem extends StandingAndWallBlockItem {
    public EndermanHeadItem(Block block, Block block2, Properties properties) {
        super(block, block2, properties, Direction.DOWN);
    }

    @ForgeOverride
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return true;
    }

}
