package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.IOwnerProtected;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WallLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;


public class WallLanternBlockTile extends EnhancedLanternBlockTile implements IBlockHolder, IOwnerProtected {

    public static final ModelProperty<BlockState> MIMIC = BlockProperties.MIMIC;
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
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(MIMIC, this.getHeldBlock())
                .withInitial(FANCY, this.shouldHaveTESR)
                .build();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldMimic = this.mimic;
        CompoundTag tag = pkt.getTag();
        //this calls load
        handleUpdateTag(tag);
        if (!Objects.equals(oldMimic, this.mimic)) {
            //not needed cause model data doesn't create new obj. updating old one instead
            ModelDataManager.requestModelDataRefresh(this);
            //this.data.setData(MIMIC, this.getHeldBlock());
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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
        if(state.hasProperty(LanternBlock.HANGING)){
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