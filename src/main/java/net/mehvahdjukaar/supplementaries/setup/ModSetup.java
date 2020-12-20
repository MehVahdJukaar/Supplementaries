package net.mehvahdjukaar.supplementaries.setup;


import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Field;

public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Spawns.registerSpawningStuff();
        //DeferredWorkQueue.runLater(Dispenser::registerBehaviors);
        Dispenser.registerBehaviors();
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
