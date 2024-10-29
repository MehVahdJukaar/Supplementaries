package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.PresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.components.PresentAddress;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class PresentBlockTile extends AbstractPresentBlockTile {

    //"" means not packed. this is used for packed but can be opened by everybody
    public static final String PUBLIC_KEY = "@e";

    private String recipient = "";
    private String sender = "";
    private String description = "";
    //TODO: filtering here

    public PresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PRESENT_TILE.get(), pos, state);
    }

    @Override
    public boolean canHoldItems() {
        return this.isPacked();
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
        this.sender = sender.trim();
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient.trim();
    }

    public void setPublic() {
        this.setRecipient(PUBLIC_KEY);
    }

    public void updateState(boolean shouldPack, String newRecipient, String sender, String description, Player playerWhoChanged) {
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
                        ModSounds.PRESENT_PACK.get(), SoundSource.BLOCKS, 1,
                        level.random.nextFloat() * 0.1F + 0.95F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_OPEN.get(), SoundSource.BLOCKS, 1F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
            level.gameEvent(playerWhoChanged, GameEvent.BLOCK_CHANGE, worldPosition);
            this.level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(PresentBlock.PACKED, shouldPack), 3);
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return super.canPlaceItem(index, stack) && !isPacked();
    }

    @Override
    public boolean canOpen(Player player) {
        if (!super.canOpen(player)) return false;
        if (!this.isUnused()) return false;
        if (player.isCreative()) return true;
        return this.recipient.isEmpty() || this.recipient.equalsIgnoreCase(PUBLIC_KEY) ||
                this.recipient.equalsIgnoreCase(player.getName().getString()) ||
                this.sender.equalsIgnoreCase(player.getName().getString());
    }

    @Override
    public InteractionResult interact(Level level, BlockPos pos, BlockState state, Player player) {
        if (this.isUnused()) {
            if (this.canOpen(player)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    //we open directly as its a container and can open contains this logic
                    PlatHelper.openCustomMenu(serverPlayer, this, pos);
                    PiglinAi.angerNearbyPiglins(player, true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            player.displayClientMessage(Component.translatable("message.supplementaries.present.info", this.recipient), true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("gui.supplementaries.present");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.recipient = "";
        this.sender = "";
        this.description = "";
        if (tag.contains("Recipient")) this.recipient = tag.getString("Recipient");
        if (tag.contains("Sender")) this.sender = tag.getString("Sender");
        if (tag.contains("Description")) this.description = tag.getString("Description");
        if(this.level != null && !this.level.isClientSide){
            boolean empty = this.getItem(0).isEmpty();
            this.level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(PresentBlock.PACKED, !empty), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.recipient.isEmpty()) tag.putString("Recipient", this.recipient);
        if (!this.sender.isEmpty()) tag.putString("Sender", this.sender);
        if (!this.description.isEmpty()) tag.putString("Description", this.description);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        PresentAddress address = PresentAddress.of(this.recipient, this.sender, this.description);
        if (address != null) {
            components.set(ModComponents.ADDRESS.get(), address);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        PresentAddress address = componentInput.get(ModComponents.ADDRESS.get());
        if (address != null) {
            this.recipient = address.recipient();
            this.sender = address.sender();
            this.description = address.description();
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Recipient");
        tag.remove("Sender");
        tag.remove("Description");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new PresentContainerMenu(id, player, this);
    }


}
