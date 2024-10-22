package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GobletBlockTile extends BlockEntity implements ISoftFluidTankProvider, IOwnerProtected, IExtraModelDataProvider {

    private UUID owner = null;

    public final SoftFluidTank fluidHolder;
    public static final ModelDataKey<ResourceKey<SoftFluid>> FLUID_ID = ModBlockProperties.FLUID;


    public GobletBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GOBLET_TILE.get(), pos, state);
        this.fluidHolder = SoftFluidTank.create(1);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(FLUID_ID, getSoftFluidTank().getFluid().getHolder().unwrapKey().get());
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {

        //interact with fluid holder
        if (this.fluidHolder.interactWithPlayer(player, hand, level, worldPosition)) {
            return true;
        }
        //empty hand: eat food
        if (!player.isShiftKeyDown()) {
            //from drink
            if (CommonConfigs.Building.GOBLET_DRINK.get()) {
                boolean b = this.fluidHolder.tryDrinkUpFluid(player, this.level);
                if (b && player instanceof ServerPlayer serverPlayer) {
                    Advancement advancement = level.getServer().getAdvancements().getAdvancement(Supplementaries.res("nether/goblet"));
                    if (advancement != null) {
                        if (!serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone()) {
                            serverPlayer.getAdvancements().award(advancement, "unlock");
                        }
                    }
                }
                return b;
            }
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        try {
            this.fluidHolder.load(compound);
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("Failed to load fluid container at {}:", this.getBlockPos(), e);
        }
        this.loadOwner(compound);

        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        try {
            this.fluidHolder.save(tag);
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("Failed to save fluid container at {}:", this.getBlockPos(), e);
        }
        this.saveOwner(tag);
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return this.fluidHolder;
    }



}