package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.WallLanternBlockTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.List;
import java.util.Random;


public class WallLanternBlock extends EnhancedLanternBlock {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public WallLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH)
                .with(LIGHT_LEVEL, 0).with(WATERLOGGED,false).with(LIT,true));
    }

    @Override
    public void addInformation(ItemStack stack,  IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add((new StringTextComponent("You shouldn't have this")).mergeStyle(TextFormatting.GRAY));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        if(state.get(LIT)) {
            return state.get(LIGHT_LEVEL);
        }
        return 0;
    }

    //TODO: replace getItem with getPickBlock
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return new ItemStack(((WallLanternBlockTile) te).lanternBlock.getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LIGHT_LEVEL,LIT);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile && ((WallLanternBlockTile) te).isRedstoneLantern) {
            if (state.get(LIT) && !worldIn.isBlockPowered(pos)) {
                worldIn.setBlockState(pos, state.func_235896_a_(LIT), 2);
                if(((WallLanternBlockTile) te).lanternBlock.hasProperty(LIT))
                    ((WallLanternBlockTile) te).lanternBlock = ((WallLanternBlockTile) te).lanternBlock.func_235896_a_(LIT);
            }
        }
    }

    //i could reference held lantern block directly but maybe it's more efficient this way idk
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof WallLanternBlockTile && ((WallLanternBlockTile) te).isRedstoneLantern) {
                boolean flag = state.get(LIT);
                if (flag != world.isBlockPowered(pos)) {
                    if (flag) {
                        world.getPendingBlockTicks().scheduleTick(pos, this, 4);
                    } else {
                        world.setBlockState(pos, state.func_235896_a_(LIT), 2);
                        if(((WallLanternBlockTile) te).lanternBlock.hasProperty(LIT))
                            ((WallLanternBlockTile) te).lanternBlock=((WallLanternBlockTile) te).lanternBlock.func_235896_a_(LIT);
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof WallLanternBlockTile){
            return Collections.singletonList(new ItemStack(((WallLanternBlockTile) tileentity).lanternBlock.getBlock()));
        }
        return super.getDrops(state,builder);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WallLanternBlockTile();
    }

}