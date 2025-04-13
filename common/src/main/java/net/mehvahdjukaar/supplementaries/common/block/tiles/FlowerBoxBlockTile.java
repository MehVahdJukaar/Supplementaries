package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.IBlockHolder;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FlowerBoxBlockTile extends ItemDisplayTile implements IBlockHolder, IExtraModelDataProvider {

    public static final ModelDataKey<BlockState> FLOWER_0 = ModBlockProperties.FLOWER_0;
    public static final ModelDataKey<BlockState> FLOWER_1 = ModBlockProperties.FLOWER_1;
    public static final ModelDataKey<BlockState> FLOWER_2 = ModBlockProperties.FLOWER_2;

    private final BlockState[] flowerStates = new BlockState[]{Blocks.AIR.defaultBlockState(),
            Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState()};

    public FlowerBoxBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FLOWER_BOX_TILE.get(), pos, state, 3);
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return flowerStates[index];
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        if (index >= 0 && index < 3) {
            this.flowerStates[index] = state;

        }
        return false;
    }

    @Override
    public void updateTileOnInventoryChanged() {
        super.updateTileOnInventoryChanged();
        for (int n = 0; n < this.getContainerSize(); n++) {
            this.setBlockFromItem(n, this.getItem(n).getItem());
        }
        //TODO: for 1.22. standardie this darn tile
        updateLight();
    }

    private void updateLight() {
        if (this.level instanceof ServerLevel) {

            int newLight = Math.max(Math.max(ForgeHelper.getLightEmission(this.getHeldBlock(), level, worldPosition),
                            ForgeHelper.getLightEmission(this.getHeldBlock(1), level, worldPosition)),
                    ForgeHelper.getLightEmission(this.getHeldBlock(2), level, worldPosition));
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FrameBlock.LIGHT_LEVEL, newLight), 3);
            //this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(FLOWER_0, flowerStates[0])
                .with(FLOWER_1, flowerStates[1])
                .with(FLOWER_2, flowerStates[2]);
    }

    @Override
    public void updateClientVisualsOnLoad() {
        for (int n = 0; n < flowerStates.length; n++) {
            Item item = this.getItem(n).getItem();
            setBlockFromItem(n, item);
        }
        this.requestModelReload();
    }

    private void setBlockFromItem(int n, Item item) {
        Block b = null;
        if (item instanceof BlockItem bi) {
            b = bi.getBlock();
        } else if (CompatHandler.DYNAMICTREES) {
            b = CompatHandler.DynTreesGetOptionalDynamicSapling(item, this.level, this.worldPosition);
        }
        if (b == null) b = Blocks.AIR;
        BlockState state = b.defaultBlockState();
        if (b == Blocks.CAVE_VINES) {
            state = state.setValue(CaveVinesBlock.BERRIES, true);
        }
        this.flowerStates[n] = state;
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.flower_box");
    }

    public float getYaw() {
        return -this.getDirection().getOpposite().toYRot();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ItemShelfBlock.FACING);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (this.getItem(index).isEmpty()) {
            if (FlowerPotHandler.hasSpecialFlowerModel(stack.getItem())) {
                return true;
            }
            if (CommonConfigs.Building.FLOWER_BOX_SIMPLE_MODE.get()) return false;
            return (stack.getItem() instanceof BlockItem && stack.is(ModTags.FLOWER_BOX_PLANTABLE));
        }
        return false;
    }


    @Override
    public SoundEvent getAddItemSound() {
        return SoundEvents.CROP_PLANTED;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }
}
