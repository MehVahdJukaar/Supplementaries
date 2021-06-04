package net.mehvahdjukaar.supplementaries.world.data.map.example;

import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecoration;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.CustomDecorationType;
import net.mehvahdjukaar.supplementaries.world.data.map.lib.MapDecorationHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CMDExampleReg {
    public static final CustomDecorationType<CustomDecoration, CMDExampleMarker> EXAMPLE_DECORATION_TYPE = new CustomDecorationType<>(
            new ResourceLocation("miecraft", "example"), CMDExampleMarker::loadFromNBT, CMDExampleMarker::getFromWorld, CustomDecoration::new);

    public static void init(FMLCommonSetupEvent event){
        MapDecorationHandler.register(EXAMPLE_DECORATION_TYPE);
    }
}
