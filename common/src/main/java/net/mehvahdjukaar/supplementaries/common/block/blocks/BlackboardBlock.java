package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;

public class BlackboardBlock extends WaterBlock implements EntityBlock, ISoapWashable {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlackboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile tile) {
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
            var id = Utils.getID(item);
            if (id.getNamespace().equals("chalk")) {
                color = DyeColor.byName(id.getPath().replace("_chalk", ""), DyeColor.WHITE);
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
        if (worldIn.getBlockEntity(pos) instanceof BlackboardBlockTile te && te.isAccessibleBy(player) && !te.isWaxed()) {
            ItemStack stack = player.getItemInHand(handIn);

            if (stack.getItem() instanceof HoneycombItem) {
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }
                stack.shrink(1);
                worldIn.levelEvent(player, 3003, pos, 0);
                te.setWaxed(true);
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }

            if (hit.getDirection() == state.getValue(FACING)) {

                if (stack.getItem() instanceof SoapItem) return InteractionResult.PASS;
                Pair<Integer, Integer> pair = getHitSubPixel(hit);
                int x = pair.getFirst();
                int y = pair.getSecond();

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    byte newColor = colorToByte(color);
                    if (te.getPixel(x, y) != newColor) {
                        te.setPixel(x, y, newColor);
                        te.setChanged();
                    }
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
            }
            if (!worldIn.isClientSide) {
                te.sendOpenGuiPacket(worldIn, pos, player);
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        return InteractionResult.PASS;
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlackboardBlockTile(pPos, pState);
    }

    public ItemStack getBlackboardItem(BlackboardBlockTile te) {
        ItemStack itemstack = new ItemStack(this);
        if (!te.isEmpty()) {
            CompoundTag tag = te.savePixels(new CompoundTag());
            if (!tag.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", tag);
            }
        }
        return itemstack;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            return this.getBlackboardItem(te);
        }
        return super.getCloneItemStack(state, target, world, pos, player);
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            if (te.isWaxed()) {
                te.setWaxed(false);
                te.setChanged();
                return true;
            } else if (!te.isEmpty()) {
                te.clear();
                te.setChanged();
                return true;
            }
        }
        return false;
    }
}
