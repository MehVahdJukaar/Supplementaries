package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class BlackboardBlock extends WaterBlock implements EntityBlock, IWashable {

    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 11.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_EAST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_WEST = MthUtils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);

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
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
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
            case 15 -> DyeColor.ORANGE.getMapColor().col;
            default -> DyeColor.byId(b).getMapColor().col;
        };
    }

    public static Vector2i getHitSubPixel(BlockHitResult hit) {
        Vec3 pos = hit.getLocation();
        Vec3 v = pos.yRot((hit.getDirection().toYRot()) * Mth.DEG_TO_RAD);
        double fx = ((v.x % 1) * 16);
        if (fx < 0) fx += 16;
        int x = Mth.clamp((int) fx, -15, 15);

        int y = 15 - (int) Mth.clamp(Math.abs((v.y % 1) * 16), 0, 15);
        if (pos.y < 0) y = 15 - y; //crappy logic
        return new Vector2i(x, y);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BlackboardBlockTile te && te.isAccessibleBy(player)) {
            ItemInteractionResult result = te.tryWaxing(level, pos, player, hand, stack);

            if (result.consumesAction()) {
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));
                te.setChanged(); //this also sends block update in tile
            }
            if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) return result;

            boolean glowChanged = false;
            if (stack.is(Items.GLOW_INK_SAC) && !state.getValue(GLOWING)) {
                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, true));

                glowChanged = true;

            } else if (stack.is(Items.INK_SAC) && state.getValue(GLOWING)) {
                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, false));

                glowChanged = true;
            }
            if (glowChanged) {
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, level.getBlockState(pos)));

                stack.consume(1, player);
                //server
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                }
                return ItemInteractionResult.CONSUME;
            }

            UseMode config = CommonConfigs.Building.BLACKBOARD_MODE.get();

            if (hit.getDirection() == state.getValue(FACING) && config.canManualDraw()) {

                DyeColor color = getStackChalkColor(stack);
                if (color != null) {
                    //exit early for client
                    Vector2i pair = getHitSubPixel(hit);
                    int x = pair.x();
                    int y = pair.y();

                    byte newColor = colorToByte(color);
                    if (te.getPixel(x, y) != newColor) {
                        te.setPixel(x, y, newColor);
                        te.setChanged(); //this also updates clients
                    }
                    return ItemInteractionResult.CONSUME;
                }
            }
            if (config.canOpenGui()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    te.tryOpeningEditGui(serverPlayer, pos, stack);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
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
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlackboardBlockTile(pPos, pState);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            return BlockUtil.saveTileToItem(te);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
        if (level.getBlockEntity(pos) instanceof BlackboardBlockTile te) {
            if (state.getValue(GLOWING)) {
                level.setBlockAndUpdate(pos, state.setValue(GLOWING, false));
            }
            if (te.isWaxed()) {
                te.setWaxed(false);
                te.setChanged();
                return true;
            } else if (!te.isEmpty()) {
                te.clearPixels();
                te.setChanged();
                return true;
            }
        }
        return false;
    }
}
