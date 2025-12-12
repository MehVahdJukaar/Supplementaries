package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetSource;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class FaucetBlockTile extends BlockEntity implements IExtraModelDataProvider {


    public static final ModelDataKey<ResourceKey<SoftFluid>> FLUID = ModBlockProperties.FLUID;
    public static final ModelDataKey<Integer> FLUID_COLOR = ModBlockProperties.FLUID_COLOR;
    public static final int COOLDOWN_PER_BOTTLE = 20;

    private int transferCooldown = 0;
    public final SoftFluidTank tempFluidHolder;

    public FaucetBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FAUCET_TILE.get(), pos, state);
        this.tempFluidHolder = SoftFluidTank.create(5, Utils.hackyGetRegistryAccess());
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        if (level != null && !tempFluidHolder.isEmpty()) {
            int color = tempFluidHolder.getCachedFlowingColor(level, worldPosition);
            builder.with(FLUID, tempFluidHolder.getFluid().getHolder().unwrapKey().get());
            builder.with(FLUID_COLOR, color);
        }
    }

    public void updateLight() {
        if (this.level == null) return;
        int light = this.tempFluidHolder.getFluidValue().getLuminosity();
        if (light != 0) light = (int) Mth.clamp(light / 2f, 1, 7);
        if (light != this.getBlockState().getValue(FaucetBlock.LIGHT_LEVEL)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FaucetBlock.LIGHT_LEVEL, light), 2);
        }
    }


    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, FaucetBlockTile tile) {
        if (tile.transferCooldown > 0) {
            tile.transferCooldown--;
        } else if (tile.isOpen()) {
            int cooldown = tile.tryExtract((ServerLevel) pLevel, false);
            tile.transferCooldown += cooldown;
        }
    }

//------fluids------

    //returns true if it has water animation
    public boolean updateContainedFluidVisuals(ServerLevel serverLevel) {
        SoftFluidStack previous = this.tempFluidHolder.getFluid().copy();
        boolean r = this.tryExtract(serverLevel, true) != 0; //this will set the internal tank
        this.updateLight();
        if (!previous.isSameFluidSameComponents(this.tempFluidHolder.getFluid())) {
            this.setChanged();//prevents recursion
            serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return r;
    }

    /**
     * @return 0 for fail, non 0 will be the transfer cooldown
     */
    private int tryExtract(ServerLevel level, boolean simulate) {
        BlockState state = this.getBlockState();
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = this.worldPosition.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        if (backState.isAir() || backState.is(ModTags.FAUCET_CANT_INTERACT)) return 0;
        var behaviors = FaucetBehaviorsManager.getInstance(level);
        Integer filledAmount = runInteractions(behaviors.getBlockInteractions(), level, dir, behind, backState, simulate);
        if (filledAmount != null) return filledAmount;

        //tile interactions
        BlockEntity tileBack = level.getBlockEntity(behind);

        if (tileBack != null) {
            filledAmount = runInteractions(behaviors.getTileInteractions(), level, dir, behind, tileBack, simulate);

            if (filledAmount != null) return filledAmount;
        }

        if (!this.isConnectedBelow() && !simulate &&
                (CommonConfigs.Redstone.FAUCET_DROP_ITEMS.get() ||
                        CommonConfigs.Redstone.FAUCET_FILL_ENTITIES.get())) {
            for (var bi : behaviors.getItemInteractions()) {
                ItemStack removed = bi.tryExtractItem(level, behind, backState, dir, tileBack);
                if (!removed.isEmpty()) {
                    if (CommonConfigs.Redstone.FAUCET_FILL_ENTITIES.get() && fillEntityBelow(removed)) {
                        //TODO
                    } else if (CommonConfigs.Redstone.FAUCET_DROP_ITEMS.get()) {
                        drop(removed);
                    }
                    return COOLDOWN_PER_BOTTLE;
                }
            }
        }

        FluidState fluidState = level.getFluidState(behind);
        filledAmount = runInteractions(behaviors.getSourceFluidInteractions(), level, dir, behind, fluidState, simulate);
        if (filledAmount != null) return filledAmount;

        return 0;
    }

    // returns cooldown
    @Nullable
    private <T, S extends FaucetSource<T>> Integer runInteractions(Iterable<S> interactions, Level level, Direction dir,
                                                                   BlockPos pos, T source, boolean justVisual) {
        for (var inter : interactions) {
            FluidOffer fluid = inter.getProvidedFluid(level, pos, dir, source);
            if (fluid == null) continue;
            if (justVisual) {
                this.tempFluidHolder.setFluid(fluid.fluid());
                return COOLDOWN_PER_BOTTLE;
            }
            Integer amountFilled = tryFillingBlockBelow(fluid);
            if (amountFilled != null) {
                if (amountFilled == 0) return 0;
                inter.drain(level, pos, dir, source, amountFilled);
                return amountFilled * COOLDOWN_PER_BOTTLE;
            }
        }
        return null;
    }

    //sf->ff/sf
    private Integer tryFillingBlockBelow(FluidOffer offer) {
        BlockPos below = this.worldPosition.below();
        BlockState belowState = level.getBlockState(below);
        var behaviors = FaucetBehaviorsManager.getInstance(level);
        for (var bi : behaviors.getTargetBlockInteractions()) {
            Integer res = bi.fill(level, below, belowState, offer);
            if (res != null) return res;
        }

        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow != null) {
            for (var bi : behaviors.getTargetTileInteractions()) {
                Integer res = bi.fill(level, below, tileBelow, offer);
                if (res != null) return res;
            }
        }
        FluidState fluidState = belowState.getFluidState();
        for (var bi : behaviors.getTargetFluidInteractions()) {
            Integer res = bi.fill(level, below, fluidState, offer);
            if (res != null) return res;
        }

        return null;
    }

//------end-fluids------

    public boolean isOpen() {
        return (this.getBlockState().getValue(BlockStateProperties.POWERED) ^ this.getBlockState().getValue(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().getValue(FaucetBlock.HAS_WATER);
    }

    public boolean isConnectedBelow() {
        return this.getBlockState().getValue(FaucetBlock.CONNECTED);
    }

//------items------

    private void drop(ItemStack extracted) {
        BlockPos pos = worldPosition;
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
        drop.setDeltaMovement(new Vec3(0, 0, 0));
        level.addFreshEntity(drop);
        float f = (level.random.nextFloat() - 0.5f) / 4f;
        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
    }

    public static final Predicate<Entity> NON_PLAYER = e -> e.isAlive() && !(e instanceof Player);

    private boolean fillEntityBelow(ItemStack stack) {
        List<Entity> list = level.getEntities((Entity) null,
                new AABB(worldPosition).move(0, -0.75, 0),
                NON_PLAYER);
        Collections.shuffle(list);
        for (Entity entity : list) {
            stack = ItemsUtil.tryAddingItem(stack, level, entity);
            if (stack.isEmpty()) return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.transferCooldown = tag.getInt("TransferCooldown");
        this.tempFluidHolder.load(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TransferCooldown", this.transferCooldown);
        this.tempFluidHolder.save(tag, registries);

    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Deprecated(forRemoval = true)
    public static void registerInteraction(Object interaction) {
        FaucetBehaviorsManager.addRegisterFaucetInteractions(iFaucetEvent -> {
            iFaucetEvent.registerInteraction(interaction);
        });
    }

}