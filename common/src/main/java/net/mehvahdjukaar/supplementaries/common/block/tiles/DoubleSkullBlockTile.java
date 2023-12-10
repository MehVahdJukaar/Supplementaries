package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Deprecated(forRemoval = true)
public class DoubleSkullBlockTile extends EnhancedSkullBlockTile {

    //so we don't have to save the whole texture location but its index instead
    private static final Supplier<List<ResourceLocation>> TEXTURE_IND =
            Suppliers.memoize(() -> new ArrayList<>(ModTextures.SKULL_CANDLES_TEXTURES.get().values()));

    @Nullable
    protected SkullBlockEntity innerTileUp = null;
    //client only
    private ResourceLocation waxTexture = null;

    public DoubleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_PILE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveInnerTile("SkullUp", this.innerTileUp, tag);

        if (waxTexture != null) {
            tag.putByte("WaxColor", (byte) TEXTURE_IND.get().indexOf(waxTexture));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.innerTileUp = this.loadInnerTile("SkullUp", this.innerTileUp, tag);

        if (tag.contains("WaxColor")) {
            this.waxTexture = TEXTURE_IND.get().get(tag.getByte("WaxColor"));
        } else {
            waxTexture = null;
        }
    }

    public ItemStack getSkullItemUp() {
        if (this.innerTileUp != null) {
            return new ItemStack(innerTileUp.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }

    public void rotateUp(Rotation rotation) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    rotation.rotate(r, 16)));
        }
    }

    public void rotateUpStep(int step) {
        if (this.innerTileUp != null) {
            BlockState state = this.innerTileUp.getBlockState();
            int r = this.innerTileUp.getBlockState().getValue(SkullBlock.ROTATION);
            this.innerTileUp.setBlockState(state.setValue(SkullBlock.ROTATION,
                    ((r - step) + 16) % 16));
        }
    }

    @Override
    public void initialize(SkullBlockEntity oldTile, ItemStack skullStack, Player player, InteractionHand hand) {
        super.initialize(oldTile, skullStack, player, hand);
        if (skullStack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof SkullBlock upSkull) {
                var context = new BlockPlaceContext(player, hand, skullStack,
                        new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, this.getBlockPos(), false));
                BlockState state = upSkull.getStateForPlacement(context);
                if (state == null) {
                    state = upSkull.defaultBlockState();
                }
                BlockEntity entity = upSkull.newBlockEntity(this.getBlockPos(), state);
                if (entity instanceof SkullBlockEntity blockEntity) {
                    this.innerTileUp = blockEntity;

                    //sets owner of upper tile
                    GameProfile gameprofile = null;
                    if (skullStack.hasTag()) {
                        CompoundTag compoundtag = skullStack.getTag();
                        if (compoundtag.contains("SkullOwner", 10)) {
                            gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
                        } else if (compoundtag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundtag.getString("SkullOwner"))) {
                            gameprofile = new GameProfile(null, compoundtag.getString("SkullOwner"));
                        }
                    }
                    this.innerTileUp.setOwner(gameprofile);
                }
            }
        }
    }

    public void updateWax(BlockState above) {
        ResourceLocation newTexture = null;
        if (above.getBlock() instanceof CandleBlock block) {
            newTexture = CandleSkullBlockTile.getWaxColor(block);
        }
        if (this.waxTexture != newTexture) {
            this.waxTexture = newTexture;
            if (this.level instanceof ServerLevel) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
            }
        }
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Nullable
    public BlockState getSkullUp() {
        if (this.innerTileUp != null) {
            return this.innerTileUp.getBlockState();
        }
        return null;
    }

    @Nullable
    public BlockEntity getSkullTileUp() {
        return this.innerTileUp;
    }

    public static void ti2ck(Level level, BlockPos pos, BlockState state, DoubleSkullBlockTile e) {
        e.tick(level, pos, state);
        var tileUp = e.getSkullTileUp();
        if (tileUp != null) {
            var b = tileUp.getBlockState();
            if (b instanceof EntityBlock eb) {
                eb.getTicker(level, b, tileUp.getType());
            }
        }
    }
}
