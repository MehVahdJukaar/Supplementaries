package net.mehvahdjukaar.supplementaries.setup;


import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Spawns.registerSpawningStuff();
        //DeferredWorkQueue.runLater(Dispenser::registerBehaviors);
        Dispenser.registerBehaviors();
    }


    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        event.getRareTrades().add(new BasicTrade(10, new ItemStack(Registry.GLOBE_ITEM, 1), 2, 20));
    }

    public static void reflectionStuff(){
        Field[] methods = DispenserTileEntity.class.getDeclaredFields();
        // get the name of every method present in the list
        for (Field method : methods) {
            String MethodName = method.getName();
            System.out.println("Name of the method: "
                    + MethodName);
        }
    }




}
