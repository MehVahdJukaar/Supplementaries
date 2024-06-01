package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.present.IPresentItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TrappedPresentBlockTile extends AbstractPresentBlockTile {

    public TrappedPresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TRAPPED_PRESENT_TILE.get(), pos, state);
    }

    @Override
    public boolean canHoldItems() {
        return this.isPrimed();
    }

    public static boolean isPrimed(ItemStack stack) {
        CompoundTag com = stack.getTag();
        if (com != null) {
            CompoundTag tag = com.getCompound("BlockEntityTag");
            return tag.contains("Items");
        }
        return false;
    }

    public boolean isPrimed() {
        return this.getBlockState().getValue(TrappedPresentBlock.PACKED);
    }

    public void updateState(boolean primed) {
        if (!this.level.isClientSide && this.isPrimed() != primed) {
            if (primed) {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_PACK.get(), SoundSource.BLOCKS, 1,
                        level.random.nextFloat() * 0.1F + 0.95F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_BREAK.get(), SoundSource.BLOCKS, 0.75F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(TrappedPresentBlock.PACKED, primed), 3);
        }
    }

    @Override
    public boolean canOpen(Player player) {
        if(!super.canOpen(player))return false;
        if(!this.isUnused())return false;
        return !this.isPrimed();
    }

    @Override
    public InteractionResult interact(Level  level, BlockPos pos, BlockState state, Player player) {
        if(state.getValue(TrappedPresentBlock.ON_COOLDOWN)){
            return InteractionResult.FAIL;
        }
        if (this.isUnused()) {
            if (this.canOpen(player)) {
                if(player instanceof ServerPlayer serverPlayer) {
                    PlatHelper.openCustomMenu(serverPlayer, this, pos);
                    PiglinAi.angerNearbyPiglins(player, true);
                }
            } else {
                //boom!
                level.setBlockAndUpdate(pos, state.setValue(TrappedPresentBlock.ON_COOLDOWN, true));
                level.scheduleTick(pos, state.getBlock(), 10);
                if(level instanceof ServerLevel sl) {
                    detonate(sl, pos);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);

        }
        return InteractionResult.PASS;
    }

    public void detonate(ServerLevel level, BlockPos pos) {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(level, pos);
        ItemStack stack = this.getItem(0);
        IPresentItemBehavior presentItemBehavior = TrappedPresentBlock.getPresentBehavior(stack);
        this.updateState(false);
        presentItemBehavior.trigger(blocksourceimpl, stack);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("gui.supplementaries.trapped_present");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        if (inv.player.isSpectator()) return null;
        return new TrappedPresentContainerMenu(id, inv, this);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
