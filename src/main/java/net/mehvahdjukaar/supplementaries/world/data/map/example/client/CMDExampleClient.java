package net.mehvahdjukaar.supplementaries.world.data.map.example.client;

import net.mehvahdjukaar.supplementaries.world.data.map.example.CMDExampleReg;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.client.DecorationRenderer;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.client.MapDecorationClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CMDExampleClient {
    private static final ResourceLocation SPRITE = new ResourceLocation("minecraft:textures/item/oak_sign.png");
    public static void init(FMLClientSetupEvent event){
        MapDecorationClient.bindDecorationRenderer(CMDExampleReg.EXAMPLE_DECORATION_TYPE, new DecorationRenderer<>(SPRITE));
    }
}
