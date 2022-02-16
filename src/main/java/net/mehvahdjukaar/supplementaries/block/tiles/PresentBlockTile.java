package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.block.util.IColored;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.items.PresentItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class PresentBlockTile extends ItemDisplayTile implements IColored {

    //"" means not packed. this is used for packed but can be opened by everybody
    public static final String PUBLIC_KEY = "@e";

    private String recipient = "";
    private String sender = "";
    private String description = "";

    public PresentBlockTile() {
        super(ModRegistry.PRESENT_TILE.get(),1);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public @Nullable DyeColor getColor() {
        return ((PresentBlock)this.getBlockState().getBlock()).getColor();
    }

    public static boolean isPacked(ItemStack stack) {
        CompoundNBT com = stack.getTag();
        if (com != null) {
            CompoundNBT nbt = com.getCompound("BlockEntityTag");
            return nbt.getBoolean("Packed");
        }
        return false;
    }

    public boolean isPacked() {
        return this.getBlockState().getValue(PresentBlock.PACKED);
    }

    public String getSender() {
        return sender;
    }

    public String getDescription() {
        return description;
    }

    public String getRecipient() {
        if (this.recipient.equalsIgnoreCase(PUBLIC_KEY)) return "";
        return recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setPublic() {
        this.setRecipient(PUBLIC_KEY);
    }

    public void updateState(boolean shouldPack, String newRecipient, String sender, String description) {
        if (shouldPack) {
            if (newRecipient.isEmpty()) newRecipient = PUBLIC_KEY;
            this.recipient = newRecipient;
            this.sender = sender;
            this.description = description;
        } else {
            this.recipient = "";
            this.sender = "";
            this.description = "";
        }

        if (!this.level.isClientSide && this.isPacked() != shouldPack)
            if (shouldPack) {
               // this.level.playSound(null, this.worldPosition,
                //        ModRegistry.PRESENT_PACK_SOUND.get(), So.BLOCKS, 1,
               //         level.random.nextFloat() * 0.1F + 0.95F);
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 0.5F,
                        level.random.nextFloat() * 0.1F + 1.2F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.WOOL_BREAK, SoundCategory.BLOCKS, 0.5F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.PACKED, shouldPack), 3);
    }

    public boolean canOpen(PlayerEntity player) {
        return this.recipient.isEmpty() || this.recipient.equalsIgnoreCase(PUBLIC_KEY) ||
                this.recipient.equalsIgnoreCase(player.getName().getString()) ||
                this.sender.equalsIgnoreCase(player.getName().getString());
    }

    public ActionResultType interact(ServerPlayerEntity player, BlockPos pos) {

        if (this.canOpen(player)) {
            NetworkHooks.openGui(player, this, pos);
            PiglinTasks.angerNearbyPiglins(player, true);
        } else {
            player.displayClientMessage(new TranslationTextComponent("message.supplementaries.present.info", this.recipient), true);
        }
        return ActionResultType.CONSUME;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("gui.supplementaries.present");
    }


    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.recipient = "";
        this.sender = "";
        this.description = "";
        if (tag.contains("Recipient")) this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender")) this.sender = tag.getString("Sender");
        if (tag.contains("Description")) this.description = tag.getString("Description");
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        if (!this.recipient.isEmpty()) tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty()) tag.putString("Sender", this.sender);
        if (!this.description.isEmpty()) tag.putString("Description", this.description);
        return tag;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new PresentContainer(id, player, this, this.worldPosition);
    }

    public static boolean isAcceptableItem(ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack) && !(stack.getItem() instanceof PresentItem);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isAcceptableItem(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, @Nullable Direction p_19237_) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, ItemStack p_19240_, Direction p_19241_) {
        return false;
    }

    //sync stuff to client

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 9, this.getUpdateTag());
    }



    public ItemStack getPresentItem(IItemProvider block) {
        CompoundNBT compoundTag = new CompoundNBT();
        this.save(compoundTag);
        ItemStack itemstack = new ItemStack(block);
        if (!compoundTag.isEmpty()) {
            itemstack.addTagElement("BlockEntityTag", compoundTag);
        }

        if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
        }
        return itemstack;
    }

    @Nullable
    public ITextComponent getSenderMessage() {
        return getSenderMessage(this.sender);
    }

    @Nullable
    public static ITextComponent getSenderMessage(String sender) {
        if (sender.isEmpty()) return null;
        return new TranslationTextComponent("message.supplementaries.present.from", sender);
    }

    @Nullable
    public ITextComponent getRecipientMessage() {
        return getRecipientMessage(this.recipient);
    }

    @Nullable
    public static ITextComponent getRecipientMessage(String recipient) {
        if (recipient.isEmpty()) return null;
        if (recipient.equalsIgnoreCase(PUBLIC_KEY)) {
            return new TranslationTextComponent("message.supplementaries.present.public");
        } else {
            return new TranslationTextComponent("message.supplementaries.present.to", recipient);
        }
    }
}