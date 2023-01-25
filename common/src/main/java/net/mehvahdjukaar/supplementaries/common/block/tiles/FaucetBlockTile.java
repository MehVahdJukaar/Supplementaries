package net.mehvahdjukaar.supplementaries.common.block.tiles;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.common.block.faucet.*;
import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FaucetBlockTile extends BlockEntity {

    private static final List<IFaucetBlockSource> BLOCK_INTERACTIONS = new ArrayList<>();
    private static final List<IFaucetTileSource> TILE_INTERACTIONS = new ArrayList<>();
    private static final List<IFaucetFluidSource> FLUID_INTERACTIONS = new ArrayList<>();
    private static final List<IFaucetItemSource> ITEM_INTERACTIONS = new ArrayList<>();
    private static final List<IFaucetBlockTarget> TARGET_BLOCK_INTERACTIONS = new ArrayList<>();
    private static final List<IFaucetTileTarget> TARGET_TILE_INTERACTIONS = new ArrayList<>();

    public static final int COOLDOWN = 20;

    private int transferCooldown = 0;
    public final SoftFluidTank tempFluidHolder = SoftFluidTank.create(5);

    public FaucetBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FAUCET_TILE.get(), pos, state);
    }


    public void updateLight() {
        if (this.level == null) return;
        int light = this.tempFluidHolder.getFluid().getLuminosity();
        if (light != 0) light = (int) Mth.clamp(light / 2f, 1, 7);
        if (light != this.getBlockState().getValue(FaucetBlock.LIGHT_LEVEL)) {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FaucetBlock.LIGHT_LEVEL, light), 2);
        }
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FaucetBlockTile tile) {
        if (tile.transferCooldown > 0) {
            tile.transferCooldown--;
        } else if (tile.isOpen()) {
            int cooldown = tile.tryExtract(pLevel, pPos, pState, true);
            tile.transferCooldown += cooldown;
        }
    }

    //------fluids------

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(Level level, BlockPos pos, BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(pos.relative(state.getValue(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            var f = SoftFluidRegistry.fromForgeFluid(fluidState.getType());
            if (f != null) { //just to be sure
                this.tempFluidHolder.fill(f);
                this.updateLight();
                return true;
            } else {
                int aa = 1;//error
            }
        }
        boolean r = this.tryExtract(level, pos, state, false) != 0;
        this.updateLight();
        return r;         //returns if it has a fluid
    }

    /**
     * @return 0 for fail, non 0 will be the transfer cooldown
     */
    private int tryExtract(Level level, BlockPos pos, BlockState state, boolean doTransfer) {
        Direction dir = state.getValue(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        this.tempFluidHolder.clear();
        if (backState.isAir()) return 0;

        FillAction fillAction = doTransfer ? this::tryFillingBlockBelow : null;
        for (var bi : BLOCK_INTERACTIONS) {
            var res = bi.tryDrain(level, this.tempFluidHolder, behind,
                    backState, fillAction);
            if (res == InteractionResult.PASS) continue;
            if (res == InteractionResult.SUCCESS) return bi.getTransferCooldown();
            else if (res == InteractionResult.CONSUME) break;
            else if (res == InteractionResult.FAIL) return 0;
        }
        if (!this.isConnectedBelow()) {
            for (var bi : ITEM_INTERACTIONS) {
                ItemStack removed = bi.tryExtractItem(level, behind, backState);
                if (!removed.isEmpty()) {
                    drop(level, pos, removed);
                    return COOLDOWN;
                }
            }
        }

        //soft fluid holders
        BlockEntity tileBack = level.getBlockEntity(behind);
        if (tileBack != null) {
            for (var bi : TILE_INTERACTIONS) {
                var res = bi.tryDrain(level, this.tempFluidHolder, behind,
                        tileBack, dir, fillAction);
                if (res == InteractionResult.PASS) continue;
                if (res == InteractionResult.SUCCESS) return bi.getTransferCooldown();
                else if (res == InteractionResult.CONSUME) break;
                else if (res == InteractionResult.FAIL) return 0;
            }

            if (!doTransfer) {
                return !this.tempFluidHolder.isEmpty() ? COOLDOWN : 0;
            }
            //pull other items from containers
            return this.spillItemsFromInventory(level, pos, dir, tileBack);
        } else {
            FluidState fluidState = level.getFluidState(behind);

            for (var bi : FLUID_INTERACTIONS) {
                var res = bi.tryDrain(level, this.tempFluidHolder, behind,
                        fluidState, fillAction);
                if (res == InteractionResult.PASS) continue;
                if (res == InteractionResult.SUCCESS) return bi.getTransferCooldown();
                else if (res == InteractionResult.CONSUME) break;
                else if (res == InteractionResult.FAIL) return 0;
            }
            if (!doTransfer) {
                return !this.tempFluidHolder.isEmpty() ? COOLDOWN : 0;
            }
        }
        return 0;
    }

    //sf->ff/sf
    @SuppressWarnings("ConstantConditions")
    private boolean tryFillingBlockBelow() {
        SoftFluid softFluid = this.tempFluidHolder.getFluid();
        if (softFluid.isEmpty()) return false;

        BlockPos below = this.worldPosition.below();
        BlockState belowState = level.getBlockState(below);

        for (var bi : TARGET_BLOCK_INTERACTIONS) {
            var res = bi.tryFill(level, this.tempFluidHolder, below, belowState);
            if (res == InteractionResult.PASS) continue;
            if (res == InteractionResult.SUCCESS) return true;
            else if (res == InteractionResult.CONSUME) break;
            else if (res == InteractionResult.FAIL) return false;
        }

        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow != null) {
            for (var bi : TARGET_TILE_INTERACTIONS) {
                var res = bi.tryFill(level, this.tempFluidHolder, below, tileBelow);
                if (res == InteractionResult.PASS) continue;
                if (res == InteractionResult.SUCCESS) return true;
                else if (res == InteractionResult.CONSUME) break;
                else if (res == InteractionResult.FAIL) return false;
            }
        }
        return false;
    }

    //------end-fluids------

    public boolean isOpen() {
        return (this.getBlockState().getValue(BlockStateProperties.POWERED) ^ this.getBlockState().getValue(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().getValue(FaucetBlock.HAS_WATER);
    }

    public boolean isConnectedBelow() {
        return this.getBlockState().getValue(FaucetBlock.HAS_JAR);
    }

    //------items------

    public int spillItemsFromInventory(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        //TODO: maybe add here insertion in containers below
        if (!this.isConnectedBelow()) {
            ItemStack removed = ItemsUtil.removeFirstStackFromInventory(level, pos, dir, tile);
            if (removed != null) {
                drop(level, pos, removed);
                return COOLDOWN;
            }
        }
        return 0;
    }

    private static void drop(Level level, BlockPos pos, ItemStack extracted) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
        drop.setDeltaMovement(new Vec3(0, 0, 0));
        level.addFreshEntity(drop);
        float f = (level.random.nextFloat() - 0.5f) / 4f;
        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.tempFluidHolder.load(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TransferCooldown", this.transferCooldown);
        this.tempFluidHolder.save(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @FunctionalInterface
    public interface FillAction {
        boolean tryExecute();
    }

    public static void registerInteraction(Object interaction) {
        boolean success = false;
        if (interaction instanceof IFaucetBlockSource bs) {
            BLOCK_INTERACTIONS.add(bs);
            success = true;
        }
        if (interaction instanceof IFaucetTileSource ts) {
            TILE_INTERACTIONS.add(ts);
            success = true;
        }
        if (interaction instanceof IFaucetFluidSource bs) {
            FLUID_INTERACTIONS.add(bs);
            success = true;
        }
        if (interaction instanceof IFaucetBlockTarget tb) {
            TARGET_BLOCK_INTERACTIONS.add(tb);
            success = true;
        }
        if (interaction instanceof IFaucetTileTarget tt) {
            TARGET_TILE_INTERACTIONS.add(tt);
            success = true;
        }
        if (interaction instanceof IFaucetItemSource is) {
            ITEM_INTERACTIONS.add(is);
            success = true;
        }
        if (!success)
            throw new UnsupportedOperationException("Unsupported faucet interaction class: " + interaction.getClass().getSimpleName());
    }

    public static <T> void removeDataInteractions(Collection<T> interactions) {
        for (var v : interactions) {
            if (v instanceof IFaucetBlockSource fs) {
                BLOCK_INTERACTIONS.remove(fs);
            } else if (v instanceof IFaucetItemSource fs) {
                ITEM_INTERACTIONS.remove(fs);
            }
        }
    }

}