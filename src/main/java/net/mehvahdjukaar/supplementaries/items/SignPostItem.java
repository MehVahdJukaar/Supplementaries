package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
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

public class SignPostItem  extends Item {
    public final IWoodType type;
    public SignPostItem(Properties properties, IWoodType wood) {
        super(properties);
        type = wood;
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return 100;
    }

    private boolean isFence(Block b){
        ResourceLocation res = b.getRegistryName();
        if(res.getNamespace().equals("blockcarpentry"))return false;
        return (b.is(ModTags.POSTS));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        //if (!context.canPlace()) return ActionResultType.FAIL;

        PlayerEntity playerentity = context.getPlayer();
        if(playerentity == null)return ActionResultType.PASS;
        BlockPos blockpos = context.getClickedPos();
        World world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();

        Block targetblock = world.getBlockState(blockpos).getBlock();

        boolean framed = false;

        boolean isfence = isFence(targetblock);
        boolean issignpost = targetblock instanceof SignPostBlock;
        if(isfence || issignpost){

            //if(!world.isRemote) world.setBlockState(blockpos, Registry.SIGN_POST.get().getDefaultState(), 3);

            if(CompatHandler.framedblocks){
                Block f = FramedSignPost.tryGettingFramedBlock(targetblock,world,blockpos);
                if(f != null){
                    framed = true;
                    if(f != Blocks.AIR)targetblock = f;
                }
            }

            boolean waterlogged = world.getFluidState(blockpos).getType() == Fluids.WATER;
            world.setBlock(blockpos, Registry.SIGN_POST.get()
                    .getStateForPlacement(new BlockItemUseContext(context)).setValue(SignPostBlock.WATERLOGGED, waterlogged), 3);

            boolean flag = false;

            TileEntity tileentity = world.getBlockEntity(blockpos);
            if(tileentity instanceof SignPostBlockTile){
                SignPostBlockTile signtile = ((SignPostBlockTile) tileentity);



                int r = MathHelper.floor((double) ((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15;

                double y = context.getClickLocation().y;

                boolean up = y%((int)y) > 0.5d;

                if(up){
                    if(signtile.up != up){
                        signtile.up = true;
                        signtile.woodTypeUp = this.type;
                        signtile.yawUp = 90 + r*-22.5f;
                        flag = true;
                    }
                }
                else if(signtile.down == up){
                    signtile.down = true;
                    signtile.woodTypeDown = this.type;
                    signtile.yawDown = 90 + r*-22.5f;
                    flag = true;
                }
                if(flag) {
                    if (isfence) signtile.mimic = targetblock.defaultBlockState();
                    signtile.framed = framed;
                    signtile.setChanged();
                }

            }
            if(flag){
                if(world.isClientSide()){
                    SoundType soundtype = SoundType.WOOD;
                    world.playSound(playerentity, blockpos, SoundEvents.WOOD_PLACE, SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                }
                if(!context.getPlayer().isCreative()) itemstack.shrink(1);
                return ActionResultType.SUCCESS;
            }


        }
        return ActionResultType.PASS;
    }
}