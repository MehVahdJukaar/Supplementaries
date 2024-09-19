package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FlagBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class FlagBlockTile extends BlockEntity implements Nameable, IColored {

    //client side param
    public final float offset;
    @Nullable
    private Component name;
    private final DyeColor baseColor;
    private BannerPatternLayers patterns;

    public FlagBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, ((IColored) state.getBlock()).getColor());
    }

    public FlagBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(ModRegistry.FLAG_TILE.get(), pos, state);
        this.baseColor = color;
        this.offset = 3f * (Mth.sin(this.worldPosition.getX()) + Mth.sin(this.worldPosition.getZ()));
        this.patterns = BannerPatternLayers.EMPTY;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    public BannerPatternLayers getPatterns() {
        return this.patterns;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.patterns.equals(BannerPatternLayers.EMPTY)) {
            tag.put("patterns", BannerPatternLayers.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.patterns)
                    .getOrThrow());
        }
        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }
        if (tag.contains("patterns")) {
            BannerPatternLayers.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("patterns")).resultOrPartial((string) -> {
                Supplementaries.LOGGER.error("Failed to parse flag patterns: '{}'", string);
            }).ifPresent((bannerPatternLayers) -> {
                this.patterns = bannerPatternLayers;
            });
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @ForgeOverride
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

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return this.baseColor;
    }

}