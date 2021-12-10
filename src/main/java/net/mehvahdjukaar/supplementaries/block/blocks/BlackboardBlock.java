package net.mehvahdjukaar.supplementaries.block.blocks;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;

public class BlackboardBlock extends WaterBlock implements EntityBlock {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, WRITTEN);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile tile) {
            tile.setCorrectBlockState(state, pos, world);
            BlockUtils.addOptionalOwnership(placer, tile);
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
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    //I started using this convention, so I have to keep it for backwards compat
    private static byte colorToByte(DyeColor color) {
        return switch (color) {
            case BLACK -> (byte) 0;
            case WHITE -> (byte) 1;
            case ORANGE -> (byte) 15;
            default -> (byte) color.getId();
        };
    }

    public static int colorFromByte(byte b) {
        return switch (b) {
            case 0, 1 -> 0xffffff;
            case 15 -> DyeColor.ORANGE.getMaterialColor().col;
            default -> DyeColor.byId(b).getMaterialColor().col;
        };
    }

    public static Pair<Integer, Integer> getHitSubPixel(BlockHitResult hit) {
        Vec3 v2 = hit.getLocation();
        Vec3 v = v2.yRot((float) ((hit.getDirection().toYRot()) * Math.PI / 180f));
        double fx = ((v.x % 1) * 16);
        if (fx < 0) fx += 16;
        int x = Mth.clamp((int) fx, -15, 15);

        int y = 15 - (int) Mth.clamp(Math.abs((v.y % 1) * 16), 0, 15);
        return new Pair<>(x, y);
    }

    @Nullable
    public static DyeColor getStackChalkColor(ItemStack stack) {
        Item item = stack.getItem();
        DyeColor color = null;
        if (ServerConfigs.cached.BLACKBOARD_COLOR) {
            if (item.getRegistryName().getNamespace().equals("chalk")) {
                color = DyeColor.byName(item.getRegistryName().getPath().replace("_chalk", ""), DyeColor.WHITE);
            } else color = DyeColor.getColor(stack);
        }
        if (color == null) {

            if (stack.is(ModTags.CHALK) || stack.is(Tags.Items.DYES_WHITE)) {
                color = DyeColor.WHITE;
            } else if (item == Items.COAL || item == Items.CHARCOAL || stack.is(Tags.Items.DYES_BLACK)) {
                color = DyeColor.BLACK;
            }
        }
        return color;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        //create tile
        if (!state.getValue(WRITTEN)) {
            worldIn.setBlock(pos, state.setValue(WRITTEN, true), (1 << 1) | (1 << 2));
        }
        if (worldIn.getBlockEntity(pos) instanceof BlackboardBlockTile te) {

            if (hit.getDirection() == state.getValue(FACING)) {
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                Pair<Integer, Integer> pair = getHitSubPixel(hit);
                int x = pair.getFirst();
                int y = pair.getSecond();

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    byte newColor = colorToByte(color);
                    if (te.pixels[x][y] != newColor) {
                        te.pixels[x][y] = newColor;
                        te.setChanged();
                        return InteractionResult.sidedSuccess(worldIn.isClientSide);
                    }
                    return InteractionResult.PASS;
                } else if (item == Items.SPONGE || item == Items.WET_SPONGE) {
                    te.pixels = new byte[16][16];
                    te.setChanged();
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                    //TODO: check if it's synced works in myltiplayer (might need mark dirty)
                }
            }
            if(!worldIn.isClientSide){
                te.sendOpenGuiPacket(worldIn, pos, player);
            }
        }
        return InteractionResult.sidedSuccess(worldIn.isClientSide);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
        if (context.getItemInHand().getTagElement("BlockEntityTag") != null) {
            state.setValue(WRITTEN, true);
        }
        return state;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return pState.getValue(WRITTEN) ? new BlackboardBlockTile(pPos, pState) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag != null) {
            tooltip.add((new TranslatableComponent("message.supplementaries.blackboard")).withStyle(ChatFormatting.GRAY));
        }
    }

    public ItemStack getBlackboardItem(BlackboardBlockTile te) {
        ItemStack itemstack = new ItemStack(this);
        if (!te.isEmpty()) {
            CompoundTag compoundnbt = te.savePixels(new CompoundTag());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            return this.getBlackboardItem(te);
        }
        return super.getCloneItemStack(state,target,world, pos, player);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_, boolean p_220069_6_) {
        super.neighborChanged(state, world, pos, p_220069_4_, p_220069_5_, p_220069_6_);
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            te.setCorrectBlockState(state, pos, world);
        }
    }

}
