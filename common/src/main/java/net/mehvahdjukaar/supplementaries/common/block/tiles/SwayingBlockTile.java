package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.PendulumAnimation;
import net.mehvahdjukaar.supplementaries.common.block.SwingAnimation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HangingSignBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class SwayingBlockTile extends BlockEntity implements IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> FANCY = ModBlockProperties.FANCY;

    public final SwingAnimation animation = new PendulumAnimation(ClientConfigs.Blocks.WALL_LANTERN_CONFIG,
            this::getNormalRotationAxis);

    // lod stuff
    protected boolean shouldHaveTESR = false; // current
    protected boolean currentlyHasTESR = false; // old
    private int ticksToSwitchMode = 0;

    protected SwayingBlockTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public boolean isAlwaysFast() {
        return ClientConfigs.Blocks.FAST_LANTERNS.get();
    }

    //called when data is actually refreshed
    @Override
    public ExtraModelData getExtraModelData() {
        this.ticksToSwitchMode = 2;
        return ExtraModelData.builder()
                .with(FANCY, this.shouldHaveTESR)
                .build();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setFancyRenderer(boolean fancy) {
        if (this.isAlwaysFast()) fancy = false;
        if (fancy != this.shouldHaveTESR) {
            this.currentlyHasTESR = this.shouldHaveTESR;
            this.shouldHaveTESR = fancy;
            //model data doesn't like other levels. linked to crashes with other mods
            if (this.level == Minecraft.getInstance().level) {
                this.requestModelReload();
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
            }
            if (!fancy) this.animation.reset();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean shouldRenderFancy() {
        if (this.currentlyHasTESR != this.shouldHaveTESR && !this.currentlyHasTESR) {
            //makes tesr wait 1 render cycle,
            // so it's in sync with model data refreshVisuals
            this.currentlyHasTESR = true;
        }
        return currentlyHasTESR;
    }

    public static void clientTick(Level pLevel, BlockPos pPos, BlockState pState, SwayingBlockTile tile) {

        if (tile.currentlyHasTESR != tile.shouldHaveTESR && tile.currentlyHasTESR && tile.ticksToSwitchMode > 0) {
            tile.ticksToSwitchMode--;
            if (tile.ticksToSwitchMode == 0) {
                //makes tesr wait 1 render cycle,
                // so it's in sync with model data refreshVisuals
                tile.currentlyHasTESR = false;
            }
        }

        if (tile.shouldRenderFancy()) {
            tile.animation.tick(pLevel, pPos, pState);
        }
    }

    //rotation axis rotate 90 deg
    public abstract Vec3i getNormalRotationAxis(BlockState state);

}
