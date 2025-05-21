package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundRequestOpenCannonGuiMessage;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public interface CannonAccess  {

    CannonBlockTile getCannon();

    void syncToServer(boolean fire, boolean removeOwner);

    Vec3 getCannonPosition();

    void sendOpenGuiRequest();

    void openCannonGui(ServerPlayer player);

    boolean stillValid(Player player);

    void updateClients();

    void playFireEffects();

    void playIgniteEffects();


    class Block implements CannonAccess {
        private final CannonBlockTile cannon;

        public Block(CannonBlockTile cannon) {
            this.cannon = cannon;
        }

        @Override
        public Vec3 getCannonPosition() {
            return cannon.getBlockPos().getCenter();
        }

        @Override
        public CannonBlockTile getCannon() {
            return this.cannon;
        }

        @Override
        public void playFireEffects() {
            Level level = cannon.getLevel();
            //call directly on client. happens 1 tick faster is this needed?
            level.blockEvent(cannon.getBlockPos(), cannon.getBlockState().getBlock(), 1, 0);
        }

        @Override
        public void playIgniteEffects() {
            Level level = cannon.getLevel();
            //call directly on client. happens 1 tick faster is this needed?
            level.blockEvent(cannon.getBlockPos(), cannon.getBlockState().getBlock(), 0, 0);

        }

        @Override
        public void updateClients() {
            var level = cannon.getLevel();
            level.sendBlockUpdated(cannon.getBlockPos(), cannon.getBlockState(), cannon.getBlockState(), 3);
        }

        @Override
        public void syncToServer(boolean fire, boolean removeOwner) {
            NetworkHelper.sendToServer(new ServerBoundSyncCannonPacket(
                    cannon.getYaw(), cannon.getPitch(), cannon.getPowerLevel(),
                    fire, removeOwner, TileOrEntityTarget.of(this.cannon)));
        }

        @Override
        public void sendOpenGuiRequest() {
            NetworkHelper.sendToServer(new ServerBoundRequestOpenCannonGuiMessage(cannon));
        }

        @Override
        public void openCannonGui(ServerPlayer player) {
            this.cannon.tryOpeningEditGui(player, this.cannon.getBlockPos(),
                    player.getMainHandItem(), Direction.UP);
        }

        @Override
        public boolean stillValid(Player player) {
            Level level = player.level();
            float maxDist = 7;
            return !cannon.isRemoved() && level.getBlockEntity(cannon.getBlockPos()) == cannon &&
                    cannon.getBlockPos().distToCenterSqr(player.position()) < maxDist * maxDist;
        }
    }

    static CannonAccess find(Level level, TileOrEntityTarget target) {
        var obj = target.getTarget(level);
        if (obj instanceof CannonBlockTile cannon) {
            return new Block(cannon);
        } else if (obj instanceof CannonAccess cannon) {
            return cannon;
        }
        return null;
    }


    static CannonAccess tile(CannonBlockTile cannonBlockTile) {
        return new Block(cannonBlockTile);
    }

}
