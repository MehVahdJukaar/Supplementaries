package net.mehvahdjukaar.supplementaries.common.block.tiles;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppPlatformStuff;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.WATCHED;

public class EndermanSkullBlockTile extends SkullBlockEntity {

    private float prevMouthAnim;
    private float mouthAnim;
    //server
    private int watchTime;

    public EndermanSkullBlockTile(BlockPos blockPos, BlockState blockState) {
        super( blockPos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModRegistry.ENDERMAN_SKULL_TILE.get();
    }

    public float getMouthAnimation(float partialTicks) {
        return Mth.lerp(partialTicks, prevMouthAnim, mouthAnim);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EndermanSkullBlockTile tile) {
        if (level.isClientSide) {
            boolean watched = state.getValue(WATCHED);
            tile.prevMouthAnim = tile.mouthAnim;
            tile.mouthAnim = Mth.clamp(tile.mouthAnim + (watched ? 0.5f : -0.5f), 0, 1);
        } else {
            boolean watched = isBeingWatched(level, pos, state);

            if (!watched) {
                tile.watchTime = 0;
            } else {
                tile.watchTime++;
            }
            int wantedPower = Mth.clamp((watched ? 1 + (tile.watchTime / CommonConfigs.Redstone.ENDERMAN_HEAD_INCREMENT.get()) : 0), 0, 15);
            if (state.getValue(WATCHED) != watched || state.getValue(EndermanSkullBlock.POWER) != wantedPower) {
                level.setBlockAndUpdate(pos, state.setValue(WATCHED,
                        watched).setValue(EndermanSkullBlock.POWER, wantedPower));
            }
        }
    }

    public static boolean isBeingWatched(Level level, BlockPos pos, BlockState state) {
        int range = 20;
        List<Player> players = level.getEntitiesOfClass(
                Player.class, new AABB(pos.offset(-range, -range, -range), pos.offset(range, range, range))
        );

        for (Player player : players) {
            ItemStack itemstack = player.getInventory().armor.get(3);
            if (!SuppPlatformStuff.isEndermanMask(null, player, itemstack)) {
                HitResult result = Utils.rayTrace(player, level, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, 64.0);
                if (result instanceof BlockHitResult hit && hit.getBlockPos().equals(pos) &&
                        isLookingAtFace(pos, state, result.getLocation(), hit.getDirection())) {
                    return true;
                }
            }
        }
        return false;
    }


    private static boolean isLookingAtFace(BlockPos pos, BlockState state, Vec3 location, Direction face) {
        if (CommonConfigs.Redstone.ENDERMAN_HEAD_WORKS_FROM_ANY_SIDE.get()) return true;
        if (face.getAxis() == Direction.Axis.Y) return false;
        if (state.hasProperty(WallSkullBlock.FACING)) {
            var f = state.getValue(WallSkullBlock.FACING);
            return f == face;
        } else if (state.hasProperty(SkullBlock.ROTATION)) {
            var r = state.getValue(SkullBlock.ROTATION);
            float angle = r * 22.5f;
            if (angle % 90 == 0) return Direction.fromYRot(angle).getOpposite() == face;
            location = location.subtract(Vec3.atCenterOf(pos));
            var relative = location.yRot(angle * Mth.DEG_TO_RAD);
            if (relative.x < -0.25 || relative.x > 0.25) return false;
            if (relative.z > 0) return false;
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("WatchTime", watchTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.watchTime = tag.getInt("WatchTime");
    }

    @Nullable
    public ResourceLocation getNoteBlockSound() {
        return ModSounds.IMITATE_ENDERMAN.getId();
    }
}
