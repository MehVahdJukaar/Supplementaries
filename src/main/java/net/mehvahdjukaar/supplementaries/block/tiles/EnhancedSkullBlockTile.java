package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class EnhancedSkullBlockTile extends BlockEntity {
    @Nullable
    private GameProfile owner;
    private SkullBlock.Types type = SkullBlock.Types.SKELETON;

    public EnhancedSkullBlockTile(BlockEntityType type, BlockPos pWorldPosition, BlockState pBlockState) {
        super(type, pWorldPosition, pBlockState);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        tag.putInt("Type", this.type.ordinal());
        if (this.owner != null) {
            CompoundTag compoundtag = new CompoundTag();
            NbtUtils.writeGameProfile(compoundtag, this.owner);
            tag.put("SkullOwner", compoundtag);
        }

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.type = SkullBlock.Types.values()[tag.getInt("Type")];
        if (tag.contains("SkullOwner", 10)) {
            this.setOwner(NbtUtils.readGameProfile(tag.getCompound("SkullOwner")));
        } else if (tag.contains("ExtraType", 8)) {
            String s = tag.getString("ExtraType");
            if (!StringUtil.isNullOrEmpty(s)) {
                this.setOwner(new GameProfile(null, s));
            }
        }
    }

    @Nullable
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 4, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void setOwner(@Nullable GameProfile pOwner) {
        synchronized (this) {
            this.owner = pOwner;
        }
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        SkullBlockEntity.updateGameprofile(this.owner, (gameProfile) -> {
            this.owner = gameProfile;
            this.setChanged();
        });
    }

    public void setSkullType(SkullBlock.Types type) {
        this.type = type;
    }

    public SkullBlock.Types getSkullType() {
        return type;
    }

    public ItemStack getSkullItem() {
        return DoubleSkullBlockTile.getSkullItem(this.type, this.owner);
    }

    public static ItemStack getSkullItem(SkullBlock.Type type, GameProfile profile) {
        if (type instanceof SkullBlock.Types types) {
            Item i = switch (types) {
                case SKELETON -> Items.SKELETON_SKULL;
                case WITHER_SKELETON -> Items.WITHER_SKELETON_SKULL;
                case ZOMBIE -> Items.ZOMBIE_HEAD;
                case PLAYER -> Items.PLAYER_HEAD;
                case DRAGON -> Items.DRAGON_HEAD;
                case CREEPER -> Items.CREEPER_HEAD;
            };
            ItemStack stack = new ItemStack(i);
            if (types == SkullBlock.Types.PLAYER && profile != null) {
                CompoundTag tag = new CompoundTag();
                NbtUtils.writeGameProfile(tag, profile);
                stack.addTagElement("SkullOwner", tag);
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack stack, Player player) {
        this.setOwner(oldTile.getOwnerProfile());
        this.setSkullType((SkullBlock.Types) skullBlock.getType());
    }
}
