package net.mehvahdjukaar.supplementaries.common.items;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class SlingshotItem extends ProjectileWeaponItem implements Vanishable, IFirstPersonAnimationProvider, IThirdPersonAnimationProvider {

    public SlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {

            ItemStack projectileStack = player.getProjectile(stack);

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
                        if (!player.getAbilities().instabuild) {
                            projectileStack.shrink(1);
                            if (projectileStack.isEmpty()) {
                                player.getInventory().removeItem(projectileStack);
                            }
                        }
                        projectileStack = player.getProjectile(stack);
                    }
                    if (!world.isClientSide) {
                        float[] pitches = getShotPitches(world.getRandom());
                        int count = projectiles.size();
                        float angle = 10;
                        for (int j = 0; j < count; j++) {

                            boolean stasis = EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), stack) != 0;
                            InteractionHand hand = player.getUsedItemHand();
                            power *= (ServerConfigs.cached.SLINGSHOT_RANGE + (stasis ? 0.5 : 0)) * 1.1;
                            shootProjectile(world, entity, hand, stack, projectiles.get(j), count == 1 ? 1 : pitches[j], power, 1, angle * (j - (count - 1) / 2f));
                        }
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private static void shootProjectile(Level world, LivingEntity entity, InteractionHand hand, ItemStack stack, ItemStack projectileStack, float soundPitch, float power, float accuracy, float yaw) {

        SlingshotProjectileEntity projectile = new SlingshotProjectileEntity(entity, world, projectileStack, stack);

        Vec3 vector3d1 = entity.getUpVector(1.0F);
        Quaternion quaternion = new Quaternion(new Vector3f(vector3d1), yaw, true);
        Vec3 vector3d = entity.getViewVector(1.0F);
        Vector3f vector3f = new Vector3f(vector3d);
        vector3f.transform(quaternion);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), power, accuracy);

        stack.hurtAndBreak(1, entity, (p) -> p.broadcastBreakEvent(hand));
        world.addFreshEntity(projectile);

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 1.0F, soundPitch * (1.0F / (world.random.nextFloat() * 0.3F + 0.9F) + power * 0.6F));

    }

    //shoot pitches for multi shot
    private static float[] getShotPitches(Random random) {
        boolean flag = random.nextBoolean();
        return new float[]{getRandomShotPitch(random, flag), 1.0F, getRandomShotPitch(random, !flag)};
    }

    private static float getRandomShotPitch(Random random, boolean left) {
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
        return i == 0 ? maxCharge : maxCharge - (maxCharge / 4) * i;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(
                Enchantments.QUICK_CHARGE, Enchantments.MULTISHOT, Enchantments.LOYALTY).contains(enchantment);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = this.getAllSupportedProjectiles().test(player.getProjectile(itemstack));

        if (!flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s ->{
            Item i = s.getItem();
            return !(i instanceof DispensibleContainerItem) && i instanceof BlockItem ||
                    ItemsOverrideHandler.hasBlockPlacementAssociated(i);
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
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            //twoHanded.setTwoHanded(true);
            model.leftArm.yRot = RotHlpr.wrapRad(0.1F + model.head.yRot);
            model.leftArm.xRot = RotHlpr.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
            return true;
        }
        return false;
    }

    //TODO: finish this
    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            //twoHanded.setTwoHanded(true);
            model.rightArm.yRot = RotHlpr.wrapRad(-0.1F + model.head.yRot);
            //model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
            model.rightArm.xRot = RotHlpr.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
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
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            float timeLeft = (float) stack.getUseDuration() - ((float)entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = getPowerForTime(stack, timeLeft);

            if (f12 > 0.1F) {
                float f15 = Mth.sin((timeLeft - 0.1F) * 1.3F);
                float f18 = f12 - 0.1F;
                float f20 = f15 * f18;
                matrixStack.translate(0, f20 * 0.004F, 0);
            }

            matrixStack.translate(0, 0, f12 * 0.04F);
            matrixStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
            //matrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
        }
    }


    public static void animateCrossbowCharge(ModelPart offHand, ModelPart mainHand, LivingEntity entity, boolean right) {

        //mainHand.xRot = -0.97079635F;
        offHand.xRot = mainHand.xRot;
        float f = (float) CrossbowItem.getChargeDuration(entity.getUseItem());
        float f1 = Mth.clamp((float) entity.getTicksUsingItem(), 0.0F, f);
        float f2 = f1 / f;
        offHand.yRot = Mth.lerp(f2, 0.4F, 0.85F) * (float) (right ? 1 : -1);
        offHand.xRot = Mth.lerp(f2, offHand.xRot, (-(float) Math.PI / 2F));
    }


}
