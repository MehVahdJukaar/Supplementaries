package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.block.IDynamicContainer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class PresentBlockTile extends OpeneableContainerBlockEntity implements IColored, IDynamicContainer {

    //"" means not packed. this is used for packed but can be opened by everybody
    public static final String PUBLIC_KEY = "@e";

    private String recipient = "";
    private String sender = "";
    private String description = "";

    public PresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PRESENT_TILE.get(), pos, state, 1);
    }

    @Override
    public boolean canHoldItems() {
        return this.isPacked();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @org.jetbrains.annotations.Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return LazyOptional.empty();
        return super.getCapability(capability, facing);
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return ((PresentBlock) this.getBlockState().getBlock()).getColor();
    }

    public static boolean isPacked(ItemStack stack) {
        CompoundTag com = stack.getTag();
        if (com != null) {
            CompoundTag nbt = com.getCompound("BlockEntityTag");
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

        if (!this.level.isClientSide && this.isPacked() != shouldPack) {
            if (shouldPack) {
                this.level.playSound(null, this.worldPosition,
                        ModRegistry.PRESENT_PACK_SOUND.get(), SoundSource.BLOCKS, 1,
                        level.random.nextFloat() * 0.1F + 0.95F);
                //this.level.playSound(null, this.worldPosition,
                //        SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 0.5F,
                //        level.random.nextFloat() * 0.1F + 1.2F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.5F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.PACKED, shouldPack), 3);
        }
    }

    @Override
    public boolean canOpen(Player player) {
        return this.recipient.isEmpty() || this.recipient.equalsIgnoreCase(PUBLIC_KEY) ||
                this.recipient.equalsIgnoreCase(player.getName().getString()) ||
                this.sender.equalsIgnoreCase(player.getName().getString());
    }

    public InteractionResult interact(ServerPlayer player, BlockPos pos) {
        if (this.isUnused()) {
            if (this.canOpen(player)) {
                NetworkHooks.openGui(player, this, pos);
                PiglinAi.angerNearbyPiglins(player, true);
            } else {
                player.displayClientMessage(new TranslatableComponent("message.supplementaries.present.info", this.recipient), true);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    ;

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("gui.supplementaries.present");
    }

    @Override
    protected void updateBlockState(BlockState state, boolean b) {

    }

    @Override
    protected void playOpenSound(BlockState state) {

    }

    @Override
    protected void playCloseSound(BlockState state) {

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.recipient = "";
        this.sender = "";
        this.description = "";
        if (tag.contains("Recipient")) this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender")) this.sender = tag.getString("Sender");
        if (tag.contains("Description")) this.description = tag.getString("Description");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.recipient.isEmpty()) tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty()) tag.putString("Sender", this.sender);
        if (!this.description.isEmpty()) tag.putString("Description", this.description);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new PresentContainerMenu(id, player, this, this.worldPosition);
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
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getPresentItem(ItemLike block) {
        CompoundTag compoundTag = new CompoundTag();
        this.saveAdditional(compoundTag);
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
    public Component getSenderMessage() {
        return getSenderMessage(this.sender);
    }

    @Nullable
    public static Component getSenderMessage(String sender) {
        if (sender.isEmpty()) return null;
        return new TranslatableComponent("message.supplementaries.present.from", sender);
    }

    @Nullable
    public Component getRecipientMessage() {
        return getRecipientMessage(this.recipient);
    }

    @Nullable
    public static Component getRecipientMessage(String recipient) {
        if (recipient.isEmpty()) return null;
        if (recipient.equalsIgnoreCase(PUBLIC_KEY)) {
            return new TranslatableComponent("message.supplementaries.present.public");
        } else {
            return new TranslatableComponent("message.supplementaries.present.to", recipient);
        }
    }
}
