package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SwayingBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class WallLanternBlock extends EnhancedLanternBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public WallLanternBlock(Properties properties) {
        super(properties.lightLevel(s -> s.getValue(LIT) ? s.getValue(LIGHT_LEVEL) : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(LIGHT_LEVEL, 0).setValue(WATERLOGGED, false).setValue(LIT, true));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        Item i = stack.getItem();
        if (te instanceof IBlockHolder && i instanceof BlockItem) {
            BlockState mimic = ((BlockItem) i).getBlock().defaultBlockState();
            ((IBlockHolder) te).setHeldBlock(mimic);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add((new TextComponent("You shouldn't have this")).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            return new ItemStack(te.getHeldBlock().getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL, LIT);
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        if (worldIn.getBlockEntity(pos) instanceof WallLanternBlockTile te && te.isRedstoneLantern) {
            if (state.getValue(LIT) && !worldIn.hasNeighborSignal(pos)) {
                worldIn.setBlock(pos, state.cycle(LIT), 2);
                if (te.getHeldBlock().hasProperty(LIT))
                    te.setHeldBlock(te.getHeldBlock().cycle(LIT));
            }
        }
    }

    //i could reference held lantern block directly but maybe it's more efficient this way idk
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof WallLanternBlockTile tile && tile.isRedstoneLantern) {
                boolean flag = state.getValue(LIT);
                if (flag != world.hasNeighborSignal(pos)) {
                    if (flag) {
                        world.scheduleTick(pos, this, 4);
                    } else {
                        world.setBlock(pos, state.cycle(LIT), 2);
                        if (tile.getHeldBlock().hasProperty(LIT))
                            tile.setHeldBlock(tile.getHeldBlock().cycle(LIT));
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof WallLanternBlockTile tile) {
            return tile.getHeldBlock().getDrops(builder);
        }
        return super.getDrops(state, builder);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile tile) {
            BlockState s = tile.getHeldBlock();
            s.getBlock().animateTick(s, level, pos, random);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new WallLanternBlockTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.WALL_LANTERN_TILE.get(), pLevel.isClientSide ? SwayingBlockTile::clientTick : null);
    }
}