package net.mehvahdjukaar.supplementaries.integration.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import vazkii.quark.addons.oddities.client.render.be.TinyPotatoRenderer;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;

import java.util.ArrayList;
import java.util.List;

public class QuarkClientCompatImpl {

    public static void init() {
        ClientPlatformHelper.addBlockEntityRenderersRegistration(e -> e.register(
                QuarkCompatImpl.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new));
    }

    public static void registerRenderLayers() {
        ClientPlatformHelper.registerRenderType(QuarkCompatImpl.TATER_IN_A_JAR.get(), RenderType.cutout());
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
        event.register(ItemsUtil.InventoryTooltip.class, QuarkInventoryTooltipComponent::new);
    }


    private static final BlockState DEFAULT_SAFE = ModRegistry.SAFE.get().defaultBlockState();
    private static final SafeBlockTile DUMMY_SAFE_TILE = new SafeBlockTile(BlockPos.ZERO, DEFAULT_SAFE);


    public static void onItemTooltipEvent(ItemStack stack, TooltipFlag tooltipFlag, List<Component> components) {
        if (canRenderQuarkTooltip()) {
            CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
            if (cmp != null && !cmp.contains("LootTable")) {
                Item i = stack.getItem();
                if (i == ModRegistry.SAFE_ITEM.get()) {
                    DUMMY_SAFE_TILE.load(cmp);
                    Player player = Minecraft.getInstance().player;
                    if (player == null || DUMMY_SAFE_TILE.canPlayerOpen(Minecraft.getInstance().player, false)) {
                        cleanupTooltip(components);
                    }
                } else if (i == ModRegistry.SACK_ITEM.get()) {
                    cleanupTooltip(components);
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
            tooltip.add(1, Component.translatable("quark.misc.shulker_box_shift"));
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

}
