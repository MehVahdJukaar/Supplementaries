package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.GlobeBlock;
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
    public GlobeType type = GlobeType.DEFAULT;
    private ITextComponent customName;

    public GlobeBlockTile() {
        super(Registry.GLOBE_TILE.get());
    }


    public void setCustomName(ITextComponent name) {
        this.customName = name;
        this.type = GlobeType.getGlobeType(name.toString());
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
    public double getMaxRenderDistanceSquared() {
        return 80;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
        }
        this.face = compound.getInt("Face");
        this.yaw = compound.getFloat("Yaw");
        this.type = GlobeType.values()[compound.getInt("GlobeType")];
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        compound.putInt("Face",this.face);
        compound.putFloat("Yaw",this.yaw);
        compound.putInt("GlobeType", this.type.ordinal());
        return compound;
    }

    public void spin(){
        int spin = 360;
        int inc = 90;
        this.face=(this.face-=inc)%360;
        this.yaw=(this.yaw+spin+inc);
        this.prevYaw=(this.prevYaw+spin+inc);
        this.markDirty();
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
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

    @Override
    public void tick() {
        this.prevYaw=this.yaw;
        if(this.yaw!=0){
            if(this.yaw<0){
                this.yaw=0;
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockState().getBlock());
            }
            else {
                this.yaw = (this.yaw * 0.94f) - 0.7f;
            }
        }

    }

    public Direction getDirection(){
        return this.getBlockState().get(GlobeBlock.FACING);
    }


    public enum GlobeType {
        DEFAULT(null, null, GLOBE_TEXTURE),
        FLAT(new String[]{"flat"}, new TranslationTextComponent("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon","luna","selene","cynthia"},
                new TranslationTextComponent("globe.supplementaries.moon"),GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth","terra","gaia","gaea","tierra","tellus","terre"},
                new TranslationTextComponent("globe.supplementaries.earth"),GLOBE_TEXTURE),
        SHEARED(null,null,GLOBE_SHEARED_TEXTURE),
        CUSTOM_1(new String[]{"plantkillable"}, null, GLOBE_CUSTOM_1),
        CUSTOM_2(new String[]{"toffanelly"}, null, GLOBE_CUSTOM_2),
        CUSTOM_3(new String[]{"sylvetichearts"}, null, GLOBE_CUSTOM_3);


        GlobeType(String[] key, TranslationTextComponent tr, ResourceLocation res){
            this.keyWords = key;
            this.transKeyWord = tr;
            this.texture = res;
        }

        public final String[] keyWords;
        public final TranslationTextComponent transKeyWord;
        public final ResourceLocation texture;

        public static GlobeType getGlobeType(String text){
            String name = text.toLowerCase();
            for (GlobeType n : GlobeType.values()) {
                if(n.keyWords==null)continue;
                if(n.transKeyWord!=null && !n.transKeyWord.getString().equals("") && name.contains(n.transKeyWord.getString().toLowerCase()))return n;
                for (String s : n.keyWords) {
                    if (!s.equals("") && name.contains(s)) {
                        return n;
                    }
                }
            }
            return GlobeType.DEFAULT;
        }

        public static GlobeType getGlobeType(TileEntity t){
            if(t instanceof INameable && ((INameable) t).hasCustomName()) {
                return getGlobeType(((INameable) t).getCustomName().toString());
            }
            return GlobeType.DEFAULT;
        }
    }

}