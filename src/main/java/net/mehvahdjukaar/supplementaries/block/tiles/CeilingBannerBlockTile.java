package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Nameable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;


public class CeilingBannerBlockTile extends BlockEntity implements Nameable {
    @Nullable
    private Component name;
    @Nullable
    private DyeColor baseColor = DyeColor.WHITE;
    @Nullable
    private ListTag itemPatterns;
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
        this.itemPatterns = BannerBlockEntity.getItemPatterns(p_195534_1_);
        this.baseColor = p_195534_2_;
        this.patterns = null;
        this.receivedData = true;
        this.name = p_195534_1_.hasCustomHoverName() ? p_195534_1_.getHoverName() : null;
    }

    public Component getName() {
        return this.name != null ? this.name : new TranslatableComponent("block.minecraft.banner");
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component p_213136_1_) {
        this.name = p_213136_1_;
    }

    public CompoundTag save(CompoundTag p_189515_1_) {
        super.save(p_189515_1_);
        if (this.itemPatterns != null) {
            p_189515_1_.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            p_189515_1_.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        return p_189515_1_;
    }

    public void load(BlockState p_230337_1_, CompoundTag p_230337_2_) {
        super.load(p_230337_1_, p_230337_2_);
        if (p_230337_2_.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(p_230337_2_.getString("CustomName"));
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = BannerBlockEntity.createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
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

