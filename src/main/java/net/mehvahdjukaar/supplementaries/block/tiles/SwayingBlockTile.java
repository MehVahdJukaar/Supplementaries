package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.SwayingBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.Random;

public abstract class SwayingBlockTile extends TileEntity implements ITickableTileEntity {
    public float angle = 0;
    public float prevAngle = 0;
    // lower counter is used by hitting animation
    public int counter = 800 + new Random().nextInt(80);
    public boolean inv = false;

    protected static float maxSwingAngle = 45f;
    protected static float minSwingAngle = 2.5f;
    protected static float maxPeriod = 25f;
    protected static float angleDamping = 150f;
    protected static float periodDamping = 100f;

    //lod stuff
    //protected boolean TESRCalled = false;
    public boolean fancyRenderer = false;
    protected boolean oldRendererState = false;

    public SwayingBlockTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);

    }

    @Override
    public double getViewDistance() {
        return 64;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 9, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(SwayingBlock.FACING);
    }

    public void setFancyRenderer(boolean fancy) {
        if (ClientConfigs.cached.FAST_LANTERNS) fancy = false;
        if (fancy != this.fancyRenderer) {
            this.oldRendererState = this.fancyRenderer;
            this.fancyRenderer = fancy;
            //model data doesn't like other levels. linked to crashes with other mods
            if(this.level == Minecraft.getInstance().level) {
                this.requestModelDataUpdate();
                //TODO: replace hardcoded int with const.blockFlags
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.RERENDER_MAIN_THREAD);

            }}
    }

    public boolean shouldRenderFancy() {
        if (oldRendererState != fancyRenderer) {
            //makes tesr wait 1 render cycle,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        so it's in sync with model data refresh
            this.oldRendererState = this.fancyRenderer;
            return oldRendererState;
        }
        return fancyRenderer;
    }

    public boolean hasAnimation() {
        return true;
    }

    @Override
    public void tick() {
        if (this.level.isClientSide && this.hasAnimation()) {

            //TODO: improve physics
            this.counter++;

            this.prevAngle = this.angle;
            //actually they are the inverse of damping. increase them to have less damping

            float a = minSwingAngle;
            float k = 0.01f;
            if (counter < 800) {
                a = (float) Math.max(maxSwingAngle * Math.pow(Math.E, -(counter / angleDamping)), minSwingAngle);
                k = (float) Math.max(Math.PI * 2 * (float) Math.pow(Math.E, -(counter / periodDamping)), 0.01f);
            }

            this.angle = a * MathHelper.cos((counter / maxPeriod) - k);
            this.angle *= this.inv ? -1 : 1;
            // this.angle = 90*(float)
            // Math.cos((float)counter/40f)/((float)this.counter/20f);;
        }
    }

}
