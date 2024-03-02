package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CannonCamera extends Entity {
    private static final List<Player> DISMOUNTED_PLAYERS = new ArrayList<>();


    public CannonCamera(EntityType<CannonCamera> cannonCameraEntityType, Level level) {
        super(cannonCameraEntityType, level);
    }
    public CannonCamera(Level level) {
        this(ModEntities.CANNON_CAMERA.get(), level);
        this.noPhysics = true;
    }

    public CannonCamera(Level level, Player player) {
        this(level);

        double x = player.getX() + 0.5D;
        double y = player.getY() + 0.5D;
        double z = player.getZ() + 0.5D;

        setPos(x, y, z);
        setYRot(player.getYRot());
        setXRot(player.getXRot());
    }


    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
        }
    }

    public static boolean hasRecentlyDismounted(Player player) {
        return DISMOUNTED_PLAYERS.remove(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        discardCamera();
    }

    public void stopViewing(ServerPlayer player) {
        if (!level().isClientSide) {
            discard();
            player.camera = player;
            NetworkHandler.CHANNEL.sendToClientPlayer(player, new SetCameraView(player));
            DISMOUNTED_PLAYERS.add(player);
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isAlwaysTicking() {
        return true;
    }
}
