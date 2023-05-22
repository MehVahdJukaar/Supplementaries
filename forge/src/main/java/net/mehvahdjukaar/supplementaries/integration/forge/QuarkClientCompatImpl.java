package net.mehvahdjukaar.supplementaries.integration.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.client.renderers.color.CrossbowColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.DefaultWaterColor;
import net.mehvahdjukaar.supplementaries.client.renderers.color.TippedSpikesColor;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.common.items.SafeItem;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryTooltip;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.integration.forge.quark.CartographersQuillItem;
import net.mehvahdjukaar.supplementaries.integration.forge.quark.QuarkInventoryTooltipComponent;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import vazkii.quark.addons.oddities.client.render.be.TinyPotatoRenderer;
import vazkii.quark.api.event.UsageTickerEvent;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;

import java.util.ArrayList;
import java.util.List;

public class QuarkClientCompatImpl {

    public static void initClient() {
        ClientPlatformHelper.addBlockEntityRenderersRegistration(QuarkClientCompatImpl::registerEntityRenderers);
        ClientPlatformHelper.addItemColorsRegistration(QuarkClientCompatImpl::registerItemColors);
        MinecraftForge.EVENT_BUS.addListener(QuarkClientCompatImpl::onItemTooltipEvent);
        MinecraftForge.EVENT_BUS.addListener(QuarkClientCompatImpl::quiverUsageTicker);
    }

    private static void registerEntityRenderers(ClientPlatformHelper.BlockEntityRendererEvent event) {
        event.register(QuarkCompatImpl.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new);
    }

    public static void setupClient() {
        ClientPlatformHelper.registerRenderType(QuarkCompatImpl.TATER_IN_A_JAR.get(), RenderType.cutout());
    }

    @EventCalled
    private static void registerItemColors(ClientPlatformHelper.ItemColorEvent event) {
        event.register(CartographersQuillItem::getItemColor,
                QuarkCompatImpl.CARTOGRAPHERS_QUILL.get());

    }

    public static boolean shouldHaveButtonOnRight() {
        return !(GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton);
    }

    public static boolean canRenderBlackboardTooltip() {
        return canRenderQuarkTooltip();
    }

    public static boolean canRenderQuarkTooltip() {
        return ModuleLoader.INSTANCE.isModuleEnabled(ExpandedItemInteractionsModule.class) &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }

    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
        event.register(InventoryTooltip.class, QuarkInventoryTooltipComponent::new);
    }


    private static final Lazy<SafeBlockTile> DUMMY_SAFE_TILE = Lazy.of(() -> new SafeBlockTile(BlockPos.ZERO, ModRegistry.SAFE.get().defaultBlockState()));

    public static void onItemTooltipEvent(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (canRenderQuarkTooltip()) {
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
                        if (!s.startsWith("\u00a7") || s.startsWith("\u00a7o"))
                            tooltip.remove(either);
                    }
                }
                if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown())
                    tooltip.add(1, Either.left(Component.translatable("quark.misc.shulker_box_shift")));
            }
        }
    }


    public static class TaterInAJarTileRenderer extends TinyPotatoRenderer {
        public TaterInAJarTileRenderer(BlockEntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public void render(TinyPotatoBlockEntity potato, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
            ms.pushPose();
            ms.translate(0, 1 / 16f, 0);
            super.render(potato, partialTicks, ms, buffers, light, overlay);
            ms.popPose();
        }
    }

    public static void quiverUsageTicker(UsageTickerEvent.GetCount event) {
        if (event.currentRealStack.getItem() instanceof ProjectileWeaponItem && event.currentStack != event.currentRealStack) {
            //adds missing ones from quiver

            if (event.player instanceof IQuiverEntity qe) {
                var q = qe.getQuiver();
                if (!q.isEmpty()) {
                    QuiverItem.Data data = QuiverItem.getQuiverData(q);
                    if (data != null) {
                        //sanity check
                        ItemStack selected = data.getSelected();

                        if (event.currentStack.is(selected.getItem())) {
                            //just recomputes everything
                            int count = data.getSelectedArrowCount();
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
