package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static net.mehvahdjukaar.supplementaries.reg.ModTextures.*;

public class GlobeBlockTile extends BlockEntity implements Nameable {

    private final boolean sepia;

    private boolean sheared = false;
    private int face = 0;

    //cient
    private Component customName;
    private float yaw = 0;
    private float prevYaw = 0;
    private Pair<GlobeModel, @Nullable ResourceLocation> renderData = Pair.of(GlobeModel.GLOBE, null);

    public GlobeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GLOBE_TILE.get(), pos, state);
        this.sepia = state.is(ModRegistry.GLOBE_SEPIA.get());
    }

    public int getFace() {
        return face;
    }

    public float getRotation(float partialTicks) {
        return Mth.lerp(partialTicks, prevYaw + face, yaw + face);
    }

    public Pair<GlobeModel, ResourceLocation> getRenderData() {
        return renderData;
    }

    public boolean isSepia() {
        return sepia;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        this.updateRenderData();
    }

    public void toggleShearing() {
        this.sheared = !this.sheared;
        this.updateRenderData();
    }

    private void updateRenderData() {
        if (this.sheared) {
            this.renderData = Pair.of(GlobeModel.SHEARED, GLOBE_SHEARED_TEXTURE);
        } else if (this.hasCustomName()) {
            this.renderData = GlobeType.getGlobeTexture(this.getCustomName().getString());
        } else this.renderData = Pair.of(GlobeModel.GLOBE, null);
    }

    @Override
    public Component getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public Component getCustomName() {
        return this.customName;
    }

    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.globe");
    }

    @Override
    public void load(CompoundTag compound) {
        if (compound.contains("CustomName", 8)) {
            this.setCustomName(Component.Serializer.fromJson(compound.getString("CustomName")));
        }
        this.face = compound.getInt("Face");
        this.yaw = compound.getFloat("Yaw");
        this.sheared = compound.getBoolean("Sheared");
        super.load(compound);

    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
        tag.putInt("Face", this.face);
        tag.putFloat("Yaw", this.yaw);
        tag.putBoolean("Sheared", this.sheared);
    }

    public void spin() {
        int spin = 360;
        int inc = 90;
        this.face = (this.face - inc) % 360;
        this.yaw = (this.yaw + spin + inc);
        this.prevYaw = (this.prevYaw + spin + inc);
        this.setChanged();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            this.spin();
            this.level.playSound(null, this.worldPosition,
                    ModSounds.GLOBE_SPIN.get(),
                    SoundSource.BLOCKS, 0.65f,
                    MthUtils.nextWeighted(level.random, 0.2f) + 0.9f);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GlobeBlockTile tile) {
        tile.prevYaw = tile.yaw;
        if (tile.yaw != 0) {
            if (tile.yaw < 0) {
                tile.yaw = 0;
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            } else {
                tile.yaw = (tile.yaw * 0.94f) - 0.7f;
            }
        }
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(GlobeBlock.FACING);
    }

    public boolean isSpinningVeryFast() {
        return yaw > 1500;
    }

    public int getSignalPower() {
        if (this.yaw != 0) return 15;
        else return this.face / -90 + 1;
    }


    //TODO: improve. this is a mess
    public enum GlobeType {
        FLAT(new String[]{"flat", "flat earth"}, Component.translatable("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon", "luna", "selene", "cynthia"},
                Component.translatable("globe.supplementaries.moon"), GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth", "terra", "gaia", "gaea", "tierra", "tellus", "terre"},
                Component.translatable("globe.supplementaries.earth"), GLOBE_TEXTURE),
        SUN(new String[]{"sun", "sol", "helios"},
                Component.translatable("globe.supplementaries.sun"), GLOBE_SUN_TEXTURE);

        GlobeType(String[] key, Component tr, ResourceLocation res) {
            this.keyWords = key;
            this.transKeyWord = tr;
            this.texture = res;
        }

        private final String[] keyWords;
        public final Component transKeyWord;
        public final ResourceLocation texture;

        public static Pair<GlobeModel, ResourceLocation> getGlobeTexture(String text) {
            GlobeModel model = GlobeModel.GLOBE;
            String name = text.toLowerCase(Locale.ROOT);
            ResourceLocation r = Credits.INSTANCE.globes().get(name);

            if (r != null) {
                if (r.getPath().contains("globe_wais")) {
                    model = GlobeModel.SNOW;
                }
                return Pair.of(model, r);
            }
            for (GlobeType n : GlobeType.values()) {
                if (n.keyWords == null) continue;
                if (n.transKeyWord != null && !n.transKeyWord.getString().equals("") && name.equals(n.transKeyWord.getString().toLowerCase(Locale.ROOT))) {
                    if (n == FLAT) model = GlobeModel.FLAT;
                    return Pair.of(model, n.texture);
                }
                for (String s : n.keyWords) {
                    if (!s.equals("") && name.equals(s)) {
                        if (n == FLAT) model = GlobeModel.FLAT;
                        return Pair.of(model, n.texture);
                    }
                }
            }
            return Pair.of(GlobeModel.GLOBE, null);
        }
    }

    public enum GlobeModel {
        GLOBE, FLAT, SNOW, SHEARED
    }

}