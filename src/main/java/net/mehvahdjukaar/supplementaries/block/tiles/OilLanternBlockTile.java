package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class OilLanternBlockTile extends EnhancedLanternBlockTile{
    public OilLanternBlockTile(BlockPos pos, BlockState state) {
        this(ModRegistry.COPPER_LANTERN_TILE.get(), pos, state);
    }

    public OilLanternBlockTile(BlockEntityType<?> type,BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    //TODO: readd
    /*
    @Override
    public void tick() {
        if(this.level.isClientSide){
            if(this.getBlockState().getValue(CopperLanternBlock.FACE)!=AttachFace.FLOOR)
                super.tick();
        }
    }
    */

}
