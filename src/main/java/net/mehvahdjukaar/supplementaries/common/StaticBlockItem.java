package net.mehvahdjukaar.supplementaries.common;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;

//utility class that contains block item place functions
public class StaticBlockItem {

    @Nullable
    public static BlockState getPlacementState(BlockItemUseContext context, Block block) {
        BlockState blockstate = block.getStateForPlacement(context);
        return blockstate != null && canPlace(context, blockstate) ? blockstate : null;
    }

    public static boolean canPlace(BlockItemUseContext context, BlockState state) {
        PlayerEntity playerentity = context.getPlayer();
        ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.empty() : ISelectionContext.of(playerentity);
        return (state.canSurvive(context.getLevel(), context.getClickedPos())) && context.getLevel().isUnobstructed(state, context.getClickedPos(), iselectioncontext);
    }

    private static BlockState updateBlockStateFromTag(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockState blockstate = state;
        CompoundNBT compoundnbt = stack.getTag();
        if (compoundnbt != null) {
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
            StateContainer<Block, BlockState> statecontainer = state.getBlock().getStateDefinition();

            for(String s : compoundnbt1.getAllKeys()) {
                Property<?> property = statecontainer.getProperty(s);
                if (property != null) {
                    String s1 = compoundnbt1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != state) {
            world.setBlock(pos, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> tProperty, String name) {
        return tProperty.getValue(name).map((p) -> state.setValue(tProperty, p)).orElse(state);
    }

    private static SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    public static ActionResultType place(BlockItemUseContext context, Block blockToPlace) {
        if (!context.canPlace() || context == null) {
            return ActionResultType.FAIL;
        } else {
            BlockState blockstate = getPlacementState(context, blockToPlace);
            if (blockstate == null) {
                return ActionResultType.FAIL;
            } else if (!context.getLevel().setBlock(context.getClickedPos(), blockstate, 11)) {
                return ActionResultType.FAIL;
            } else {
                BlockPos blockpos = context.getClickedPos();
                World world = context.getLevel();
                PlayerEntity playerentity = context.getPlayer();
                ItemStack itemstack = context.getItemInHand();
                BlockState placedState = world.getBlockState(blockpos);
                Block block = placedState.getBlock();
                if (block == blockstate.getBlock()) {
                    placedState = updateBlockStateFromTag(blockpos, world, itemstack, placedState);
                    BlockItem.updateCustomBlockEntityTag(world, playerentity, blockpos, itemstack);
                    block.setPlacedBy(world, blockpos, placedState, playerentity, itemstack);
                    if (playerentity instanceof ServerPlayerEntity) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) playerentity, blockpos, itemstack);
                    }
                }

                SoundType soundtype = placedState.getSoundType(world, blockpos, context.getPlayer());
                world.playSound(playerentity, blockpos, getPlaceSound(placedState, world, blockpos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (playerentity == null || !playerentity.abilities.instabuild) {
                    itemstack.shrink(1);
                }

                return ActionResultType.sidedSuccess(world.isClientSide);
            }

        }
    }
}
