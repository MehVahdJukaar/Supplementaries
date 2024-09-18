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

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.SCONCE_NAME_GLOW;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regBlock;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regItem;

public class InfernalExpCompat {

    public static void init() {
        ModRegistry.SCONCES.add(SCONCE_ITEM_GLOW);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(SCONCE_GLOW.get(), RenderType.cutout());
        ClientHelper.registerRenderType(SCONCE_WALL_GLOW.get(), RenderType.cutout());
    }

    //glow
    public static final Supplier<Block> SCONCE_GLOW = regBlock(SCONCE_NAME_GLOW, () -> new SconceBlock(
            BlockBehaviour.Properties.ofFullCopy(ModRegistry.SCONCE.get()), 13,
            CompatObjects.GLOW_FLAME));
    public static final Supplier<Block> SCONCE_WALL_GLOW = regBlock("sconce_wall_glow", () -> new SconceWallBlock(
            BlockBehaviour.Properties.ofFullCopy(ModRegistry.SCONCE.get())
                    .dropsLike(SCONCE_GLOW.get()),
            CompatObjects.GLOW_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_GLOW = regItem(SCONCE_NAME_GLOW, () -> new StandingAndWallBlockItem(
            SCONCE_GLOW.get(), SCONCE_WALL_GLOW.get(), new Item.Properties(), Direction.DOWN));
}
