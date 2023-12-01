package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

@Deprecated(forRemoval = true)
public class WallLanternBlockTile extends SwayingBlockTile implements IBlockHolder, IOwnerProtected, IExtraModelDataProvider {

    public static final ModelDataKey<BlockState> MIMIC_KEY = MimicBlockTile.MIMIC_KEY;

    private BlockState mimic = Blocks.LANTERN.defaultBlockState();
    protected double attachmentOffset = 0;

    //for charm compat
    protected boolean isRedstoneLantern = false;
    private UUID owner = null;

    public WallLanternBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WALL_LANTERN_TILE.get(), pos, state);
    }

    public boolean isRedstoneLantern() {
        return isRedstoneLantern;
    }

    public double getAttachmentOffset() {
        return attachmentOffset;
    }

    @Override
    public Vector3f getRotationAxis() {
        return getBlockState().getValue(WallLanternBlock.FACING).step();
    }

    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(MIMIC_KEY, this.getHeldBlock())
                .with(FANCY, this.shouldHaveTESR)
                .build();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.setHeldBlock(Utils.readBlockState(compound.getCompound("Lantern"), level));
        this.isRedstoneLantern = compound.getBoolean("IsRedstone");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Lantern", NbtUtils.writeBlockState(mimic));
        compound.putBoolean("IsRedstone", this.isRedstoneLantern);
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return this.mimic;
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        if (state.hasProperty(LanternBlock.HANGING)) {
            state = state.setValue(LanternBlock.HANGING, false);
        }
        this.mimic = state;


        int light = state.getLightEmission();
        boolean lit = true;
        var res = Utils.getID(this.mimic.getBlock());
        if (res.toString().equals("charm:redstone_lantern")) {
            this.isRedstoneLantern = true;
            light = 15;
            lit = false;
        }

        if (this.level != null && !this.mimic.isAir()) {
            var shape = state.getShape(this.level, this.worldPosition);
            if (!shape.isEmpty() && !res.getNamespace().equals("twigs")) {
                this.attachmentOffset = (shape.bounds().maxY - (9 / 16d));
            }
            if (this.getBlockState().getValue(WallLanternBlock.LIGHT_LEVEL) != light) {
                if (light == 0) lit = false;
                BlockState newState = this.getBlockState().setValue(WallLanternBlock.LIT, lit)
                        .setValue(WallLanternBlock.LIGHT_LEVEL, Math.max(light, 5));
                this.getLevel().setBlock(this.worldPosition, newState, 4 | 16);
            }
        }
        return true;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }


}