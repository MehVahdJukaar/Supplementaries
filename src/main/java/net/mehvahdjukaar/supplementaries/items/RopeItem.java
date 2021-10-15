package net.mehvahdjukaar.supplementaries.items;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item.Properties;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }


    @Override
    public InteractionResult place(BlockPlaceContext context) {

        Player player = context.getPlayer();
        if (player.abilities.mayBuild || player==null) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState state = world.getBlockState(pos);
            BlockProperties.PostType type = RopeKnotBlock.getPostType(state);

            if (type != null) {

                if(RopeKnotBlock.convertToRopeKnot(type, state, world, pos) == null) {
                    return InteractionResult.FAIL;
                }

                ItemStack stack = context.getItemInHand();
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }

                SoundType soundtype =  ModRegistry.ROPE.get().defaultBlockState().getSoundType(world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.abilities.instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
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
