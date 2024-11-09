package net.mehvahdjukaar.supplementaries.mixins.neoforge.self;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = {"net.mehvahdjukaar.supplementaries.common.block.blocks.NetheriteTrapdoorBlock",
        "net.mehvahdjukaar.supplementaries.common.block.blocks.NetheriteDoorBlock"})
public abstract class SelfNetheriteDoorMixin extends Block {
    protected SelfNetheriteDoorMixin(Properties arg) {
        super(arg);
    }

    //break protection
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (CommonConfigs.Building.NETHERITE_DOOR_UNBREAKABLE.get()) {
            if (world.getBlockEntity(pos) instanceof KeyLockableTile tile) {
                if (!tile.getKeyInInventoryStatus(player).isCorrect()) return false;
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }
}
