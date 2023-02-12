package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BlackboardBlock extends WaterBlock implements EntityBlock, ISoapWashable {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty GLOWING = ModBlockProperties.GLOWING;


    public BlackboardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(GLOWING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, GLOWING);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof BlackboardBlockTile tile) {
            BlockUtil.addOptionalOwnership(placer, tile);
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
    public static byte colorToByte(DyeColor color) {
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

    public static Vec2i getHitSubPixel(BlockHitResult hit) {
        Vec3 pos = hit.getLocation();
        Vec3 v = pos.yRot((float) ((hit.getDirection().toYRot()) * Math.PI / 180f));
        double fx = ((v.x % 1) * 16);
        if (fx < 0) fx += 16;
        int x = Mth.clamp((int) fx, -15, 15);

        int y = 15 - (int) Mth.clamp(Math.abs((v.y % 1) * 16), 0, 15);
        if (pos.y < 0) y = 15 - y; //crappy logic
        return new Vec2i(x, y);
    }

    @Nullable
    public static DyeColor getStackChalkColor(ItemStack stack) {
        boolean hasColor = CommonConfigs.Building.BLACKBOARD_COLOR.get();

        for (DyeColor dyeColor : DyeColor.values()) {
            if (!hasColor && (dyeColor != DyeColor.WHITE && dyeColor != DyeColor.BLACK)) continue;
            if (stack.is(ModTags.BLACKBOARD_TAGS.get(dyeColor))) {
                return dyeColor;
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te && te.isAccessibleBy(player) && !te.isWaxed()) {
            ItemStack stack = player.getItemInHand(handIn);
            Item i = stack.getItem();
            if (i instanceof HoneycombItem) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                //TODO use better particles shape
                level.levelEvent(player, 3003, pos, 0);
                te.setWaxed(true);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (i == Items.GLOW_INK_SAC && !state.getValue(GLOWING)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, true));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (i == Items.INK_SAC && state.getValue(GLOWING)) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, false));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            UseMode mode = CommonConfigs.Building.BLACKBOARD_MODE.get();

            if (hit.getDirection() == state.getValue(FACING) && mode.canManualDraw()) {

                if (i instanceof SoapItem) return InteractionResult.PASS;
                Vec2i pair = getHitSubPixel(hit);
                int x = pair.x();
                int y = pair.y();

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    byte newColor = colorToByte(color);
                    if (te.getPixel(x, y) != newColor) {
                        te.setPixel(x, y, newColor);
                        te.setChanged();
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            if (!level.isClientSide && mode.canOpenGui()) {
                te.sendOpenGuiPacket(level, pos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public enum UseMode {
        BOTH, GUI, MANUAL;

        public boolean canOpenGui() {
            return this != MANUAL;
        }

        public boolean canManualDraw() {
            return this != GUI;
        }
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
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            return this.getBlackboardItem(te);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            if (state.getValue(GLOWING)) {
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, false));
            }
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
