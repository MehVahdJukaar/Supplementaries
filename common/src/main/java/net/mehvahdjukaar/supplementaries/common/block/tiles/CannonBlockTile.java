package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.client.CannonCameraController;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CannonBlockTile extends OpeneableContainerBlockEntity {

    private float pitch = 0;
    private float prevPitch = 0;
    private float yaw = 0;
    private float prevYaw = 0;
    private float explosionTimer = 0;
    private float firePower = 0;

    private boolean controlledByPlayer = true;

    public CannonBlockTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CANNON_TILE.get(), pos, blockState, 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("yaw", this.yaw);
        tag.putFloat("pitch", this.pitch);
        tag.putFloat("explosion_timer", this.explosionTimer);
        tag.putFloat("fire_power", this.firePower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("yaw");
        this.pitch = tag.getFloat("pitch");
        this.explosionTimer = tag.getFloat("explosion_timer");
        this.firePower = tag.getFloat("fire_power");
    }

    public float getYaw(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevYaw, this.yaw);
    }

    public float getPitch(float partialTicks) {
        return Mth.lerp(partialTicks, this.prevPitch, this.pitch);
    }

    public void addRotation(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
        this.pitch = Mth.clamp(this.pitch, -80, 0);
    }

    public void setPitch(float angle) {
        this.pitch = angle;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.cannon");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return null;
    }

    @Override
    protected void updateBlockState(BlockState state, boolean b) {
    }

    @Override
    protected void playOpenSound(BlockState state) {
    }

    @Override
    protected void playCloseSound(BlockState state) {
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockTile t) {
        t.prevYaw = t.yaw;
        t.prevPitch = t.pitch;
    }


    public void use(Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                //  startControlling(serverPlayer);
            } else CannonCameraController.activateCannonCamera(worldPosition);
        }
    }

}
