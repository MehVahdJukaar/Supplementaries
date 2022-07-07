package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HangingFlowerPotBlockTile extends MimicBlockTile implements IOwnerProtected {

    private UUID owner = null;

    public HangingFlowerPotBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HANGING_FLOWER_POT_TILE.get(), pos, state);
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

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.saveOwner(tag);
    }

    @Override
    public void load(CompoundTag compound) {
        if(compound.contains("Pot")) compound.put("Mimic",compound.get("Pot"));
        super.load(compound);
        this.loadOwner(compound);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition);
    }

}
