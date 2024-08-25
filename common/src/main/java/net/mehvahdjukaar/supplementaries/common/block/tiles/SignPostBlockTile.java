package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.supplementaries.client.screens.SignPostScreen;
import net.mehvahdjukaar.supplementaries.common.block.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.TextHolder;
import net.mehvahdjukaar.supplementaries.common.block.blocks.StickBlock;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;


public class SignPostBlockTile extends MimicBlockTile implements ITextHolderProvider, IOwnerProtected, IScreenProvider {

    public static final ModelDataKey<Boolean> FRAMED_KEY = ModBlockProperties.FRAMED;
    public static final ModelDataKey<Boolean> SLIM_KEY = ModBlockProperties.SLIM;
    public static final ModelDataKey<Sign> SIGN_UP_KEY = ModBlockProperties.SIGN_UP;
    public static final ModelDataKey<Sign> SIGN_DOWN_KEY = ModBlockProperties.SIGN_DOWN;

    private final Sign signUp = new Sign(false, true, 0, WoodTypeRegistry.OAK_TYPE);
    private final Sign signDown = new Sign(false, false, 0, WoodTypeRegistry.OAK_TYPE);
    private boolean isWaxed = false;
    private UUID owner = null;
    private boolean isSlim = false;

    //is holding a framed fence (for framed blocks mod compat)
    private boolean framed = false;
    @Nullable
    private UUID playerWhoMayEdit;

    public SignPostBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SIGN_POST_TILE.get(), pos, state);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        super.addExtraModelData(builder);
        builder.with(FRAMED_KEY, this.framed)
                .with(SIGN_UP_KEY, this.signUp)
                .with(SIGN_DOWN_KEY, this.signDown)
                .with(SLIM_KEY, this.isSlim);
    }

    @Override
    public TextHolder getTextHolder(int i) {
        return getSign(i == 0).text;
    }

    @Override
    public TextHolder getTextHolderAt(Vec3 hit) {
        return getClickedSign(hit).text;
    }

    @Override
    public int textHoldersCount() {
        return 2;
    }

    @ForgeOverride
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.getBlockPos();
        return new AABB(pos.getX() - 0.25, pos.getY(), pos.getZ() - 0.25,
                pos.getX() + 1.25, pos.getY() + 1d, pos.getZ() + 1.25);
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
        this.signUp.load(compound.getCompound("SignUp"), this.level, this.worldPosition);
        this.signDown.load(compound.getCompound("SignDown"), this.level, this.worldPosition);
        this.loadOwner(compound);
        this.isSlim = this.mimic.getBlock() instanceof StickBlock;
        if (compound.contains("Waxed")) {
            this.isWaxed = compound.getBoolean("Waxed");
        }
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("Framed", this.framed);
        compound.put("SignUp", this.signUp.save());
        compound.put("SignDown", this.signDown.save());
        this.saveOwner(compound);
        if (isWaxed) compound.putBoolean("Waxed", isWaxed);
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

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        SignPostScreen.open(this);
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player, Direction direction) {
        SignPostScreen.open(this);
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

    public boolean isFramed() {
        return framed;
    }

    public static final class Sign {
        public final TextHolder text;
        private boolean active;
        private boolean left;
        private float yaw;
        private WoodType woodType;

        private Sign(boolean active, boolean left, float yaw, WoodType woodType) {
            this.active = active;
            this.left = left;
            this.yaw = yaw;
            this.woodType = woodType;
            this.text = new TextHolder(1, 90);
        }

        public void load(CompoundTag compound, Level level, BlockPos pos) {
            this.active = compound.getBoolean("Active");
            this.left = compound.getBoolean("Left");
            this.yaw = compound.getFloat("Yaw");
            this.woodType = WoodTypeRegistry.fromNBT(compound.getString("WoodType"));
            this.text.load(compound, level, pos);

        }

        public CompoundTag save() {
            CompoundTag compound = new CompoundTag();
            compound.putFloat("Yaw", this.yaw);
            compound.putBoolean("Left", this.left);
            compound.putBoolean("Active", this.active);
            compound.putString("WoodType", this.woodType.toString());
            this.text.save(compound);
            return compound;
        }

        public void pointToward(BlockPos myPos, BlockPos targetPos) {
            float yaw = (float) (Math.atan2(targetPos.getX() - (double) myPos.getX(),
                    targetPos.getZ() - (double) myPos.getZ()) * Mth.RAD_TO_DEG);
            this.setYaw(yaw);
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

        public void setActive(boolean active) {
            this.active = active;
        }

        public void setLeft(boolean left) {
            this.left = left;
        }

        public void setWoodType(WoodType woodType) {
            this.woodType = woodType;
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

        public void toggleDirection() {
            this.left = !this.left;
        }

        public ItemStack getItem() {
            return new ItemStack(ModRegistry.SIGN_POST_ITEMS.get(woodType));
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

    public InteractionResult handleInteraction(BlockState state, ServerLevel level, BlockPos pos, Player player,
                                               InteractionHand handIn, BlockHitResult hit, ItemStack itemstack) {
        Item item = itemstack.getItem();

        boolean emptyHand = itemstack.isEmpty();
        boolean isSneaking = player.isShiftKeyDown() && emptyHand;

        boolean ind = getClickedSignIndex(hit.getLocation());

        if (hit.getDirection().getAxis() != Direction.Axis.Y) {

            Sign sign = getSign(ind);

            if (!sign.active && item instanceof SignPostItem) return InteractionResult.PASS;

            if (isSneaking) {
                sign.toggleDirection();

                this.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(null, pos, ModSounds.BLOCK_ROTATE.get(), SoundSource.BLOCKS, 1.0F, 1);
                return InteractionResult.CONSUME;
            }
            //change direction with compass
            else if (item instanceof CompassItem) {
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                        this.getLodestonePos(level, itemstack) : this.getWorldSpawnPos(level);

                if (pointingPos != null) {
                    if (sign.active) {
                        sign.pointToward(pos, pointingPos);
                    }
                    this.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.FAIL;
            } else if (CompatHandler.FRAMEDBLOCKS && this.framed) {
                boolean success = FramedBlocksCompat.interactWithFramedSignPost(this, player, handIn, itemstack, level, pos);
                if (success) return InteractionResult.CONSUME;
            }
        }
        return this.interactWithTextHolder(ind ? 0 : 1, level, pos, state, player, handIn);
    }

    public boolean getClickedSignIndex(Vec3 hit) {
        double y = hit.y;
        //negative y yay!
        if (y < 0) y = y + (1 - (int) y);
        else y = y - (int) y;
        return (y > 0.5d);
    }

    public Sign getClickedSign(Vec3 hit) {
        return getSign(getClickedSignIndex(hit));
    }

    @Nullable
    private BlockPos getLodestonePos(Level world, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            boolean flag = tag.contains("LodestonePos");
            boolean flag1 = tag.contains("LodestoneDimension");
            if (flag && flag1) {
                Optional<ResourceKey<Level>> optional =
                        Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("LodestoneDimension")).result();
                if (optional.isPresent() && world.dimension() == optional.get()) {
                    return NbtUtils.readBlockPos(tag.getCompound("LodestonePos"));
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(Level world) {
        return world.dimensionType().natural() ? new BlockPos(world.getLevelData().getXSpawn(),
                world.getLevelData().getYSpawn(), world.getLevelData().getZSpawn()) : null;
    }

    @Override
    public void setWaxed(boolean waxed) {
        isWaxed = waxed;
    }

    @Override
    public boolean isWaxed() {
        return isWaxed;
    }

    @Override
    public void setPlayerWhoMayEdit(UUID playerWhoMayEdit) {
        this.playerWhoMayEdit = playerWhoMayEdit;
    }

    @Override
    public UUID getPlayerWhoMayEdit() {
        return playerWhoMayEdit;
    }

}

