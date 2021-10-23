package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class FlagBlockTile extends BlockEntity implements Nameable {

    //client side param
    public final float offset = 3f * (Mth.sin(this.worldPosition.getX()) + Mth.sin(this.worldPosition.getZ()));
    @Nullable
    private Component name;
    @Nullable
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public FlagBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, null);
    }

    public FlagBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(ModRegistry.FLAG_TILE.get(), pos, state);
        this.baseColor = color;
    }

    public static ResourceLocation getFlagLocation(BannerPattern pattern) {
        return new ResourceLocation(Supplementaries.MOD_ID, "textures/entity/flags/" + pattern.getFilename() + ".png");
    }

    public void setCustomName(Component p_213136_1_) {
        this.name = p_213136_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public void fromItem(ItemStack stack, DyeColor color) {
        this.itemPatterns = BannerBlockEntity.getItemPatterns(stack);
        this.baseColor = color;
        this.patterns = null;
        this.receivedData = true;
    }

    @OnlyIn(Dist.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = BannerBlockEntity.createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
        }
        return this.patterns;
    }


    public ItemStack getItem(BlockState state) {
        ItemStack itemstack = new ItemStack(FlagBlock.byColor(this.getBaseColor(() -> state)));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }
        if (this.name != null) {
            itemstack.setHoverName(this.name);
        }
        return itemstack;
    }

    public DyeColor getBaseColor(Supplier<BlockState> state) {
        if (this.baseColor == null) {
            this.baseColor = ((FlagBlock) state.get().getBlock()).getColor();
        }
        return this.baseColor;
    }

    @Override
    public CompoundTag save(CompoundTag compoundNBT) {
        super.save(compoundNBT);
        if (this.itemPatterns != null) {
            compoundNBT.put("Patterns", this.itemPatterns);
        }
        if (this.name != null) {
            compoundNBT.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundNBT;
    }

    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        if (compoundNBT.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundNBT.getString("CustomName"));
        }
        if (this.hasLevel()) {
            this.baseColor = ((FlagBlock) this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }
        this.itemPatterns = compoundNBT.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
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
        return this.name != null ? this.name : new TranslatableComponent("block.supplementaries.flag_" + this.getBaseColor(this::getBlockState).getName());
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }
}