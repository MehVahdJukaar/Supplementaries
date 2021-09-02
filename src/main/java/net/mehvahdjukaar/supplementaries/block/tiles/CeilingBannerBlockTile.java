package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;


public class CeilingBannerBlockTile extends TileEntity implements INameable {
    @Nullable
    private ITextComponent name;
    @Nullable
    private DyeColor baseColor = DyeColor.WHITE;
    @Nullable
    private ListNBT itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public CeilingBannerBlockTile() {
        super(ModRegistry.CEILING_BANNER_TILE.get());
    }

    public CeilingBannerBlockTile(DyeColor p_i47731_1_) {
        this();
        this.baseColor = p_i47731_1_;
    }


    @OnlyIn(Dist.CLIENT)
    public void fromItem(ItemStack p_195534_1_, DyeColor p_195534_2_) {
        this.itemPatterns = BannerTileEntity.getItemPatterns(p_195534_1_);
        this.baseColor = p_195534_2_;
        this.patterns = null;
        this.receivedData = true;
        this.name = p_195534_1_.hasCustomHoverName() ? p_195534_1_.getHoverName() : null;
    }

    public ITextComponent getName() {
        return this.name != null ? this.name : new TranslationTextComponent("block.minecraft.banner");
    }

    @Nullable
    public ITextComponent getCustomName() {
        return this.name;
    }

    public void setCustomName(ITextComponent p_213136_1_) {
        this.name = p_213136_1_;
    }

    public CompoundNBT save(CompoundNBT p_189515_1_) {
        super.save(p_189515_1_);
        if (this.itemPatterns != null) {
            p_189515_1_.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            p_189515_1_.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
        }

        return p_189515_1_;
    }

    public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
        super.load(p_230337_1_, p_230337_2_);
        if (p_230337_2_.contains("CustomName", 8)) {
            this.name = ITextComponent.Serializer.fromJson(p_230337_2_.getString("CustomName"));
        }

        if (this.hasLevel()) {
            this.baseColor = ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }

        this.itemPatterns = p_230337_2_.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }

    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = BannerTileEntity.createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
        }

        return this.patterns;
    }

    public ItemStack getItem(BlockState state) {
        ItemStack itemstack = new ItemStack(BannerBlock.byColor(this.getBaseColor(() -> state)));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }

        if (this.name != null) {
            itemstack.setHoverName(this.name);
        }

        return itemstack;
    }

    public DyeColor getBaseColor(Supplier<BlockState> p_195533_1_) {
        if (this.baseColor == null) {
            this.baseColor = ((AbstractBannerBlock)p_195533_1_.get().getBlock()).getColor();
        }

        return this.baseColor;
    }
}

