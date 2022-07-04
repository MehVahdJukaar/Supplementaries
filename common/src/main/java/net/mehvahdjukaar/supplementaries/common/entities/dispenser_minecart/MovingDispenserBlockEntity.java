package net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MovingDispenserBlockEntity extends DispenserBlockEntity {

    private final DispenserMinecartEntity minecart;

    protected MovingDispenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, DispenserMinecartEntity entity) {
        super(blockEntityType, pos, state);
        this.minecart = entity;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return minecart.stillValid(pPlayer);
    }
}
