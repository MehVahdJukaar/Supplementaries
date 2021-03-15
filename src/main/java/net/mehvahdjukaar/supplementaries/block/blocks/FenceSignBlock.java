package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.FenceSignBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.FenceSignGui;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class FenceSignBlock extends FenceMimicBlock{

    public FenceSignBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof FenceSignBlockTile) {
            FenceSignBlockTile te = (FenceSignBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            Item item = itemstack.getItem();

            boolean server = !worldIn.isRemote();
            boolean isDye = item instanceof DyeItem && player.abilities.allowEdit;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.markDirty();
                }
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                FenceSignGui.open(te);
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof FenceSignBlockTile){
            FenceSignBlockTile tile = ((FenceSignBlockTile)te);
            double y = target.getHitVec().y%1;
            if(y<0.8&&y>0.4){
                return new ItemStack(tile.signBlock.getBlock());
            }
            else return new ItemStack(tile.fenceBlock.getBlock());
        }
        return new ItemStack(Registry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof FenceSignBlockTile){
            FenceSignBlockTile tile = ((FenceSignBlockTile) tileentity);
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.fenceBlock.getBlock()));
            list.add(new ItemStack(tile.signBlock.getBlock()));

            return list;
        }
        return super.getDrops(state,builder);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof FenceSignBlockTile) {
            FenceSignBlockTile tile = (FenceSignBlockTile) te;

            tile.signFacing = rot.rotate(tile.signFacing);
            tile.markDirty();
        }
        return state;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FenceSignBlockTile();
    }

}