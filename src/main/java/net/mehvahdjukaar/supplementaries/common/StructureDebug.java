package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StructureDebug {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();
        if(stack.getItem() == Items.STICK){
            Level world = event.getWorld();
            if(world instanceof ServerLevel) {
                BlockPos pos = event.getPos();
                world.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof StructureBlockEntity) {
                    StructureBlockEntity te = (StructureBlockEntity) tile;
                    te.setMode(StructureMode.LOAD);
                    te.setStructureName("minecraft:desert_temple");
                    te.loadStructure((ServerLevel) world, true);
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
        }
    }

    public static void doStuff(ServerLevel world, BlockPos pos,String folder){


        //TemplateManager templatemanager = world.getStructureTemplateManager();
        Path base = Paths.get("saves/StructuresDebug/str/"+folder);
        File k = base.toFile();
        File[] fileList = k.listFiles();
        for (File f : fileList){

            String name = f.getName();
            name = name.replace(base.toString()+"/","");
            name = name.replace(".nbt","");
            world.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
            BlockEntity tile = world.getBlockEntity(pos);
            if(tile instanceof StructureBlockEntity){
                StructureBlockEntity te = (StructureBlockEntity) tile;
                te.setMode(StructureMode.LOAD);
                te.setStructureName("minecraft:"+folder+"/"+name);
                te.loadStructure(world,true);
                BlockPos p2 = te.getStructureSize();
                pos = new BlockPos(pos.getX()+p2.getX()+1,pos.getY(),pos.getZ());
                te.loadStructure(world,true);
            }
        }



    }
}
