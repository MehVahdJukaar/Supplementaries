package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEnchantments;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class SlingshotItem extends ProjectileWeaponItem implements IFirstPersonAnimationProvider, IThirdPersonAnimationProvider {

    public SlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        //same as bow
        if (entity instanceof Player player) {
            ItemStack projectileStack = player.getProjectile(stack);
            if (!projectileStack.isEmpty() && this.getAllSupportedProjectiles().test(projectileStack)) {

                int useDuration = this.getUseDuration(stack, entity) - timeCharged;
                float power = getPowerForTime(useDuration);
                if ((power >= 0.085D)) {

                    List<ItemStack> projectiles = draw(stack, stack, player);

                    boolean noGravity = EnchantmentHelper.has(stack, ModEnchantments.PROJECTILE_NO_GRAVITY.get());
                    power *= (float) ((CommonConfigs.Tools.SLINGSHOT_RANGE.get() + (noGravity ? 0.5 : 0)) * 1.1);

                    if (level instanceof ServerLevel serverLevel && !projectiles.isEmpty()) {
                        this.shoot(serverLevel, player, player.getUsedItemHand(), stack, projectiles,
                                power, 1.0F, false, null);
                    }


                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        return new SlingshotProjectileEntity(level, ammo, weapon, shooter);
    }

    // crossbow code
    @Override
    protected void shootProjectile(
            LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target
    ) {
        Vector3f vector3f;
        if (target != null) {
            double d = target.getX() - shooter.getX();
            double e = target.getZ() - shooter.getZ();
            double f = Math.sqrt(d * d + e * e);
            double g = target.getY(0.3333333333333333) - projectile.getY() + f * 0.2F;
            vector3f = getProjectileShotVector(shooter, new Vec3(d, g, e), angle);
        } else {
            Vec3 vec3 = shooter.getUpVector(1.0F);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((angle * (float) (Math.PI / 180.0)), vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = shooter.getViewVector(1.0F);
            vector3f = vec32.toVector3f().rotate(quaternionf);
        }

        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), velocity, inaccuracy);
        float pitch = getShotPitch(shooter.getRandom(), index);
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, shooter.getSoundSource(), 1.0F, pitch);
    }

    private static Vector3f getProjectileShotVector(LivingEntity shooter, Vec3 distance, float angle) {
        Vector3f vector3f = distance.toVector3f().normalize();
        Vector3f vector3f2 = new Vector3f(vector3f).cross(new Vector3f(0.0F, 1.0F, 0.0F));
        if ((double) vector3f2.lengthSquared() <= 1.0E-7) {
            Vec3 vec3 = shooter.getUpVector(1.0F);
            vector3f2 = new Vector3f(vector3f).cross(vec3.toVector3f());
        }

        Vector3f vector3f3 = new Vector3f(vector3f).rotateAxis((float) (Math.PI / 2), vector3f2.x, vector3f2.y, vector3f2.z);
        return new Vector3f(vector3f).rotateAxis(angle * (float) (Math.PI / 180.0), vector3f3.x, vector3f3.y, vector3f3.z);
    }


    //shoot pitches for multi shot
    private static float getShotPitch(RandomSource random, int index) {
        return index == 0 ? 1.0F : getRandomShotPitch((index & 1) == 1, random);
    }

    private static float getRandomShotPitch(boolean isHighPitched, RandomSource random) {
        float f = isHighPitched ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    //this is max use time
    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 72000;
    }

    // same as bow but with float argument
    public static float getPowerForTime(float charge) {
        float f = charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = this.getAllSupportedProjectiles().test(player.getProjectile(itemstack));

        if (!flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            player.level().playSound(player, player,
                    getChargeSound(itemstack), SoundSource.PLAYERS, 1.0F,
                    1 * (1.0F / (world.random.nextFloat() * 0.3F + 0.9F)));
            return InteractionResultHolder.consume(itemstack);
        }
    }

    public SoundEvent getChargeSound(ItemStack stack) {
        var pair = EnchantmentHelper.getHighestLevel(stack, EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME);
        int i = pair == null ? 0 : pair.getSecond();
        return switch (i) {
            case 0 -> ModSounds.SLINGSHOT_CHARGE_0.get();
            case 1 -> ModSounds.SLINGSHOT_CHARGE_1.get();
            case 2 -> ModSounds.SLINGSHOT_CHARGE_2.get();
            default -> ModSounds.SLINGSHOT_CHARGE_3.get();
        };
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> {
            Item i = s.getItem();
            //no buckets
            if (i instanceof ThrowablePotionItem) {
                return CommonConfigs.Tools.SLINGSHOT_POTIONS.get();
            }
            if (i instanceof BombItem) {
                return CommonConfigs.Tools.SLINGSHOT_BOMBS.get();
            }
            if (i instanceof SnowballItem) {
                return CommonConfigs.Tools.SLINGSHOT_SNOWBALL.get();
            }
            if (i instanceof EnderpearlItem) {
                return CommonConfigs.Tools.SLINGSHOT_ENDERPEARLS.get();
            }
            if (i instanceof FireChargeItem) {
                return CommonConfigs.Tools.SLINGSHOT_FIRECHARGE.get();
            }
            if (i instanceof DispensibleContainerItem && i.hasCraftingRemainingItem()) {
                return CommonConfigs.Tools.SLINGSHOT_BUCKETS.get();
            }
            if (s.is(ModTags.SLINGSHOT_DAMAGEABLE)) {
                return true;
            }
            return !(i instanceof DispensibleContainerItem || s.is(ModTags.SLINGSHOT_BLACKLIST)) &&
                    i instanceof BlockItem ||
                    AdditionalItemPlacementsAPI.hasBehavior(i) ||
                    InteractEventsHandler.hasBlockPlacementAssociated(i);
        };
    }

    @Override
    public int getDefaultProjectileRange() {
        return 10;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        //need to use NONE for custom one
        return UseAnim.NONE;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            model.leftArm.yRot = MthUtils.wrapRad(0.1F + model.head.yRot);
            model.leftArm.xRot = MthUtils.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
            return true;
        }
        return false;
    }

    @Override
    public boolean isTwoHanded() {
        return false;
    }

    //TODO: finish this
    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            model.rightArm.yRot = MthUtils.wrapRad(-0.1F + model.head.yRot);
            //model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
            model.rightArm.xRot = MthUtils.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
            //model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;

            /*
            model.leftArm.xRot = model.rightArm.xRot;
            float f = (float) SlingshotItem.getChargeDuration(entity.getUseItem());
            float f1 = MathHelper.clamp((float) entity.getTicksUsingItem(), 0.0F, f);
            float f2 = f1 / f;

            model.leftArm.yRot = (float) (0.1F + model.head.yRot + MathHelper.lerp(f2, ClientConfigs.general.TEST1.get(), ClientConfigs.general.TEST2.get()) * (float) (true ? 1 : -1));
            */
            //if(ClientConfigs.general.TEST3.get()<0)
            // model.leftArm.xRot = (float) (1f*ClientConfigs.general.TEST3.get());//MathHelper.lerp(f2, model.leftArm.xRot, (-(float) Math.PI / 2F));

            //animateCrossbowCharge(model.leftArm, model.leftArm, entity, mainHand == HandSide.RIGHT);
            return true;
        }
        return false;
    }

    @Override
    public void animateItemFirstPerson(Player entity, ItemStack stack, InteractionHand hand, HumanoidArm humanoidArm,
                                       PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            float useDuration = stack.getUseDuration(entity) - (entity.getUseItemRemainingTicks() - partialTicks + 1.0F);

            float power = getPowerForTime(useDuration);

            if (power > 0.1F) {
                float f15 = Mth.sin((useDuration - 0.1F) * 1.3F);
                float f18 = power - 0.1F;
                float f20 = f15 * f18;
                matrixStack.translate(0, f20 * 0.004F, 0);
            }

            matrixStack.translate(0, 0, power * 0.04F);
            matrixStack.scale(1.0F, 1.0F, 1.0F + power * 0.2F);
            //matrixStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
        }
    }

}
