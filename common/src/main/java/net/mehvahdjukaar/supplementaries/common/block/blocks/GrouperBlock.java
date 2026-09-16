package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.GrouperMatch;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrouperBlock extends HorizontalDirectionalBlock {

    public static final EnumProperty<GrouperMatch> MATCH = ModBlockProperties.GROUPER_MATCH;
    private static final MapCodec<GrouperBlock> CODEC = simpleCodec(GrouperBlock::new);
    private static final int MAX_ENTITIES_PER_SIDE = 16;

    public GrouperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(MATCH, GrouperMatch.NONE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATCH);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        GrouperMatch match = comparesEntities(level, pos, facing) ? GrouperMatch.NONE : findBlockMatch(level, pos, facing);
        return this.defaultBlockState().setValue(FACING, facing).setValue(MATCH, match);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(this) && comparesEntities(level, pos, state.getValue(FACING))) {
            this.updateNextTick(level, pos);
        }
        if (state.getValue(MATCH) != GrouperMatch.NONE) {
            this.updateNeighborsBehind(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (neighborBlock == this) {
            this.updateNextTick(level, pos);
        } else {
            this.updateMatchState(state, level, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() == state.getValue(FACING).getClockWise().getAxis()) {
            this.updateNextTick(level, pos);
        }
        return state;
    }

    private void updateNextTick(LevelAccessor level, BlockPos pos) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (!comparesEntities(level, pos, facing)) {
            this.updateMatchState(state, level, pos);
            return;
        }
        List<Entity> left = getEntitiesInside(level, pos.relative(facing.getCounterClockWise()));
        List<Entity> right = left.isEmpty() ? List.of() : getEntitiesInside(level, pos.relative(facing.getClockWise()));
        this.setMatch(state, level, pos, findEntityMatch(left, right));
        level.scheduleTick(pos, this, left.isEmpty() ? 10 : 2);
    }

    private void updateMatchState(BlockState state, Level level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        if (comparesEntities(level, pos, facing)) {
            this.updateNextTick(level, pos);
            return;
        }
        this.setMatch(state, level, pos, findBlockMatch(level, pos, facing));
    }

    private void setMatch(BlockState state, Level level, BlockPos pos, GrouperMatch match) {
        if (match == state.getValue(MATCH)) return;
        level.setBlockAndUpdate(pos, state.setValue(MATCH, match));
        this.updateNeighborsBehind(level, pos, state);
    }

    private void updateNeighborsBehind(Level level, BlockPos pos, BlockState state) {
        level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
    }

    private static boolean isOpenSide(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock;
    }

    private static boolean comparesEntities(BlockGetter level, BlockPos pos, Direction facing) {
        return CommonConfigs.Redstone.GROUPER_ENTITIES.get()
                && isOpenSide(level.getBlockState(pos.relative(facing.getCounterClockWise())))
                && isOpenSide(level.getBlockState(pos.relative(facing.getClockWise())));
    }

    private static List<Entity> getEntitiesInside(Level level, BlockPos pos) {
        List<Entity> entities = new ArrayList<>();
        level.getEntities(EntityTypeTest.forClass(Entity.class), new AABB(pos), EntitySelector.NO_SPECTATORS, entities, MAX_ENTITIES_PER_SIDE);
        return entities;
    }

    private static GrouperMatch findEntityMatch(List<Entity> left, List<Entity> right) {
        GrouperMatch match = GrouperMatch.NONE;
        for (Entity l : left) {
            for (Entity r : right) {
                if (l.getType() != r.getType()) continue;
                if (isExactEntityMatch(l, r)) return GrouperMatch.FULL;
                match = GrouperMatch.PARTIAL;
            }
        }
        return match;
    }

    //special entity matches here
    private static boolean isExactEntityMatch(Entity a, Entity b) {
        if (a instanceof ItemEntity ia && b instanceof ItemEntity ib) {
            return ItemStack.isSameItemSameComponents(ia.getItem(), ib.getItem());
        }
        if (a instanceof ItemFrame fa && b instanceof ItemFrame fb) {
            return ItemStack.isSameItemSameComponents(fa.getItem(), fb.getItem());
        }
        if (a instanceof FallingBlockEntity fa && b instanceof FallingBlockEntity fb) {
            return fa.getBlockState() == fb.getBlockState();
        }
        if (a instanceof LivingEntity la && b instanceof LivingEntity lb && la.isBaby() != lb.isBaby()) return false;
        if (a.hasCustomName() || b.hasCustomName()) {
            return Objects.equals(a.getCustomName(), b.getCustomName());
        }
        if (a instanceof Sheep sa && b instanceof Sheep sb) return sa.getColor() == sb.getColor();
        if (a instanceof VillagerDataHolder va && b instanceof VillagerDataHolder vb) {
            return va.getVillagerData().getProfession() == vb.getVillagerData().getProfession();
        }
        if (a instanceof VariantHolder<?> va && b instanceof VariantHolder<?> vb) {
            return Objects.equals(va.getVariant(), vb.getVariant());
        }
        return false;
    }

    private static GrouperMatch findBlockMatch(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));
        if (left.isAir() || !left.is(right.getBlock())) return GrouperMatch.NONE;
        return left == right ? GrouperMatch.FULL : GrouperMatch.PARTIAL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock()) && state.getValue(MATCH) != GrouperMatch.NONE) {
            this.updateNeighborsBehind(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return side == state.getValue(FACING) ? state.getValue(MATCH).power : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @VirtualOverride("neoforge")
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return direction == state.getValue(FACING);
    }
}
