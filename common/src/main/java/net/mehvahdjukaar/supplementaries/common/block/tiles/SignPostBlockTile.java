package net.mehvahdjukaar.supplementaries.common.block.tiles;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class SignPostBlockTile extends MimicBlockTile implements ITextHolderProvider, IOwnerProtected, IExtraModelDataProvider {

    private UUID owner = null;

    //is holding a framed fence (for framed blocks mod compat)
    public boolean framed = false;
    public static final ModelDataKey<Boolean> FRAMED = BlockProperties.FRAMED;

    public TextHolder textHolder;

    public float yawUp = 0;
    public float yawDown = 0;
    public boolean leftUp = true;
    public boolean leftDown = false;
    public boolean up = false;
    public boolean down = false;

    public boolean isSlim = false;

    @NotNull
    public WoodType woodTypeUp = WoodTypeRegistry.OAK_TYPE;
    @NotNull
    public WoodType woodTypeDown = WoodTypeRegistry.OAK_TYPE;

    public SignPostBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SIGN_POST_TILE.get(), pos, state);
        this.textHolder = new TextHolder(2);
    }

    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FRAMED, this.framed)
                .with(MIMIC, this.getHeldBlock())
                .build();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.isSlim = this.mimic.getBlock() instanceof StickBlock;
    }

    @Override
    public TextHolder getTextHolder() {
        return this.textHolder;
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos().offset(-0.25, 0, -0.25), this.getBlockPos().offset(1.25, 1, 1.25));
    }

    //TODO: maybe add constraints to this so it snaps to 22.5deg
    public void pointToward(BlockPos targetPos, boolean up) {
        //int r = MathHelper.floor((double) ((180.0F + yaw) * 16.0F / 360.0F) + 0.5D) & 15;
        // r*-22.5f;
        float yaw = (float) (Math.atan2(targetPos.getX() - worldPosition.getX(), targetPos.getZ() - worldPosition.getZ()) * 180d / Math.PI);
        if (up) {
            this.yawUp = Mth.wrapDegrees(yaw - (this.leftUp ? 180 : 0));
        } else {
            this.yawDown = Mth.wrapDegrees(yaw - (this.leftDown ? 180 : 0));
        }
    }

    public float getPointingYaw(boolean up) {
        if (up) {
            return Mth.wrapDegrees(-this.yawUp - (this.leftUp ? 180 : 0));
        } else {
            return Mth.wrapDegrees(-this.yawDown - (this.leftDown ? 180 : 0));
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.framed = compound.getBoolean("Framed");

        this.textHolder.read(compound);

        this.yawUp = compound.getFloat("YawUp");
        this.yawDown = compound.getFloat("YawDown");
        this.leftUp = compound.getBoolean("LeftUp");
        this.leftDown = compound.getBoolean("LeftDown");
        this.up = compound.getBoolean("Up");
        this.down = compound.getBoolean("Down");
        this.woodTypeUp = WoodTypeRegistry.fromNBT(compound.getString("TypeUp"));
        this.woodTypeDown = WoodTypeRegistry.fromNBT(compound.getString("TypeDown"));

        this.loadOwner(compound);
        this.isSlim = this.mimic.getBlock() instanceof StickBlock;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("Framed", this.framed);

        this.textHolder.write(compound);

        compound.putFloat("YawUp", this.yawUp);
        compound.putFloat("YawDown", this.yawDown);
        compound.putBoolean("LeftUp", this.leftUp);
        compound.putBoolean("LeftDown", this.leftDown);
        compound.putBoolean("Up", this.up);
        compound.putBoolean("Down", this.down);
        compound.putString("TypeUp", this.woodTypeUp.toString());
        compound.putString("TypeDown", this.woodTypeDown.toString());
        this.saveOwner(compound);
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean rotateSign(boolean up, float angle, boolean constrainAngle) {
        if (up && this.up) {
            this.yawUp = Mth.wrapDegrees(this.yawUp + angle);
            if (constrainAngle) this.yawUp -= this.yawUp % 22.5f;
            return true;
        } else if (this.down) {
            this.yawDown = Mth.wrapDegrees(this.yawDown + angle);
            if (constrainAngle)
                this.yawDown -= this.yawDown % 22.5f;
            return true;
        }
        return false;
    }

    //TODO: add antique ink cap also
    /*
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        //if(cap == CapabilityHandler.ANTIQUE_TEXT_CAP) return LazyOptional.of(()->this.textHolder);
        return super.getCapability(cap, side);
    }*/

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        SignPostGui.open(this);
    }
}