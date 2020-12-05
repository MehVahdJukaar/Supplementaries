package net.mehvahdjukaar.supplementaries.setup;


import net.minecraft.block.FireBlock;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.ICustomPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {

        Dispenser.registerBehaviors();
        Spawns.registerSpawningStuff();
        //DeferredWorkQueue.runLater(()->{ });

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
