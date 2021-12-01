package net.mehvahdjukaar.supplementaries.compat.farmersdelight;

import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import vectorwing.farmersdelight.registry.ModItems;
import vectorwing.farmersdelight.utils.tags.ModTags;

public class FDCompatRegistry {

    public static final String PLANTER_RICH_NAME = "planter_rich";
    public static final RegistryObject<Block> PLANTER_RICH;

    public static final String PLANTER_RICH_SOUL_NAME = "planter_rich_soul";
    public static final RegistryObject<Block> PLANTER_RICH_SOUL;

    static {
        PLANTER_RICH = ModRegistry.BLOCKS.register(PLANTER_RICH_NAME, () -> new PlanterRichBlock(
                AbstractBlock.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(), CompatObjects.RICH_SOIL));

        ModRegistry.regBlockItem(PLANTER_RICH, ItemGroup.TAB_DECORATIONS);

        if (CompatHandler.nethersdelight) {
            PLANTER_RICH_SOUL = ModRegistry.BLOCKS.register(PLANTER_RICH_SOUL_NAME, () -> new PlanterRichBlock(
                    AbstractBlock.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(), CompatObjects.RICH_SOUL_SOIL));

            ModRegistry.regBlockItem(PLANTER_RICH_SOUL, ItemGroup.TAB_DECORATIONS);
        }
        else{
            PLANTER_RICH_SOUL = null;
        }

    }

    public static void registerStuff() {
        //I just need to load this class to register all the needed stuff
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
