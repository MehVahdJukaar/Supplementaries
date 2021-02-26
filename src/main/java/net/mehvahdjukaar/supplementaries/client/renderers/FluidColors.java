package net.mehvahdjukaar.supplementaries.client.renderers;

import net.mehvahdjukaar.supplementaries.fluids.SoftFluid;
import net.mehvahdjukaar.supplementaries.fluids.SoftFluidList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class FluidColors {
    public static HashMap<String, Integer> particleColor = new HashMap<>();
    //TODO: possibly do it for ALL fluids, not only non grayscale ones
    public static void refresh() {
        particleColor = new HashMap<>();
        for (Fluid f : ForgeRegistries.FLUIDS) {
            String key = f.getRegistryName().toString();
            if(!particleColor.containsKey(key)){
                ResourceLocation location = f.getAttributes().getStillTexture();
                if(location==null)continue;
                AtlasTexture textureMap = Minecraft.getInstance().getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                if(sprite==null)continue;
                int fluidTint = f.getAttributes().getColor();

                int averageColor = getColorFrom(sprite,fluidTint);
                particleColor.put(key, averageColor);
            }
        }
        for (SoftFluid s : SoftFluidList.ID_MAP.values()){
            if(!particleColor.containsKey(s.getID()) && !s.isColored()){
                ResourceLocation location = s.getStillTexture();
                if(location==null)continue;
                AtlasTexture textureMap = Minecraft.getInstance().getModelManager().getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                if(sprite==null)continue;

                int averageColor = getColorFrom(sprite,s.getTintColor());
                particleColor.put(s.getID(), averageColor);
            }
        }
    }

    public static int get(Fluid f){
        return particleColor.getOrDefault(f.getRegistryName().toString(),-1);
    }
    public static int get(String s){
        return particleColor.getOrDefault(s,-1);
    }


    //credits to Random on forge discord
    private static int getColorFrom(TextureAtlasSprite sprite, int tint) {
        if (sprite == null || sprite.getFrameCount() == 0) return -1;
        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;
        int total = 0, totalR = 0, totalB = 0, totalG = 0;
        for (int x = 0; x < sprite.getWidth(); x++) {
            for (int y = 0; y < sprite.getHeight(); y++) {
                int pixel = sprite.getPixelRGBA(0, x, y);
                // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
                int pixelB = pixel >> 16 & 255;
                int pixelG = pixel >> 8 & 255;
                int pixelR = pixel & 255;
                ++total;
                totalR += pixelR;
                totalG += pixelG;
                totalB += pixelB;
            }
        }
        if (total <= 0) return -1;
        return ColorHelper.PackedColor.packColor(255,
                totalR / total * tintR / 255,
                totalG / total * tintG / 255,
                totalB / total * tintB / 255);
    }



}
