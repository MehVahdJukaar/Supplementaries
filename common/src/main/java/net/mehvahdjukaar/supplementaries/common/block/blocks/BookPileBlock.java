package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.core.misc.IExtendedItem;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.common.events.overrides.SuppAdditionalPlacement;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BookPileBlock extends WaterBlock implements EntityBlock {

    private static final VoxelShape SHAPE_1 = Block.box(3D, 0D, 3D, 13D, 4D, 13D);
    private static final VoxelShape SHAPE_2 = Block.box(3D, 0D, 3D, 13D, 8D, 13D);
    private static final VoxelShape SHAPE_3 = Block.box(3D, 0D, 3D, 13D, 12D, 13D);
    private static final VoxelShape SHAPE_4 = Block.box(3D, 0D, 3D, 13D, 16D, 13D);

    public static final IntegerProperty BOOKS = ModBlockProperties.BOOKS;

    public BookPileBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false).setValue(BOOKS, 1));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if(player.isCreative()){
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            tile.setItem(state.getValue(BOOKS) - 1, copy);
        }
    }

    public boolean isAcceptedItem(Item i) {
        if (((IExtendedItem) i).moonlight$getAdditionalBehavior() instanceof SuppAdditionalPlacement sp) {
            Block b = sp.getPlacedBlock();
            if (CommonConfigs.Tweaks.MIXED_BOOKS.get()) {
                return b == ModRegistry.BOOK_PILE.get() || b == ModRegistry.BOOK_PILE_H.get();
            }
            return b == this;
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(BOOKS) < 4) {
            Item item = context.getItemInHand().getItem();
            if (isAcceptedItem(item)) {
                return true;
            }
        }
        return super.canBeReplaced(state, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOOKS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.getBlock() instanceof BookPileBlock) {
            return blockstate.setValue(BOOKS, blockstate.getValue(BOOKS) + 1);
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BookPileBlockTile(pPos, pState, false);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            tile.getItem(state.getValue(BOOKS) - 1);
        }
        return Items.BOOK.getDefaultInstance();
    }

    @ForgeOverride
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            return tile.getItem(getBookIndex(state, pos, target.getLocation()));
        }
        return Items.BOOK.getDefaultInstance();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BOOKS)) {
            default -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;
        };
    }

    @ForgeOverride
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            return tile.getEnchantPower();
        }
        return 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSecondaryUseActive() && level.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            if (player.getItemInHand(hand).isEmpty()) {
                return tile.interact(player, hand, getBookIndex(state, pos, hit.getLocation()));
            }
        }
        return InteractionResult.PASS;
    }

    protected int getBookIndex(BlockState state, BlockPos pos, Vec3 location) {
        double f = 5 * (location.y - pos.getY()) / SHAPE_4.bounds().maxY;
        return Mth.clamp((int) f, 0, state.getValue(BOOKS) - 1);
    }

}
