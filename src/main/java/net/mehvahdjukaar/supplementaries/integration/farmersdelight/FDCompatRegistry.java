package net.mehvahdjukaar.supplementaries.integration.farmersdelight;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.RegistryHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.function.Supplier;

public class FDCompatRegistry {

    public static final SoundType STICK_TOMATO_SOUND = new ForgeSoundType(1.0F, 1.0F,
            () -> SoundEvents.CROP_BREAK,
            () -> SoundEvents.GRASS_STEP,
            () -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.GRASS_HIT,
            () -> SoundEvents.GRASS_FALL);

    public static final String PLANTER_RICH_NAME = "planter_rich";
    public static final RegistryObject<Block> PLANTER_RICH;

    public static final String PLANTER_RICH_SOUL_NAME = "planter_rich_soul";
    public static final RegistryObject<Block> PLANTER_RICH_SOUL;

    public static final RegistryObject<Block> ROPE_TOMATO = ModRegistry.BLOCKS.register(("rope_tomatoes"),
            () -> new TomatoRopeBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));

    public static final RegistryObject<Block> STICK_TOMATOES = ModRegistry.BLOCKS.register(("stick_tomatoes"),
            () -> new TomatoStickBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).sound(STICK_TOMATO_SOUND)));


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


    public static boolean tryTomatoLogging(BlockState facingState, LevelAccessor level, BlockPos facingPos, boolean isRope) {
        if (facingState.is(ModBlocks.TOMATO_CROP.get()) && facingState.getValue(TomatoVineBlock.ROPELOGGED)) {
            if (Configuration.ENABLE_TOMATO_VINE_CLIMBING_TAGGED_ROPES.get()) {
                BlockState toPlace;
                if (isRope) {
                    toPlace = ROPE_TOMATO.get().defaultBlockState();
                    toPlace = Block.updateFromNeighbourShapes(toPlace, level, facingPos);
                } else {
                    toPlace = STICK_TOMATOES.get().defaultBlockState();
                }
                level.setBlock(facingPos, toPlace, 3);

                return true;
            }
        }
        return false;
    }


    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(ROPE_TOMATO.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(STICK_TOMATOES.get(), RenderType.cutout());
    }


    public static boolean canAddStickToTomato(BlockState blockstate, BooleanProperty axis) {
        if (blockstate.getBlock() == STICK_TOMATOES.get()) {
            return !blockstate.getValue(axis);
        }
        return false;
    }

    public static Block getStickTomato() {
        return STICK_TOMATOES.get();
    }

    public interface ITomatoLoggable {

        void doTomatoLog(Level level, BlockPos pos, BlockState state);

    }




    public static void init() {
    }


}
