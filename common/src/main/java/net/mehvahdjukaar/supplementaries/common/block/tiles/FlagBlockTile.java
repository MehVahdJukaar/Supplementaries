package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.datafixers.util.Pair;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class FlagBlockTile extends BlockEntity implements Nameable, IColored {

    //client side param
    public final float offset;
    @Nullable
    private Component name;
    private final DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    @Nullable
    private List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public FlagBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, ((IColored)state.getBlock()).getColor());
    }

    public FlagBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(ModRegistry.FLAG_TILE.get(), pos, state);
        this.baseColor = color;
        this.offset = 3f * (Mth.sin(this.worldPosition.getX()) + Mth.sin(this.worldPosition.getZ()));
    }

    public void setCustomName(Component p_213136_1_) {
        this.name = p_213136_1_;
    }

    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns() {
        if (this.patterns == null) {
            this.patterns = BannerBlockEntity.createPatterns(this.baseColor, this.itemPatterns);
        }
        return this.patterns;
    }

    public ItemStack getItem(BlockState state) {
        ItemStack itemstack = new ItemStack(FlagBlock.byColor(this.getColor()));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }
        if (this.name != null) {
            itemstack.setHoverName(this.name);
        }
        return itemstack;
    }

    @Override
    public DyeColor getColor() {
        return this.baseColor;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.itemPatterns != null) {
            compound.put("Patterns", this.itemPatterns);
        }
        if (this.name != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        if (compoundNBT.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundNBT.getString("CustomName"));
        }
        this.itemPatterns = compoundNBT.getList("Patterns", 10);
        this.patterns = null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {
        Direction dir = this.getDirection();
        return new AABB(0.25, 0, 0.25, 0.75, 1, 0.75).expandTowards(
                dir.getStepX() * 1.35f, 0, dir.getStepZ() * 1.35f).move(this.worldPosition);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(FlagBlock.FACING);
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : Component.translatable("block.supplementaries.flag_" + this.baseColor.getName());
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }
}