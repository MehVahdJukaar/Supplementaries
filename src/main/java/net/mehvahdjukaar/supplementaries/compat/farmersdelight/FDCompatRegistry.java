package net.mehvahdjukaar.supplementaries.compat.farmersdelight;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import vectorwing.farmersdelight.registry.ModItems;
import vectorwing.farmersdelight.utils.tags.ModTags;

public class FDCompatRegistry {

    public static final String PLANTER_RICH_NAME = "planter_rich";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + PLANTER_RICH_NAME)
    public static final Block PLANTER_RICH = null;

    public static final String PLANTER_RICH_SOUL_NAME = "planter_rich_soul";
    @ObjectHolder(Supplementaries.MOD_ID + ":" + PLANTER_RICH_SOUL_NAME)
    public static final Block PLANTER_RICH_SOUL = null;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.register(new PlanterRichBlock(AbstractBlock.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(),
                CompatObjects.RICH_SOIL)
                .setRegistryName(PLANTER_RICH_NAME));

        if (CompatHandler.nethersdelight) {
            reg.register(new PlanterRichBlock(AbstractBlock.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(),
                    CompatObjects.RICH_SOUL_SOIL)
                    .setRegistryName(PLANTER_RICH_SOUL_NAME));
        }

    }


    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();
        reg.register(new BlockItem(PLANTER_RICH,
                new Item.Properties().tab(ModRegistry.getTab(ItemGroup.TAB_DECORATIONS, PLANTER_RICH_NAME))
        ).setRegistryName(PLANTER_RICH_NAME));

        if (CompatHandler.nethersdelight) {
            reg.register(new BlockItem(PLANTER_RICH_SOUL,
                    new Item.Properties().tab(ModRegistry.getTab(ItemGroup.TAB_DECORATIONS, PLANTER_RICH_SOUL_NAME))
            ).setRegistryName(PLANTER_RICH_SOUL_NAME));
        }

    }


    public static ActionResultType onCakeInteraction(BlockState state, BlockPos pos, World world, ItemStack stack) {
        if (ModTags.KNIVES.contains(stack.getItem())) {
            int bites = state.getValue(CakeBlock.BITES);
            if (bites < 6) {
                world.setBlock(pos, state.setValue(CakeBlock.BITES, bites + 1), 3);
            } else {
                if (state.is(ModRegistry.DOUBLE_CAKE.get()))
                    world.setBlock(pos, Blocks.CAKE.defaultBlockState(), 3);
                else
                    world.removeBlock(pos, false);
            }
            //Block.popResource();
            InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.CAKE_SLICE.get()));
            world.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F);
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;

    }


}
