package net.mehvahdjukaar.supplementaries.integration.platform;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.components.LunchBaskedContent;
import net.mehvahdjukaar.supplementaries.common.items.components.QuiverContent;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryViewTooltip;
import net.mehvahdjukaar.supplementaries.integration.QuarkClientCompat;
import net.mehvahdjukaar.supplementaries.integration.quark.TaterInAJarTileRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.neoforged.neoforge.common.NeoForge;
import org.violetmoon.quark.api.event.UsageTickerEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;

public class QuarkClientCompatImpl {

    public static void initClient() {
        ClientHelper.addBlockEntityRenderersRegistration(QuarkClientCompat::registerEntityRenderers);
        NeoForge.EVENT_BUS.addListener(QuarkClientCompatImpl::usageTickerCount);
        NeoForge.EVENT_BUS.addListener(QuarkClientCompatImpl::usageTickerStack);
        ClientHelper.addTooltipComponentRegistration(QuarkClientCompatImpl::registerTooltipComponent);
    }

    public static void registerEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(QuarkCompatImpl.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(QuarkCompatImpl.TATER_IN_A_JAR.get(), RenderType.cutout());
    }

    public static boolean canRenderBlackboardTooltip() {
        return canRenderQuarkTooltip();
    }

    public static boolean canRenderQuarkTooltip() {
        return Quark.ZETA.modules.isEnabled(ImprovedTooltipsModule.class)
                && ImprovedTooltipsModule.shulkerTooltips &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }


    public static void registerTooltipComponent(ClientHelper.TooltipComponentEvent event) {
        event.register(InventoryViewTooltip.class, InventoryTooltipComponent::new);
    }

    /*
    public static void onItemTooltipEvent(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        boolean quarkTooltip = QuarkClientCompat.canRenderQuarkTooltip();
        boolean sbtTooltip = CompatHandler.SHULKER_BOX_TOOLTIP && ShulkerBoxTooltipCompat.hasPreviewProvider(stack);
        if (quarkTooltip && !sbtTooltip) {
            Item item = stack.getItem();
            if (item instanceof SafeItem || item instanceof SackItem) {

                ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
                if (contents == null) return;

                if (item instanceof SafeItem) {
                    // DUMMY_SAFE_TILE.get().load(cmp);
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
    }*/


    public static void usageTickerStack(UsageTickerEvent.GetStack event) {
        ItemStack stack = event.currentRealStack;
        if (stack.getItem() instanceof LunchBoxItem li) {
            LunchBaskedContent data = stack.get(li.getComponentType());
            if (data != null) event.setResultStack(data.getSelected());
        }
    }

    public static void usageTickerCount(UsageTickerEvent.GetCount event) {
        ItemStack stack = event.currentRealStack;
        if (stack.getItem() instanceof LunchBoxItem li) {
            LunchBaskedContent data = stack.get(li.getComponentType());
            if (data != null) event.setResultCount(data.getSelectedItemCount());
        } else if (stack.getItem() instanceof ProjectileWeaponItem && event.currentStack != stack) {
            //adds missing ones from quiver

            if (event.player instanceof IQuiverEntity qe) {
                ItemStack entityQuiver = qe.supplementaries$getQuiver();
                if (!entityQuiver.isEmpty() && entityQuiver.getItem() instanceof QuiverItem qi) {
                    QuiverContent data = stack.get(qi.getComponentType());
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
