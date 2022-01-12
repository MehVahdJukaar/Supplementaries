package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SignPostBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.utils.ModTags;
import net.mehvahdjukaar.supplementaries.datagen.types.IWoodType;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.framedblocks.FramedSignPost;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;

public class SignPostItem extends Item {
    public final IWoodType type;

    public SignPostItem(Properties properties, IWoodType wood) {
        super(properties);
        type = wood;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return type.canBurn() ? 100 : 0;
    }

    private boolean isFence(Block b) {
        ResourceLocation res = b.getRegistryName();
        if (res.getNamespace().equals("blockcarpentry")) return false;
        return ModTags.POSTS.contains(b);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //if (!context.canPlace()) return ActionResultType.FAIL;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos blockpos = context.getClickedPos();
        Level world = context.getLevel();
        ItemStack itemstack = context.getItemInHand();

        BlockState state = world.getBlockState(blockpos);
        Block targetBlock = state.getBlock();

        boolean framed = false;

        boolean isFence = isFence(targetBlock);
        boolean isSignPost = targetBlock instanceof SignPostBlock;
        if (isFence || isSignPost) {

            //if(!world.isRemote) world.setBlockState(blockpos, Registry.SIGN_POST.get().getDefaultState(), 3);

            if (CompatHandler.framedblocks) {
                Block f = FramedSignPost.tryGettingFramedBlock(targetBlock, world, blockpos);
                if (f != null) {
                    framed = true;
                    if (f != Blocks.AIR) targetBlock = f;
                }
            }

            boolean waterlogged = world.getFluidState(blockpos).getType() == Fluids.WATER;
            if (!isSignPost) {
                world.setBlock(blockpos, ModRegistry.SIGN_POST.get()
                        .getStateForPlacement(new BlockPlaceContext(context)).setValue(SignPostBlock.WATERLOGGED, waterlogged), 3);
            }
            boolean flag = false;

            if (world.getBlockEntity(blockpos) instanceof SignPostBlockTile tile) {

                BlockUtils.addOptionalOwnership(player, tile);

                int r = Mth.floor((double) ((180.0F + context.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15;

                double y = context.getClickLocation().y - (double) blockpos.getY();

                boolean up = y > 0.5d;

                if (up) {
                    if (tile.up != up) {
                        tile.up = true;
                        tile.woodTypeUp = this.type;
                        tile.yawUp = 90 + r * -22.5f;
                        flag = true;
                    }
                } else if (tile.down == up) {
                    tile.down = true;
                    tile.woodTypeDown = this.type;
                    tile.yawDown = 90 + r * -22.5f;
                    flag = true;
                }
                if (flag) {
                    if (isFence) tile.mimic = targetBlock.defaultBlockState();
                    tile.framed = framed;
                    tile.setChanged();
                    world.sendBlockUpdated(blockpos, state, state, 3);
                }

            }
            if (flag) {

                SoundType soundtype = SoundType.WOOD;
                world.playSound(null, blockpos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!context.getPlayer().isCreative()) itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}