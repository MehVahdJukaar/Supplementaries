package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.block.DynamicRenderedItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBuntingBlock;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItemOld;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBuntingBlock.canSupportBunting;


public class BuntingBlockTile extends DynamicRenderedItemDisplayTile {

    public static final ModelDataKey<DyeColor> NORTH_BUNTING = new ModelDataKey<>(DyeColor.class);
    public static final ModelDataKey<DyeColor> SOUTH_BUNTING = new ModelDataKey<>(DyeColor.class);
    public static final ModelDataKey<DyeColor> EAST_BUNTING = new ModelDataKey<>(DyeColor.class);
    public static final ModelDataKey<DyeColor> WEST_BUNTING = new ModelDataKey<>(DyeColor.class);
    // client model cache
    private final Map<Direction, DyeColor> buntings = new EnumMap<>(Direction.class);

    public BuntingBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BUNTING_TILE.get(), pos, state, 4);
    }

    public Map<Direction, DyeColor> getBuntings() {
        return buntings;
    }

    @Override
    public void updateClientVisualsOnLoad() {
        buntings.clear();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            ItemStack stack = this.getItem(d.get2DDataValue());
            if (stack.getItem() instanceof BuntingItem bi) {
                DyeColor color = bi.getColor();
                this.buntings.put(d, color);
            }
        }
        if (buntings.isEmpty()) {
            //error; should be cleared soon
            // Supplementaries.error();
        }
        requestModelReload();
    }

    @Override
    public void updateTileOnInventoryChanged() {
        BlockState state = getBlockState();
        if (this.isEmpty()) {
            level.setBlockAndUpdate(worldPosition, RopeBuntingBlock.toRope(state));
        } else {
            BlockState state2 = state;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                var prop = RopeBuntingBlock.HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir);
                var old = state2.getValue(prop);
                boolean isEmpty = this.getItem(dir.get2DDataValue()).isEmpty();
                state2 = state2.setValue(prop, isEmpty ? (old == ModBlockProperties.Bunting.NONE ? ModBlockProperties.Bunting.NONE : ModBlockProperties.Bunting.ROPE) :
                        ModBlockProperties.Bunting.BUNTING);

            }
            if (state != state2) {
                level.setBlockAndUpdate(worldPosition, state2);
            }
        }
    }

    @Override
    public boolean needsToUpdateClientWhenChanged() {
        return false; //dont need as we always set block ourselves. 2 packets will otherwise confuse clients
    }

    @Override
    public boolean isNeverFancy() {
        return ClientConfigs.Blocks.FAST_BUNTINGS.get();
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        super.addExtraModelData(builder);
        builder.with(NORTH_BUNTING, buntings.getOrDefault(Direction.NORTH, null));
        builder.with(SOUTH_BUNTING, buntings.getOrDefault(Direction.SOUTH, null));
        builder.with(EAST_BUNTING, buntings.getOrDefault(Direction.EAST, null));
        builder.with(WEST_BUNTING, buntings.getOrDefault(Direction.WEST, null));
    }

    @Override
    protected boolean getFancyDistance() {
        LOD lod = LOD.at(this);
        return lod.isNear();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof BuntingItem && getItem(index).isEmpty() &&
                canSupportBunting(getBlockState(), index);
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canOpen(Player player) {
        return false;
    }

    public boolean rotateBuntings(BlockState state, Rotation rotation) {
        Map<Direction, ItemStack> newMap = new HashMap<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            ItemStack stack = this.getItem(dir.get2DDataValue());
            if (stack.isEmpty()) continue;
            Direction newDir = rotation.rotate(dir);
            if (canSupportBunting(state, newDir.get2DDataValue())) {
                newMap.put(newDir, stack);
            } else return false;
        }
        if (!newMap.isEmpty()) {
            this.clearContent();
            newMap.forEach((dir, stack) ->
                    this.setItem(dir.get2DDataValue(), stack));
            return true;
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        //structure block rotation decoding
        BlockState state = this.getBlockState();
        if (state.getValue(RopeBuntingBlock.FLIP_TILE) && level != null && !level.isClientSide) {
            rotateBuntings(state, Rotation.CLOCKWISE_90);
            level.setBlockAndUpdate(worldPosition, state.setValue(RopeBuntingBlock.FLIP_TILE, false));
        }
        //NBT items backward compat

        ListTag listTag = tag.getList("Items", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            CompoundTag nbt = compoundTag.getCompound("tag");
            if (!nbt.isEmpty()) {
                int j = compoundTag.getByte("Slot") & 255;
                DyeColor dye = DyeColor.byName(nbt.getString("Color"), DyeColor.WHITE);
                this.getItem(j).set(DataComponents.BASE_COLOR, dye);
            }
        }

        //backward compat 2
        for (int i = 0; i < this.getItems().size(); i++) {
            var item = this.getItem(i);
            if (item.is(ModRegistry.BUNTING_OLD.get())) {
                var color = BuntingItemOld.getColor(item);
                this.setItem(i, ModRegistry.BUNTING_BLOCKS.get(color).get().asItem().getDefaultInstance());
            }
        }
    }

}