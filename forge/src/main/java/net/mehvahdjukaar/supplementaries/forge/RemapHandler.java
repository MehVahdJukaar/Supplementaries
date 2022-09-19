package net.mehvahdjukaar.supplementaries.forge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.BuddingTomatoBlock;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class RemapHandler {

    private static final Map<String, ResourceLocation> remap = new HashMap<>();

    @SubscribeEvent
    public static void onRemapBlocks(MissingMappingsEvent event) {
        event.getMappings(ForgeRegistries.BLOCKS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);
        event.getMappings(ForgeRegistries.ITEMS.getRegistryKey(), Supplementaries.MOD_ID)
                .forEach(MissingMappingsEvent.Mapping::ignore);
    }


    public static class aa extends Item{

        public aa(Properties arg) {
            super(arg);
        }


        @Override
        public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
            return ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction);
        }
    }


    public static class aaaaa extends Block{
        public aaaaa(Properties arg) {
            super(arg);
        }

        @Override
        public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
            return super.getToolModifiedState(state, context, toolAction, simulate);
        }

        @Override
        public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
            return super.hidesNeighborFace(level, pos, state, neighborState, dir);
        }

        @Override
        public boolean supportsExternalFaceHiding(BlockState state) {
            return super.supportsExternalFaceHiding(state);
        }


    }


}
