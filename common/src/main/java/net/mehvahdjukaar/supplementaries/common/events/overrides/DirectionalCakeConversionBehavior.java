package net.mehvahdjukaar.supplementaries.common.events.overrides;

import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

class DirectionalCakeConversionBehavior implements BlockUseOverride {

    @Override
    public boolean altersWorld() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return CommonConfigs.Tweaks.DIRECTIONAL_CAKE.get();
    }

    @Override
    public boolean appliesToBlock(Block block) {
        return block == net.minecraft.world.level.block.Blocks.CAKE || (block.builtInRegistryHolder().is(BlockTags.CANDLE_CAKES) && Utils.getID(block).getNamespace().equals("minecraft"));
    }

    @Override
    public InteractionResult tryPerformingAction(BlockState state, BlockPos pos, Level world, Player player,
                                                 InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        //lets converting to candle cake
        if (state.is(BlockTags.CANDLE_CAKES) && stack.is(ItemTags.CANDLES)) {
            return InteractionResult.PASS;
        }
        if (state.is(net.minecraft.world.level.block.Blocks.CAKE) && (stack.is(ItemTags.CANDLES) || player.getDirection() == Direction.EAST || state.getValue(CakeBlock.BITES) != 0)) {
            return InteractionResult.PASS;
        }
        if (!(CommonConfigs.Tweaks.DOUBLE_CAKE_PLACEMENT.get() && stack.is(Items.CAKE))) {
            //for candles. normal cakes have no drops
            BlockState newState = ModRegistry.DIRECTIONAL_CAKE.get().defaultBlockState();
            if (world.isClientSide) world.setBlock(pos, newState, 3);
            BlockHitResult raytrace = new BlockHitResult(
                    new Vec3(pos.getX(), pos.getY(), pos.getZ()), hit.getDirection(), pos, false);

            var r = newState.use(world, player, hand, raytrace);
            if (world instanceof ServerLevel serverLevel) {
                if (r.consumesAction()) {
                    //prevents dropping cake
                    Block.getDrops(state, serverLevel, pos, null).forEach((d) -> {
                        if (d.getItem() != Items.CAKE) {
                            Block.popResource(world, pos, d);
                        }
                    });
                    state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
                } else world.setBlock(pos, state, 3); //returns to normal
            }
            return r;
        }
        //fallback to default cake interaction
        return InteractionResult.PASS;
    }
}

