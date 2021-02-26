package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;


public class BambooSpikesBlockTile extends TileEntity {
    //private List<EffectInstance> effects = new ArrayList<>();
    public Potion potion = Potions.EMPTY;
    public int charges = 0;
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

    public boolean isOnCooldown(World world){
        return world.getGameTime()-this.lastTicked<20;
    }

    public boolean consumeCharge(World world){
        this.lastTicked = world.getGameTime();
        this.charges-=1;
        this.markDirty();
        if(this.charges<=0){
            this.charges=0;
            this.potion=Potions.EMPTY;
            return true;
        }
        return false;
    }

    public void setMissingCharges(int missing){
        this.charges = Math.max(MAX_CHARGES-missing,0);
    }

    public boolean tryApplyPotion(ItemStack stack){
        Potion p = PotionUtils.getPotionFromItem(stack);
        if(this.charges==0||this.potion==Potions.EMPTY||(this.potion.equals(p)&&this.charges!=MAX_CHARGES)) {
            this.potion = p;
            this.charges = MAX_CHARGES;
            this.markDirty();
            //needed for buggy white tipped state. aparently not enough
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(),this.getBlockState(),3);
            return true;
        }
        return false;
    }

    //returns true if BlockState needs to be changed
    //will be called from moving block so it needs a world
    public boolean interactWithEntity(LivingEntity le, @Nonnull World world){
        if(this.hasPotion() && !this.isOnCooldown(world)) {
            boolean used = false;
            for(EffectInstance effect : this.potion.getEffects()){
                if(!le.isPotionApplicable(effect))continue;
                if(le.isPotionActive(effect.getPotion()))continue;

                if (effect.getPotion().isInstant()) {
                    float health = 0.5f;//no idea of what this does. it's either 0.5 or 1
                    effect.getPotion().affectEntity(null, null, le, effect.getAmplifier(), health);
                } else {
                    le.addPotionEffect( new EffectInstance(effect.getPotion(),
                            (int)(effect.getDuration()*BambooSpikesBlockTile.POTION_MULTIPLIER),
                            effect.getAmplifier()));
                }
                used=true;
            }
            if(used){
                return this.consumeCharge(world);
            }
        }
        return false;
    }

    public ItemStack getSpikeItem(){
        if(this.hasPotion()) {
            ItemStack stack = BambooSpikesTippedItem.makeSpikeItem(this.potion);
            stack.setDamage(stack.getMaxDamage() - this.charges);
            return stack;
        }
        return new ItemStack(Registry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("Charges",this.charges);
        compound.putLong("LastTicked",this.lastTicked);

        ResourceLocation resourcelocation = net.minecraft.util.registry.Registry.POTION.getKey(this.potion);
        compound.putString("Potion", resourcelocation.toString());

        return super.write(compound);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        this.charges = compound.getInt("Charges");
        this.lastTicked = compound.getLong("LastTicked");
        this.potion = PotionUtils.getPotionTypeFromNBT(compound);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }
    //TODO:Make base tile
    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }


}
