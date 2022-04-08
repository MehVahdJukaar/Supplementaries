package net.mehvahdjukaar.supplementaries.integration.create.behaviors;


import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Map;

public class MovementUtils {

    public static void changeState(MovementContext context, BlockState newState) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = context.contraption.getBlocks();
        if (blocks.containsKey(context.localPos)) {
            context.state = newState;
            StructureTemplate.StructureBlockInfo info = blocks.get(context.localPos);
            StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(info.pos, newState, info.nbt);
            blocks.replace(context.localPos, newInfo);
        }
    }

}
