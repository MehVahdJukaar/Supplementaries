package net.mehvahdjukaar.supplementaries.datagen;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    public DataGenerators(){}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        gen.addProvider(new ModItemModelProvider(gen,Supplementaries.MOD_ID,helper));
        gen.addProvider(new ModRecipeProvider(gen));
        gen.addProvider(new ModLanguageProvider(gen,Supplementaries.MOD_ID,"en_us"));

        //https://www.youtube.com/watch?v=YD_ajlZ5TdY
    }
}
