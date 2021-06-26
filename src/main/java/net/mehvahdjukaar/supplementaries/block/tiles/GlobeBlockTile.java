package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.common.SpecialPlayers;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static net.mehvahdjukaar.supplementaries.common.Textures.*;

public class GlobeBlockTile extends TileEntity implements ITickableTileEntity, INameable {
    public float yaw = 0;
    public float prevYaw = 0;
    public int face = 0;

    private ITextComponent customName;

    public boolean sheared = false;

    //client
    public ResourceLocation texture = null;
    public boolean isFlat = false;

    public GlobeBlockTile() {
        super(Registry.GLOBE_TILE.get());
    }


    public void setCustomName(ITextComponent name) {
        this.customName = name;
        this.updateTexture();
    }

    private void updateTexture(){
        if(this.hasCustomName()) {
            this.isFlat = false;
            this.texture = GlobeType.getGlobeTexture(this.getCustomName().getString(),this);
        }else this.texture = null;
    }

    @Override
    public ITextComponent getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public ITextComponent getCustomName() {
        return this.customName;
    }

    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.globe");
    }

    @Override
    public double getViewDistance() {
        return 80;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        if (compound.contains("CustomName", 8)) {
            this.setCustomName(ITextComponent.Serializer.fromJson(compound.getString("CustomName")));
        }
        this.face = compound.getInt("Face");
        this.yaw = compound.getFloat("Yaw");
        this.sheared = compound.getBoolean("Sheared");
        super.load(state, compound);

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        compound.putInt("Face",this.face);
        compound.putFloat("Yaw",this.yaw);
        compound.putBoolean("Sheared",this.sheared);
        return compound;
    }

    public void spin(){
        int spin = 360;
        int inc = 90;
        this.face=(this.face-=inc)%360;
        this.yaw=(this.yaw+spin+inc);
        this.prevYaw=(this.prevYaw+spin+inc);
        this.setChanged();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public void tick() {
        this.prevYaw=this.yaw;
        if(this.yaw!=0){
            if(this.yaw<0){
                this.yaw=0;
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
            else {
                this.yaw = (this.yaw * 0.94f) - 0.7f;
            }
        }

    }

    public Direction getDirection(){
        return this.getBlockState().getValue(GlobeBlock.FACING);
    }

    //TODO: improve
    public enum GlobeType {
        FLAT(new String[]{"flat","flat earth"}, new TranslationTextComponent("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon","luna","selene","cynthia"},
                new TranslationTextComponent("globe.supplementaries.moon"),GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth","terra","gaia","gaea","tierra","tellus","terre"},
                new TranslationTextComponent("globe.supplementaries.earth"),GLOBE_TEXTURE),
        SUN(new String[]{"sun","sol","helios"},
                new TranslationTextComponent("globe.supplementaries.sun"),GLOBE_SUN_TEXTURE);

        GlobeType(String[] key, TranslationTextComponent tr, ResourceLocation res){
            this.keyWords = key;
            this.transKeyWord = tr;
            this.texture = res;
        }

        public final String[] keyWords;
        public final TranslationTextComponent transKeyWord;
        public final ResourceLocation texture;

        public static ResourceLocation getGlobeTexture(String text, GlobeBlockTile tile){
            String name = text.toLowerCase();
            ResourceLocation r = SpecialPlayers.GLOBES.get(name);
            if(r != null)return r;
            for (GlobeType n : GlobeType.values()) {
                if(n.keyWords==null)continue;
                if(n.transKeyWord!=null && !n.transKeyWord.getString().equals("") && name.equals(n.transKeyWord.getString().toLowerCase())){
                    tile.isFlat = (n==FLAT);
                    return n.texture;
                }
                for (String s : n.keyWords) {
                    if (!s.equals("") && name.equals(s)) {
                        tile.isFlat = (n==FLAT);
                        return n.texture;
                    }
                }
            }
            return null;
        }
        
    }

}