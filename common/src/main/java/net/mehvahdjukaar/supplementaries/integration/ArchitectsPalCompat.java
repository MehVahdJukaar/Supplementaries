package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceWallBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.SCONCE_NAME_NETHER_BRASS;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regBlock;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regItem;

public class ArchitectsPalCompat {

    public static void init() {
        ModRegistry.SCONCES.add(SCONCE_ITEM_NETHER_BRASS);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(SCONCE_NETHER_BRASS.get(), RenderType.cutout());
        ClientHelper.registerRenderType(SCONCE_WALL_NETHER_BRASS.get(), RenderType.cutout());
    }
    //nether brass
    public static final Supplier<Block> SCONCE_NETHER_BRASS = regBlock(SCONCE_NAME_NETHER_BRASS, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()), 14,
                CompatObjects.NETHER_BRASS_FLAME));
    public static final Supplier<Block> SCONCE_WALL_NETHER_BRASS = regBlock("sconce_wall_nether_brass", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get())
                    .dropsLike(SCONCE_NETHER_BRASS.get()),
            CompatObjects.NETHER_BRASS_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_NETHER_BRASS = regItem(SCONCE_NAME_NETHER_BRASS, () -> new StandingAndWallBlockItem(
            SCONCE_NETHER_BRASS.get(), SCONCE_WALL_NETHER_BRASS.get(),
            new Item.Properties(), Direction.DOWN));
}
