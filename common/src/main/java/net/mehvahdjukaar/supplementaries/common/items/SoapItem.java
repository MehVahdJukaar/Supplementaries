package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSlimedMessage;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
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
        if (player instanceof ISlimeable s && s.supp$getSlimedTicks() != 0) {
            s.supp$setSlimedTicks(0, true);
            playEffectsAndConsume(stack, player, player);
            return InteractionResultHolder.success(stack);
        }
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

    //needed because some entities dont fire the normal method so we use event instead
    public static boolean interactWithEntity(ItemStack stack, Player player, Entity target, InteractionHand hand) {
        Level level = player.level();
        boolean success = false;
        if (target instanceof Sheep s) {
            if (s.getColor() != DyeColor.WHITE) {
                s.setColor(DyeColor.WHITE);
                success = true;
            }
        }

        if (target instanceof TamableAnimal ta && ta.isOwnedBy(player)) {
            if (target instanceof Wolf wolf) {
                wolf.setCollarColor(DyeColor.RED);
                wolf.isWet = true;
                //TODO: test on servers
                //wolf.level.broadcastEntityEvent(wolf, (byte)8);
            }
            ta.setOrderedToSit(true);
            if (level.isClientSide) {
                var p = target instanceof Cat ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HEART;
                level.addParticle(p, target.getX(), target.getEyeY(), target.getZ(), 0, 0, 0);
            }
            success = true;
        }

        if (target instanceof ISlimeable s && s.supp$getSlimedTicks() != 0) {
            s.supp$setSlimedTicks(0, true);
            success = true;
        }

        if (success) {
            //TODO: custom sound?
            playEffectsAndConsume(stack, player, target);
            return true;
        }
        return false;
    }

    private static void playEffectsAndConsume(ItemStack stack, Player player, Entity entity) {
        Level level = player.level();
        level.playSound(player, entity, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (level.isClientSide) {
            ParticleUtil.spawnParticleOnBoundingBox(entity.getBoundingBox(), level, ModParticles.SUDS_PARTICLE.get(),
                    UniformInt.of(2, 3), 0);
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

}
