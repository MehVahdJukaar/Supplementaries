package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
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
    private int cooldown = TurnTableBlock.PERIOD;
    private boolean canRotate = false;
    // private long tickedGameTime;
    public TurnTableBlockTile() {
        super(Registry.TURN_TABLE_TILE.get());
    }

    public void tryRotate() {
        this.canRotate = true;
        // allows for a rotation try nedxt period
    }

    public void tick() {
        if (this.world != null && !this.world.isRemote) {

            // cd > 0
            if (this.cooldown == 0) {
                boolean success = this.doRotateBlock();
                this.cooldown = TurnTableBlock.PERIOD;
                // if it didn't rotate last block that means that block is immovable
                this.canRotate = (success && this.getBlockState().get(TurnTableBlock.POWERED));
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
        // can't rotate blocks that it can't toutch
        return state.getBlock() instanceof WallTorchBlock || state.getBlock() instanceof WallBlock || state.getBlock() instanceof WallBannerBlock;
    }

    // spaghetti code incoming
    public boolean doRotateBlock() {
        BlockState state = this.getBlockState();

        World world = this.world;
        BlockPos mypos = this.pos;
        Direction mydir = state.get(BlockStateProperties.FACING);
        BlockPos targetpos = mypos.offset(mydir);
        BlockState _bs = world.getBlockState(targetpos);
        // is block blacklisted?
        if (this.isInBlacklist(_bs))
            return false;
        boolean ccw = (state.get(BlockStateProperties.INVERTED) ^ (state.get(BlockStateProperties.FACING) == Direction.DOWN));
        Rotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        try {
            // horizontal blocks. only if facing up or down. using each block rotation
            // method
            if (_bs.hasProperty(BlockStateProperties.HORIZONTAL_FACING) || _bs.hasProperty(BlockStateProperties.FACING_EXCEPT_UP)
                    || _bs.hasProperty(BlockStateProperties.RAIL_SHAPE)) {
                if (mydir.getAxis() == Direction.Axis.Y) {
                    world.setBlockState(targetpos, _bs.rotate(rot), 3);
                    return true;
                } else {
                    return false;
                }
            }
            // rotateable blocks
            else if (_bs.hasProperty(BlockStateProperties.FACING)) {
                if (mydir.getAxis() == Axis.Y) {
                    world.setBlockState(targetpos, _bs.rotate(rot), 3);
                } else {
                    Vector3f targetvec = _bs.get(BlockStateProperties.FACING).toVector3f();
                    Vector3f myvec = mydir.toVector3f();
                    if (!ccw)
                        targetvec.mul(-1);
                    // hacky I know..
                    myvec.cross(targetvec);
                    if (myvec.equals(new Vector3f(0, 0, 0))) {
                        // same axis, can't rotate
                        return false;
                    }
                    Direction newdir = Direction.getFacingFromVector(myvec.getX(), myvec.getY(), myvec.getZ());
                    world.setBlockState(targetpos, _bs.with(BlockStateProperties.FACING, newdir), 3);
                }
                return true;
            }
            // axis blocks
            else if (_bs.hasProperty(BlockStateProperties.AXIS)) {
                if (mydir.getAxis() == Axis.Y) {
                    world.setBlockState(targetpos, _bs.rotate(rot), 3);
                    return true;
                } else {
                    Axis targetaxis = _bs.get(BlockStateProperties.AXIS);
                    Axis myaxis = mydir.getAxis();
                    if (myaxis == targetaxis) {
                        // same axis, can't rotate
                        return false;
                    } else if (myaxis == Axis.X) {
                        world.setBlockState(targetpos, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.Z : Axis.Y), 3);
                        return true;
                    } else if (myaxis == Axis.Z) {
                        world.setBlockState(targetpos, _bs.with(BlockStateProperties.AXIS, targetaxis == Axis.Y ? Axis.X : Axis.Y), 3);
                        return true;
                    }
                }
                // TODO:add sign post support
            }
            else if (_bs.hasProperty(BlockStateProperties.ROTATION_0_15)){
                if (mydir.getAxis() == Axis.Y) {
                    world.setBlockState(targetpos, _bs.rotate(rot), 3);
                    return true;
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