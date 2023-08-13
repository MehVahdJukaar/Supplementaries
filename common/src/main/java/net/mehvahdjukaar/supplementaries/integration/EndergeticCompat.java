package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceWallBlock;
import net.mehvahdjukaar.supplementaries.reg.ModCreativeTabs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static net.mehvahdjukaar.supplementaries.reg.ModConstants.SCONCE_NAME_ENDER;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regBlock;
import static net.mehvahdjukaar.supplementaries.reg.RegUtils.regItem;

public class EndergeticCompat {

    public static void init(){
        ModRegistry.SCONCES.add(SCONCE_ITEM_ENDER);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(SCONCE_ENDER.get(), RenderType.cutout());
        ClientHelper.registerRenderType(SCONCE_WALL_ENDER.get(), RenderType.cutout());
    }

    public static final Supplier<Block> SCONCE_ENDER = regBlock(SCONCE_NAME_ENDER, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(ModRegistry.SCONCE.get()), 13,
            CompatObjects.ENDER_FLAME));
    public static final Supplier<Block> SCONCE_WALL_ENDER = regBlock("sconce_wall_ender", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_ENDER.get()),
            CompatObjects.ENDER_FLAME));
    public static final Supplier<Item> SCONCE_ITEM_ENDER = regItem(SCONCE_NAME_ENDER, () -> new StandingAndWallBlockItem(
            SCONCE_ENDER.get(), SCONCE_WALL_ENDER.get(), new Item.Properties(), Direction.DOWN));

}
