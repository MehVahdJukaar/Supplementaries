package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.utils.Textures;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DoubleSkullBlockTile extends EnhancedSkullBlockTile {

    @Nullable
    protected SkullBlockEntity innerTileUp = null;

    private int waxColorInd = -1;
    private ResourceLocation waxTexture = null;

    public DoubleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_PILE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveInnerTile("SkullUp", this.innerTileUp, tag);

        if (waxColorInd != -1) {
            tag.putInt("WaxColor", waxColorInd);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.innerTileUp = this.loadInnerTile("SkullUp", this.innerTileUp, tag);

        if (tag.contains("WaxColor")) {
            this.waxColorInd = tag.getInt("WaxColor");
            DyeColor d = waxColorInd == 17 ? null : DyeColor.byId(waxColorInd);
            this.waxTexture = Textures.SKULL_CANDLES_TEXTURES.get(d);
        } else {
            waxTexture = null;
        }
    }

    public ItemStack getSkullItemUp() {
        if (this.innerTileUp != null) {
            return new ItemStack(innerTileUp.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }

    public void rotateUp(Rotation rotation) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    rotation.rotate(r, 16)));
        }
    }

    public void rotateUpStep(int step) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    ((r - step) + 16) % 16));
        }
    }

    @Override
    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack skullStack, Player player, InteractionHand hand) {
        super.initialize(oldTile, skullBlock, skullStack, player, hand);
        if (skullStack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof SkullBlock upSkull) {
                var context = new BlockPlaceContext(player, hand, skullStack,
                        new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, this.getBlockPos(), false));
                BlockState state = upSkull.getStateForPlacement(context);
                if (state == null) {
                    state = upSkull.defaultBlockState();
                }
                BlockEntity entity = upSkull.newBlockEntity(this.getBlockPos(), state);
                if (entity instanceof SkullBlockEntity blockEntity) {
                    this.innerTileUp = blockEntity;
                }
            }
        }
    }

    public void updateWax(BlockState above) {
        int index = -1;
        DyeColor c = null;
        if (above.getBlock() instanceof CandleBlock block) {
            c = CandleSkullBlockTile.colorFromCandle(block);
            if (c == null) index = 17;
            else index = c.getId();
        }
        if (this.waxColorInd != index) {
            this.waxColorInd = index;
            if (this.level instanceof ServerLevel) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
            } else {
                this.waxTexture = waxColorInd == -1 ? null : Textures.SKULL_CANDLES_TEXTURES.get(c);
            }
        }
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Nullable
    public BlockState getSkullUp() {
        if (this.innerTileUp != null) {
            return this.innerTileUp.getBlockState();
        }
        return null;
    }

    @Nullable
    public BlockEntity getSkullTileUp() {
        return this.innerTileUp;
    }
}
