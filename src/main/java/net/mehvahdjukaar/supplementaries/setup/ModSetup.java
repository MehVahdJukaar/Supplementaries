package net.mehvahdjukaar.supplementaries.setup;


import net.minecraft.block.FireBlock;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.reflect.Method;

public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Dispenser.registerBehaviors();
        Spawns.registerSpawningStuff();
        //DeferredWorkQueue.runLater(()->{ });


    }



    public static void reflectionStuff(){
        Method[] methods = FireBlock.class.getMethods();
        // get the name of every method present in the list
        for (Method method : methods) {
            String MethodName = method.getName();
            System.out.println("Name of the method: "
                    + MethodName);
        }
    }




}
