package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

    private static final List<IReorderingProcessor> CREDITS = new ArrayList<>();
    private static boolean init = true;

    private static final float SCALINGFACTOR = 16*8;
    private static final float SCALE = 1/SCALINGFACTOR;

    //client only stuff
    private static void setup(FontRenderer fontrenderer){
        String c =
                "\n\n\n\n\n\n\n\n\n\n\n"+
                "\u00A76\n"+
                "\u00A7lSupplementaries"+
                "\n\n"+
                "\n\n\u00A74Author:\u00A7r\n\n\u00A70"+
                "MehVahdJukaar"+
                "\n\n\n\u00A74Donators:\u00A7r\n\n\u00A70"+
                "Toffanelly"+
                "\n\n\n\u00A74Credits:"+
                "\n\n\u00A75Textures:\u00A7r\n\n\u00A70"+
                "Plantkillable"+
                "\nYaBoiCinn"+
                "\nNary"+
                "\nVladimirLemon"+
                "\n\n\u00A75Translations:\u00A7r\n\n\u00A70"+
                "Bart_Riot23"+
                "\nMikeliro"+
                "\nmayonaka8478"+
                "\neZio"+
                "\nOthuntgithub"+
                "\nYe Weiguo"+
                "\nTenebris_AK"+
                "\nBaliocraft"+
                "\nAnton Bidenko"+
                "\nsarlix"+
                "\n\n\u00A75Mod Compat.:\u00A7r\n\n\u00A70"+
                "WenXin2"+
                "\nFrogbirdd"+
                "\n\n\u00A75Others:\u00A7r\n\n\u00A70"+
                 "Guys on Discord"+
                "\nUmaroth"+
                "\nHowester84"+
                "\nspiritwolf_twitch"+
                "\nMcreator" +
                "\nYoutube tutorials"+
                "\nJoe Mama"+
                "\nand You <3"+
                "\n\n\n\n\n\n\n\n\n\n\n\n";

        float lx = 1 - (2 * 0.125f);
        CREDITS.addAll(fontrenderer.trimStringToWidth(iGetPageText(c), MathHelper.floor(lx * SCALINGFACTOR)));
    }


    public static void renderCredits(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int light, FontRenderer fontRenderer, float side){

        if(init){
            setup(fontRenderer);
            init = false;
        }

        long time = System.currentTimeMillis();

        int numberOfLines = CREDITS.size();

        float progress = ((time%(numberOfLines* 600L))/90f);

        matrixStack.scale(SCALE, -SCALE, SCALE);

        float lin = progress/8f;
        float offset = progress%8;
        int startLin = (int)lin;
        float o = lin-startLin;

        float bordery = 0.125f;

        matrixStack.push();
        for (int n = 0; n < 13; ++n) {
            if(startLin+n>=numberOfLines)break;
            IReorderingProcessor str = CREDITS.get(startLin+n);
            float dx = (float) (-fontRenderer.func_243245_a(str) / 2) + 0.5f;
            int a = (int) (255*(-Math.pow(((-o+n-6f)/7.2f),2)+1));
            int col = (int)(255*side);
            int rgba = NativeImage.getCombined(a, col, col, col);

            fontRenderer.func_238416_a_(str, dx, bordery*SCALINGFACTOR-(offset) + 8 * n, rgba, false, matrixStack.getLast().getMatrix(), bufferIn, false, 0, light);
        }
        matrixStack.pop();
    }

    public static ITextProperties iGetPageText(String s) {
        try {
            ITextProperties itextproperties = ITextComponent.Serializer.getComponentFromJson(s);
            if (itextproperties != null) {
                return itextproperties;
            }
        } catch (Exception ignored) {
        }
        return ITextProperties.func_240652_a_(s);
    }


}
