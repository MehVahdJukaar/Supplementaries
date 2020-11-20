package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

public class FireflyJarBlockTile extends TileEntity implements ITickableTileEntity {
    protected final Random rand = new Random();
    public FireflyJarBlockTile() {
        super(Registry.FIREFLY_JAR_TILE.get());
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
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

    public void tick() {
        if (this.world.isRemote() && this.world.getGameTime() % 8L == 0L && this.rand.nextFloat() > 0.55f) {
            int x = this.pos.getX();
            int y = this.pos.getY();
            int z = this.pos.getZ();
            double pr = 0.15d;
            for (int l = 0; l < 1; ++l) {
                double d0 = (x + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                double d1 = (y + 0.5 - 0.0625 + (this.rand.nextFloat() - 0.5) * (0.875D - pr));
                double d2 = (z + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                world.addParticle(Registry.FIREFLY_GLOW.get(), d0, d1, d2, 0, 0, 0);
            }
        }
    }
}