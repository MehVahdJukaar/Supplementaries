package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

public class FireflyJarBlockTile extends TileEntity implements ITickableTileEntity {
    protected final Random rand = new Random();
    public final boolean soul;

    public FireflyJarBlockTile(boolean isSoul) {
        super(Registry.FIREFLY_JAR_TILE.get());
        soul=isSoul;
    }
    public FireflyJarBlockTile() {
        this(false);
    }

    @Override
    public double getViewDistance() {
        return 64;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
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

    public void tick() {

        if (this.level.isClientSide()){
            int p = ClientConfigs.cached.FIREFLY_SPAWN_PERIOD;
            float c = (float) ClientConfigs.cached.FIREFLY_SPAWN_CHANCE;
            if(this.level.getGameTime() % p == 0L && this.rand.nextFloat() > c) {
                int x = this.worldPosition.getX();
                int y = this.worldPosition.getY();
                int z = this.worldPosition.getZ();
                double pr = 0.15;
                if(soul){
                    pr=0.25;
                    for (int l = 0; l < 1; ++l) {
                        double d0 = (x + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                        double d1 = (y + 0.25);
                        double d2 = (z + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                        level.addParticle(ParticleTypes.SOUL, d0, d1, d2, 0, this.rand.nextFloat()*0.02, 0);
                    }
                }
                else {
                    for (int l = 0; l < 1; ++l) {
                        double d0 = (x + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                        double d1 = (y + 0.5 - 0.0625 + (this.rand.nextFloat() - 0.5) * (0.875D - pr));
                        double d2 = (z + 0.5 + (this.rand.nextFloat() - 0.5) * (0.625D - pr));
                        level.addParticle(Registry.FIREFLY_GLOW.get(), d0, d1, d2, 0, 0, 0);
                    }
                }
            }
        }
    }

}