package net.mehvahdjukaar.supplementaries.common.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.item.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FlagItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FlagItem extends WoodBasedBlockItem implements IColored, ICustomItemRendererProvider {

    public FlagItem(Block block, Properties properties) {
        super(block, properties, 300);
    }

    @Override
    public DyeColor getColor() {
        return ((FlagBlock) this.getBlock()).getColor();
    }

    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(stack, tooltip);
    }

    @Override
    public @Nullable Map<DyeColor, Supplier<Block>> getItemColorMap() {
        return ModRegistry.FLAGS;
    }

    @Override
    public Supplier<ItemStackRenderer> getRendererFactory() {
        return FlagItemRenderer::new;
    }
}
