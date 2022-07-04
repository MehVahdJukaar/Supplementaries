package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CopperLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

//TODO: remove since I'm not animating this anymore
@Deprecated
public class VerticalLanternBlockTile extends EnhancedLanternBlockTile{
    private final boolean flippable;
    public VerticalLanternBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WALL_LANTERN_TILE.get(), pos, state);
        this.flippable = state.hasProperty(CopperLanternBlock.AXIS);
    }

    @Override
    public boolean isFlipped() {
        return flippable && this.getBlockState().getValue(HangingSignBlock.AXIS) != Direction.Axis.Z;
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
