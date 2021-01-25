package net.mehvahdjukaar.supplementaries.setup.registration;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HangingSigns {
    public static final List<String> woodTypes = Arrays.asList();
    //public static final Map<String, RegistryObject<Block>> SIGSN;
    private static final DeferredRegister<Block> SIGNS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);

    static{

        for(String n : woodTypes){
            //SIGSN.put(n,SIGNS.register(n,))
        }

    }
}
