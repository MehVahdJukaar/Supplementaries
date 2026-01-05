package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.utils.fake_level.IEntityInterceptFakeLevel;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TntBehavior extends GenericProjectileBehavior {

    @Override
    public @Nullable Entity createEntity(ItemStack projectile, IEntityInterceptFakeLevel fakeLevel, Vec3 facing) {
        if (projectile.isEmpty()) return null;
        if (projectile.getItem() instanceof BlockItem bi) {
            BlockState tntState = bi.getBlock().defaultBlockState();
            BlockPos pos = BlockPos.ZERO;
            Level level = (Level) fakeLevel;
            level.setBlock(pos, tntState, Block.UPDATE_NONE);
            igniteTntHack(tntState, level, pos);
            var e = fakeLevel.getIntercepted();
            if (e != null) e.setDeltaMovement(0, 1, 0);
            return e;
        }
        return null;
    }

    public static boolean isTNTLikeBlock(BlockState tntState) {
        return tntState.is(ModTags.CANNON_TNTS) || tntState.getBlock() instanceof TntBlock;
    }

    public static void igniteTntHack(BlockState tntState, Level level, BlockPos pos) {
        Arrow dummyArrow = new Arrow(level, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5);
        dummyArrow.setRemainingFireTicks(20);
        tntState.onProjectileHit(level, tntState,
                new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, pos, true),
                dummyArrow);
    }


}
