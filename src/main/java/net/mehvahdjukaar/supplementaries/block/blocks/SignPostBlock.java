package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignPostBlock extends FenceMimicBlock{

    public SignPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    //TODO: add flip sound here
    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {

        if(hit.getDirection().getAxis() == Direction.Axis.Y) return ActionResultType.PASS;

        if(!worldIn.isClientSide) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SignPostBlockTile && ((IOwnerProtected) tileentity).isAccessibleBy(player)) {
                SignPostBlockTile te = (SignPostBlockTile) tileentity;
                ItemStack itemstack = player.getItemInHand(handIn);
                Item item = itemstack.getItem();

                //put post on map
                if (item instanceof FilledMapItem) {
                    MapData data = FilledMapItem.getOrCreateSavedData(itemstack, worldIn);
                    if (data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(worldIn, pos);
                    }
                    return ActionResultType.CONSUME;
                }


                boolean server = !worldIn.isClientSide();
                boolean emptyhand = itemstack.isEmpty();
                boolean isDye = item instanceof DyeItem && player.abilities.mayBuild;
                boolean isSneaking = player.isShiftKeyDown() && emptyhand;
                boolean isSignPost = item instanceof SignPostItem;
                boolean isCompass = item instanceof CompassItem;
                //color
                if (isDye) {
                    if (te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())) {
                        if (!player.isCreative()) {
                            itemstack.shrink(1);
                        }
                        if (server) te.setChanged();
                        return ActionResultType.CONSUME;
                    }
                    return ActionResultType.FAIL;
                }
                //sneak right click rotates the sign on z axis
                else if (isSneaking) {
                    double y = hit.getLocation().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up) {
                        te.leftUp = !te.leftUp;
                    } else {
                        te.leftDown = !te.leftDown;
                    }
                    if (server) te.setChanged();
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundCategory.BLOCKS, 1.0F, 0.6F);
                    return ActionResultType.CONSUME;
                }
                //change direction with compass
                else if (isCompass) {
                    //itemModelProperties code
                    BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                            this.getLodestonePos(worldIn, itemstack.getOrCreateTag()) : this.getWorldSpawnPos(worldIn);

                    if (pointingPos != null) {
                        double y = hit.getLocation().y;
                        boolean up = y % ((int) y) > 0.5d;
                        if (up && te.up) {
                            te.pointToward(pointingPos, true);
                        } else if (!up && te.down) {
                            te.pointToward(pointingPos, false);
                        }
                        if (server) te.setChanged();
                        return ActionResultType.CONSUME;
                    }
                    return ActionResultType.FAIL;
                } else if (CompatHandler.framedblocks && te.framed) {
                    boolean success = FramedSignPost.handleInteraction(te, player, handIn, itemstack, worldIn, pos);
                    if (success) return ActionResultType.CONSUME;
                } else if (isSignPost) {
                    //let sign item handle this one
                    return ActionResultType.PASS;
                }
                // open gui (edit sign with empty hand)
                te.sendOpenTextEditScreenPacket(worldIn, pos, (ServerPlayerEntity) player);

                return ActionResultType.CONSUME;
            }
            return ActionResultType.PASS;
        }
        else{
            return ActionResultType.SUCCESS;
        }
    }


    @Nullable
    private BlockPos getLodestonePos(World world, CompoundNBT cmp) {
        boolean flag = cmp.contains("LodestonePos");
        boolean flag1 = cmp.contains("LodestoneDimension");
        if (flag && flag1) {
            Optional<RegistryKey<World>> optional = CompassItem.getLodestoneDimension(cmp);
            if ( optional.isPresent() && world.dimension() == optional.get() ) {
                return NBTUtil.readBlockPos(cmp.getCompound("LodestonePos"));
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(World world) {
        return world.dimensionType().natural() ? new BlockPos(world.getLevelData().getXSpawn(),
                world.getLevelData().getYSpawn(),world.getLevelData().getZSpawn()) : null;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile)te);
            double y = target.getLocation().y;
            boolean up = y%((int)y) > 0.5d;
            if(up && tile.up){
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
            }
            else if(!up && tile.down){
                return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
            }
            else return new ItemStack(tile.mimic.getBlock());
        }
        return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile) tileentity);
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
        return super.getDrops(state,builder);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        float angle = rot.equals(Rotation.CLOCKWISE_90)? 90 : -90;
        TileEntity te = world.getBlockEntity(pos);
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
                tile.setChanged();
            }
        }
        return state;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SignPostBlockTile();
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}