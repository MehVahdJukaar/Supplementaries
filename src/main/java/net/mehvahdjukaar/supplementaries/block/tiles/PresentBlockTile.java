package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PresentBlockTile extends ItemDisplayTile {

    private int numPlayersUsing;

    private String recipient = "";
    private String sender = "";
    private boolean packed = false;


    public PresentBlockTile() {
        super(ModRegistry.PRESENT_TILE.get());
    }



    public boolean isUnused() {
        return this.numPlayersUsing <= 0;
    }

    public static boolean isPacked(ItemStack stack) {
        CompoundNBT com = stack.getTag();
        if (com != null) {
            CompoundNBT nbt = com.getCompound("BlockEntityTag");
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
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.present");
    }

    @Override
    public void startOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
        }
    }

    @Override
    public void stopOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        if (tag.contains("Recipient"))
            this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender"))
            this.sender = tag.getString("Sender");
        this.packed = tag.getBoolean("Packed");
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        if (!this.recipient.isEmpty())
            tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty())
            tag.putString("Sender", this.sender);

        tag.putBoolean("Packed", this.packed);
        return tag;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
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
