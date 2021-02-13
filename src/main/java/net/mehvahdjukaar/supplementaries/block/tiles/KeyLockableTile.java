package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.items.KeyItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.concurrent.atomic.AtomicReference;

public class KeyLockableTile extends TileEntity {

    public String password = null;

    public KeyLockableTile() {
        super(Registry.KEY_LOCKABLE_TILE.get());
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void clearOwner(){
        this.password=null;
    }

    public static boolean isKeyInInventory(PlayerEntity player, String key){
        AtomicReference<IItemHandler> _iitemhandlerref = new AtomicReference<>();
        player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(_iitemhandlerref::set);
        if (_iitemhandlerref.get() != null) {
            boolean hasKey = false;
            for (int _idx = 0; _idx < _iitemhandlerref.get().getSlots(); _idx++) {
                ItemStack stack = _iitemhandlerref.get().getStackInSlot(_idx);
                if(stack.getItem() instanceof KeyItem){
                    hasKey = true;
                    String s = stack.getDisplayName().getString();
                    if(s.equals(key))return true;
                }
            }
            if(hasKey){
                player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.incorrect_key"), true);
                return false;
            }
        }
        player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.locked"), true);
        return false;
    }



    public ActionResultType handleAction(PlayerEntity player, Hand hand){
        ItemStack stack = player.getHeldItem(hand);
        Item item = stack.getItem();
        if(player.isSneaking() && item instanceof KeyItem && (player.isCreative() ||
                stack.getDisplayName().getString().equals(this.password))) {
            this.clearOwner();
            player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.cleared"), true);
            this.world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, 1.5F);
            return ActionResultType.CONSUME;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if(compound.contains("Password"))
            this.password=compound.getString("Password");;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if(this.password!=null)
            compound.putString("Password",this.password);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }
}
