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
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FramedBlocksCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
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
        this.signUp.load(compound.getCompound("SignUp"));
        this.signDown.load(compound.getCompound("SignDown"));

        this.loadOwner(compound);
        this.isSlim = this.mimic.getBlock() instanceof StickBlock;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("Framed", this.framed);
        this.textHolder.save(compound);
        compound.put("SignUp", this.signUp.save());
        compound.put("SignDown", this.signDown.save());
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

    public boolean isFramed() {
        return framed;
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

    public InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, ItemStack itemstack, Item item, SignPostBlockTile tile) {
        boolean emptyHand = itemstack.isEmpty();
        boolean isSneaking = player.isShiftKeyDown() && emptyHand;

        if (hit.getDirection().getAxis() != Direction.Axis.Y) {

            InteractionResult result = tile.getTextHolder().playerInteract(level, pos, player, handIn, tile);
            if (result != InteractionResult.PASS) return result;

            //sneak right click rotates the sign on z axis
            if (isSneaking) {
                tile.getSign(getClickedSign(hit.getLocation())).toggleDirection();

                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1.0F, 0.6F);
                return InteractionResult.CONSUME;
            }
            //change direction with compass
            else if (item instanceof CompassItem) {
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.isLodestoneCompass(itemstack) ?
                        this.getLodestonePos(level, itemstack) : this.getWorldSpawnPos(level);

                if (pointingPos != null) {
                    boolean up = getClickedSign(hit.getLocation());
                    var s = getSign(up);
                    if (s.active) {
                        tile.pointToward(pointingPos, up);
                    }
                    tile.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.FAIL;
            } else if (CompatHandler.FRAMEDBLOCKS && tile.framed) {
                boolean success = FramedBlocksCompat.interactWithFramedSignPost(tile, player, handIn, itemstack, level, pos);
                if (success) return InteractionResult.CONSUME;
            } else if (item instanceof SignPostItem) {
                //let sign item handle this one
                return InteractionResult.PASS;
            }
        }
        // open gui (edit sign with empty hand)
        tile.sendOpenGuiPacket(level, pos, player);

        return InteractionResult.CONSUME;
    }

    public boolean getClickedSign(Vec3 hit) {
        double y = hit.y;
        //negative y yay!
        if (y < 0) y = y + (1 - (int) y);
        else y = y - (int) y;
        return y > 0.5d;
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

}

