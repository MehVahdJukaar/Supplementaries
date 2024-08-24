package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SoapItem extends Item {

    public static final FoodProperties SOAP_FOOD = (new FoodProperties.Builder())
            .nutrition(0).saturationMod(0.1F).alwaysEat().effect(
                    new MobEffectInstance(MobEffects.POISON, 120, 2), 1).build();

    public SoapItem(Properties pProperties) {
        super(pProperties.food(SOAP_FOOD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!hasBeenEatenBefore(player, level)) {
            if (player.canEat(true)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        } else {
            return InteractionResultHolder.pass(stack);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity entity) {
        if (pLevel.isClientSide) {
            Vec3 v = entity.getViewVector(0).normalize();
            double x = entity.getX() + v.x;
            double y = entity.getEyeY() + v.y - 0.12;
            double z = entity.getZ() + v.z;
            for (int j = 0; j < 4; j++) {
                RandomSource r = entity.getRandom();
                v = v.scale(0.1 + r.nextFloat() * 0.1f);
                double dx = v.x + ((0.5 - r.nextFloat()) * 0.9);
                double dy = v.y + ((0.5 - r.nextFloat()) * 0.06);
                double dz = v.z + ((0.5 - r.nextFloat()) * 0.9);

                pLevel.addParticle(ModParticles.SUDS_PARTICLE.get(), x, y, z, dx, dy, dz);
            }
        }
        return super.finishUsingItem(pStack, pLevel, entity);
    }

    public static boolean hasBeenEatenBefore(Player player, Level level) {
        ResourceLocation res = Supplementaries.res("husbandry/soap");
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

    //needed because some entities don't fire the normal method so we use event instead
    @EventCalled
    public static boolean interactWithEntity(ItemStack stack, Player player, Entity target, InteractionHand hand) {
        Level level = player.level();
        boolean success = false;

        if (player instanceof ISlimeable s && s.supp$getSlimedTicks() != 0) {
            s.supp$setSlimedTicks(0, true);
            success = true;
        } else if (target instanceof Sheep s && s.getColor() != DyeColor.WHITE) {
            s.setColor(DyeColor.WHITE);
            success = true;
        } else if (target instanceof TamableAnimal ta && ta.isOwnedBy(player)) {
            if (target instanceof Wolf wolf) {
                wolf.setCollarColor(DyeColor.RED);
                wolf.isWet = true;
                //TODO: test on servers
                //wolf.level.broadcastEntityEvent(wolf, (byte)8);
            }
            ta.setOrderedToSit(true);
            if (level instanceof ServerLevel serverLevel) {
                // extra particles for these mobs
                var p = target instanceof Cat ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HEART;
                serverLevel.sendParticles(p, target.getX(), target.getEyeY(), target.getZ(), 1,
                        0, 0, 0, 0);
            }
            success = true;
        }

        if (success) {
            playEffectsAndConsume(stack, player, target);
            return true;
        }
        return false;
    }

    private static void playEffectsAndConsume(ItemStack stack, Player player, Entity entity) {
        Level level = player.level();
        // called on both sides so we pass the player
        level.playSound(player, entity, ModSounds.SOAP_WASH.get(), SoundSource.PLAYERS, 1.0F,
                0.9f + level.random.nextFloat() * 0.3f);
        if (!level.isClientSide) {
            // spawn particles
            ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntity(entity,
                    new ClientBoundParticlePacket(entity.blockPosition(),
                            ClientBoundParticlePacket.Type.BUBBLE_CLEAN_ENTITY));
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

}
