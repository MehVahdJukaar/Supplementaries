package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MobJarBlock extends Block {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.create(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
            VoxelShapes.create(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public MobJarBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }


    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        super.animateTick(state, world, pos, random);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MobJarBlockTile();
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }

    // shulker box code
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof MobJarBlockTile) {
            MobJarBlockTile jarMobTile = (MobJarBlockTile) tileentity;
            if (!worldIn.isRemote && player.isCreative()) {
                ItemStack itemstack = new ItemStack(this);

                CommonUtil.saveJarMobItemNBT(itemstack, jarMobTile.mob);

                ItemEntity itementity = new ItemEntity(worldIn, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof MobJarBlockTile) {
            MobJarBlockTile jarMobTile = (MobJarBlockTile) tileentity;
            ItemStack itemstack = new ItemStack(this);

            CommonUtil.saveJarMobItemNBT(itemstack, jarMobTile.mob);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }


    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getItem(worldIn, pos, state);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof MobJarBlockTile) {
            MobJarBlockTile jarMobTile = (MobJarBlockTile) tileentity;

            CommonUtil.saveJarMobItemNBT(itemstack, jarMobTile.mob);
        }
        return itemstack;
    }



}