package net.mehvahdjukaar.supplementaries.integration.quark;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;

import java.util.ArrayList;
import java.util.List;

// credits to Vazkii and Quark
public class QuarkTooltipPlugin {

    private static final BlockState DEFAULT_SAFE = ModRegistry.SAFE.get().defaultBlockState();
    private static final SafeBlockTile DUMMY_SAFE_TILE = new SafeBlockTile(BlockPos.ZERO, DEFAULT_SAFE);


    public static boolean canRenderTooltip() {
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class) &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }

    public static void onItemTooltipEvent(ItemTooltipEvent event) {
        if (canRenderTooltip()) {
            ItemStack stack = event.getItemStack();
            CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
            if (cmp != null && !cmp.contains("LootTable")) {
                Item i = stack.getItem();
                if (i == ModRegistry.SAFE_ITEM.get()) {
                    DUMMY_SAFE_TILE.load(cmp);
                    Player player = Minecraft.getInstance().player;
                    if (player == null || DUMMY_SAFE_TILE.canPlayerOpen(Minecraft.getInstance().player, false)) {
                        cleanupTooltip(event.getToolTip());
                    }
                } else if (i == ModRegistry.SACK_ITEM.get()) {
                    cleanupTooltip(event.getToolTip());
                }
            }
        }
    }

    private static void cleanupTooltip(List<Component> tooltip) {

        var tooltipCopy = new ArrayList<>(tooltip);

        for (int i = 1; i < tooltipCopy.size(); ++i) {
            Component component = tooltipCopy.get(i);
            String s = component.getString();
            if (!s.startsWith("§") || s.startsWith("§o")) {
                tooltip.remove(component);
            }
        }

        if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown()) {
            tooltip.add(1, new TranslatableComponent("quark.misc.shulker_box_shift"));
        }
    }


}

