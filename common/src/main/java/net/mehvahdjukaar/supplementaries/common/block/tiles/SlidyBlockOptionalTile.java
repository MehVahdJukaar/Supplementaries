package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SlidyBlockOptionalTile extends BlockEntity {

    private Block pressurePlate = null;

    public SlidyBlockOptionalTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.SLIDY_BLOCK_TILE.get(), pos, blockState);
    }

    public static void place(BlockState state, BlockPos pos, Level level, BlockState pressurePlate) {
        var blockEntity = new SlidyBlockOptionalTile(pos, state);
        blockEntity.pressurePlate = pressurePlate.getBlock();
        level.setBlockEntity(blockEntity);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.pressurePlate != null) {
            tag.putString("pressure_plate", Utils.getID(this.pressurePlate).toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("pressure_plate")) {
            this.pressurePlate = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString("pressure_plate")));
        }
    }

    public Block getPressurePlate() {
        return this.pressurePlate;
    }
}
