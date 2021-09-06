package net.mehvahdjukaar.supplementaries.items;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.supplementaries.entities.SlingshotProjectileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class SlingshotItem extends ShootableItem implements IVanishable, IFirstPersonAnimationProvider, IThirdPersonAnimationProvider {

    public SlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int useTime) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity)entity;

            ItemStack itemstack = playerentity.getProjectile(stack);

            int i = this.getUseDuration(stack) - useTime;

            if (!itemstack.isEmpty() && this.getAllSupportedProjectiles().test(itemstack)) {

                float f = getPowerForTime(i);
                if (!((double)f < 0.01D)) {
                    boolean flag1 = playerentity.abilities.instabuild;
                    if (!world.isClientSide) {

                        SlingshotProjectileEntity projectile = new SlingshotProjectileEntity(playerentity, world, itemstack);

                        projectile.shootFromRotation(playerentity, playerentity.xRot, playerentity.yRot, 0.0F, f * 3.0F, 1.0F);
                        if (f == 1.0F) {
                            //projectile.setCritArrow(true);
                        }

                        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                        if (j > 0) {
                            //projectile.setBaseDamage(projectile.getBaseDamage() + (double)j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                        if (k > 0) {
                            //projectile.setKnockback(k);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                            projectile.setSecondsOnFire(100);
                        }

                        stack.hurtAndBreak(1, playerentity, (p_220009_1_) -> {
                            p_220009_1_.broadcastBreakEvent(playerentity.getUsedItemHand());
                        });
                        if (flag1 || playerentity.abilities.instabuild && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW)) {
                            //projectile.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                        }

                        world.addFreshEntity(projectile);
                    }

                    world.playSound(null, playerentity.getX(), playerentity.getY(), playerentity.getZ(), SoundEvents.ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!flag1 && !playerentity.abilities.instabuild) {
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            playerentity.inventory.removeItem(itemstack);
                        }
                    }

                    playerentity.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public static float getPowerForTime(float timeLeft) {
        float f = timeLeft / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }


    /** Determines how much force a charged right click item will release on player letting go
     * To be used in conjunction with onPlayerStoppedUsing
     * @param stack - Item used (get from onPlayerStoppedUsing)
     * @param timeLeft - (get from onPlayerStoppedUsing)
     * @return appropriate charge for item */
    public float getForce(ItemStack stack, int timeLeft) {
        int i = this.getUseDuration(stack) - timeLeft;
        float f = i / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        f *= 4f;

        if (f > 6f) {
            f = 6f;
        }
        return f;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAnimation(ItemStack stack) {
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
        return s->s.getItem() instanceof BlockItem;
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

            float timeLeft = (float)stack.getUseDuration() - ((float) Minecraft.getInstance().player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = getPowerForTime(timeLeft);

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


    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded) {
        twoHanded.setTwoHanded(true);
        model.rightArm.yRot = -0.1F + model.head.yRot;
        model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
        model.rightArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
        model.leftArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
        animateCrossbowCharge(model.rightArm, model.rightArm, entity, mainHand == HandSide.RIGHT);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, BipedModel<T> model, T entity, HandSide mainHand, TwoHandedAnimation twoHanded) {
        twoHanded.setTwoHanded(true);
        model.rightArm.yRot = -0.1F + model.head.yRot - 0.4F;
        model.leftArm.yRot = 0.1F + model.head.yRot;
        model.rightArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
        model.leftArm.xRot = (-(float)Math.PI / 2F) + model.head.xRot;
        return true;
    }

    public static void animateCrossbowCharge(ModelRenderer arm1, ModelRenderer arm2, LivingEntity entity, boolean right) {
        ModelRenderer mainHand = right ? arm1 : arm2;
        ModelRenderer offHand = right ? arm2 : arm1;
        mainHand.yRot = right ? -0.8F : 0.8F;
        //mainHand.xRot = -0.97079635F;
        offHand.xRot = mainHand.xRot;
        float f = (float)CrossbowItem.getChargeDuration(entity.getUseItem());
        float f1 = MathHelper.clamp((float)entity.getTicksUsingItem(), 0.0F, f);
        float f2 = f1 / f;
        offHand.yRot = MathHelper.lerp(f2, 0.4F, 0.85F) * (float)(right ? 1 : -1);
        offHand.xRot = MathHelper.lerp(f2, offHand.xRot, (-(float)Math.PI / 2F));
    }
}
