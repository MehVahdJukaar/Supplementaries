package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.WrenchItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;


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
            boolean success = BlockUtils.tryRotatingBlock(dir,ccw, pos.relative(dir), level);
            if(success){
                level.blockEvent(pos, state.getBlock(),0,0);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
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
            ServerLevel l;
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
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putInt("Cooldown", this.cooldown);
        compound.putBoolean("CanRotate", this.canRotate);
        return compound;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}