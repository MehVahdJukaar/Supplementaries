package net.mehvahdjukaar.supplementaries.compat.quark;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.inventories.SackContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.content.client.module.ChestSearchingModule;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.client.tooltip.ShulkerBoxTooltips;

import java.util.ArrayList;
import java.util.List;

// credits to Vazkii and Quark
public class QuarkTooltipPlugin {

    private static final SackBlockTile DUMMY_SACK_TILE = new SackBlockTile();
    private static final BlockState DEFAULT_SACK = ModRegistry.SACK.get().defaultBlockState();

    private static final SafeBlockTile DUMMY_SAFE_TILE = new SafeBlockTile();
    private static final BlockState DEFAULT_SAFE = ModRegistry.SAFE.get().defaultBlockState();

    public static boolean canRenderTooltip(){
       return (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }

    private static final int CORNER = 5;
    private static final int EDGE = 18;


    public static void onItemTooltipEvent(ItemTooltipEvent event){
        if(canRenderTooltip()){
            ItemStack stack = event.getItemStack();
            CompoundNBT cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
            if (cmp != null && !cmp.contains("LootTable")) {
                Item i = stack.getItem();
                if(i == ModRegistry.SAFE_ITEM.get()){
                    DUMMY_SAFE_TILE.load(DEFAULT_SAFE, cmp);
                    PlayerEntity player = Minecraft.getInstance().player;
                    if(player == null || DUMMY_SAFE_TILE.canPlayerOpen(Minecraft.getInstance().player,false)){
                        cleanupTooltip(event.getToolTip());
                    }
                }else if(i == ModRegistry.SACK_ITEM.get()){
                    cleanupTooltip(event.getToolTip());
                }
            }
        }
    }

    public static void renderTooltipEvent(RenderTooltipEvent.PostText event){
        if(canRenderTooltip()){
            ItemStack stack = event.getStack();
            CompoundNBT cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
            if (cmp != null && !cmp.contains("LootTable")) {
                Item i = stack.getItem();
                if(i == ModRegistry.SAFE_ITEM.get()){
                    DUMMY_SAFE_TILE.load(DEFAULT_SAFE, cmp);
                    PlayerEntity player = Minecraft.getInstance().player;
                    if(player == null || DUMMY_SAFE_TILE.canPlayerOpen(Minecraft.getInstance().player,false)){
                        doRenderTooltip(event, DUMMY_SAFE_TILE, DUMMY_SAFE_TILE.getContainerSize());
                    }

                }else if(i == ModRegistry.SACK_ITEM.get()){
                    DUMMY_SACK_TILE.load(DEFAULT_SACK, cmp);
                    doRenderTooltip(event, DUMMY_SACK_TILE, DUMMY_SACK_TILE.getUnlockedSlots());
                }
            }
        }
    }

    private static void cleanupTooltip(List<ITextComponent> tooltip){
        List<ITextComponent> tooltipCopy = new ArrayList<>(tooltip);
        for(int i = 1; i < tooltipCopy.size(); ++i) {
            ITextComponent t = tooltipCopy.get(i);
            String s = t.getString();
            if (!s.startsWith("§") || s.startsWith("§o")) {
                tooltip.remove(t);
            }
        }
        if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown()) {
            tooltip.add(1, new TranslationTextComponent("quark.misc.shulker_box_shift"));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void doRenderTooltip(RenderTooltipEvent.PostText event, LockableLootTileEntity container, int size) {


        container.setLootTable(null, 0);
        LazyOptional<IItemHandler> handler = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        handler.ifPresent((capability) -> {
            Minecraft mc = Minecraft.getInstance();
            MatrixStack matrix = event.getMatrixStack();


            //23 last allowed prime number
            int[] dims = SackContainer.getRatio(size);

            int currentX = event.getX() - 5;

            //fix -70 = -16 - 18*dims[1]
            int currentY = event.getY() - 16 - 18*dims[1];

            int texWidth = CORNER * 2 + EDGE * dims[0];

            if (currentY < 0)
                currentY = event.getY() + event.getLines().size() * 10 + 5;

            int right = currentX + texWidth;
            MainWindow window = mc.getWindow();
            if (right > window.getGuiScaledWidth())
                currentX -= (right - window.getGuiScaledWidth());

            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, 0, 700);

            int color = -1;

            ShulkerBoxTooltips.renderTooltipBackground(mc, matrix, currentX, currentY, dims[0], dims[1], color);

            ItemRenderer render = mc.getItemRenderer();

            for (int i = 0; i < size; i++) {
                ItemStack itemstack = capability.getStackInSlot(i);
                //fix 9=dims[0]
                int xp = currentX + 6 + (i % dims[0]) * 18;
                int yp = currentY + 6 + (i / dims[0]) * 18;

                if (!itemstack.isEmpty()) {
                    render.renderAndDecorateFakeItem(itemstack, xp, yp);
                    render.renderGuiItemDecorations(mc.font, itemstack, xp, yp);
                }

                if (!ChestSearchingModule.namesMatch(itemstack)) {
                    RenderSystem.disableDepthTest();
                    AbstractGui.fill(matrix, xp, yp, xp + 16, yp + 16, 0xAA000000);
                }
            }

            RenderSystem.popMatrix();
        });
    }


}

