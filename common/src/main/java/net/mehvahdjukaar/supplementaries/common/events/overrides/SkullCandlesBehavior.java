package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

class SkullCandlesBehavior implements ItemUseOnBlockOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean placesBlock() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.SKULL_CANDLES.get();
    }

    @Override
    public boolean appliesToItem(Item item) {
        if (item.builtInRegistryHolder().is(ItemTags.CANDLES)) {
            var n = Utils.getID(item).getNamespace();
            return (n.equals("minecraft") || n.equals("tinted") ||
                    item == CompatObjects.SOUL_CANDLE_ITEM.get() ||
                    item == CompatObjects.SPECTACLE_CANDLE_ITEM.get());
        }
        return false;
    }

    @Override
    public InteractionResult tryPerformingAction(Level world, Player player, InteractionHand hand,
                                                 ItemStack stack, BlockHitResult hit) {
        if (player.getAbilities().mayBuild) {
            BlockPos pos = hit.getBlockPos();

            if (world.getBlockEntity(pos) instanceof SkullBlockEntity oldTile) {
                BlockState state = oldTile.getBlockState();
                if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                    ItemStack copy = stack.copy();

                    Block b;
                    if (CompatHandler.BUZZIER_BEES && stack.getItem() == CompatObjects.SOUL_CANDLE_ITEM.get()) {
                        b = ModRegistry.SKULL_CANDLE_SOUL.get();
                    } else b = ModRegistry.SKULL_CANDLE.get();

                    InteractionResult result = InteractEventOverrideHandler.replaceSimilarBlock(b,
                            player, stack, pos, world, state, SoundType.CANDLE, SkullBlock.ROTATION);

                    if (result.consumesAction()) {
                        if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                            tile.initialize(oldTile, skullBlock, copy, player, hand);
                        }
                    }
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }
}

