package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.IKeyLockable;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.SafeContainerMenu;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SafeBlockTile extends OpeneableContainerBlockEntity implements IOwnerProtected, IKeyLockable {

    //max length a item name can have
    @Nullable
    private String password = null;
    private String ownerName = null;
    private UUID owner = null;

    public SafeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SAFE_TILE.get(), pos, state, 27);
    }


    public boolean handleAction(Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        Item item = stack.getItem();

        //clear ownership with tripwire
        boolean cleared = false;
        if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
            if ((item == Items.TRIPWIRE_HOOK || stack.is(ModTags.KEYS)) &&
                    (this.isOwnedBy(player) || (this.isNotOwnedBy(player) && player.isCreative()))) {
                cleared = true;
            }
        } else {
            if (player.isShiftKeyDown() && (player.isCreative() || this.getKeyStatus(stack).isCorrect())) {
                cleared = true;
            }
        }

        if (cleared) {
            this.clearPassword();
            this.onPasswordCleared(player, worldPosition);
            return true;
        }

        BlockPos frontPos = worldPosition.relative(getBlockState().getValue(SafeBlock.FACING));
        if (!level.getBlockState(frontPos).isRedstoneConductor(level, frontPos)) {
            if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
                UUID owner = this.getOwner();
                if (owner == null) {
                    owner = player.getUUID();
                    this.setOwner(owner);
                }
                if (!owner.equals(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("message.supplementaries.safe.owner", this.ownerName), true);
                    if (!player.isCreative()) {
                        return true;
                    }
                }
            } else {
                String key = this.getPassword();
                if (key == null) {
                    String newKey = IKeyLockable.getKeyPassword(stack);
                    if (newKey != null) {
                        this.setPassword(newKey);
                        this.onKeyAssigned(level, worldPosition, player, newKey);
                        return true;
                    }
                } else if (!this.canPlayerOpen(player, true) && !player.isCreative()) {
                    return true;
                }
            }
            PlatHelper.openCustomMenu((ServerPlayer) player, this, worldPosition);

            PiglinAi.angerNearbyPiglins(player, true);
        }

        return true;
    }

    public boolean canPlayerOpen(Player player, boolean feedbackMessage) {
        if (player == null || player.isCreative()) return true;
        if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
            if (this.isNotOwnedBy(player)) {
                if (feedbackMessage) {
                    player.displayClientMessage(Component.translatable("message.supplementaries.safe.owner", this.ownerName), true);
                }
                return false;
            }
        } else {
            return this.testIfHasCorrectKey(player, this.password, feedbackMessage, "safe");
        }
        return true;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void clearPassword() {
        this.ownerName = null;
        this.owner = null;
        this.password = null;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        if (this.level != null) {
            if (owner != null) {
                var p = level.getPlayerByUUID(owner);
                if (p != null) this.ownerName = p.getName().getString();
                this.owner = owner;
            }
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    //TODO: use vanilla system??
    //default lockable tile method. just used for compat
    @Override
    public boolean canOpen(Player player) {
        if (!super.canOpen(player)) return false;
        return canPlayerOpen(player, false);
    }

    @Override
    public Component getDisplayName() {
        if (CommonConfigs.Functional.SAFE_SIMPLE.get()) {
            if (this.ownerName != null) {
                if (this.shouldShowPassword()) {
                    return (Component.translatable("gui.supplementaries.safe.name", this.ownerName, super.getDisplayName()));
                }
            }
        } else if (this.password != null) {
            if (this.shouldShowPassword()) {
                return (Component.translatable("gui.supplementaries.safe.password", this.password, super.getDisplayName()));
            }
        }
        return super.getDisplayName();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.supplementaries.safe");
    }

    @Override
    protected void playOpenSound(BlockState state) {
        Vec3i vec3i = state.getValue(SafeBlock.FACING).getNormal();
        double d0 = (double) this.worldPosition.getX() + 0.5D + vec3i.getX() / 2.0D;
        double d1 = (double) this.worldPosition.getY() + 0.5D + vec3i.getY() / 2.0D;
        double d2 = (double) this.worldPosition.getZ() + 0.5D + vec3i.getZ() / 2.0D;
        this.level.playSound(null, d0, d1, d2, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
    }

    @Override
    protected void playCloseSound(BlockState state) {
        Vec3i vec3i = state.getValue(SafeBlock.FACING).getNormal();
        double d0 = (double) this.worldPosition.getX() + 0.5D + vec3i.getX() / 2.0D;
        double d1 = (double) this.worldPosition.getY() + 0.5D + vec3i.getY() / 2.0D;
        double d2 = (double) this.worldPosition.getZ() + 0.5D + vec3i.getZ() / 2.0D;
        this.level.playSound(null, d0, d1, d2, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
    }

    @Override
    protected void updateBlockState(BlockState state, boolean open) {
        this.level.setBlock(this.getBlockPos(), state.setValue(SafeBlock.OPEN, open), 3);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner")) {
            this.owner = tag.getUUID("Owner");
        } else this.owner = null;
        if (tag.contains("OwnerName")) {
            this.ownerName = tag.getString("OwnerName");
        } else this.owner = null;
        if (tag.contains("Password")) {
            this.password = tag.getString("Password");
        } else this.password = null;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        this.saveOwner(compound);
        if (this.ownerName != null)
            compound.putString("OwnerName", this.ownerName);
        if (this.password != null)
            compound.putString("Password", this.password);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return MiscUtils.isAllowedInShulker(stack, this.getLevel()) && !getKeyStatus(stack).isCorrect();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        if (inv.player.isSpectator()) return null;
        return new SafeContainerMenu(id, inv, this);
    }


}
