package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AbstractRopeKnotBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {

        Player player = context.getPlayer();
        if (player == null || Utils.mayPerformBlockAction(player, context.getClickedPos(), context.getItemInHand())) {
            Level world = context.getLevel();

            // #1915: clicking a rope places at the bottom of its chain instead.
            // getClickedPos() is the placement target, not the clicked block, so use the hit result
            BlockPos hitPos = context.getHitResult().getBlockPos();
            BlockState blockHit = world.getBlockState(hitPos);
            if (blockHit.is(ModRegistry.ROPE.get())) {
                BlockPos.MutableBlockPos cursor = hitPos.mutable();
                while (world.getBlockState(cursor.below()).is(ModRegistry.ROPE.get())) {
                    cursor.move(0, -1, 0);
                }
                return super.place(BlockPlaceContext.at(context, cursor.immutable(), Direction.UP));
            }

            BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
            BlockState state = world.getBlockState(pos);
            ModBlockProperties.PostType type = ModBlockProperties.PostType.get(state);

            if (type != null) {
                ItemStack stack = context.getItemInHand();

                if (AbstractRopeKnotBlock.convertToRopeKnot(type, state, world, pos) == null) {
                    return InteractionResult.FAIL;
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
                }

                SoundType soundtype = ModRegistry.ROPE.get().defaultBlockState().getSoundType();
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
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
