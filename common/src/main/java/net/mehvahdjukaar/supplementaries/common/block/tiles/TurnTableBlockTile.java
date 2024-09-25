package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TurnTableBlock;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


//TODO: improve this
public class TurnTableBlockTile extends BlockEntity {
    private int cooldown = 5;
    private boolean canRotate = true;
    // private long tickedGameTime;
    private int catTimer = 0;

    public TurnTableBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TURN_TABLE_TILE.get(), pos, state);
    }

    public void tryRotate() {
        this.canRotate = true;
        //updates correct cooldown
        this.cooldown = TurnTableBlock.getPeriod(this.getBlockState());
        // allows for a rotation try nedxt period
    }

    public int getCatTimer() {
        return catTimer;
    }

    public void setCat() {
        this.catTimer = 20 * 20;
    }

    //server only
    public static void tick(Level level, BlockPos pos, BlockState state, TurnTableBlockTile tile) {
        tile.catTimer = Math.max(tile.catTimer - 1, 0);
        // cd > 0
        if (tile.cooldown == 0) {
            Direction dir = state.getValue(TurnTableBlock.FACING);
            boolean ccw = state.getValue(TurnTableBlock.INVERTED) ^ (state.getValue(TurnTableBlock.FACING) == Direction.DOWN);
            BlockPos targetPos = pos.relative(dir);
            boolean success = BlockUtil.tryRotatingBlock(dir, ccw, targetPos, level, null).isPresent();
            if (success) {
                //play particle with block event
                level.blockEvent(pos, state.getBlock(), 0, 0);
                level.gameEvent(null, GameEvent.BLOCK_CHANGE, targetPos);
                level.playSound(null, targetPos, ModSounds.BLOCK_ROTATE.get(), SoundSource.BLOCKS, 1.0F, 1);

                if (dir == Direction.UP) {
                    tile.tryRotatingMinecartsAbove(level, pos, ccw);
                }
            }

            tile.cooldown = TurnTableBlock.getPeriod(state);
            // if it didn't rotate last block that means that block is immovable
            int power = state.getValue(TurnTableBlock.POWER);
            tile.canRotate = (success && power != 0);
            //change blockstate after rotation if is powered off
            if (power == 0) {
                level.setBlock(pos, state.setValue(TurnTableBlock.ROTATING, false), 3);
            }
        } else if (tile.canRotate) {
            tile.cooldown--;
        }
    }

    private void tryRotatingMinecartsAbove(Level level, BlockPos pos, boolean ccw) {
        BlockPos above = pos.above();
        BlockState state = level.getBlockState(above);
        if (state.getBlock() instanceof BaseRailBlock rb) {
            for (var c : level.getEntitiesOfClass(AbstractMinecart.class, new AABB(above))) {
                RailShape shape = ForgeHelper.getRailDirection(rb, state, level, above, c);
                Direction.Axis axis = getRailAxis(shape);
                if (axis == Direction.Axis.Y) continue;
                //if (axis == getMinecartMovementAxis(c)) continue;
                //this whole thing wont work properly as cart can be on a to be rotated rail before its rotated.
                //onceit touches it it will rotate as per the default cart logic so such a mechanism just is not possible...
                c.setYRot(c.getYRot() + (ccw ? 90 : -90));
                c.yRotO = c.getYRot();
                c.setPos(new Vec3(pos.getX() + 0.5f, c.getY(), pos.getZ() + 0.5));
                c.xOld = c.getX();
                c.zOld = c.getZ();

                var movement = c.getDeltaMovement();
                c.setDeltaMovement(movement.yRot(ccw ? Mth.HALF_PI : -Mth.HALF_PI));
            }
        }
    }

    private static Direction.Axis getMinecartMovementAxis(AbstractMinecart m) {
        Vec3 dir = Vec3.directionFromRotation(0, m.getYRot());
        return Direction.getNearest(dir.x, dir.y, dir.z).getAxis();
    }

    private static Direction.Axis getRailAxis(RailShape rail) {
        return switch (rail) {
            case EAST_WEST, ASCENDING_WEST, ASCENDING_EAST -> Direction.Axis.X;
            case NORTH_SOUTH, ASCENDING_SOUTH, ASCENDING_NORTH -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
    }

    //TODO: this makes block instantly rotate when condition becomes true


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cooldown = tag.getInt("Cooldown");
        this.canRotate = tag.getBoolean("CanRotate");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Cooldown", this.cooldown);
        tag.putBoolean("CanRotate", this.canRotate);
    }

}