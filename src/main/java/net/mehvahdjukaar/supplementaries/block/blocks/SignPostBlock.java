package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignPostBlock extends FenceMimicBlock implements EntityBlock {

    public SignPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(handIn);
        Item item = itemstack.getItem();

        //put post on map
        if (item instanceof MapItem) {
            if (!level.isClientSide) {
                if (MapItem.getSavedData(itemstack, level) instanceof ExpandedMapData data) {
                    data.toggleCustomDecoration(level, pos);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }


        if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile && tile.isAccessibleBy(player)) {

            boolean server = !level.isClientSide();
            boolean emptyHand = itemstack.isEmpty();
            boolean isSneaking = player.isShiftKeyDown() && emptyHand;

            if (hit.getDirection().getAxis() != Direction.Axis.Y) {

                InteractionResult result = tile.textHolder.playerInteract(level, pos, player, handIn, tile::setChanged);
                if(result != InteractionResult.PASS) return result;

                //sneak right click rotates the sign on z axis
                if (isSneaking) {
                    double y = hit.getLocation().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up) {
                        tile.leftUp = !tile.leftUp;
                    } else {
                        tile.leftDown = !tile.leftDown;
                    }
                    if (server) tile.setChanged();
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                //change direction with compass
                else if (item instanceof CompassItem) {
                    //itemModelProperties code
                    BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                            this.getLodestonePos(level, itemstack) : this.getWorldSpawnPos(level);

                    if (pointingPos != null) {
                        double y = hit.getLocation().y;
                        boolean up = y % ((int) y) > 0.5d;
                        if (up && tile.up) {
                            tile.pointToward(pointingPos, true);
                        } else if (!up && tile.down) {
                            tile.pointToward(pointingPos, false);
                        }
                        if (server) tile.setChanged();
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                    return InteractionResult.FAIL;
                } else if (CompatHandler.framedblocks && tile.framed) {
                    boolean success = FramedSignPost.handleInteraction(tile, player, handIn, itemstack, level, pos);
                    if (success) return InteractionResult.sidedSuccess(level.isClientSide);
                } else if (item instanceof SignPostItem) {
                    //let sign item handle this one
                    return InteractionResult.PASS;
                }
            }
            // open gui (edit sign with empty hand)
            if (!server) {
                SignPostGui.open(tile);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private BlockPos getLodestonePos(Level world, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if(tag != null) {
            boolean flag = tag.contains("LodestonePos");
            boolean flag1 = tag.contains("LodestoneDimension");
            if (flag && flag1) {
                Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(tag);
                if (optional.isPresent() && world.dimension() == optional.get()) {
                    return NbtUtils.readBlockPos(tag.getCompound("LodestonePos"));
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(Level world) {
        return world.dimensionType().natural() ? new BlockPos(world.getLevelData().getXSpawn(),
                world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn()) : null;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            double y = target.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            if (up && tile.up) {
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
            } else if (!up && tile.down) {
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
            } else return new ItemStack(tile.mimic.getBlock());
        }
        return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SignPostBlockTile tile) {
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.mimic.getBlock()));

            if (tile.up) {
                ItemStack s = new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
                list.add(s);
            }
            if (tile.down) {
                ItemStack s = new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
                list.add(s);
            }
            return list;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        float angle = rot.equals(Rotation.CLOCKWISE_90) ? 90 : -90;
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            boolean success = false;
            if (tile.up) {
                tile.yawUp = Mth.wrapDegrees(tile.yawUp + angle);
                success = true;
            }
            if (tile.down) {
                tile.yawDown = Mth.wrapDegrees(tile.yawDown + angle);
                success = true;
            }

            if (success) {
                //world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                tile.setChanged();
            }
        }
        return state;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignPostBlockTile(pPos, pState);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}