package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.moonlight.api.client.util.ParticleUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
    public static boolean interactWithEntity(ItemStack stack, Player player, Entity entity, InteractionHand hand) {
        Level level = player.level;
        boolean success = false;
        if (entity instanceof Sheep s) {
            if (s.getColor() != DyeColor.WHITE) {
                s.setColor(DyeColor.WHITE);
                success = true;
            }
        }
        else if(entity instanceof TamableAnimal ta && ta.isOwnedBy(player)){
            if(entity instanceof Wolf wolf){
                wolf.setCollarColor(DyeColor.RED);
            }
            ta.setOrderedToSit(true);
            if(level.isClientSide){
                var p = entity instanceof Cat ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HEART;
                level.addParticle(p, entity.getX(), entity.getEyeY(), entity.getZ(),0,0,0);
            }
            success = true;
        }

        if(success){
            level.playSound(player, entity, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 1.0F, 1.0F);
            if(level.isClientSide) {
                ParticleUtil.spawnParticleOnBoundingBox(entity.getBoundingBox(), level, ModParticles.SUDS_PARTICLE.get(),
                        UniformInt.of(3, 4), 0);
            }

            if (!player.getAbilities().instabuild) stack.shrink(1);

            return true;
        }
        return false;
    }

}
