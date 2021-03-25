package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.FenceSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.FenceSignGui;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


import net.minecraft.block.AbstractBlock.Properties;

public class FenceSignBlock extends FenceMimicBlock{

    public FenceSignBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof FenceSignBlockTile) {
            FenceSignBlockTile te = (FenceSignBlockTile) tileentity;
            ItemStack itemstack = player.getItemInHand(handIn);
            Item item = itemstack.getItem();

            boolean server = !worldIn.isClientSide();
            boolean isDye = item instanceof DyeItem && player.abilities.mayBuild;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.setChanged();
                }
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                FenceSignGui.open(te);
            }
            return ActionResultType.sidedSuccess(worldIn.isClientSide);
        }
        return ActionResultType.PASS;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof FenceSignBlockTile){
            FenceSignBlockTile tile = ((FenceSignBlockTile)te);
            double y = target.getLocation().y%1;
            if(y<0.8&&y>0.4){
                return new ItemStack(tile.signBlock.getBlock());
            }
            else return new ItemStack(tile.fenceBlock.getBlock());
        }
        return new ItemStack(Registry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
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
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof FenceSignBlockTile) {
            FenceSignBlockTile tile = (FenceSignBlockTile) te;

            tile.signFacing = rot.rotate(tile.signFacing);
            tile.setChanged();
        }
        return state;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FenceSignBlockTile();
    }

}