package net.mehvahdjukaar.supplementaries.common.block.tiles;


import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.screens.SignPostGui;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class SignPostBlockTile extends MimicBlockTile implements ITextHolderProvider, IOwnerProtected, IExtraModelDataProvider {

    public static final ModelDataKey<Boolean> FRAMED = ModBlockProperties.FRAMED;

    private final TextHolder textHolder;
    private final Sign signUp = new Sign(false, true, 0, WoodTypeRegistry.OAK_TYPE);
    private final Sign signDown = new Sign(false, false, 0, WoodTypeRegistry.OAK_TYPE);
    private UUID owner = null;
    private boolean isSlim = false;

    //is holding a framed fence (for framed blocks mod compat)
    private boolean framed = false;

    public SignPostBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SIGN_POST_TILE.get(), pos, state);
        this.textHolder = new TextHolder(2, 90);
    }

    //TODO: add fence mimic block
    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FRAMED, this.framed)
                .with(MIMIC, this.getHeldBlock())
                .build();
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

    public void pointToward(BlockPos targetPos, boolean up) {
        float yaw = (float) (Math.atan2(targetPos.getX() - (double) worldPosition.getX(),
                targetPos.getZ() - (double) worldPosition.getZ()) * 180d / Math.PI);
        if (up) {
            this.signUp.setYaw(yaw);
        } else {
            this.signDown.setYaw(yaw);
        }
    }

    public float getPointingYaw(boolean up) {
        if (up) {
            return this.signUp.getPointing();
        } else {
            return this.signDown.getPointing();
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.framed = compound.getBoolean("Framed");

        this.textHolder.load(compound);

        //TODO: remove. backward compat
        if (compound.contains("YawUp")) {
            this.signUp.yaw = compound.getFloat("YawUp");
            this.signDown.yaw = compound.getFloat("YawDown");
            this.signUp.left = compound.getBoolean("LeftUp");
            this.signDown.left = compound.getBoolean("LeftDown");
            this.signUp.active = compound.getBoolean("Up");
            this.signDown.active = compound.getBoolean("Down");
            this.signUp.woodType = WoodTypeRegistry.fromNBT(compound.getString("TypeUp"));
            this.signDown.woodType = WoodTypeRegistry.fromNBT(compound.getString("TypeDown"));
        } else {
            this.signUp.load(compound.getCompound("SignUp"));
            this.signUp.load(compound.getCompound("SignDown"));
        }
        this.loadOwner(compound);
        this.isSlim = this.mimic.getBlock() instanceof StickBlock;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("Framed", this.framed);
        this.textHolder.save(compound);
        compound.put("SignUp", this.signUp.save());
        compound.put("SignDown", this.signUp.save());
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
        if (up && this.signUp.active) {
            this.signUp.rotateBy(angle, constrainAngle);
            return true;
        } else if (this.signDown.active) {
            this.signDown.rotateBy(angle, constrainAngle);
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

    public boolean isSlim() {
        return isSlim;
    }

    public Sign getSignUp() {
        return signUp;
    }

    public Sign getSignDown() {
        return signDown;
    }

    public Sign getSign(boolean up) {
        return up ? getSignUp() : getSignDown();
    }

    public static final class Sign {
        private boolean active;
        private boolean left;
        private float yaw;
        private WoodType woodType;

        private Sign(boolean active, boolean left, float yaw, WoodType woodType) {
            this.active = active;
            this.left = left;
            this.yaw = yaw;
            this.woodType = woodType;
        }

        public void load(CompoundTag compound) {
            this.active = compound.getBoolean("Active");
            this.left = compound.getBoolean("Left");
            this.yaw = compound.getFloat("Yaw");
            this.woodType = WoodTypeRegistry.fromNBT(compound.getString("WoodType"));
        }

        public CompoundTag save() {
            CompoundTag compound = new CompoundTag();
            compound.putFloat("Yaw", this.yaw);
            compound.putBoolean("Left", this.left);
            compound.putBoolean("Active", this.active);
            compound.putString("WoodType", this.woodType.toString());
            return compound;
        }

        private float getPointing() {
            return Mth.wrapDegrees(-this.yaw - (this.left ? 180 : 0));
        }

        private void setYaw(float yaw) {
            this.yaw = Mth.wrapDegrees(yaw - (this.left ? 180 : 0));
        }

        private void rotateBy(float angle, boolean constrainAngle) {
            this.yaw = Mth.wrapDegrees(this.yaw + angle);
            if (constrainAngle) this.yaw -= this.yaw % 22.5f;
        }

        public boolean active() {
            return active;
        }

        public boolean left() {
            return left;
        }

        public float yaw() {
            return yaw;
        }

        public WoodType woodType() {
            return woodType;
        }
    }

    public boolean initializeSignAfterConversion(WoodType woodType, int r, boolean up,
                                                 boolean slim, boolean framed) {
        var sign = getSign(up);
        if (!sign.active) {
            sign.active = true;
            sign.woodType = woodType;
            sign.yaw = 90 + r * -22.5f;

            this.framed = framed;
            this.isSlim = slim;
            return true;
        }
        return false;
    }
}

