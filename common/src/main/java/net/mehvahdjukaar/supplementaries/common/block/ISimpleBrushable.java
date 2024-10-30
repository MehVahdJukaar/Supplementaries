package net.mehvahdjukaar.supplementaries.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface ISimpleBrushable {

    // true if it should cancel the sound and particles
    boolean brush(BlockState state, BlockPos pos, Level level, ItemStack stack, Player livingEntity,
                  HumanoidArm arm, BlockHitResult hit, Vec3 particlesDir);
}
