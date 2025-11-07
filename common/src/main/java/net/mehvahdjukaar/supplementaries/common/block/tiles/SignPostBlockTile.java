package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
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
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class SignPostBlockTile extends MimicBlockTile implements ITextHolderProvider, IScreenProvider {

    public static final ModelDataKey<Boolean> FRAMED_KEY = ModBlockProperties.FRAMED;
    public static final ModelDataKey<Float> RENDER_OFFSET_KEY = ModBlockProperties.RENDER_OFFSET;
    public static final ModelDataKey<Sign> SIGN_UP_KEY = ModBlockProperties.SIGN_UP;
    public static final ModelDataKey<Sign> SIGN_DOWN_KEY = ModBlockProperties.SIGN_DOWN;

    private final Sign signUp = new Sign(false, true, 0, VanillaWoodTypes.OAK);
    private final Sign signDown = new Sign(false, false, 0, VanillaWoodTypes.OAK);
    private boolean isWaxed = false;
    private float zRenderOffset = 0;

    //is holding a framed fence (for framed blocks mod compat)
    private boolean framed = false;
    @Nullable
    private UUID playerWhoMayEdit;

    public SignPostBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WAY_SIGN_TILE.get(), pos, state);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        super.addExtraModelData(builder);
        builder.with(FRAMED_KEY, this.framed)
                .with(SIGN_UP_KEY, this.signUp)
                .with(SIGN_DOWN_KEY, this.signDown)
                .with(RENDER_OFFSET_KEY, this.zRenderOffset);
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.framed = tag.getBoolean("Framed");
        this.signUp.load(tag.getCompound("SignUp"), registries, this.worldPosition);
        this.signDown.load(tag.getCompound("SignDown"), registries, this.worldPosition);
        if (tag.contains("Waxed")) {
            this.isWaxed = tag.getBoolean("Waxed");
        }
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
        //structure block rotation decoding
        BlockState state = this.getBlockState();
        if (state.hasProperty(ModBlockProperties.ROTATE_TILE) && level != null) {
            Rotation rot = state.getValue(ModBlockProperties.ROTATE_TILE);
            if (rot != Rotation.NONE) {
                rotateSign(false, rot.ordinal() * 90, false);
                rotateSign(true, rot.ordinal() * 90, false);
                level.setBlockAndUpdate(worldPosition, state.setValue(ModBlockProperties.ROTATE_TILE, Rotation.NONE));
            }
        }
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        Block b = state.getBlock();
        if (b instanceof StickBlock) {
            zRenderOffset = 9 / 16f;
        } else if (b == Blocks.AIR) {
            zRenderOffset = 0;
        } else {
            zRenderOffset = 10 / 16f;
        }
        return super.setHeldBlock(state, index);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.mimic.isAir()) {
            //for wall ones
            tag.remove("Mimic");
        }
        tag.putBoolean("Framed", this.framed);
        tag.put("SignUp", this.signUp.save(registries));
        tag.put("SignDown", this.signDown.save(registries));
        if (isWaxed) tag.putBoolean("Waxed", isWaxed);
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
    public void openScreen(Level level, Player player, Direction direction) {
        SignPostScreen.open(this);
    }

    public float getOffset() {
        return zRenderOffset;
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
        private Vec3 signNormal;
        private WoodType woodType;

        private Sign(boolean active, boolean left, float yaw, WoodType woodType) {
            this.setYaw(yaw);
            this.active = active;
            this.left = left;
            this.woodType = woodType;
            this.text = new TextHolder(1, 90);
        }

        public void load(CompoundTag compound, HolderLookup.Provider registries, BlockPos pos) {
            this.active = compound.getBoolean("Active");
            this.left = compound.getBoolean("Left");
            this.yaw = compound.getFloat("Yaw");
            this.woodType = WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(compound.getString("WoodType")));
            this.text.load(compound, registries, pos);

        }

        public CompoundTag save(HolderLookup.Provider registries) {
            CompoundTag compound = new CompoundTag();
            compound.putFloat("Yaw", this.yaw);
            compound.putBoolean("Left", this.left);
            compound.putBoolean("Active", this.active);
            compound.putString("WoodType", this.woodType.toString());
            this.text.save(compound, registries);
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
            this.signNormal = new Vec3(0,0,1).yRot(this.yaw * Mth.DEG_TO_RAD);
        }

        private void rotateBy(float angle, boolean constrainAngle) {
            this.setYaw(Mth.wrapDegrees(this.yaw + angle));
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

        public Vec3 getNormal() {
            return signNormal;
        }

        public WoodType woodType() {
            return woodType;
        }

        public void toggleDirection() {
            this.left = !this.left;
        }

        public ItemStack getItem() {
            return new ItemStack(ModRegistry.WAY_SIGN_ITEMS.get(woodType));
        }
    }

    public boolean trySetSign(WoodType woodType, int r, boolean up, boolean framed) {
        var sign = getSign(up);
        if (!sign.active) {
            sign.active = true;
            sign.woodType = woodType;
            if (this.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)) {
                sign.setYaw(90 - this.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot());
            } else {
                sign.setYaw(90 + r * -22.5f);
            }
            this.framed = framed;
            return true;
        }
        return false;
    }

    public ItemInteractionResult handleInteraction(BlockState state, ServerLevel level, BlockPos pos, Player player,
                                                   InteractionHand handIn, BlockHitResult hit, ItemStack itemstack) {
        Item item = itemstack.getItem();

        boolean emptyHand = itemstack.isEmpty();
        boolean isSneaking = player.isShiftKeyDown() && emptyHand;

        boolean ind = getClickedSignIndex(hit.getLocation());

        if (hit.getDirection().getAxis() != Direction.Axis.Y) {

            Sign sign = getSign(ind);

            if (!sign.active && item instanceof SignPostItem)
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (isSneaking) {
                sign.toggleDirection();

                this.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(null, pos, ModSounds.BLOCK_ROTATE.get(), SoundSource.BLOCKS, 1.0F, 1);
                return ItemInteractionResult.CONSUME;
            }
            //change direction with compass
            else if (item instanceof CompassItem && !state.hasProperty(HorizontalDirectionalBlock.FACING)) {

                BlockPos pointingPos = getCompassTrackedPos(level, itemstack);

                if (pointingPos != null) {
                    if (sign.active) {
                        sign.pointToward(pos, pointingPos);
                    }
                    this.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return ItemInteractionResult.CONSUME;
                }
                return ItemInteractionResult.FAIL;
            } else if (CompatHandler.FRAMEDBLOCKS && this.framed) {
                boolean success = FramedBlocksCompat.interactWithFramedSignPost(this, player, handIn, itemstack, level, pos);
                if (success) return ItemInteractionResult.CONSUME;
            }
        }
        return this.textHolderInteract(ind ? 0 : 1, level, pos, state, player, handIn, itemstack, hit.getDirection());
    }

    private static BlockPos getCompassTrackedPos(ServerLevel level, ItemStack itemstack) {
        LodestoneTracker tracker = itemstack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker != null && tracker.target().isPresent()) {
            GlobalPos gp = tracker.target().get();
            if (level.dimension() == gp.dimension()) {
                return tracker.target().get().pos();
            }
        }
        return level.dimensionType().natural() ? level.getLevelData().getSpawnPos() : null;
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

