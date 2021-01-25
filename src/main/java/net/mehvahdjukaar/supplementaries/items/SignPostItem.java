package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;


public class SignPostItem  extends Item {
    public SignPostItem(Properties properties) {
        super(properties);
    }


    private boolean isFence(Block b){
        ResourceLocation res = b.getRegistryName();
        if(res.getNamespace().equals("blockcarpentry"))return false;
        return (b instanceof FenceBlock || ServerConfigs.cached.SIGN_POST_ADDITIONAL.contains(res.toString())
                || (Tags.Blocks.FENCES!=null && b.isIn(Tags.Blocks.FENCES)));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        //if (!context.canPlace()) return ActionResultType.FAIL;

        PlayerEntity playerentity = context.getPlayer();
        if(playerentity == null)return ActionResultType.PASS;
        BlockPos blockpos = context.getPos();
        World world = context.getWorld();
        ItemStack itemstack = context.getItem();

        Block targetblock = world.getBlockState(blockpos).getBlock();

        boolean isfence = isFence(targetblock);
        boolean issignpost = targetblock instanceof SignPostBlock;
        if(isfence || issignpost){

            //if(!world.isRemote) world.setBlockState(blockpos, Registry.SIGN_POST.get().getDefaultState(), 3);

            world.setBlockState(blockpos, Registry.SIGN_POST.get().getStateForPlacement(new BlockItemUseContext(context)), 3);

            boolean flag = false;

            TileEntity tileentity = world.getTileEntity(blockpos);
            if(tileentity instanceof SignPostBlockTile){
                SignPostBlockTile signtile = ((SignPostBlockTile) tileentity);


                int r = MathHelper.floor((double) ((180.0F + context.getPlacementYaw()) * 16.0F / 360.0F) + 0.5D) & 15;

                double y = context.getHitVec().y;

                boolean up = y%((int)y) > 0.5d;

                if(up){
                    if(signtile.up != up){
                        signtile.up = true;
                        signtile.woodTypeUp = CommonUtil.getWoodTypeFromSignPostItem(this.getItem());
                        signtile.yawUp = 90 + r*-22.5f;
                        flag = true;
                    }
                }
                else if(signtile.down == up){
                    signtile.down = true;
                    signtile.woodTypeDown = CommonUtil.getWoodTypeFromSignPostItem(this.getItem());
                    signtile.yawDown = 90 + r*-22.5f;
                    flag = true;
                }
                if(flag) {
                    if (isfence) signtile.fenceBlock = targetblock.getDefaultState();
                    signtile.markDirty();
                }

            }
            if(flag){
                if(world.isRemote()){
                    SoundType soundtype = SoundType.WOOD;
                    world.playSound(playerentity, blockpos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                }
                if(!context.getPlayer().isCreative()) itemstack.shrink(1);
                return ActionResultType.SUCCESS;
            }


        }
        return ActionResultType.PASS;
    }
}