package net.mehvahdjukaar.supplementaries.integration.neoforge;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryViewTooltip;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.violetmoon.quark.api.event.UsageTickerEvent;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.zeta.util.ItemNBTHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class QuarkClientCompatImpl {

    public static void initClient() {
        ClientHelper.addBlockEntityRenderersRegistration(QuarkClientCompat::registerEntityRenderers);
        NeoForge.EVENT_BUS.addListener(QuarkClientCompatImpl::onItemTooltipEvent);
        NeoForge.EVENT_BUS.addListener(QuarkClientCompatImpl::usageTickerCount);
        NeoForge.EVENT_BUS.addListener(QuarkClientCompatImpl::usageTickerStack);
        ClientHelper.addTooltipComponentRegistration(QuarkClientCompatImpl::registerTooltipComponent);
    }

    public static void registerTooltipComponent(ClientHelper.TooltipComponentEvent event) {
        event.register(InventoryViewTooltip.class, InventoryTooltipComponent::new);
    }

    private static final Supplier<SafeBlockTile> DUMMY_SAFE_TILE = Suppliers.memoize(() -> new SafeBlockTile(BlockPos.ZERO, ModRegistry.SAFE.get().defaultBlockState()));


    public static void onItemTooltipEvent(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (QuarkClientCompat.canRenderQuarkTooltip()) {
            Item item = stack.getItem();
            if (item instanceof SafeItem || item instanceof SackItem) {
                CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", false);
                if (cmp.contains("LootTable")) return;

                if (item instanceof SafeItem) {
                    DUMMY_SAFE_TILE.get().load(cmp);
                    Player player = Minecraft.getInstance().player;
                    if (!(player == null || DUMMY_SAFE_TILE.get().canPlayerOpen(Minecraft.getInstance().player, false))) {
                        return;
                    }
                }
                List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
                List<Either<FormattedText, TooltipComponent>> tooltipCopy = new ArrayList<>(tooltip);

                for (int i = 1; i < tooltipCopy.size(); i++) {
                    Either<FormattedText, TooltipComponent> either = tooltipCopy.get(i);
                    if (either.left().isPresent()) {
                        String s = either.left().get().getString();
                        if (!s.startsWith("§") || s.startsWith("§o"))
                            tooltip.remove(either);
                    }
                }
                if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown())
                    tooltip.add(1, Either.left(Component.translatable("quark.misc.shulker_box_shift")));
            }
        }
    }


    public static void usageTickerStack(UsageTickerEvent.GetStack event) {
        if (event.currentRealStack.getItem() instanceof LunchBoxItem li) {
            var data = li.getComponentKey(event.currentRealStack);
            event.setResultStack(data.getSelected());
        }
    }

    public static void usageTickerCount(UsageTickerEvent.GetCount event) {
        if (event.currentRealStack.getItem() instanceof LunchBoxItem li) {
            var data = li.getComponentKey(event.currentRealStack);
            event.setResultCount(data.getSelectedItemCount());
        } else if (event.currentRealStack.getItem() instanceof ProjectileWeaponItem && event.currentStack != event.currentRealStack) {
            //adds missing ones from quiver

            if (event.player instanceof IQuiverEntity qe) {
                var q = qe.supplementaries$getQuiver();
                if (!q.isEmpty()) {
                    SelectableContainerItem.AbstractData data = QuiverItem.getQuiverData(q);
                    if (data != null) {
                        //sanity check
                        ItemStack selected = data.getSelected();

                        if (event.currentStack.is(selected.getItem())) {
                            //just recomputes everything
                            int count = data.getSelectedItemCount();
                            Inventory inventory = event.player.getInventory();

                            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                                ItemStack stackAt = inventory.getItem(i);
                                if (selected.is(stackAt.getItem())) {
                                    count += stackAt.getCount();
                                }
                            }
                            event.setResultCount(count);
                        }
                    }
                }
            }
        }
    }


}
