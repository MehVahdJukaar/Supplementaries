package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.GlobeBlock;
import net.mehvahdjukaar.supplementaries.common.SpecialPlayers;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.common.Textures.*;

public class GlobeBlockTile extends BlockEntity implements Nameable {
    public float yaw = 0;
    public float prevYaw = 0;
    public int face = 0;

    private Component customName;

    public boolean sheared = false;

    //client
    public ResourceLocation texture = null;
    public boolean isFlat = false;
    public boolean isSnow = false;

    public GlobeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GLOBE_TILE.get(), pos, state);
    }

    public void setCustomName(Component name) {
        this.customName = name;
        this.updateTexture();
    }

    private void updateTexture() {
        if (this.hasCustomName()) {
            this.isFlat = false;
            this.texture = GlobeType.getGlobeTexture(this.getCustomName().getString(), this);
        } else this.texture = null;
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
        return new TranslatableComponent("block.supplementaries.globe");
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
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
        }
        compound.putInt("Face", this.face);
        compound.putFloat("Yaw", this.yaw);
        compound.putBoolean("Sheared", this.sheared);
        return compound;
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
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
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

    //TODO: improve. this is a mess
    public enum GlobeType {
        FLAT(new String[]{"flat", "flat earth"}, new TranslatableComponent("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon", "luna", "selene", "cynthia"},
                new TranslatableComponent("globe.supplementaries.moon"), GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth", "terra", "gaia", "gaea", "tierra", "tellus", "terre"},
                new TranslatableComponent("globe.supplementaries.earth"), GLOBE_TEXTURE),
        SUN(new String[]{"sun", "sol", "helios"},
                new TranslatableComponent("globe.supplementaries.sun"), GLOBE_SUN_TEXTURE);

        GlobeType(String[] key, TranslatableComponent tr, ResourceLocation res) {
            this.keyWords = key;
            this.transKeyWord = tr;
            this.texture = res;
        }

        public final String[] keyWords;
        public final TranslatableComponent transKeyWord;
        public final ResourceLocation texture;

        public static ResourceLocation getGlobeTexture(String text, GlobeBlockTile tile) {
            String name = text.toLowerCase();
            ResourceLocation r = SpecialPlayers.GLOBES.get(name);
            //TODO: generalize this mess
            tile.isSnow = r != null && r.getPath().contains("globe_wais");
            if (r != null) return r;
            for (GlobeType n : GlobeType.values()) {
                if (n.keyWords == null) continue;
                if (n.transKeyWord != null && !n.transKeyWord.getString().equals("") && name.equals(n.transKeyWord.getString().toLowerCase())) {
                    tile.isFlat = (n == FLAT);
                    return n.texture;
                }
                for (String s : n.keyWords) {
                    if (!s.equals("") && name.equals(s)) {
                        tile.isFlat = (n == FLAT);
                        return n.texture;
                    }
                }
            }
            return null;
        }

    }

}