package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.PendulumAnimation;
import net.mehvahdjukaar.supplementaries.common.block.SwingAnimation;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HangingSignTileExtension {

    @Nullable
    private ModBlockProperties.PostType leftAttachment = null;

    @Nullable
    private ModBlockProperties.PostType rightAttachment = null;

    private final boolean isCeiling;

    private boolean canSwing = true;

    public final SwingAnimation animation;

    public HangingSignTileExtension(BlockState state) {
        super();
        //cheaty. will create on dedicated client on both server and client this as configs are loaded there
        if (PlatHelper.getPhysicalSide().isClient()) {
            animation = new PendulumAnimation(ClientConfigs.Blocks.HANGING_SIGN_CONFIG, this::getRotationAxis);
        } else {
            animation = null;
        }
        isCeiling = state.getBlock() instanceof CeilingHangingSignBlock;

    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        if (!canSwing || isCeiling) {
            animation. reset();
        } else {
            animation.tick(level, pos, state);
        }
    }

    private Vec3i getRotationAxis(BlockState state) {
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


    //just called by wall hanging sign
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
