package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.HorizontalHollowLogBlock;

public class TFCompat {

    public static final TagKey<Block> HORIZONTAL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("twilightforest", "hollow_logs_horizontal"));
    private static final TagKey<Block> VERTICAL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("twilightforest", "hollow_logs_vertical"));

    public static BlockState tryRotateHollowLog(BlockState state, Direction face) {
        if (state.is(VERTICAL) && face.getAxis() != Direction.Axis.Y) {
            ResourceLocation newId = state.getBlockHolder().unwrapKey().get().location()
                    .withPath(p -> p.replace("vertical", "horizontal"));
            Block newBlock = BuiltInRegistries.BLOCK.get(newId);
            return newBlock.withPropertiesOf(state)
                    .setValue(HorizontalHollowLogBlock.HORIZONTAL_AXIS, face.getClockWise().getAxis());
        }
        if (state.is(HORIZONTAL) && state.getValue(HorizontalHollowLogBlock.HORIZONTAL_AXIS) != face.getAxis()) {
            ResourceLocation newId = state.getBlockHolder().unwrapKey().get().location()
                    .withPath(p -> p.replace("horizontal", "vertical"));
            Block newBlock = BuiltInRegistries.BLOCK.get(newId);
            return newBlock.withPropertiesOf(state);
        }
        return null;
    }
}
