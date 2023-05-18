package net.mehvahdjukaar.supplementaries.mixins;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.IHangingSignExtension;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingSignBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HangingSignBlockEntity.class)
public abstract class HangingSignBlockEntityMixin extends BlockEntity implements IHangingSignExtension {

    protected HangingSignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Nullable
    private PostType leftAttachment = null;
    @Nullable
    private PostType rightAttachment = null;

    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {

        return new AABB(worldPosition).inflate(0.5);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (leftAttachment != null) {
            tag.putByte("left_attachment", (byte) leftAttachment.ordinal());
        }
        if (rightAttachment != null) {
            tag.putByte("right_attachment", (byte) rightAttachment.ordinal());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("left_attachment")) {
            leftAttachment = PostType.values()[tag.getByte("left_attachment")];
        }
        if (tag.contains("right_attachment")) {
            rightAttachment = PostType.values()[tag.getByte("right_attachment")];
        }
    }

    @Override
    public PostType getLeftAttachment() {
        return leftAttachment;
    }

    @Override
    public PostType getRightAttachment() {
        return rightAttachment;
    }

    @Override
    public void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                            BlockPos currentPos, BlockPos neighborPos) {
        Direction selfFacing = state.getValue(WallHangingSignBlock.FACING);
        if (direction == selfFacing.getClockWise()) {
            rightAttachment = PostType.get(neighborState, true);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        } else if (direction == selfFacing.getCounterClockWise()) {
            leftAttachment = PostType.get(neighborState,true);
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void updateAttachments() {
        Direction selfFacing = this.getBlockState().getValue(WallHangingSignBlock.FACING);

        rightAttachment = PostType.get(level.getBlockState(this.getBlockPos().relative(selfFacing.getClockWise())), true);
        leftAttachment = PostType.get(level.getBlockState(this.getBlockPos().relative(selfFacing.getCounterClockWise())), true);
    }
}
