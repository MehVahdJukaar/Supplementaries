package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.block.CommonUtil;
import net.mehvahdjukaar.supplementaries.block.CommonUtil.WoodType;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SignPostBlock extends Block implements IWaterLoggable, IForgeBlock{
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(5D, 0.0D, 5D, 11D, 16.0D, 11D);
    protected static final VoxelShape COLLISION_SHAPE = Block.makeCuboidShape(5D, 0.0D, 5D, 11D, 24.0D, 11D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SignPostBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof SignPostBlockTile){
            return ((SignPostBlockTile)te).fenceBlock.getPlayerRelativeBlockHardness(player,worldIn,pos);
        }
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    //might cause lag when breaking?
    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof SignPostBlockTile){
            BlockState s = ((SignPostBlockTile) te).fenceBlock;
            if(s!=null)return s.getSoundType(world,pos,entity);
        }
        return super.getSoundType(state,world,pos,entity);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, flag);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
                                          BlockPos facingPos) {
        if (state.get(WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = (SignPostBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            Item item = itemstack.getItem();

            //put post on map
            if(item instanceof FilledMapItem){
                MapData data = FilledMapItem.getMapData(itemstack,worldIn);
                if(data!=null) {
                    data.tryAddBanner(worldIn, pos);
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }


            boolean server = !worldIn.isRemote();
            boolean emptyhand = itemstack.isEmpty();
            boolean isDye = item instanceof DyeItem && player.abilities.allowEdit;
            boolean isSneaking = player.isSneaking() && emptyhand;
            boolean isSignPost = item instanceof SignPostItem;
            boolean isCompass = item instanceof CompassItem;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.markDirty();
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
            //sneak right click rotates the sign on z axis
            else if (isSneaking){
                double y = hit.getHitVec().y;
                boolean up = y%((int)y) > 0.5d;
                if(up){
                    te.leftUp = !te.leftUp;
                }
                else{
                    te.leftDown = !te.leftDown;
                }
                if(server)te.markDirty();
                return ActionResultType.func_233537_a_(worldIn.isRemote);
            }
            //change direction with compass
            else if (isCompass){
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.func_234670_d_(itemstack) ?
                        this.getLodestonePos(worldIn, itemstack.getOrCreateTag()) : this.getWorldSpawnPos(worldIn);

                if(pointingPos!=null) {
                    double yaw = Math.atan2(pointingPos.getX() - pos.getX(), pointingPos.getZ() - pos.getZ()) * 180d / Math.PI;
                    //int r = MathHelper.floor((double) ((180.0F + yaw) * 16.0F / 360.0F) + 0.5D) & 15;
                    double y = hit.getHitVec().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up && te.up) {
                        int d = te.leftUp ? 180 : 0;
                        te.yawUp = (float) yaw - d;// r*-22.5f;
                        if(server)te.markDirty();
                        return ActionResultType.func_233537_a_(worldIn.isRemote);
                    } else if (!up && te.down) {
                        int d = te.leftDown ? 180 : 0;
                        te.yawDown = (float) yaw - d;// r*-22.5f;
                        if(server)te.markDirty();
                        return ActionResultType.func_233537_a_(worldIn.isRemote);
                    }
                }
            }
            // open gui (edit sign with empty hand)
            else if (!isSignPost) {
                if(!server) SignPostGui.open(te);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Nullable
    private BlockPos getLodestonePos(World world, CompoundNBT cmp) {
        boolean flag = cmp.contains("LodestonePos");
        boolean flag1 = cmp.contains("LodestoneDimension");
        if (flag && flag1) {
            Optional<RegistryKey<World>> optional = CompassItem.func_234667_a_(cmp);
            if ( optional.isPresent() && world.getDimensionKey() == optional.get() ) {
                return NBTUtil.readBlockPos(cmp.getCompound("LodestonePos"));
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(World world) {
        return world.getDimensionType().isNatural() ? new BlockPos(world.getWorldInfo().getSpawnX(),
                world.getWorldInfo().getSpawnY(),world.getWorldInfo().getSpawnZ()) : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile)te);
            double y = target.getHitVec().y;
            boolean up = y%((int)y) > 0.5d;
            if(up && tile.up){
                return new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
            }
            else if(!up && tile.down){
                return new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
            }
            else return new ItemStack(tile.fenceBlock.getBlock());
        }
        return new ItemStack(Registry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile) tileentity);
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.fenceBlock.getBlock()));

            if (tile.up) {
                ItemStack s = new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
                list.add(s);
            }
            if (tile.down) {
                ItemStack s = new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
                list.add(s);
            }
            return list;
        }
        return super.getDrops(state,builder);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        float angle = rot.equals(Rotation.CLOCKWISE_90)? 90 : -90;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SignPostBlockTile) {
            SignPostBlockTile tile = (SignPostBlockTile) te;
            boolean success = false;
            if(tile.up){
                tile.yawUp= MathHelper.wrapDegrees(tile.yawUp+angle);
                success=true;
            }
            if(tile.down){
                tile.yawDown= MathHelper.wrapDegrees(tile.yawDown+angle);
                success=true;
            }

            if(success){
                //world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                tile.markDirty();
            }
        }
        return state;
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SignPostBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}