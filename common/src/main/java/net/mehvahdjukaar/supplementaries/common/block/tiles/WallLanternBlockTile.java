package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class WallLanternBlockTile extends EnhancedLanternBlockTile implements IBlockHolder, IOwnerProtected, IExtraModelDataProvider {

    public static final ModelDataKey<BlockState> MIMIC = MimicBlockTile.MIMIC;

    private BlockState mimic = Blocks.LANTERN.defaultBlockState();

    //for charm compat
    public boolean isRedstoneLantern = false;

    private UUID owner = null;

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 1.9f;
        maxPeriod = 28f;
        angleDamping = 80f;
        periodDamping = 70f;
    }

    public WallLanternBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WALL_LANTERN_TILE.get(), pos, state);
    }

    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .withProperty(MIMIC, this.getHeldBlock())
                .withProperty(FANCY, this.shouldHaveTESR)
                .build();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.setHeldBlock(NbtUtils.readBlockState(compound.getCompound("Lantern")));
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
        if (Utils.getID(this.mimic.getBlock()).toString().equals("charm:redstone_lantern")) {
            this.isRedstoneLantern = true;
            light = 15;
            lit = false;
        }

        if (this.level != null && !this.mimic.isAir()) {
            var shape = state.getShape(this.level, this.worldPosition);
            if (!shape.isEmpty()) {
                this.attachmentOffset = (shape.bounds().maxY - (9 / 16d));
            }
            if (this.getBlockState().getValue(WallLanternBlock.LIGHT_LEVEL) != light)
                this.getLevel().setBlock(this.worldPosition, this.getBlockState().setValue(WallLanternBlock.LIT, lit)
                        .setValue(WallLanternBlock.LIGHT_LEVEL, light), 4 | 16);
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