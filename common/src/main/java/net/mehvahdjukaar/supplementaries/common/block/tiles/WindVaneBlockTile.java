package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.integration.BreezyCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.WilderWildCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindVaneBlockTile extends BlockEntity {
    private static final int WIND_CHARGED_DURATION = 40;

    private int windChargedTicks = 0;

    private float yaw = 0;
    private float prevYaw = 0;
    private float offset = 0;


    public WindVaneBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WIND_VANE_TILE.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.windChargedTicks = tag.getInt("wind_charged_ticks");


        float tp = (float) (Math.PI * 2);
        this.offset = 400 * (Mth.sin((0.005f * this.worldPosition.getX()) % tp) + Mth.sin((0.005f * this.worldPosition.getZ()) % tp) + Mth.sin((0.005f * this.worldPosition.getY()) % tp));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("wind_charged_ticks", this.windChargedTicks);
    }

    public float getYaw(float partialTicks) {
        return yaw;//Mth.lerp(partialTicks, prevYaw, yaw);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, WindVaneBlockTile tile) {

        float currentYaw = tile.yaw;
        tile.prevYaw = currentYaw;
        if (!pLevel.isClientSide()) {
            if (pLevel.getGameTime() % 20L == 0L) {
                WindVaneBlock.updatePower(pState, pLevel, pPos, tile.windChargedTicks != 0);
            }
        } else {
            int power = pState.getValue(WindVaneBlock.WIND_STRENGTH);
            float tp = (float) (2f * Math.PI);
            float t = pLevel.getGameTime() % 24000 + tile.offset;
            float b = (float) Math.max(1, (power * ClientConfigs.Blocks.WIND_VANE_POWER_SCALING.get()));
            double maxAngle1 = ClientConfigs.Blocks.WIND_VANE_ANGLE_1.get();
            double maxAngle2 = ClientConfigs.Blocks.WIND_VANE_ANGLE_2.get();
            double period1 = ClientConfigs.Blocks.WIND_VANE_PERIOD_1.get();
            double period2 = ClientConfigs.Blocks.WIND_VANE_PERIOD_2.get();
            float newYaw = (float) (maxAngle1 * Mth.sin((float) (tp * ((t * b / period1) % 360)))
                    + maxAngle2 * Mth.sin((float) (tp * ((t * b / period2) % 360))));


//TODO: this is shit, not smooth and bad. redo
            float windCharged = (float) tile.windChargedTicks / WIND_CHARGED_DURATION;
            float rise = 0.3f;
            float trapezoidal = trapezoidal(1 - windCharged, rise);
            tile.yaw = (1 - trapezoidal) * newYaw;

            if (CompatHandler.WILDER_WILD) {
                tile.yaw += WilderWildCompat.getWindAngle(pPos, pLevel);
            } else if (CompatHandler.BREEZY) {
                tile.yaw += BreezyCompat.getWindAngle(pPos, pLevel);
            }

            //tile.yaw = Mth.clamp(tile.yaw, currentYaw - 8, currentYaw + 8);

            float p = 1 - (windCharged * windCharged);
            if (windCharged < rise) p = 1;
            tile.yaw += (p * 360 * 5f);

            tile.yaw = Mth.wrapDegrees(tile.yaw);

        }

        if (tile.windChargedTicks > 0) {
            tile.windChargedTicks--;

            if (tile.windChargedTicks == 0 && !pLevel.isClientSide) {
                WindVaneBlock.updatePower(pState, pLevel, pPos, false);
            }
        }
    }

    private static float trapezoidal(float x, float rise) {
        // End of rise phase
        float fallStart = 1f - rise;        // Start of fall phase

        if (x < 0f) {
            // Before the start, return 0
            return 0f;
        } else if (x < rise) {
            // Quadratic increase from 0 to 1 over [0, riseEnd]
            float a = (x - rise) / rise;
            return 1 - a * a;
        } else if (x < fallStart) {
            // Constant at 1 over [riseEnd, fallStart]
            return 1f;
        } else if (x <= 1.0) {
            // Quadratic fall from 1 to 0 over [fallStart, 1]
            float normalizedX = (x - fallStart) / rise;
            return (1 - normalizedX) * (1 - normalizedX) * (3 - 2 * (1 - normalizedX));
        } else {
            // After the end, return 0
            return 0f;
        }
    }

    public void setWindCharged() {
        this.windChargedTicks = WIND_CHARGED_DURATION;
    }
}