package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
            BlockUtils.addOptionalOwnership(entity, tile);
        }
    }

    @Override
    public MutableComponent getName() {
        return new TranslatableComponent("block.minecraft.flower_pot");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.DOWN ? super.getStateForPlacement(context) : null;
    }
    //TODO: use dynamic block model
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile tile && tile.isAccessibleBy(player)) {
            Block pot = tile.getHeldBlock().getBlock();
            if (pot instanceof FlowerPotBlock flowerPot) {
                ItemStack itemstack = player.getItemInHand(handIn); //&& FlowerPotHandler.isEmptyPot(flowerPot)
                Item item = itemstack.getItem();
                //mimics flowerPorBlock behavior
                Block newPot = item instanceof BlockItem bi ? FlowerPotHandler.getFullPot(flowerPot, bi.getBlock()) : Blocks.AIR;
                /*Block newPot = item instanceof BlockItem ? FlowerPotHelper.FULL_POTS.get(((FlowerPotBlock) pot).getEmptyPot())
                        .getOrDefault(((BlockItem)item).getBlock().getRegistryName(), Blocks.AIR.delegate).get() : Blocks.AIR;*/

                boolean isEmptyFlower = newPot == Blocks.AIR;
                boolean isPotEmpty = FlowerPotHandler.isEmptyPot(pot);

                if (isEmptyFlower != isPotEmpty) {
                    if (isPotEmpty) {
                        tile.setHeldBlock(newPot.defaultBlockState());
                        player.awardStat(Stats.POT_FLOWER);
                        if (!player.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                    } else {
                        //drop item
                        ItemStack flowerItem = pot.getCloneItemStack(worldIn, pos, state);
                        if (!flowerItem.equals(new ItemStack(this))) {
                            if (itemstack.isEmpty()) {
                                player.setItemInHand(handIn, flowerItem);
                            } else if (!player.addItem(flowerItem)) {
                                player.drop(flowerItem, false);
                            }
                        }
                        tile.setHeldBlock(((FlowerPotBlock) pot).getEmptyPot().defaultBlockState());
                    }
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof HangingFlowerPotBlockTile) {
            Block b = ((HangingFlowerPotBlockTile) te).getHeldBlock().getBlock();
            if (b instanceof FlowerPotBlock) {
                Block flower = ((FlowerPotBlock) b).getContent();
                if (flower == Blocks.AIR) return new ItemStack(((FlowerPotBlock) b).getEmptyPot());
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
                return Arrays.asList(new ItemStack(((FlowerPotBlock) b).getContent()), new ItemStack(((FlowerPotBlock) b).getEmptyPot()));
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