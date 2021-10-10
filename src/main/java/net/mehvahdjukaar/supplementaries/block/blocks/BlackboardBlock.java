package net.mehvahdjukaar.supplementaries.block.blocks;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.BlackBoardGui;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class BlackboardBlock extends WaterBlock {
    public static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 5.0D);
    public static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    public static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 5.0D, 16.0D, 16.0D);
    public static final VoxelShape SHAPE_WEST = Block.box(11.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WRITTEN = BlockProperties.WRITTEN;

    public BlackboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(WRITTEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, WRITTEN);
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        //TODO: backwards compat. remove
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof BlackboardBlockTile) {
                if (compoundnbt.contains("pixels_0")) {
                    for (int i = 0; i < 16; i++) {
                        byte[] b = compoundnbt.getByteArray("pixels_" + i);
                        if (b.length == 16) ((BlackboardBlockTile) te).pixels[i] = b;
                    }
                }
            }
        }
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof BlackboardBlockTile) {
            ((BlackboardBlockTile) tileentity).setCorrectBlockState(state, pos, world);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)) {
            default:
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
        }
    }

    //I started using this convention, so I have to keep it for backwards compat
    private static byte colorToByte(DyeColor color) {
        switch (color) {
            case BLACK:
                return 0;
            case WHITE:
                return 1;
            case ORANGE:
                return 15;
            default:
                return (byte) color.getId();
        }
    }

    public static int colorFromByte(byte b) {
        switch (b) {
            case 0:
            case 1:
                return 0xffffff;
            case 15:
                return DyeColor.ORANGE.getColorValue();
            default:
                return DyeColor.byId(b).getColorValue();
        }
    }

    public static Pair<Integer, Integer> getHitSubPixel(BlockRayTraceResult hit){
        Vector3d v2 = hit.getLocation();
        Vector3d v = v2.yRot((float) ((hit.getDirection().toYRot()) * Math.PI / 180f));
        double fx = ((v.x % 1) * 16);
        if (fx < 0) fx += 16;
        int x = MathHelper.clamp((int) fx, -15, 15);

        int y = 15 - (int) MathHelper.clamp(Math.abs((v.y % 1) * 16), 0, 15);
        return new Pair<>(x,y);
    }

    @Nullable
    public static DyeColor getStackChalkColor(ItemStack stack){
        Item item = stack.getItem();
        DyeColor color = null;
        if (ServerConfigs.cached.BLACKBOARD_COLOR) {
            color = DyeColor.getColor(stack);
        }
        if(color == null) {
            if (item.is(ModTags.CHALK) || item.is(Tags.Items.DYES_WHITE)) {
                color = DyeColor.WHITE;
            } else if (item == Items.COAL || item == Items.CHARCOAL || item.is(Tags.Items.DYES_BLACK)) {
                color = DyeColor.BLACK;
            }
        }
        return color;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        //create tile
        if (!state.getValue(WRITTEN)) {
            worldIn.setBlock(pos, state.setValue(WRITTEN, true), Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
        }
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof BlackboardBlockTile) {
            BlackboardBlockTile te = (BlackboardBlockTile) tileentity;

            if (hit.getDirection() == state.getValue(FACING)) {
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                Pair<Integer, Integer> pair = getHitSubPixel(hit);
                int x = pair.getFirst();
                int y = pair.getSecond();

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    te.pixels[x][y] = colorToByte(color);
                    te.setChanged();
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                }
                else if (item == Items.SPONGE || item == Items.WET_SPONGE) {
                    te.pixels = new byte[16][16];
                    te.setChanged();
                    return ActionResultType.sidedSuccess(worldIn.isClientSide);
                    //TODO: check if it's synced works in myltiplayer (might need mark dirty)
                }
            }

            if (worldIn.isClientSide()) BlackBoardGui.open(te);
        }
        return ActionResultType.sidedSuccess(worldIn.isClientSide);
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
        if (context.getItemInHand().getTagElement("BlockEntityTag") != null) {
            state.setValue(WRITTEN, true);
        }
        return state;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(WRITTEN);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlackboardBlockTile();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            tooltip.add((new TranslationTextComponent("message.supplementaries.blackboard")).withStyle(TextFormatting.GRAY));
        }
    }

    public ItemStack getBlackboardItem(BlackboardBlockTile te) {
        ItemStack itemstack = new ItemStack(this);
        if (!te.isEmpty()) {
            CompoundNBT compoundnbt = te.saveToTag(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }

    //normal drop
    /*
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof BlackboardBlockTile) {
            ItemStack itemstack = this.getBlackboardItem((BlackboardBlockTile) tileentity);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }*/


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof BlackboardBlockTile) {
            return this.getBlackboardItem((BlackboardBlockTile) te);
        }
        return super.getPickBlock(state, target, world, pos, player);
    }


}
