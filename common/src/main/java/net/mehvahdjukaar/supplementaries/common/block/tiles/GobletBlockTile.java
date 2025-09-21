package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GobletBlockTile extends BlockEntity implements ISoftFluidTankProvider, IExtraModelDataProvider {

    public final SoftFluidTank fluidHolder;
    public static final ModelDataKey<ResourceKey<SoftFluid>> FLUID_ID = ModBlockProperties.FLUID;


    public GobletBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GOBLET_TILE.get(), pos, state);
        this.fluidHolder = SoftFluidTank.create(1, Utils.hackyGetRegistryAccess());
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        SoftFluidTank softFluidTank = getSoftFluidTank();
        if (!softFluidTank.isEmpty()) {
            builder.with(FLUID_ID, softFluidTank.getFluid().getHolder().unwrapKey().get());
        }
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());
        int light = this.fluidHolder.getFluidValue().getLuminosity();
        if (light != this.getBlockState().getValue(ModBlockProperties.LIGHT_LEVEL_0_15)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(ModBlockProperties.LIGHT_LEVEL_0_15, light), 2);
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fluidHolder.load(tag, registries);

        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.fluidHolder.save(tag, registries);
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return this.fluidHolder;
    }


}