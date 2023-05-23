package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

class PancakeDiscBehavior extends OptionalDispenseItemBehavior {

    @Override
    @NotNull
    protected ItemStack execute(BlockSource source, @NotNull ItemStack stack) {
        Direction dir = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos pos = source.getPos().relative(dir);
        Level level = source.getLevel();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.JUKEBOX) {
            if (level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                ItemStack currentRecord = jukebox.getFirstItem();
                jukebox.setFirstItem(stack.copy());
                return currentRecord;
            }
        }
        return super.execute(source, stack);
    }
}

