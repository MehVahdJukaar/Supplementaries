package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GobletBlockTile extends BlockEntity implements ISoftFluidTankProvider, IOwnerProtected {

    private UUID owner = null;

    public ISoftFluidTank fluidTank;

    public GobletBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GOBLET_TILE.get(), pos, state);
        int CAPACITY = 1;
        this.fluidTank = ISoftFluidTank.create(CAPACITY);
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
        int light = this.fluidTank.getFluid().getLuminosity();
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
        if (this.fluidTank.interactWithPlayer(player, hand, level, worldPosition)) {
            return true;
        }
        //empty hand: eat food
        if (!player.isShiftKeyDown()) {
            //from drink
            if (CommonConfigs.Blocks.GOBLET_DRINK.get()) {
                boolean b = this.fluidTank.tryDrinkUpFluid(player, this.level);
                if (b && player instanceof ServerPlayer serverPlayer) {
                    Advancement advancement = level.getServer().getAdvancements().getAdvancement(new ResourceLocation("supplementaries:nether/goblet"));
                    if (advancement != null) {
                        serverPlayer.getAdvancements().award(advancement, "unlock");
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
        this.fluidTank.load(compound);
        this.loadOwner(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.fluidTank.save(tag);
        this.saveOwner(tag);
    }

    @Override
    public ISoftFluidTank getSoftFluidTank() {
        return this.fluidTank;
    }
}