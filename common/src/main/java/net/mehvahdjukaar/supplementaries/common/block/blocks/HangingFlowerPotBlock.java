package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class HangingFlowerPotBlock extends Block implements EntityBlock {

    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public HangingFlowerPotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        Item i = stack.getItem();
        if (world.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile tile) {
            if (i instanceof BlockItem blockItem) {
                BlockState mimic = blockItem.getBlock().defaultBlockState();
                tile.setHeldBlock(mimic);
            }
            BlockUtil.addOptionalOwnership(entity, tile);
        }
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("block.minecraft.flower_pot");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.DOWN ? super.getStateForPlacement(context) : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile tile && tile.isAccessibleBy(player)) {
            Block pot = tile.getHeldBlock().getBlock();
            if (pot instanceof FlowerPotBlock flowerPot) {
                ItemStack itemstack = player.getItemInHand(handIn); //&& FlowerPotHandler.isEmptyPot(flowerPot)
                Item item = itemstack.getItem();
                //mimics flowerPorBlock behavior for consistency
                Block newPot = item instanceof BlockItem bi ? FlowerPotHandler.getFullPot(flowerPot, bi.getBlock()) : Blocks.AIR;

                boolean isEmptyFlower = newPot == Blocks.AIR;
                boolean isPotEmpty = FlowerPotHandler.isEmptyPot(pot);

                if (isEmptyFlower != isPotEmpty) {
                    if (isPotEmpty) {
                        if (!level.isClientSide) {
                            tile.setHeldBlock(newPot.defaultBlockState());
                            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                            tile.setChanged();
                        }

                        player.awardStat(Stats.POT_FLOWER);
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                    } else {
                        //drop item
                        ItemStack flowerItem = pot.getCloneItemStack(level, pos, state);
                        if (!flowerItem.equals(new ItemStack(this))) {
                            if (itemstack.isEmpty()) {
                                player.setItemInHand(handIn, flowerItem);
                            } else if (!player.addItem(flowerItem)) {
                                player.drop(flowerItem, false);
                            }
                        }
                        if (!level.isClientSide) {
                            tile.setHeldBlock(FlowerPotHandler.getEmptyPot(flowerPot).defaultBlockState());
                            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                            tile.setChanged();
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HangingFlowerPotBlockTile(pPos, pState);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile te) {
            if (te.getHeldBlock().getBlock() instanceof FlowerPotBlock b) {
                Block flower = b.getContent();
                if (flower == Blocks.AIR) return new ItemStack(Blocks.FLOWER_POT, 1);
                return new ItemStack(flower);
            }
        }
        return new ItemStack(Blocks.FLOWER_POT, 1);
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof HangingFlowerPotBlockTile tile) {
            Block b = tile.getHeldBlock().getBlock();
            if (b instanceof FlowerPotBlock)
                return Arrays.asList(new ItemStack(((FlowerPotBlock) b).getContent()), new ItemStack(Items.FLOWER_POT));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return RopeBlock.isSupportingCeiling(pos.relative(Direction.UP), worldIn);
    }
}