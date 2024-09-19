package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ClockBlockTile extends BlockEntity {

    private int power = 0;


    private float roll = 0;
    private float prevRoll = 0;
    private float targetRoll = 0;

    private float sRoll = 0;
    private float sPrevRoll = 0;
    private float sTargetRoll = 0;

    private float rota;
    private float sRota;

    public ClockBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.CLOCK_BLOCK_TILE.get(), pos, state);
    }

    public int getPower() {
        return power;
    }

    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        this.power = compound.getInt("Power");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Power", this.power);
    }

    public void updateInitialTime(Level level, BlockState state, BlockPos pos) {
        int time = (int) (level.getDayTime() % 24000);
        this.updateTargetTime(time, level, state, pos);
        this.roll = this.targetRoll;
        this.prevRoll = this.targetRoll;
        this.sRoll = this.sTargetRoll;
        this.sPrevRoll = this.sTargetRoll;
    }

    public static int calculateHour(int time){
        return Mth.clamp(time / 1000, 0, 24);
    }

    private static int calculateMinute(int time) {
        return Mth.clamp((time % 1000) / 20, 0, 50);
    }

    protected void updateTargetTime(int time, Level level, BlockState state, BlockPos pos) {
        //minute here are 1 rl second -> 50m in a minecraft hour
        int minute = calculateMinute(time);
        int hour = calculateHour(time);

        //server
        if (!level.isClientSide) {
            if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                if (hour != state.getValue(ClockBlock.HOUR)) {
                    //if they are sent to the client the animation gets broken. Side effect is that you can't see hour with f3
                    level.setBlock(pos, state.setValue(ClockBlock.HOUR, hour), 3);
                }
                int p = Mth.clamp(time / 1500, 0, 15);
                if (p != this.power) {
                    this.power = p;
                    level.updateNeighbourForOutputSignal(pos, this.getBlockState().getBlock());
                }
                this.level.playSound(null, this.worldPosition,
                        (minute % 2 == 0 ? ModSounds.CLOCK_TICK_1 : ModSounds.CLOCK_TICK_2).get(), SoundSource.BLOCKS,
                        0.08f, MthUtils.nextWeighted(level.random, 0.1f) + 0.95f);
            }
        } else {
            //hours
            this.targetRoll = (hour * 30) % 360;
            //minutes
            this.sTargetRoll = (minute * 7.2f + 180) % 360f;
        }
    }



    protected boolean canReadTime(Level level) {
        return level.dimensionType().natural() ^ MiscUtils.FESTIVITY.isAprilsFool();
    }

    public static void tick(Level level, BlockPos pPos, BlockState pState, ClockBlockTile tile) {
        int dayTime = (int) (level.getDayTime() % 24000);
        int time = level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) ?
                dayTime : (int) (level.getGameTime() % 24000);

        boolean canReadTime = tile.canReadTime(level);

        if (canReadTime && time % 20 == 0) {
            tile.updateTargetTime(dayTime, level, pState, pPos);
        }
        if (!level.isClientSide) {
            return;
        }

        tile.prevRoll = tile.roll;
        if (canReadTime) {

            //hours
            if (tile.roll != tile.targetRoll) {
                float r = (tile.roll + 8) % 360;
                if ((r >= tile.targetRoll) && (r <= tile.targetRoll + 8)) {
                    r = tile.targetRoll;
                }
                tile.roll = r;
            }
            //minutes
            tile.sPrevRoll = tile.sRoll;
            if (tile.sRoll != tile.sTargetRoll) {
                float r = (tile.sRoll + 8) % 360;
                if ((r >= tile.sTargetRoll) && (r <= tile.sTargetRoll + 8)) {
                    r = tile.sTargetRoll;
                }
                tile.sRoll = r;
            }
        } else {

            if (time % 5 == 0) {

                float d = level.random.nextFloat() * 360;
                float d0 = d - tile.roll;
                d0 = (Mth.positiveModulo(d0 + 180, 360) - 180);
                tile.targetRoll = d0;

                d = level.random.nextFloat() * 360;
                d0 = d - tile.sRoll;
                d0 = Mth.positiveModulo(d0 + 180, 360) - 180;
                tile.sTargetRoll = d0;
            }

            tile.rota += (float) (tile.targetRoll * 0.1);
            tile.rota *= 0.8F;
            tile.roll = Mth.positiveModulo(tile.roll + tile.rota, 360);

            tile.sPrevRoll = tile.sRoll;


            tile.sRota += (float) (tile.sTargetRoll * 0.1);
            tile.sRota *= 0.8F;
            tile.sRoll = Mth.positiveModulo(tile.sRoll + tile.sRota, 360);
        }
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ClockBlock.FACING);
    }

    public float getRollS(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.sPrevRoll, this.sRoll);
    }

    public float getRoll(float partialTicks) {
        return Mth.rotLerp(partialTicks, this.prevRoll, this.roll);
    }

}

