package net.mehvahdjukaar.supplementaries.common.items.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SoapItemImpl {
    public static boolean tryCleaning(ItemStack stack, Level level, BlockPos pos, @Nullable Player player) {
                /*    TODO: use tool modifier event instead
        BlockState newState = null;
        BlockState oldState = level.getBlockState(pos);
        Block b = oldState.getBlock();
        boolean success = false;

        if (b instanceof ISoapWashable soapWashable) {
            success = soapWashable.tryWash(level, pos, oldState);
        } else {

            ItemStack temp = new ItemStack(Items.IRON_AXE);

            Optional<BlockState> optional = Optional.ofNullable(b.getToolModifiedState(oldState, level, pos, null, temp, ToolActions.AXE_WAX_OFF));
            if (optional.isPresent()) {
                newState = optional.get();
            }

            optional = Optional.ofNullable(b.getToolModifiedState(oldState, level, pos, null, temp, ToolActions.AXE_SCRAPE));
            while (optional.isPresent()) {
                newState = optional.get();
                optional = Optional.ofNullable(b.getToolModifiedState(newState, level, pos, null, temp, ToolActions.AXE_SCRAPE));
            }

            //try parsing it if mods aren't using that tool modifier state (cause they arent god darn)
            if (newState == null) {
                ResourceLocation r = Utils.getID(oldState.getBlock());
                //hardcoding goes brr. This is needed, and I can't just use forge event since I only want to react to axe scrape, not stripping
                String name = r.getPath();
                String[] keywords = new String[]{"waxed_", "weathered_", "exposed_", "oxidized_",
                        "_waxed", "_weathered", "_exposed", "_oxidized"};
                for (String key : keywords) {
                    if (name.contains(key)) {
                        String newName = name.replace(key, "");
                        Block bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r.getNamespace(), newName));
                        if(bb == null){
                            //tries minecraft namespace
                            bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(newName));
                        }
                        if (bb != null && bb != Blocks.AIR) {
                            newState = bb.withPropertiesOf(oldState);
                            break;
                        }
                    }
                }
            }

            if (newState != null && newState != oldState) {
                success = true;
                if (!level.isClientSide) {
                    level.setBlock(pos, newState, 11);
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            if (success) {
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON,
                        player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
                NetworkHandler.sendToAllInRangeClients(pos, serverLevel, 64,
                        new ClientBoundSpawnBlockParticlePacket(pos, ParticleUtil.EventType.BUBBLE_CLEAN));
                stack.shrink(1);

                if (player != null) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
        }
        return success;
        */
        return false;
    }
}
