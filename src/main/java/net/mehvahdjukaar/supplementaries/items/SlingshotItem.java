package net.mehvahdjukaar.supplementaries.items;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.events.ItemsOverrideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class SlingshotItem extends ShootableItem implements IVanishable, IFirstPersonAnimationProvider, IThirdPersonAnimationProvider {

    public SlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int timeLeft) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity) entity;

            ItemStack projectileStack = playerentity.getProjectile(stack);

            if (!projectileStack.isEmpty() && this.getAllSupportedProjectiles().test(projectileStack)) {

                float power = getPowerForTime(stack, timeLeft);
                if (!((double) power < 0.085D)) {

                    int maxProjectiles = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0 ? 3 : 1;

                    List<ItemStack> projectiles = new ArrayList<>();

                    for (int p = 0; p < maxProjectiles; p++) {
                        if (!this.getAllSupportedProjectiles().test(projectileStack)) {
                            break;
                        }
                        projectiles.add(projectileStack.copy());
                        if (!playerentity.abilities.instabuild) {
                            projectileStack.shrink(1);
                            if (projectileStack.isEmpty()) {
                                playerentity.inventory.removeItem(projectileStack);
                            }
                        }
                        projectileStack = playerentity.getProjectile(stack);
                    }
                    if (!world.isClientSide) {
                        float[] pitches = getShotPitches(world.getRandom());
                        int count = projectiles.size();
                        float angle = 10;
                        for (int j = 0; j < count; j++) {

                            Hand hand = playerentity.getUsedItemHand();
                            power *= ServerConfigs.cached.SLINGSHOT_RANGE * 1.1;
                            shootProjectile(world, entity, hand, stack, projectiles.get(j), count == 1 ? 1 : pitches[j], power, 1, angle * (j - (count - 1) / 2f));
                        }
                    }
                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }


    private static void shootProjectile(World world, LivingEntity entity, Hand hand, ItemStack stack, ItemStack projectileStack, float soundPitch, float power, float accuracy, float yaw) {

        SlingshotProjectileEntity projectile = new SlingshotProjectileEntity(entity, world, projectileStack);

        Vector3d vector3d1 = entity.getUpVector(1.0F);
        Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), yaw, true);
        Vector3d vector3d = entity.getViewVector(1.0F);
        Vector3f vector3f = new Vector3f(vector3d);
        vector3f.transform(quaternion);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), power, accuracy);

        stack.hurtAndBreak(1, entity, (p) -> p.broadcastBreakEvent(hand));
        world.addFreshEntity(projectile);

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITCH_THROW, SoundCategory.PLAYERS, 1.0F, soundPitch * (1.0F / (random.nextFloat() * 0.3F + 0.9F) + power * 0.6F));

    }

    //shoot pitches for multi shot
    private static float[] getShotPitches(Random random) {
        boolean flag = random.nextBoolean();
        return new float[]{getRandomShotPitch(flag), 1.0F, getRandomShotPitch(!flag)};
    }

    private static float getRandomShotPitch(boolean left) {
        float f = left ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    public float getPowerForTime(ItemStack stack, float timeLeft) {
        float useTime = this.getUseDuration(stack) - timeLeft;
        float f = useTime / getChargeDuration(stack);
        //parabolic power increase
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    //this is max use time
    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    //actual use duration
    public static int getChargeDuration(ItemStack stack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        int maxCharge = ServerConfigs.cached.SLINGSHOT_CHARGE;
        return i == 0 ? maxCharge : maxCharge - (maxCharge/5) * i;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(
                Enchantments.QUICK_CHARGE, Enchantments.MULTISHOT, Enchantments.LOYALTY).contains(enchantment);
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
        //needed for custom one
        return UseAction.NONE;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = this.getAllSupportedProjectiles().test(player.getProjectile(itemstack));

        if (!flag) {
            return ActionResult.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return ActionResult.consume(itemstack);
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> s.getItem() instanceof BlockItem || ItemsOverrideHandler.hasBlockOverride(s.getItem());
    }

    @Override
    public int getDefaultProjectileRange() {
        return 10;
    }

    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, Hand hand, MatrixStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            float timeLeft = (float) stack.getUseDuration() - ((float) Minecraft.getInstance().player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = getPowerForTime(stack, timeLeft);

            if (f12 > 0.1F) {
                float f15 = MathHelper.sin((timeLeft - 0.1F) * 1.3F);
                float f18 = f12 - 0.1F;
                float f20 = f15 * f18;
                matrixStack.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
            }

            matrixStack.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
            matrixStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
            //matrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
        }
    }

    //TODO: finish this
    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded) {
        //twoHanded.setTwoHanded(true);
        model.rightArm.yRot = -0.1F + model.head.yRot;
        //model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
        model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        //model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        //animateCrossbowCharge(model.rightArm, model.rightArm, entity, mainHand == HandSide.RIGHT);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded) {
        //twoHanded.setTwoHanded(true);
        //model.rightArm.yRot = -0.1F + model.head.yRot - 0.4F;
        model.leftArm.yRot = 0.1F + model.head.yRot;
        //model.rightArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;
        return true;
    }

    public static void animateCrossbowCharge(ModelRenderer arm1, ModelRenderer arm2, LivingEntity entity, boolean right) {
        ModelRenderer mainHand = right ? arm1 : arm2;
        ModelRenderer offHand = right ? arm2 : arm1;
        mainHand.yRot = right ? -0.8F : 0.8F;
        //mainHand.xRot = -0.97079635F;
        offHand.xRot = mainHand.xRot;
        float f = (float) CrossbowItem.getChargeDuration(entity.getUseItem());
        float f1 = MathHelper.clamp((float) entity.getTicksUsingItem(), 0.0F, f);
        float f2 = f1 / f;
        offHand.yRot = MathHelper.lerp(f2, 0.4F, 0.85F) * (float) (right ? 1 : -1);
        offHand.xRot = MathHelper.lerp(f2, offHand.xRot, (-(float) Math.PI / 2F));
    }
}
