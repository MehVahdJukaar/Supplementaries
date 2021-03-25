package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.block.Blocks;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StructureDebug {
    public static void doStuff(ServerWorld world, BlockPos pos,String folder){


        //TemplateManager templatemanager = world.getStructureTemplateManager();
        Path base = Paths.get("saves/StructuresDebug/str/"+folder);
        File k = base.toFile();
        File[] fileList = k.listFiles();
        for (File f : fileList){

            String name = f.getName();
            name = name.replace(base.toString()+"/","");
            name = name.replace(".nbt","");
            world.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
            TileEntity tile = world.getBlockEntity(pos);
            if(tile instanceof StructureBlockTileEntity){
                StructureBlockTileEntity te = (StructureBlockTileEntity) tile;
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
