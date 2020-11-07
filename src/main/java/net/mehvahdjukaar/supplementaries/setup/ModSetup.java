package net.mehvahdjukaar.supplementaries.setup;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.FireBlock;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Method;

public class ModSetup {

    public static void init(final FMLClientSetupEvent event) {

        Dispenser.registerBehaviors();

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
