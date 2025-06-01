package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.BookType;
import net.mehvahdjukaar.supplementaries.common.block.placeable_book.PlaceableBookManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    private final boolean horizontal;

    public BookPileBlock(Properties properties) {
        this(properties, false);
    }

    public BookPileBlock(Properties properties, boolean horizontal) {
        super(properties);
        this.horizontal = horizontal;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false).setValue(BOOKS, 1));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            ItemStack copy = stack.copyWithCount(1);
            tile.setItem(state.getValue(BOOKS) - 1, copy);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (state.getValue(BOOKS) < 4) {
            ItemStack stack = context.getItemInHand();
            BookType type = PlaceableBookManager.get(stack.getItem(), horizontal, context.getLevel().registryAccess());
            if (type != null) {
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            tile.getItem(state.getValue(BOOKS) - 1);
        }
        return Items.BOOK.getDefaultInstance();
    }

    @ForgeOverride
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
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
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;
            default -> SHAPE_1;
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (Utils.mayPerformBlockAction(player, pos, stack) && level.getBlockEntity(pos) instanceof BookPileBlockTile tile) {

            //add books. done by block logic so we can click on neighboring blocks too
            /*

            PlaceableBookManager bookReg = PlaceableBookManager.getInstance(level.registryAccess());
            BookType type = bookReg.get(stack.getItem(), horizontal);
            if (type != null && state.getValue(BOOKS) < 4) {

                SoundType sound = this.defaultBlockState().getSoundType();
                level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                stack.consume(1, player);

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }

                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                tile.setItem(state.getValue(BOOKS), stack.copyWithCount(1));

                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
             */
            //take book
            if (stack.isEmpty() && player.isSecondaryUseActive()) {
                return tile.interactWithPlayerItem(player, hand, stack, getBookIndex(state, pos, hitResult.getLocation()));
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected int getBookIndex(BlockState state, BlockPos pos, Vec3 location) {
        double f = 5 * (location.y - pos.getY()) / SHAPE_4.bounds().maxY;
        return Mth.clamp((int) f, 0, state.getValue(BOOKS) - 1);
    }

}
