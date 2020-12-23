package net.mehvahdjukaar.supplementaries.blocks.tiles;


import net.mehvahdjukaar.supplementaries.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BedBlock;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;



public class TurnTableBlockTile extends TileEntity implements ITickableTileEntity {
    private int cooldown = 5;
    private boolean canRotate = false;
    // private long tickedGameTime;
    public TurnTableBlockTile() {
        super(Registry.TURN_TABLE_TILE);
    }

    public void tryRotate() {
        this.canRotate = true;
        //updates correct cooldown
        this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());
        // allows for a rotation try nedxt period
    }

    public void tick() {
        if (this.world != null && !this.world.isRemote) {

            // cd > 0
            if (this.cooldown == 0) {
                boolean success = this.handleRotation();
                this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());//ServerConfigs.cached.TURN_TABLE_PERIOD;
                // if it didn't rotate last block that means that block is immovable
                this.canRotate = (success && this.getBlockState().get(TurnTableBlock.POWER)!=0);
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
            if (!(state.get(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE))
                return true;
        }
        // no piston bases
        if (state.hasProperty(BlockStateProperties.EXTENDED)) {
            if (state.get(BlockStateProperties.EXTENDED))
                return true;
        }
        // neither piston arms
        if (state.hasProperty(BlockStateProperties.SHORT))
            return true;

        return ServerConfigs.cached.TURN_TABLE_BLACKLIST.contains(state.getBlock().getRegistryName().toString());
    }


    private boolean doRotateBlocks(BlockState oldstate, BlockState newState, BlockPos pos) {
        if(newState!=oldstate){
            //always returns true because block could be able to rotate in the future even if it can't now
            if(newState.isValidPosition(world,pos)) {
                world.setBlockState(pos, newState, 3);
                //self update post placement to connect to others
                for(int i = 0; i<6; i++){
                    Direction d = Direction.byIndex(i);
                    BlockPos otherPos = pos.offset(d);
                    BlockState neighbour = world.getBlockState(otherPos);
                    BlockState updatedNewState = newState.updatePostPlacement(d, neighbour, this.world, pos, otherPos);

                    if(!updatedNewState.isAir()) {
                        world.setBlockState(pos, updatedNewState,3);
                        newState = updatedNewState;
                    }
                    else {
                        //turns into air == rotation failed
                        world.setBlockState(pos, oldstate,3);
                        return false;
                    }
                }
                //update others so they connect to this block
                for(int i = 0; i<6; i++){
                    Direction d = Direction.byIndex(i);
                    BlockPos otherPos = pos.offset(d);
                    BlockState neighbour = world.getBlockState(otherPos);
                    BlockState updatedNeighbour = neighbour.updatePostPlacement(d.getOpposite(), newState, this.world, otherPos, pos);
                    if(updatedNeighbour!=neighbour)
                        world.setBlockState(otherPos, updatedNeighbour,3);
                }
            }
            return true;
            //TODO: this makes block instantly rotate when condition becomes true
        }
        return false;
    }

    public Vector3f toVector3f(Direction dir) {
        return new Vector3f((float)dir.getXOffset(), (float)dir.getYOffset(), (float)dir.getZOffset());
    }


    // spaghetti code incoming
    public boolean handleRotation() {
        BlockState state = this.getBlockState();

        World world = this.world;
        BlockPos mypos = this.pos;
        Direction mydir = state.get(BlockStateProperties.FACING);
        BlockPos targetpos = mypos.offset(mydir);
        BlockState _bs = world.getBlockState(targetpos);
        // is block blacklisted?
        if (this.isInBlacklist(_bs)) return false;
        boolean ccw = (state.get(BlockStateProperties.INVERTED) ^ (state.get(BlockStateProperties.FACING) == Direction.DOWN));
        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;

        try {
            //horizontal facing blocks -easy
            if (mydir.getAxis() == Direction.Axis.Y) {
                //sign posts
                if (_bs.getBlock() instanceof SignPostBlock) {
                    _bs.rotate(world,targetpos,rot);
                    return true;
                }
                BlockState rotatedstate = _bs.rotate(world,targetpos,rot);
                return doRotateBlocks(_bs, rotatedstate, targetpos);
            }
            // 6 dir blocks blocks
            else if (_bs.hasProperty(BlockStateProperties.FACING)) {
                Vector3f targetvec = toVector3f(_bs.get(BlockStateProperties.FACING));
                Vector3f myvec =toVector3f(mydir);
                if (!ccw)
                    targetvec.mul(-1);
                // hacky I know..
                myvec.cross(targetvec);
                if (myvec.equals(new Vector3f(0, 0, 0))) {
                    // same axis, can't rotate
                    return false;
                }
                Direction newdir = Direction.getFacingFromVector(myvec.getX(), myvec.getY(), myvec.getZ());
                return this.doRotateBlocks(_bs, _bs.with(BlockStateProperties.FACING, newdir), targetpos);
            }
            // axis blocks
            else if (_bs.hasProperty(BlockStateProperties.AXIS)) {
                Axis targetaxis = _bs.get(BlockStateProperties.AXIS);
                Axis myaxis = mydir.getAxis();
                if (myaxis == targetaxis) {
                    // same axis, can't rotate
                    return false;
                } else if (myaxis == Axis.X) {
                    return this.doRotateBlocks(_bs, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.Z : Axis.Y), targetpos);
                } else if (myaxis == Axis.Z) {
                    return this.doRotateBlocks(_bs, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.X : Axis.Y), targetpos);
                }
            }

        } catch (Exception e) {
            // mcserv.getPlayerList().sendMessage(new StringTextComponent("error rotating
            // block: "+e.toString()));
        }
        return false;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.cooldown = compound.getInt("Cooldown");
        this.canRotate = compound.getBoolean("Can_rotate");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("Cooldown", this.cooldown);
        compound.putBoolean("Can_rotate", this.canRotate);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}