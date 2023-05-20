package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.PendulumAnimation;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HangingSignTileExtension extends PendulumAnimation {

    @Nullable
    private ModBlockProperties.PostType leftAttachment = null;

    @Nullable
    private ModBlockProperties.PostType rightAttachment = null;

    private boolean canSwing = true;

    public HangingSignTileExtension() {
        super(ClientConfigs.Blocks.HANGING_SIGN_CONFIG, HangingSignTileExtension::getAxis);

    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
        if(!canSwing){
            angle = config.get().getMinAngle();
            angularVel = 0;
        }else{
            super.tick(level, pos, state);
        }
    }

    private static Vec3i getAxis(BlockState state) {
        return state.getValue(WallHangingSignBlock.FACING).getClockWise().getNormal();
    }


    public ModBlockProperties.PostType getRightAttachment() {
        return rightAttachment;
    }

    public ModBlockProperties.PostType getLeftAttachment() {
        return leftAttachment;
    }

    public void saveAdditional(CompoundTag tag) {
        if (leftAttachment != null) {
            tag.putByte("left_attachment", (byte) leftAttachment.ordinal());
        }
        if (rightAttachment != null) {
            tag.putByte("right_attachment", (byte) rightAttachment.ordinal());
        }
        if (!canSwing) {
            tag.putBoolean("can_swing", false);
        }
    }

    public void load(CompoundTag tag) {
        if (tag.contains("left_attachment")) {
            leftAttachment = ModBlockProperties.PostType.values()[tag.getByte("left_attachment")];
        }
        if (tag.contains("right_attachment")) {
            rightAttachment = ModBlockProperties.PostType.values()[tag.getByte("right_attachment")];
        }
        if (tag.contains("can_swing")) {
            canSwing = tag.getBoolean("can_swing");
        }
    }


    public void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                            BlockPos pos, BlockPos neighborPos) {
        Direction selfFacing = state.getValue(WallHangingSignBlock.FACING);
        if (direction == selfFacing.getClockWise()) {
            rightAttachment = ModBlockProperties.PostType.get(neighborState, true);
            ((Level) level).sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        } else if (direction == selfFacing.getCounterClockWise()) {
            leftAttachment = ModBlockProperties.PostType.get(neighborState, true);
            ((Level) level).sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        } else if (direction == Direction.DOWN) {
            canSwing = !IRopeConnection.canConnectDown(neighborState);
        }
    }

    public void updateAttachments(Level level, BlockPos pos, BlockState state) {
        Direction selfFacing = state.getValue(WallHangingSignBlock.FACING);

        rightAttachment = ModBlockProperties.PostType.get(level.getBlockState(pos.relative(selfFacing.getClockWise())), true);
        leftAttachment = ModBlockProperties.PostType.get(level.getBlockState(pos.relative(selfFacing.getCounterClockWise())), true);
        BlockState below = level.getBlockState(pos.below());
        canSwing = !IRopeConnection.canConnectDown(below);

    }

    public boolean canSwing() {
        return canSwing;
    }
}
