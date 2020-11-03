package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;


public class WallLanternBlockTile extends TileEntity implements ITickableTileEntity {
    public float angle = 0;
    public float prevAngle = 0;
    public int counter = 800;
    public BlockState lanternBlock = Blocks.AIR.getDefaultState();
    // lower counter is used by hitting animation
    public WallLanternBlockTile() {
        super(Registry.WALL_LANTERN_TILE.get());
    }


    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.lanternBlock = NBTUtil.readBlockState(compound.getCompound("Lantern"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put("Lantern", NBTUtil.writeBlockState(lanternBlock));
        return compound;
    }


    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 9, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }

    public Direction getDirection() {
        return this.getBlockState().get(WallLanternBlock.FACING);
    }

    @Override
    public void tick() {
        if (this.world.isRemote) {
            this.counter++;
            this.prevAngle = this.angle;
            float maxswingangle = 45f;
            float minswingangle = 1.9f;
            float maxperiod = 28f;
            float angleledamping = 80f;
            float perioddamping = 70f;
            // actually tey are the inverse of damping. increase them to fave less damping
            float a = minswingangle;
            float k = 0.01f;
            if (counter < 800) {
                a = (float) Math.max(maxswingangle * Math.pow(Math.E, -(counter / angleledamping)), minswingangle);
                k = (float) Math.max(Math.PI * 2 * (float) Math.pow(Math.E, -(counter / perioddamping)), 0.01f);
            }
            this.angle = a * MathHelper.cos((counter / maxperiod) - k);
            // this.angle = 90*(float)
            // Math.cos((float)counter/40f)/((float)this.counter/20f);;
        }
    }
}