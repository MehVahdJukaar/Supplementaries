package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ColorHelper {
    private static final float[][] SOAP_COLORS;

    static {
        int[] c = new int[]{0xd3a4f7, 0xf3c1f0, 0xd3a4f7, 0xa2c0f8, 0xa2f8df, 0xa2c0f8,};
        float[][] temp = new float[c.length][];
        for (int i = 0; i < c.length; i++) {
            int j = c[i];
            temp[i] = new float[]{FastColor.ARGB32.red(j) / 255f,
                    FastColor.ARGB32.green(j) / 255f, FastColor.ARGB32.blue(j) / 255f};
        }
        SOAP_COLORS = temp;
    }

    public static int pack(float[] rgb) {
        return FastColor.ARGB32.color(255, (int) (rgb[0] * 255), (int) (rgb[1] * 255), (int) (rgb[2] * 255));
    }

    public static float oneToOneSaturation(float saturation, float lightness) {
        float c = 1 - Math.abs((2 * lightness) - 1);
        return Math.min(saturation, c);
    }

    public static int getRainbowColorPost(float division) {
        float scale = 3600f / division;
        float h = (((int) ((System.currentTimeMillis()) % (int) scale) / scale));
        HSLColor hsl = prettyfyColor(new HSLColor(h, 0.6f, 0.5f,1));
        return hsl.asRGB().toInt();
    }

    public static int getRandomBrightColor(RandomSource random) {
        float h = random.nextFloat();
        HSLColor hsl = prettyfyColor(new HSLColor(h, 0.62f + random.nextFloat() * 0.3f, 0.43f + random.nextFloat() * 0.15f,1));
        return hsl.asRGB().toInt();
    }

    public static int getRainbowColor(float division) {
        float scale = 3600f / division;
        float h = (((int) ((System.currentTimeMillis()) % (int) scale) / scale));
        var color = new HSLColor(h, 0.6f, 0.5f, 1);
        return color.asRGB().toInt();
    }

    public static float[] getBubbleColor(float phase) {
        int n = SOAP_COLORS.length;
        int ind = (int) Math.floor(n * phase);

        float delta = n * phase % 1;

        float[] start = SOAP_COLORS[ind];
        float[] end = SOAP_COLORS[(ind + 1) % n];

        float red = Mth.lerp(delta, start[0], end[0]);
        float green = Mth.lerp(delta, start[1], end[1]);
        float blue = Mth.lerp(delta, start[2], end[2]);
        return new float[]{red, green, blue};
    }


    public static HSLColor prettyfyColor(HSLColor hsl) {
        float h = hsl.hue();
        float s = hsl.saturation();
        float l = hsl.lightness();
        //map one to one. no effect on its own (false...)
        //s = s + (float)((1-s)*ClientConfigs.general.TEST3.get());
        s = oneToOneSaturation(s, l);

        //remove darker colors
        float minLightness = 0.47f;
        l = Math.max(l, minLightness);

        //saturate dark colors
        float j = (1 - l);
        float ratio = 0.35f;
        if (s < j) s = (ratio * j + (1 - ratio) * s);


        //desaturate blue
        float scaling = 0.15f;
        float angle = 90;
        float n = (float) (scaling * Math.exp(-angle * Math.pow((h - 0.6666f), 2)));
        s -= n;

        return new HSLColor(h, s, l, 1);
    }

}
