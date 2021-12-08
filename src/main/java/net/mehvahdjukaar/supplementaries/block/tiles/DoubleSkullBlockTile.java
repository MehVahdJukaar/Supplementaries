package net.mehvahdjukaar.supplementaries.block.tiles;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.UUID;

public class DoubleSkullBlockTile extends EnhancedSkullBlockTile {

    private int rotationUp;
    @Nullable
    private GameProfile ownerUp;
    private SkullBlock.Types typeUp = SkullBlock.Types.SKELETON;

    private int waxColorInd = -1;
    private ResourceLocation waxTexture = null;

    public DoubleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_PILE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        if (waxColorInd != -1) {
            tag.putInt("WaxColor", waxColorInd);
        }
        tag.putInt("RotationUp", this.rotationUp);
        tag.putInt("TypeUp", this.typeUp.ordinal());
        if (this.ownerUp != null) {
            CompoundTag compoundtag = new CompoundTag();
            NbtUtils.writeGameProfile(compoundtag, this.ownerUp);
            tag.put("SkullOwnerUp", compoundtag);
        }

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("WaxColor")) {
            this.waxColorInd = tag.getInt("WaxColor");
            DyeColor d = waxColorInd == 17 ? null : DyeColor.byId(waxColorInd);
            this.waxTexture = Textures.SKULL_CANDLES_TEXTURES.get(d);
        } else {
            waxTexture = null;
        }
        this.rotationUp = tag.getInt("RotationUp");
        this.typeUp = SkullBlock.Types.values()[tag.getInt("TypeUp")];
        if (tag.contains("SkullOwnerUp", 10)) {
            this.setOwnerUp(NbtUtils.readGameProfile(tag.getCompound("SkullOwnerUp")));
        } else if (tag.contains("ExtraTypeUp", 8)) {
            String s = tag.getString("ExtraTypeUp");
            if (!StringUtil.isNullOrEmpty(s)) {
                this.setOwnerUp(new GameProfile((UUID) null, s));
            }
        }

    }

    @Nullable
    public GameProfile getOwnerProfileUp() {
        return this.ownerUp;
    }

    public void setOwnerUp(@Nullable GameProfile ownerUp) {
        synchronized (this) {
            this.ownerUp = ownerUp;
        }
        SkullBlockEntity.updateGameprofile(this.ownerUp, (gameProfile) -> {
            this.ownerUp = gameProfile;
            this.setChanged();
        });
    }


    public SkullBlock.Type getSkullTypeUp() {
        return this.typeUp;
    }

    public void setSkullTypeUp(SkullBlock.Types type) {
        this.typeUp = type;
    }

    public void setRotationUp(float yaw) {
        this.rotationUp = (Mth.floor((double) (yaw * 16.0F / 360.0F) + 0.5D) & 15);
    }

    public void rotateUp(Rotation rotation) {
        this.rotationUp = rotation.rotate(this.rotationUp, 16);
    }

    public void rotateUpStep(int step) {
        this.rotationUp = ((this.rotationUp - step) + 16) % 16;
    }

    public int getUpRotation() {
        return rotationUp;
    }

    public ItemStack getSkullItemUp() {
        return getSkullItem(this.typeUp, this.ownerUp);
    }

    @Override
    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack skullStack, Player player) {
        super.initialize(oldTile, skullBlock, skullStack, player);
        this.setRotationUp(player.getYRot());
        if (skullStack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof SkullBlock up) {
                this.setSkullTypeUp((SkullBlock.Types) up.getType());
                GameProfile gameprofile = null;
                if (skullStack.hasTag()) {
                    CompoundTag compoundtag = skullStack.getTag();
                    if (compoundtag.contains("SkullOwner", 10)) {
                        gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                    } else if (compoundtag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundtag.getString("SkullOwner"))) {
                        gameprofile = new GameProfile(null, compoundtag.getString("SkullOwner"));
                    }
                }
                this.setOwnerUp(gameprofile);
            }
        }
    }

    public void updateWax(BlockState above) {
        int index = -1;
        DyeColor c = null;
        if (above.getBlock() instanceof CandleBlock block) {
            c = CandleSkullBlockTile.colorFromCandle(block);
            if (c == null) index = 17;
            else index = c.getId();
        }
        if (this.waxColorInd != index) {
            this.waxColorInd = index;
            if (this.level instanceof ServerLevel) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
            } else {
                this.waxTexture = waxColorInd == -1 ? null : Textures.SKULL_CANDLES_TEXTURES.get(c);
            }
        }
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }
}
