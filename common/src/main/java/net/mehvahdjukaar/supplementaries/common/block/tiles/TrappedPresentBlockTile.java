package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.IDynamicContainer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.common.block.IPresentItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class TrappedPresentBlockTile extends AbstractPresentBlockTile {

    private long lastActivated = 0;

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
        return !this.isPrimed();
    }

    @Override
    public InteractionResult interact(ServerPlayer player, BlockPos pos) {
        long time = player.level.getGameTime();
        if (this.isUnused() &&
                Mth.abs((float)time - lastActivated) > 10) {
            if (this.canOpen(player)) {
                PlatformHelper.openCustomMenu(player, this, pos);
                PiglinAi.angerNearbyPiglins(player, true);
            } else {
                detonate(player.getLevel(), pos);
                this.lastActivated = time;
            }
            return InteractionResult.CONSUME;
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
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new TrappedPresentContainerMenu(id, player, this, this.worldPosition);
    }

}
