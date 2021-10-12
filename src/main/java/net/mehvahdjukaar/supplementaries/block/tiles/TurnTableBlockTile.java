package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.PulleyBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;


//TODO: improve this
public class TurnTableBlockTile extends TileEntity implements ITickableTileEntity {
    private int cooldown = 5;
    private boolean canRotate = false;
    // private long tickedGameTime;
    public int cat = 0;

    public TurnTableBlockTile() {
        super(ModRegistry.TURN_TABLE_TILE.get());
    }

    public void tryRotate() {
        this.canRotate = true;
        //updates correct cooldown
        this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());
        // allows for a rotation try nedxt period
    }

    @Override
    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            this.cat = Math.max(cat - 1, 0);
            // cd > 0
            if (this.cooldown == 0) {
                boolean success = this.handleRotation();
                this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());//ServerConfigs.cached.TURN_TABLE_PERIOD;
                // if it didn't rotate last block that means that block is immovable
                int power = this.getBlockState().getValue(TurnTableBlock.POWER);
                this.canRotate = (success && power != 0);
                //change blockstate after rotation if is powered off
                if (power == 0) {
                    level.setBlock(worldPosition, this.getBlockState().setValue(TurnTableBlock.ROTATING, false), 3);
                }
            } else if (this.canRotate) {
                this.cooldown--;
            }
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
                this.level.playSound(null, this.getBlockPos(), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundCategory.BLOCKS, 1.0F, 0.6F);
            }
            return true;
            //TODO: this makes block instantly rotate when condition becomes true
        }
        return false;
    }

    public Vector3d toVector3d(Direction dir) {
        return new Vector3d((float) dir.getStepX(), (float) dir.getStepY(), (float) dir.getStepZ());
    }


    // spaghetti code incoming
    public boolean handleRotation() {
        BlockState state = this.getBlockState();

        World world = this.level;
        BlockPos mypos = this.worldPosition;
        Direction mydir = state.getValue(BlockStateProperties.FACING);
        BlockPos targetpos = mypos.relative(mydir);
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
                Vector3d targetvec = toVector3d(targetState.getValue(BlockStateProperties.FACING));
                Vector3d myvec = toVector3d(mydir);
                if (!ccw)
                    targetvec.multiply(-1, -1, -1);
                // hacky I know..
                myvec = myvec.cross(targetvec);
                if (myvec.equals(new Vector3d(0, 0, 0))) {
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
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.cooldown = compound.getInt("Cooldown");
        this.canRotate = compound.getBoolean("CanRotate");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("Cooldown", this.cooldown);
        compound.putBoolean("CanRotate", this.canRotate);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}