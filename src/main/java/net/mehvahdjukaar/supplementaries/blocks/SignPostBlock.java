package net.mehvahdjukaar.supplementaries.blocks;


import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.WoodType;
import net.mehvahdjukaar.supplementaries.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;


public class SignPostBlock extends Block {
    public static final EnumProperty WOOD_TYPE = CommonUtil.WOOD_TYPE; //null = rendered by te. other states hold all the wood types sign models
    public SignPostBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(WOOD_TYPE, WoodType.NONE));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = (SignPostBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            boolean server = !worldIn.isRemote();
            boolean emptyhand = itemstack.isEmpty();
            boolean flag = itemstack.getItem() instanceof DyeItem && player.abilities.allowEdit;
            boolean flag1 = player.isSneaking() && emptyhand;
            boolean flag2 = itemstack.getItem() instanceof SignPostItem;
            //color
            if (flag){
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
            else if (flag1){
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
            // open gui (edit sign with empty hand)
            else if (!flag2) {
                if(player instanceof PlayerEntity && !server) SignPostGui.open(te);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.create(0.625D, 0D, 0.625D, 0.375D, 1D, 0.375D);
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
        return new ItemStack(Registry.SIGN_POST_ITEM_OAK.get());
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if(tileentity instanceof SignPostBlockTile){
            SignPostBlockTile signtile = ((SignPostBlockTile) tileentity);
            if(signtile.up && signtile.down){
                spawnDrops(state, worldIn, pos);
            }
            spawnDrops(signtile.fenceblock, worldIn, pos);
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
        return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
    }
}