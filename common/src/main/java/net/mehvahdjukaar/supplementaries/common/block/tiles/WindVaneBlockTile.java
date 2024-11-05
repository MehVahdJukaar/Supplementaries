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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindVaneBlockTile extends BlockEntity {
    private float yaw = 0;
    private float prevYaw = 0;
    private float offset = 0;

    private int windChargedTicks = 0;

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
        return Mth.lerp(partialTicks, prevYaw, yaw);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, WindVaneBlockTile tile) {

        float currentYaw = tile.yaw;
        tile.prevYaw = currentYaw;
        if (!pLevel.isClientSide()) {
            if (pLevel.getGameTime() % 20L == 0L) {
                Block block = pState.getBlock();
                if (block instanceof WindVaneBlock) {
                    WindVaneBlock.updatePower(pState, pLevel, pPos);
                }
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

            if (CompatHandler.WILDER_WILD) {
                newYaw += WilderWildCompat.getWindAngle(pPos, pLevel);
            } else if (CompatHandler.BREEZY) {
                newYaw += BreezyCompat.getWindAngle(pPos, pLevel);
            }

            tile.yaw = Mth.clamp(newYaw, currentYaw - 8, currentYaw + 8);

            tile.yaw += (float) (tile.windChargedTicks*0.2);

            if (tile.windChargedTicks > 0) {
                tile.windChargedTicks--;
            }
        }
    }

    public void setWindCharged() {
        this.windChargedTicks = 40;
    }
}