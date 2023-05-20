package net.mehvahdjukaar.supplementaries.forge;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import umpaz.farmersrespite.common.block.KettleBlock;
import umpaz.farmersrespite.common.block.TeaBushBlock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {

    private static final Map<String, ResourceLocation> REMAP = new HashMap<>();

    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);
        event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);

    }

    public static void del(CommentedFileConfig cf, IConfigSpec con, F f){
        ForgeConfigSpec spec =  ((ForgeConfigSpec)con);
        LinkedList<String> parentPath = new LinkedList();

      //  var l = spec.correct(spec.config, cf, parentPath, Collections.unmodifiableList(parentPath), (a, b, c, d) -> {
      //  }, null, true);
    }

    @FunctionalInterface
    public interface F{
        Object aa(Object a,Object b, Object c);
    }
}
