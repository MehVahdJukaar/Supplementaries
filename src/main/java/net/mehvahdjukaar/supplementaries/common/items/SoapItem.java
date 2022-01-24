package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSpawnBlockParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public class SoapItem extends Item {
    public static final FoodProperties SOAP_FOOD = (new FoodProperties.Builder())
            .nutrition(0).saturationMod(0.1F).alwaysEat().effect(
                    () -> new MobEffectInstance(MobEffects.POISON, 120, 2), 1).build();
    ;

    public SoapItem(Properties pProperties) {
        super(pProperties.food(SOAP_FOOD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!hasBeenEatenBefore(player, level)) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (player.canEat(true)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
    }

    public static boolean hasBeenEatenBefore(Player player, Level level) {
        ResourceLocation res = new ResourceLocation("supplementaries", "husbandry/soap");
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Advancement a = serverLevel.getServer().getAdvancements().getAdvancement(res);
            if (a != null) {
                return serverPlayer.getAdvancements().getOrStartProgress(a).isDone();
            }
        } else if (player instanceof LocalPlayer localPlayer) {
            var advancements = localPlayer.connection.getAdvancements();
            Advancement a = advancements.getAdvancements().get(res);
            return a != null;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (tryCleaning(context.getItemInHand(), level, context.getClickedPos(), context.getPlayer()))
            return InteractionResult.sidedSuccess(level.isClientSide);

        return super.useOn(context);
    }

    //move all of this into the event so it takes priority
    public static boolean tryCleaning(ItemStack stack, Level level, BlockPos pos, @Nullable Player player) {
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
                ResourceLocation r = oldState.getBlock().getRegistryName();
                //hardcoding goes brr. This is needed and I can't just use forge event since I only want to react to axe scrape, not stripping
                String name = r.getPath();
                String[] keywords = new String[]{"waxed_", "weathered_", "exposed_", "oxidized_"};
                for (String key : keywords) {
                    if (name.contains(key)) {
                        Block bb = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(r.getNamespace(),
                                name.replace(key, "")));
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
                NetworkHandler.sendToAllInRangeClients(pos, serverLevel, 32,
                        new ClientBoundSpawnBlockParticlePacket(pos, ParticleUtil.EventType.BUBBLE_CLEAN));
                stack.shrink(1);

                if (player != null) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                } else {
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos);
                }
            }
        }
        return success;
    }
}
