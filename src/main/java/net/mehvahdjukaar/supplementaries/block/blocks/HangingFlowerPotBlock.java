package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHelper;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;

public class HangingFlowerPotBlock extends Block{

    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
    public static final BooleanProperty TILE = BlockProperties.TILE; // is it tile only. used for rendering to store model
    public HangingFlowerPotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TILE, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).withStyle(TextFormatting.GRAY));
    }

    @Override
    public IFormattableTextComponent getName() {
        return new TranslationTextComponent("minecraft:flower_pot");
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return context.getClickedFace() == Direction.DOWN?super.getStateForPlacement(context):null;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TILE);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        if(tileEntity instanceof HangingFlowerPotBlockTile) {
            HangingFlowerPotBlockTile te = ((HangingFlowerPotBlockTile)tileEntity);
            Block pot = te.pot.getBlock();
            if(pot instanceof FlowerPotBlock && FlowerPotHelper.isEmptyPot(((FlowerPotBlock) pot).getEmptyPot())) {
                ItemStack itemstack = player.getItemInHand(handIn);
                Item item = itemstack.getItem();
                //mimics flowerPorBlock behavior
                Block newPot = item instanceof BlockItem ? FlowerPotHelper.getFullPot((FlowerPotBlock) pot,((BlockItem)item).getBlock()): Blocks.AIR;
                /*Block newPot = item instanceof BlockItem ? FlowerPotHelper.FULL_POTS.get(((FlowerPotBlock) pot).getEmptyPot())
                        .getOrDefault(((BlockItem)item).getBlock().getRegistryName(), Blocks.AIR.delegate).get() : Blocks.AIR;*/

                boolean isEmptyFlower = newPot == Blocks.AIR;
                boolean isPotEmpty = FlowerPotHelper.isEmptyPot(pot);

                if (isEmptyFlower != isPotEmpty) {
                    if (isPotEmpty) {
                        te.setHeldBlock(newPot.defaultBlockState());
                        player.awardStat(Stats.POT_FLOWER);
                        if (!player.abilities.instabuild) {
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
                        te.setHeldBlock(((FlowerPotBlock) pot).getEmptyPot().defaultBlockState());
                    }
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                } else {
                    return ActionResultType.CONSUME;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return state.getValue(TILE)?BlockRenderType.MODEL : BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new HangingFlowerPotBlockTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof HangingFlowerPotBlockTile) {
            Block b = ((HangingFlowerPotBlockTile) te).pot.getBlock();
            if(b instanceof FlowerPotBlock){
                Block flower = ((FlowerPotBlock) b).getContent();
                if(flower==Blocks.AIR)return new ItemStack(((FlowerPotBlock) b).getEmptyPot());
                return new ItemStack(flower);
            }
        }
        return new ItemStack(Blocks.FLOWER_POT, 1);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof HangingFlowerPotBlockTile){
            Block b = ((HangingFlowerPotBlockTile) tileentity).pot.getBlock();
            if(b instanceof FlowerPotBlock)
                return Arrays.asList(new ItemStack(((FlowerPotBlock) b).getContent()), new ItemStack(((FlowerPotBlock) b).getEmptyPot()));
        }

        return super.getDrops(state,builder);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return VoxelShapes.block();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return RopeBlock.isSupportingCeiling(pos.relative(Direction.UP),worldIn);
    }
}