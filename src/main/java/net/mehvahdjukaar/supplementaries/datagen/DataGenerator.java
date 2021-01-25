package net.mehvahdjukaar.supplementaries.datagen;

import java.nio.file.Path;
import java.util.Collection;

//@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DataGenerator extends net.minecraft.data.DataGenerator {
    public DataGenerator(Path output, Collection<Path> input) {
        super(output, input);
    }

    public void DataGenerator(){}

    //@SubscribeEvent
    public static void gatherData(){
        //https://www.youtube.com/watch?v=YD_ajlZ5TdY
    }
}
