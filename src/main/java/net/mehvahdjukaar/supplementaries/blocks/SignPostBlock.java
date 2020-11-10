package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.WoodType;
import net.mehvahdjukaar.supplementaries.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.CompassItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;


public class SignPostBlock extends Block {
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(5D, 0.0D, 5D, 11D, 16.0D, 11D);
    public static final EnumProperty<WoodType> WOOD_TYPE = CommonUtil.WOOD_TYPE; //null = rendered by te. other states hold all the wood types sign models
    public SignPostBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WOOD_TYPE, WoodType.NONE));
    }

    public boolean rotateSigns(World world, BlockPos pos, float angle){
        TileEntity te = world.getTileEntity(pos);
        boolean success = false;
        if (te instanceof SignPostBlockTile) {
            SignPostBlockTile tile = (SignPostBlockTile) te;

            if(tile.up){
                tile.yawUp= MathHelper.wrapDegrees(tile.yawUp+angle);
                success = true;
            }
            if(tile.down){
                tile.yawDown= MathHelper.wrapDegrees(tile.yawDown+angle);
                success = true;
            }
            if(success){
                world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                tile.markDirty();
            }
        }

        return success;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = (SignPostBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            Item item = itemstack.getItem();
            boolean server = !worldIn.isRemote();
            boolean emptyhand = itemstack.isEmpty();
            boolean isDye = item instanceof DyeItem && player.abilities.allowEdit;
            boolean isSneaking = player.isSneaking() && emptyhand;
            boolean isSignPost = item instanceof SignPostItem;
            boolean isCompass = item instanceof CompassItem;
            //color
            if (isDye){
                if(te.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server){
                        te.markDirty();
                    }
                    return ActionResultType.SUCCESS;
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
                if(server){
                    te.markDirty();
                }
                return ActionResultType.SUCCESS;
            }
            //change direction with compass
            else if (isCompass){
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.func_234670_d_(itemstack) ?
                        this.getLodestonePos(worldIn, itemstack.getOrCreateTag()) : this.getWorldSpawnPos(worldIn);

                if(pointingPos!=null) {
                    double yaw = Math.atan2(pointingPos.getX() - pos.getX(), pointingPos.getZ() - pos.getZ()) * 180d / Math.PI;
                    //System.out.print("@"+yaw+"-"+pointingPos+"\n");
                    //this could probably be simplified
                    //int r = MathHelper.floor((double) ((180.0F + yaw) * 16.0F / 360.0F) + 0.5D) & 15;

                    double y = hit.getHitVec().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up && te.up) {
                        int d = te.leftUp ? 180 : 0;
                        te.yawUp = (float) yaw - d;// r*-22.5f;
                        return ActionResultType.SUCCESS;
                    } else if (!up && te.down) {
                        int d = te.leftDown ? 180 : 0;
                        te.yawDown = (float) yaw - d;// r*-22.5f;
                        return ActionResultType.SUCCESS;
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
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return state.get(WOOD_TYPE)==WoodType.NONE ? BlockRenderType.INVISIBLE : super.getRenderType(state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(WOOD_TYPE);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile)te);
            double y = target.getHitVec().y;
            boolean up = y%((int)y) > 0.5d;
            if(up && tile.up){
                return new ItemStack(CommonUtil.getSignPostItemFromWoodType(tile.woodTypeUp));
            }
            else if(!up && tile.down){
                return new ItemStack(CommonUtil.getSignPostItemFromWoodType(tile.woodTypeDown));
            }
        }
        return new ItemStack(Registry.SIGN_POST_ITEM_OAK.get());
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SignPostBlockTile) {
                SignPostBlockTile tile = ((SignPostBlockTile) tileentity);
                if (tile.up) {
                    ItemStack itemstack = new ItemStack(CommonUtil.getSignPostItemFromWoodType(tile.woodTypeUp));
                    ItemEntity itementity = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);
                    itementity.setDefaultPickupDelay();
                    worldIn.addEntity(itementity);
                }
                if (tile.down) {
                    ItemStack itemstack = new ItemStack(CommonUtil.getSignPostItemFromWoodType(tile.woodTypeDown));
                    ItemEntity itementity = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemstack);
                    itementity.setDefaultPickupDelay();
                    worldIn.addEntity(itementity);
                }
                spawnDrops(tile.fenceBlock, worldIn, pos);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
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