package net.mehvahdjukaar.supplementaries.gui;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;


public class SliderStep extends Slider {
    public final double step;
    public SliderStep(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, IPressable handler, Slider.ISlider par) {
        super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, handler, par);
        this.step=1f/maxVal;
    }



    /*
    @Override
    public void onClick(double mouseX, double mouseY) {
        this.sliderValue = this.snapToStep(((mouseX - (this.x + 4)) / (this.width - 8))/(this.width/this.maxValue));
        updateSlider();
        this.dragging = true;
    }

    private double snapToStep(double val){
        double v = val%step;
        return (val-v);
    }



    @Override
    protected void renderBg(MatrixStack mStack, Minecraft par1Minecraft, int par2, int par3) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = this.snapToStep((par2 - (this.x + 4)) / (float)(this.width - 8));
                updateSlider();
            }
            GuiUtils.drawContinuousTexturedBox(mStack, WIDGETS_LOCATION, this.x + (int)(this.sliderValue * (float)(this.width - 8)), this.y, 0, 66, 8, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
        }
    }
    */

}
