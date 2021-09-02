package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.util.Constants;


public class RopeKnotBlockTile extends MimicBlockTile {


    public RopeKnotBlockTile() {
        super(ModRegistry.ROPE_KNOT_TILE.get());
        this.setHeldBlock(Blocks.OAK_FENCE.defaultBlockState());
    }


    @Override
    public void setChanged() {
        if(this.level==null)return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        this.requestModelDataUpdate();
        super.setChanged();
    }

}