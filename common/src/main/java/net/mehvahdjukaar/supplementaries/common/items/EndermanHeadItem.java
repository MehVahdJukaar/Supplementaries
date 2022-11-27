package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.BubbleBlockItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.EndermanHeadItemRenderer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class EndermanHeadItem extends StandingAndWallBlockItem   {
    public EndermanHeadItem(Block block, Block block2, Properties properties) {
        super(block, block2, properties);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return true;
    }

    //@Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return EndermanHeadItemRenderer::new;
    }

}
