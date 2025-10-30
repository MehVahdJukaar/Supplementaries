package net.mehvahdjukaar.supplementaries.unused;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureSpawner {


    public static void debugSpawnAllVillages(Level level, BlockPos pos) {
        if (!PlatHelper.isDev()) return;
        if (level instanceof ServerLevel serverLevel) {
            int off = 0;
            int zOff = 0;
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
            int max = 800;
            for (var t : structureTemplateManager.listTemplates().toList()) {
                String string = t.toString();
                if (string.contains("pillager")) {
                    var template = structureTemplateManager.get(t).get();
                    BlockPos offset = pos.offset(off, 0, zOff);
                    level.setBlock(offset, Blocks.STRUCTURE_BLOCK.defaultBlockState(), 3);
                    var te = BlockEntityType.STRUCTURE_BLOCK.getBlockEntity(level, offset);
                    CompoundTag compoundTag = te.saveWithoutMetadata(level.registryAccess());
                    compoundTag.putString("name", string);
                    te.loadWithComponents(compoundTag, level.registryAccess());
                    te.setStructureName(t);
                    te.loadStructureInfo(serverLevel);
                    te.placeStructure(serverLevel);
                    off += template.getSize().get(Direction.Axis.X) + 3;

                    if (max-- < 0) break;
                    if (off > 200) {
                        zOff += 18;
                        off = 0;
                    }
                }
            }


        }
    }
}
