package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nonnull;

public class ClockBlockTile extends BlockEntity implements TickableBlockEntity {
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
        super(ModRegistry.CLOCK_BLOCK_TILE.get());
    }

    @Override
    public void load(@Nonnull BlockState state,@Nonnull CompoundTag compound) {
        super.load(state, compound);
        this.roll = compound.getFloat("MinRoll");
        this.prevRoll = this.roll;
        this.targetRoll = this.roll;

        this.sRoll = compound.getFloat("SecRoll");
        this.sPrevRoll = this.sRoll;
        this.sTargetRoll = this.sRoll;
        this.power = compound.getInt("Power");
    }

    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        super.save(compound);
        compound.putFloat("MinRoll", this.targetRoll);
        compound.putFloat("SecRoll", this.sTargetRoll);
        compound.putInt("Power",this.power);
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
        this.load(this.getBlockState(), pkt.getTag());
    }

    public void updateInitialTime(){
        int time = (int) (level.getDayTime() % 24000);
        this.updateTime(time);
        this.roll = this.targetRoll;
        this.prevRoll = this.targetRoll;
        this.sRoll = this.sTargetRoll;
        this.sPrevRoll = this.sTargetRoll;
    }

    //TODO: rewrite
    public void updateTime(int time){

        if(this.level.dimensionType().natural()) {

            //minute here are 1 rl second -> 50m in a minecraft hour
            int minute = Mth.clamp((time % 1000) / 20, 0, 50);
            int hour = Mth.clamp(time / 1000, 0, 24);

            //server
            if (!this.level.isClientSide) {
                BlockState state = this.getBlockState();
                if (hour != state.getValue(ClockBlock.HOUR)) {
                    //if they are sent to the client the animation gets broken. Side effect is that you can't see hour with f3
                    level.setBlock(this.worldPosition, state.setValue(ClockBlock.HOUR, hour), 3);
                }
                int p = Mth.clamp(time / 1500, 0, 15);
                if (p != this.power) {
                    this.power = p;
                    this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
                }
                //TODO: add proper sounds
                //this.world.playSound(null, this.pos, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, SoundCategory.BLOCKS,0.03f,time%40==0?2:1.92f);

            }
            //hours
            this.targetRoll = (hour * 30) % 360;
            //minutes
            this.sTargetRoll = (minute * 7.2f + 180) % 360f;

        }
        else {

            /*
            double d0 = Math.random() - (this.targetRoll/360f);
            d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
            this.rota += d0 * 0.1D;
            this.rota *= 0.9D;
            this.targetRoll = 360*((float) MathHelper.positiveModulo(this.targetRoll/360f + this.rota, 1.0D));

            this.roll = this.targetRoll;
            */


            this.targetRoll = this.level.random.nextFloat()*360;
            this.sTargetRoll = this.level.random.nextFloat()*360;
            //TODO: make it wobbly
        }





    }

    //TODO: use this on other blocks
    //can't access chunk data. use with care
    @Override
    public void onLoad() {
        //this.calculateOffset();
    }
    //makes clock sync with daytime
    private void calculateOffset(){
        long dayTime = (this.level.getDayTime()%24000)%20;
        long gameTime = (this.level.getGameTime()%24000)%20;
        this.offset = (int) (dayTime - gameTime);
    }

    @Override
    public void tick() {

        int time = this.level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)?
                (int) (level.getDayTime() % 24000) : (int) (level.getGameTime() % 24000);
        if (this.level != null && (time) % 20 == 0) {
            this.updateTime((int) (this.level.getDayTime()%24000));

        }
        //TODO: fix hour hand
        //hours
        this.prevRoll = this.roll;
        if (this.roll != this.targetRoll) {
            float r = (this.roll + 8) % 360;
            if ((r >= this.targetRoll) && (r <= this.targetRoll + 8)) {
                r = this.targetRoll;
            }
            this.roll = r;
        }
        //minutes
        this.sPrevRoll = this.sRoll;
        if (this.sRoll != this.sTargetRoll) {
            float r = (this.sRoll + 8) % 360;
            if ((r >= this.sTargetRoll) && (r <= this.sTargetRoll + 8)) {
                r = this.sTargetRoll;
            }
            this.sRoll = r;
        }
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }
}

