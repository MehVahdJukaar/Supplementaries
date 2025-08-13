package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.client.GlobeRenderData;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GlobeBlockTile extends BlockEntity implements Nameable {

    private final boolean sepia;

    private boolean sheared = false;

    //client
    private Component customName = null;
    private float yaw = 0;
    private float prevYaw = 0;
    private GlobeRenderData renderData;


    public GlobeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GLOBE_TILE.get(), pos, state);
        this.sepia = state.is(ModRegistry.GLOBE_SEPIA.get());
        if (PlatHelper.getPhysicalSide().isClient()) {
            renderData = GlobeManager.DEFAULT_DATA;
        }
    }

    public int getFaceRot() {
        return (3 - getBlockState().getValue(GlobeBlock.ROTATION)) * 90;
    }

    public float getRotation(float partialTicks) {
        int face = getFaceRot();
        return Mth.lerp(partialTicks, prevYaw + face, yaw + face);
    }

    @NotNull
    public GlobeRenderData getRenderData() {
        return renderData;
    }

    public boolean isSepia() {
        return sepia;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        this.updateRenderData();
    }

    public void toggleShearing() {
        this.sheared = !this.sheared;
        this.updateRenderData();
    }

    private void updateRenderData() {
        if (this.level == null || !this.level.isClientSide) return;
        if (this.sheared) {
            this.renderData = Pair.of(Model.SHEARED,
                    sepia ? GLOBE_SHEARED_SEPIA_TEXTURE :
                            GLOBE_SHEARED_TEXTURE);
        } else if (this.hasCustomName()) {
            var customData = GlobeManager.Type.getModelAndTexture(this.getCustomName().getString());
            if (customData != null) this.renderData = customData;
            else this.renderData = DEFAULT_DATA;
        } else this.renderData = DEFAULT_DATA;
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public Component getCustomName() {
        return this.customName;
    }

    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.globe");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName", 8)) {
            this.setCustomName(Component.Serializer.fromJson(tag.getString("CustomName"), registries));
        }
        this.yaw = tag.getFloat("Yaw");
        this.sheared = tag.getBoolean("Sheared");
        this.updateRenderData();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName, registries));
        }
        tag.putFloat("Yaw", this.yaw);
        tag.putBoolean("Sheared", this.sheared);
    }

    public void spin() {
        int spin = 360;
        int inc = 90;
        int face = ((this.getFaceRot() - inc) + 360) % 360;
        this.yaw = (this.yaw + spin + inc);
        this.prevYaw = (this.prevYaw + spin + inc);
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(GlobeBlock.ROTATION, 3 - face / 90));
        this.setChanged();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            this.level.playSound(null, this.worldPosition,
                    ModSounds.GLOBE_SPIN.get(),
                    SoundSource.BLOCKS, 0.65f,
                    MthUtils.nextWeighted(level.random, 0.2f) + 0.9f);
            return true;
        } else if (id == 2) {
            level.addDestroyBlockEffect(worldPosition, getBlockState());
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GlobeBlockTile tile) {
        tile.prevYaw = tile.yaw;
        if (tile.yaw != 0) {
            if (tile.yaw < 0) {
                tile.yaw = 0;
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            } else {
                tile.yaw = (tile.yaw * 0.94f) - 0.7f;
            }
        }
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(GlobeBlock.FACING);
    }

    public boolean isSpinningVeryFast() {
        return yaw > 1500;
    }

    public int getSignalPower() {
        if (this.yaw != 0) return 15;
        else return this.getFaceRot() / 90 + 1;
    }

}