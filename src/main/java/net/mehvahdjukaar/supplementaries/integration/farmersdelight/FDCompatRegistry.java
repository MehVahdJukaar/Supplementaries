package net.mehvahdjukaar.supplementaries.integration.farmersdelight;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

public class FDCompatRegistry {


    public static final String PLANTER_RICH_NAME = "planter_rich";
    public static final RegistryObject<Block> PLANTER_RICH;

    public static final String PLANTER_RICH_SOUL_NAME = "planter_rich_soul";
    public static final RegistryObject<Block> PLANTER_RICH_SOUL;

    static {
        PLANTER_RICH = ModRegistry.BLOCKS.register(PLANTER_RICH_NAME, () -> new PlanterRichBlock(
                BlockBehaviour.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(), CompatObjects.RICH_SOIL));

        RegistryHelper.regBlockItem(PLANTER_RICH, CreativeModeTab.TAB_DECORATIONS);

        if (CompatHandler.nethersdelight) {
            PLANTER_RICH_SOUL = ModRegistry.BLOCKS.register(PLANTER_RICH_SOUL_NAME, () -> new PlanterRichBlock(
                    BlockBehaviour.Properties.copy(ModRegistry.PLANTER.get()).randomTicks(), CompatObjects.RICH_SOUL_SOIL));

            RegistryHelper.regBlockItem(PLANTER_RICH_SOUL, CreativeModeTab.TAB_DECORATIONS);
        }
        else{
            PLANTER_RICH_SOUL = null;
        }

    }

    public static void registerStuff() {
        //I just need to load this class to register all the needed stuff
    }


    public static InteractionResult onCakeInteraction(BlockState state, BlockPos pos, Level world, ItemStack stack) {
        if (stack.is(ModTags.KNIVES)) {
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
            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ModItems.CAKE_SLICE.get()));
            world.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;

    }

}
