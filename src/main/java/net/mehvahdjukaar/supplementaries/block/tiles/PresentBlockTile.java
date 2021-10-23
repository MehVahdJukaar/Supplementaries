package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class PresentBlockTile extends ItemDisplayTile {

    private int numPlayersUsing;

    private String recipient = "";
    private String sender = "";
    private boolean packed = false;


    public PresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PRESENT_TILE.get(), pos, state);
    }

    public boolean isUnused() {
        return this.numPlayersUsing <= 0;
    }

    public static boolean isPacked(ItemStack stack) {
        CompoundTag com = stack.getTag();
        if (com != null) {
            CompoundTag nbt = com.getCompound("BlockEntityTag");
            if (nbt != null) {
                return nbt.getBoolean("Packed");
            }
        }
        return false;
    }

    public boolean isPacked() {
        return this.packed;
    }

    public void unpack() {
        this.recipient = "";
        this.sender = "";
        this.packed = false;
        if (!this.level.isClientSide)
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.OPEN, true), 3);
    }

    public void pack(String recipient, String sender, boolean doPack) {
        this.recipient = recipient;
        this.sender = sender;
        this.packed = doPack;
        if (doPack && !this.level.isClientSide)
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.OPEN, false), 3);
    }

    public void pack(String recipient, String sender) {
        this.pack(recipient, sender, true);
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.present");
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Recipient"))
            this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender"))
            this.sender = tag.getString("Sender");
        this.packed = tag.getBoolean("Packed");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        if (!this.recipient.isEmpty())
            tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty())
            tag.putString("Sender", this.sender);

        tag.putBoolean("Packed", this.packed);
        return tag;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new PresentContainer(id, player, this, this.worldPosition);
    }

    public static boolean isAcceptableItem(ItemStack stack) {
        Item i = stack.getItem();
        return CommonUtil.isAllowedInShulker(stack) && !(i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof PresentBlock);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isAcceptableItem(stack);
    }

    @Override
    public boolean needsToUpdateClientWhenChanged() {
        return false;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }
}
