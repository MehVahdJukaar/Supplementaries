package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
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

    public static void tick(Level level, BlockPos pos, BlockState state, TurnTableBlockTile tile) {
        tile.cat = Math.max(tile.cat - 1, 0);
        // cd > 0
        if (tile.cooldown == 0) {
            boolean success = tile.handleRotation();

            if (success) {
                level.blockEvent(pos, state.getBlock(), 0, 0);
            }

            tile.cooldown = TurnTableBlock.getPeriod(state);//ServerConfigs.cached.TURN_TABLE_PERIOD;
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

    private boolean isInBlacklist(BlockState state) {
        // double blocks
        if (state.getBlock() instanceof BedBlock)
            return true;
        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            if (!(state.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE))
                return true;
        }
        // no piston bases
        if (state.hasProperty(BlockStateProperties.EXTENDED)) {
            if (state.getValue(BlockStateProperties.EXTENDED))
                return true;
        }
        // neither piston arms
        if (state.hasProperty(BlockStateProperties.SHORT))
            return true;

        return ServerConfigs.cached.TURN_TABLE_BLACKLIST.contains(state.getBlock().getRegistryName().toString());
    }


    private boolean doRotateBlocks(BlockState oldstate, BlockState newState, BlockPos pos, Direction mydir) {
        if (newState != oldstate) {
            //always returns true because block could be able to rotate in the future even if it can't now
            if (newState.canSurvive(level, pos)) {
                BlockState updatedState = Block.updateFromNeighbourShapes(newState, level, pos);
                level.setBlock(pos, updatedState, 3);
                level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                this.level.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
            }
            return true;
            //TODO: this makes block instantly rotate when condition becomes true
        }
        return false;
    }

    public Vec3 toVector3d(Direction dir) {
        return new Vec3((float) dir.getStepX(), (float) dir.getStepY(), (float) dir.getStepZ());
    }


    // spaghetti code incoming
    public boolean handleRotation() {
        BlockState state = this.getBlockState();

        Level world = this.level;
        Direction mydir = state.getValue(BlockStateProperties.FACING);
        BlockPos targetpos = this.worldPosition.relative(mydir);
        BlockState targetState = world.getBlockState(targetpos);
        // is block blacklisted?
        if (this.isInBlacklist(targetState)) return false;
        boolean ccw = (state.getValue(BlockStateProperties.INVERTED) ^ (state.getValue(BlockStateProperties.FACING) == Direction.DOWN));
        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;

        try {
            //horizontal facing blocks -easy
            if (mydir.getAxis() == Direction.Axis.Y) {
                //sign posts
                if (targetState.getBlock() instanceof SignPostBlock) {
                    targetState.rotate(world, targetpos, rot);
                    return true;
                }
                BlockState rotatedstate = targetState.rotate(world, targetpos, rot);
                return doRotateBlocks(targetState, rotatedstate, targetpos, mydir);
            }
            // 6 dir blocks blocks
            else if (targetState.hasProperty(BlockStateProperties.FACING)) {
                Vec3 targetvec = toVector3d(targetState.getValue(BlockStateProperties.FACING));
                Vec3 myvec = toVector3d(mydir);
                if (!ccw)
                    targetvec.multiply(-1, -1, -1);
                // hacky I know..
                myvec = myvec.cross(targetvec);
                if (myvec.equals(new Vec3(0, 0, 0))) {
                    // same axis, can't rotate
                    return false;
                }
                Direction newdir = Direction.getNearest(myvec.x(), myvec.y(), myvec.z());
                return this.doRotateBlocks(targetState, targetState.setValue(BlockStateProperties.FACING, newdir), targetpos, mydir);
            }
            // axis blocks
            else if (targetState.hasProperty(BlockStateProperties.AXIS)) {
                Axis targetaxis = targetState.getValue(BlockStateProperties.AXIS);
                Axis myaxis = mydir.getAxis();
                if (myaxis == targetaxis) {
                    if (myaxis != Axis.Y && targetState.getBlock() instanceof PulleyBlock) {
                        ((PulleyBlock) targetState.getBlock()).axisRotate(targetState, targetpos, world, rot, mydir);
                        return true;
                    }
                    // same axis, can't rotate
                    return false;
                } else if (myaxis == Axis.X) {
                    return this.doRotateBlocks(targetState, targetState.setValue(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.Z : Axis.Y), targetpos, mydir);
                } else if (myaxis == Axis.Z) {
                    return this.doRotateBlocks(targetState, targetState.setValue(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.X : Axis.Y), targetpos, mydir);
                }
            }

        } catch (Exception e) {
            // mcserv.getPlayerList().sendMessage(new StringTextComponent("error rotating
            // block: "+e.toString()));
        }
        return false;
    }

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