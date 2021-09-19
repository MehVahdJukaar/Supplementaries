package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }


    @Override
    public ActionResultType place(BlockItemUseContext context) {

        PlayerEntity player = context.getPlayer();
        if (player.abilities.mayBuild || player==null) {
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState state = world.getBlockState(pos);
            BlockProperties.PostType type = RopeKnotBlock.getPostType(state);

            if (type != null) {

                if(RopeKnotBlock.convertToRopeKnot(type, state, world, pos) == null) {
                    return ActionResultType.FAIL;
                }

                ItemStack stack = context.getItemInHand();
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }

                SoundType soundtype =  ModRegistry.ROPE.get().defaultBlockState().getSoundType(world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.abilities.instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                return ActionResultType.sidedSuccess(world.isClientSide);
            }
        }
        return super.place(context);
    }


    //this fixes some stuff
    //@Override
    //protected boolean mustSurvive() {
    //    return false;
    //}
}
