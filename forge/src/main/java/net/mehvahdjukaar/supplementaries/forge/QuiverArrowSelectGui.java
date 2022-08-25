package net.mehvahdjukaar.supplementaries.forge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Optional;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
//
//public class QuiverArrowSelectGui extends Gui implements IGuiOverlay {
//    static final ResourceLocation TEXTURE = Supplementaries.res("textures/gui/quiver_select.png");
//    private static final int SPRITE_SHEET_WIDTH = 128;
//    private static final int SPRITE_SHEET_HEIGHT = 128;
//    private static final int SLOT_AREA = 26;
//    private static final int SLOT_PADDING = 5;
//    private static final int SLOT_AREA_PADDED = 31;
//    private static final int HELP_TIPS_OFFSET_Y = 5;
//    private int firstMouseX;
//    private int firstMouseY;
//    private boolean setFirstMousePos;
//
//    public QuiverArrowSelectGui(Minecraft minecraft, ItemRenderer itemRenderer) {
//        super(minecraft, itemRenderer);
//    }
//
//
//    public static void register() {
//            RegisterGuiOverlaysEvent e;
//            e.registerAbove(ForgeIngameGui.FROSTBITE_ELEMENT, "Slimed",
//                    new SlimedGuiOverlay(Minecraft.getInstance(), Minecraft.getInstance().getItemRenderer()));
//        }
//
//
//    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
//        if (!this.checkToClose()) {
//            RenderSystem.setShader(GameRenderer::getPositionTexShader);
//            pPoseStack.pushPose();
//            RenderSystem.enableBlend();
//            RenderSystem.setShaderTexture(0, TEXTURE);
//            int i = this.width / 2 - 62;
//            int j = this.height / 2 - 31 - 27;
//            blit(pPoseStack, i, j, 0.0F, 0.0F, 125, 75, 128, 128);
//            pPoseStack.popPose();
//            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
//            this.currentlyHovered.ifPresent((p_97563_) -> {
//                drawCenteredString(pPoseStack, this.font, p_97563_.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
//            });
//
//            if (!this.setFirstMousePos) {
//                this.firstMouseX = pMouseX;
//                this.firstMouseY = pMouseY;
//                this.setFirstMousePos = true;
//            }
//
//            boolean flag = this.firstMouseX == pMouseX && this.firstMouseY == pMouseY;
//
//            for(GameModeSlot gamemodeswitcherscreen$gamemodeslot : this.slots) {
//                gamemodeswitcherscreen$gamemodeslot.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
//                this.currentlyHovered.ifPresent((p_97569_) -> {
//                    gamemodeswitcherscreen$gamemodeslot.setSelected(p_97569_ == gamemodeswitcherscreen$gamemodeslot.icon);
//                });
//                if (!flag && gamemodeswitcherscreen$gamemodeslot.isHoveredOrFocused()) {
//                    this.currentlyHovered = Optional.of(gamemodeswitcherscreen$gamemodeslot.icon);
//                }
//            }
//
//        }
//    }
//
//    private void switchToHoveredGameMode() {
//        switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
//    }
//
//
//    private boolean checkToClose() {
//        if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
//            this.switchToHoveredGameMode();
//            this.minecraft.setScreen((Screen)null);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
//        if (pKeyCode == 293 && this.currentlyHovered.isPresent()) {
//            this.setFirstMousePos = false;
//            this.currentlyHovered = this.currentlyHovered.get().getNext();
//            return true;
//        } else {
//            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
//        }
//    }
//
//    public void renderHotbar(float pPartialTick, PoseStack pPoseStack) {
//
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//        ItemStack itemstack = player.getOffhandItem();
//        HumanoidArm humanoidarm = player.getMainArm().getOpposite();
//        int i = this.screenWidth / 2;
//        int j = this.getBlitOffset();
//        int k = 182;
//        int l = 91;
//        this.setBlitOffset(-90);
//        this.blit(pPoseStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
//        this.blit(pPoseStack, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
//        if (!itemstack.isEmpty()) {
//            if (humanoidarm == HumanoidArm.LEFT) {
//                this.blit(pPoseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
//            } else {
//                this.blit(pPoseStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
//            }
//        }
//
//        this.setBlitOffset(j);
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        int i1 = 1;
//
//        for(int j1 = 0; j1 < 9; ++j1) {
//            int k1 = i - 90 + j1 * 20 + 2;
//            int l1 = this.screenHeight - 16 - 3;
//            this.renderSlot(k1, l1, pPartialTick, player, player.getInventory().items.get(j1), i1++);
//        }
//
//        if (!itemstack.isEmpty()) {
//            int j2 = this.screenHeight - 16 - 3;
//            if (humanoidarm == HumanoidArm.LEFT) {
//                this.renderSlot(i - 91 - 26, j2, pPartialTick, player, itemstack, i1++);
//            } else {
//                this.renderSlot(i + 91 + 10, j2, pPartialTick, player, itemstack, i1++);
//            }
//        }
//
//        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
//            float f = this.minecraft.player.getAttackStrengthScale(0.0F);
//            if (f < 1.0F) {
//                int k2 = this.screenHeight - 20;
//                int l2 = i + 91 + 6;
//                if (humanoidarm == HumanoidArm.RIGHT) {
//                    l2 = i - 91 - 22;
//                }
//
//                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
//                int i2 = (int)(f * 19.0F);
//                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//                this.blit(pPoseStack, l2, k2, 0, 94, 18, 18);
//                this.blit(pPoseStack, l2, k2 + 18 - i2, 18, 112 - i2, 18, i2);
//            }
//        }
//
//        RenderSystem.disableBlend();
//
//    }
//
//    @Override
//    public void render(ForgeGui forgeGui, PoseStack poseStack, float partialTicks, int width, int height) {
//
//    }
//}