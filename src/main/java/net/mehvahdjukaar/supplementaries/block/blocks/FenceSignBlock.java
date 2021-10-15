package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.FenceSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.FenceSignGui;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class FenceSignBlock extends FenceMimicBlock{

    public FenceSignBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                             BlockHitResult hit) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
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
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockEntity te = world.getBlockEntity(pos);
        if(te instanceof FenceSignBlockTile){
            FenceSignBlockTile tile = ((FenceSignBlockTile)te);
            double y = target.getLocation().y%1;
            if(y<0.8&&y>0.4){
                return new ItemStack(tile.signBlock.getBlock());
            }
            else return new ItemStack(tile.fenceBlock.getBlock());
        }
        return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity tileentity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
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
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof FenceSignBlockTile) {
            FenceSignBlockTile tile = (FenceSignBlockTile) te;

            tile.signFacing = rot.rotate(tile.signFacing);
            tile.setChanged();
        }
        return state;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new FenceSignBlockTile();
    }

}