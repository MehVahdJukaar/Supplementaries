package net.mehvahdjukaar.supplementaries.integration.create;

import com.simibubi.create.api.schematic.requirement.SchematicRequirementRegistries;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Create's schematic cannon refuses to place blocks with no item form (sign posts, book piles, skull
// candles...), so we charge those from their loot drops instead, which also gets composite and block
// entity blocks right. Blocks that do have an item are left to Create, and blocks with no drops stay
// INVALID so technical blocks don't become cannon placeable.
public class SchematicRequirements {

    public static void register() {
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (!key.getNamespace().equals(Supplementaries.MOD_ID)) continue;
            // Create's defaultOf already yields the right thing when the block has an item form
            if (block.asItem() != Items.AIR) continue;
            SchematicRequirementRegistries.BLOCKS.register(block, SchematicRequirements::fromDrops);
        }
    }

    private static ItemRequirement fromDrops(BlockState state, @Nullable BlockEntity be) {
        MinecraftServer server = PlatHelper.getCurrentServer();
        if (server == null) return ItemRequirement.INVALID; // no loot tables available (e.g. remote client)
        ServerLevel level = server.overworld();
        BlockPos pos = be != null ? be.getBlockPos() : BlockPos.ZERO;
        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, be);
        List<ItemStack> drops = state.getDrops(params);
        if (drops.isEmpty()) return ItemRequirement.INVALID;
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, drops);
    }
}
