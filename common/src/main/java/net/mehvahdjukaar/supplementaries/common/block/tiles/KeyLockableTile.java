package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KeyLockableTile extends BlockEntity implements IKeyLockable {

    public static final MutableComponent KEY_LOCKABLE_TOOLTIP = Component.translatable("message.supplementaries.key.lockable")
            .withStyle(ChatFormatting.ITALIC)
            .withStyle(ChatFormatting.GRAY);


    private String password = null;

    public KeyLockableTile(BlockPos pos, BlockState state) {
        super(ModRegistry.KEY_LOCKABLE_TILE.get(), pos, state);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void clearPassword() {
        this.password = null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    //returns true if door has to open
    public boolean handleAction(Player player, InteractionHand handIn, ItemStack stack, String translName) {
        if (player.isSpectator()) return false;

        String keyPassword = IKeyLockable.getKeyPassword(stack);
        //clear ownership
        if (player.isSecondaryUseActive() && keyPassword != null) {
            if (tryClearingKey(player, stack)) return false;
        }
        //set key
        else if (this.password == null) {
            if (keyPassword != null) {
                this.setPassword(keyPassword);
                this.onKeyAssigned(level, worldPosition, player, keyPassword);
                return false;
            }
            return true;
        }
        //open
        return player.isCreative() || this.testIfHasCorrectKey(player, this.password, true, translName);
    }

    public boolean tryClearingKey(Player player, ItemStack stack) {
        if ((player.isCreative() || this.getKeyStatus(stack) == KeyStatus.CORRECT_KEY)) {
            this.clearPassword();
            this.onPasswordCleared(player, worldPosition);
            return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Password")) {
            this.password = tag.getString("Password");
        } else this.password = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.password != null) {
            tag.putString("Password", this.password);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
