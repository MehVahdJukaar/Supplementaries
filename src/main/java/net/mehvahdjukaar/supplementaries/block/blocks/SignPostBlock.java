package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SignPostBlock extends FenceMimicBlock{

    public SignPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //TODO: add flip sound here
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                             BlockHitResult hit) {
        if(hit.getDirection().getAxis() == Direction.Axis.Y) return InteractionResult.PASS;

        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SignPostBlockTile && ((IOwnerProtected) tileentity).isAccessibleBy(player)) {
            SignPostBlockTile te = (SignPostBlockTile) tileentity;
            ItemStack itemstack = player.getItemInHand(handIn);
            Item item = itemstack.getItem();

            //put post on map
            if(item instanceof MapItem){
                if(!worldIn.isClientSide){
                    MapItemSavedData data = MapItem.getOrCreateSavedData(itemstack,worldIn);
                    if(data instanceof CustomDecorationHolder) {
                        ((CustomDecorationHolder) data).toggleCustomDecoration(worldIn, pos);
                    }
                }
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }


            boolean server = !worldIn.isClientSide();
            boolean emptyhand = itemstack.isEmpty();
            boolean isDye = item instanceof DyeItem && player.abilities.mayBuild;
            boolean isSneaking = player.isShiftKeyDown() && emptyhand;
            boolean isSignPost = item instanceof SignPostItem;
            boolean isCompass = item instanceof CompassItem;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.setChanged();
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
                return InteractionResult.FAIL;
            }
            //sneak right click rotates the sign on z axis
            else if (isSneaking){
                double y = hit.getLocation().y;
                boolean up = y%((int)y) > 0.5d;
                if(up){
                    te.leftUp = !te.leftUp;
                }
                else{
                    te.leftDown = !te.leftDown;
                }
                if(server)te.setChanged();
                worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
            //change direction with compass
            else if (isCompass){
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                        this.getLodestonePos(worldIn, itemstack.getOrCreateTag()) : this.getWorldSpawnPos(worldIn);

                if(pointingPos!=null) {
                    double y = hit.getLocation().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up && te.up) {
                        te.pointToward(pointingPos,true);
                    } else if (!up && te.down) {
                        te.pointToward(pointingPos,false);
                    }
                    if(server)te.setChanged();
                    return InteractionResult.sidedSuccess(worldIn.isClientSide);
                }
                return InteractionResult.FAIL;
            }
            else if (CompatHandler.framedblocks && te.framed){
                boolean success = FramedSignPost.handleInteraction(te, player, handIn, itemstack, worldIn, pos);
                if(success)return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
            else if (isSignPost){
                //let sign item handle this one
                return InteractionResult.PASS;
            }
            // open gui (edit sign with empty hand)
            if (!server) {
                SignPostGui.open(te);
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        return InteractionResult.PASS;
    }


    @Nullable
    private BlockPos getLodestonePos(Level world, CompoundTag cmp) {
        boolean flag = cmp.contains("LodestonePos");
        boolean flag1 = cmp.contains("LodestoneDimension");
        if (flag && flag1) {
            Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(cmp);
            if ( optional.isPresent() && world.dimension() == optional.get() ) {
                return NbtUtils.readBlockPos(cmp.getCompound("LodestonePos"));
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(Level world) {
        return world.dimensionType().natural() ? new BlockPos(world.getLevelData().getXSpawn(),
                world.getLevelData().getYSpawn(),world.getLevelData().getZSpawn()) : null;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity te = world.getBlockEntity(pos);
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
        BlockEntity tileentity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
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
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        float angle = rot.equals(Rotation.CLOCKWISE_90)? 90 : -90;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof SignPostBlockTile) {
            SignPostBlockTile tile = (SignPostBlockTile) te;
            boolean success = false;
            if(tile.up){
                tile.yawUp= Mth.wrapDegrees(tile.yawUp+angle);
                success=true;
            }
            if(tile.down){
                tile.yawDown= Mth.wrapDegrees(tile.yawDown+angle);
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
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new SignPostBlockTile();
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}