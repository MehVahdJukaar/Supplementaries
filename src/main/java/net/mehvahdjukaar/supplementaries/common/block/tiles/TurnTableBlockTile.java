package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;


//TODO: improve this
public class TurnTableBlockTile extends BlockEntity {
    private int cooldown = 5;
    private boolean canRotate = false;
    // private long tickedGameTime;
    public int cat = 0;

    public TurnTableBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TURN_TABLE_TILE.get(), pos, state);
    }

    public void tryRotate() {
        this.canRotate = true;
        //updates correct cooldown
        this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());
        // allows for a rotation try nedxt period
    }

    //server only
    public static void tick(Level level, BlockPos pos, BlockState state, TurnTableBlockTile tile) {
        tile.cat = Math.max(tile.cat - 1, 0);
        // cd > 0
        if (tile.cooldown == 0) {
            Direction dir = state.getValue(TurnTableBlock.FACING);
            boolean ccw = state.getValue(TurnTableBlock.INVERTED) ^ (state.getValue(TurnTableBlock.FACING) == Direction.DOWN);
            BlockPos targetPos = pos.relative(dir);
            boolean success = BlockUtils.tryRotatingBlock(dir,ccw, targetPos, level, null).isPresent();
            if(success){
                //play particle with block event
                level.blockEvent(pos, state.getBlock(),0,0);
                level.gameEvent(GameEvent.BLOCK_CHANGE, targetPos);
                level.playSound(null, targetPos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
            }

            tile.cooldown = TurnTableBlock.getPeriod(state);
            // if it didn't rotate last block that means that block is immovable
            int power = state.getValue(TurnTableBlock.POWER);
            tile.canRotate = (success && power != 0);
            //change blockstate after rotation if is powered off
            if (power == 0) {
                level.setBlock(pos, state.setValue(TurnTableBlock.ROTATING, false), 3);
            }
        } else if (tile.canRotate) {
            tile.cooldown--;
        }
    }
    //TODO: this makes block instantly rotate when condition becomes true

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.cooldown = compound.getInt("Cooldown");
        this.canRotate = compound.getBoolean("CanRotate");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("Cooldown", this.cooldown);
        compound.putBoolean("CanRotate", this.canRotate);
    }
    /*
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
    */

}