package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;

import javax.annotation.Nonnull;

public class ClockBlockTile extends TileEntity implements ITickableTileEntity {
    public float roll = 0;
    public float prevRoll = 0;
    public float targetRoll = 0;

    public float sRoll = 0;
    public float sPrevRoll = 0;
    public float sTargetRoll = 0;

    public int power = 0;

    private int offset = 0; //difference between gameTime and DayTime
    private boolean updateOffset = true;

    public ClockBlockTile() {
        super(Registry.CLOCK_BLOCK_TILE);
    }

    @Override
    public void read(@Nonnull BlockState state,@Nonnull CompoundNBT compound) {
        super.read(state, compound);
        this.roll = compound.getFloat("MinRoll");
        this.prevRoll = this.roll;
        this.targetRoll = this.roll;

        this.sRoll = compound.getFloat("SecRoll");
        this.sPrevRoll = this.sRoll;
        this.targetRoll = this.sRoll;
        this.power = compound.getInt("power");
    }

    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        super.write(compound);
        compound.putFloat("MinRoll", this.targetRoll);
        compound.putFloat("SecRoll", this.sTargetRoll);
        compound.putInt("Power",this.power);
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

    public void updateInitialTime(){
        int time = (int) (world.getDayTime() % 24000);
        this.updateTime(time);
        this.roll = this.targetRoll;
        this.prevRoll = this.targetRoll;
        this.sRoll = this.sTargetRoll;
        this.sPrevRoll = this.sTargetRoll;
    }

    public void updateTime(int time){
        //minute here are 1 rl second -> 50m in a minecraft hour
        int minute = MathHelper.clamp((time%1000)/20 , 0, 50);
        int hour = MathHelper.clamp(time / 1000, 0, 24);

        //server
        if(!this.world.isRemote){
            BlockState state = this.getBlockState();
            if(hour!=state.get(ClockBlock.HOUR)){
                world.setBlockState(this.pos, state.with(ClockBlock.HOUR, hour));
            }
            int p = MathHelper.clamp(time / 1500, 0, 15);
            if (p!=this.power){
                this.power=p;
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
            }
        }
        //hours
        this.targetRoll = (hour*30)%360;
        //minutes
        this.sTargetRoll = (minute*7.2f + 180)%360f;
    }

    //TODO: use this on other blocks
    //can't access chunk data. use with care
    @Override
    public void onLoad() {
        //this.calculateOffset();
    }
    //makes clock sync with daytime
    private void calculateOffset(){
        long dayTime = (this.world.getDayTime()%24000)%20;
        long gameTime = (this.world.getGameTime()%24000)%20;
        this.offset = (int) (dayTime - gameTime);
    }

    public void tick() {
        int time = this.world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)?
                (int) (world.getDayTime() % 24000) : (int) (world.getGameTime() % 24000);
        if (this.world != null && (time) % 20 == 0) {
            this.updateTime((int) (this.world.getDayTime()%24000));
        }
        //hours
        this.prevRoll = this.roll;
        if (this.roll != this.targetRoll) {
            float r = (this.roll + 8) % 360;
            if (r >= this.targetRoll && r <= this.targetRoll + 8)
                r = this.targetRoll;
            this.roll = r;
        }
        //minutes
        this.sPrevRoll = this.sRoll;
        if (this.sRoll != this.sTargetRoll) {
            float r = (this.sRoll + 8) % 360;
            if (r >= this.sTargetRoll && r <= this.sTargetRoll + 8)
                r = this.sTargetRoll;
            this.sRoll = r;
        }
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }
}

