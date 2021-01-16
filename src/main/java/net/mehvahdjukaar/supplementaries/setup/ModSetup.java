package net.mehvahdjukaar.supplementaries.setup;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.network.commands.ModCommands;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Spawns.registerSpawningStuff();
        event.enqueueWork(Dispenser::registerBehaviors);
        //Dispenser.registerBehaviors();



    }

    @SubscribeEvent
    public static void onServerStart(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }


    @SubscribeEvent
    public static void registerWanderingTraderTrades(WandererTradesEvent event) {
        //adding twice cause it's showing up too rarely
        event.getRareTrades()
                .add(new BasicTrade(10, new ItemStack(Registry.GLOBE_ITEM, 1), 2, 20));
        event.getRareTrades()
                .add(new BasicTrade(10, new ItemStack(Registry.GLOBE_ITEM, 1), 2, 20));
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
