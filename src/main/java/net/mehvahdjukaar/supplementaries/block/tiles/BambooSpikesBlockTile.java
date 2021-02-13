package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class BambooSpikesBlockTile extends TileEntity {
    //private List<EffectInstance> effects = new ArrayList<>();
    public Potion potion = Potions.EMPTY;
    public int charges = MAX_CHARGES;
    public long lastTicked = 0;
    //put these two in config
    public static final float POTION_MULTIPLIER = 0.1f;
    public static final int MAX_CHARGES = 16;

    public BambooSpikesBlockTile() {
        super(Registry.BAMBOO_SPIKES_TILE.get());
    }

    public int getColor(){
        if(this.hasPotion())
            return PotionUtils.getPotionColor(potion);
        return 0xffffff;
    }

    public boolean hasPotion(){
        return this.potion!=Potions.EMPTY && this.charges!=0;
    }

    public boolean isOnCooldown(){
        return this.world.getGameTime()-this.lastTicked<20;
    }

    public void consumeCharge(){
        this.lastTicked = this.world.getGameTime();
        this.charges-=1;
        this.markDirty();
        if(this.charges==0){
            this.charges=0;
            this.world.setBlockState(this.pos,this.getBlockState().with(BambooSpikesBlock.TIPPED,false),3);
        }
    }

    public void setMissingCharges(int missing){
        this.charges = Math.max(MAX_CHARGES-missing,0);
    }

    public boolean tryApplyPotion(ItemStack stack){
        Potion p = PotionUtils.getPotionFromItem(stack);
        if(this.potion==Potions.EMPTY||(this.potion.equals(p)&&this.charges!=MAX_CHARGES)) {
            this.potion = p;
            this.charges = MAX_CHARGES;
            this.markDirty();
            //this.world.notifyBlockUpdate(this.pos, this.getBlockState(),this.getBlockState(),3);
            return true;
        }
        return false;
    }

    public ItemStack getSpikeItem(){

        if(this.hasPotion()) {
            ItemStack stack = new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
            CompoundNBT com = new CompoundNBT();
            ResourceLocation resourcelocation = net.minecraft.util.registry.Registry.POTION.getKey(this.potion);
            com.putString("Potion", resourcelocation.toString());
            stack.setTagInfo("BlockEntityTag",com);

            stack.setDamage(stack.getMaxDamage() - this.charges);
            return stack;
        }
        return new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("Charges",this.charges);

        ResourceLocation resourcelocation = net.minecraft.util.registry.Registry.POTION.getKey(this.potion);
        compound.putString("Potion", resourcelocation.toString());

        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.charges = compound.getInt("Charges");
        this.potion = PotionUtils.getPotionTypeFromNBT(compound);
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
