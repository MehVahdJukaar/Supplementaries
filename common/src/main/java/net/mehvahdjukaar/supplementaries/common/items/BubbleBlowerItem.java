package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FlanCompat;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BubbleBlowerItem extends Item implements IThirdPersonAnimationProvider, IFirstPersonAnimationProvider {

    public BubbleBlowerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        int charges = this.getCharges(itemstack);

        if (charges > 0) {

            int ench = EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), itemstack);
            if (ench > 0) {
                return this.deployBubbleBlock(itemstack, level, player, hand);
            }

            player.startUsingItem(hand);

            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    //bubble block
    private InteractionResultHolder<ItemStack> deployBubbleBlock(ItemStack stack, Level level, Player player, InteractionHand hand) {
        double reachDistance = ForgeHelper.getReachDistance(player);
        if (!player.isCreative()) reachDistance = Math.min(2, reachDistance);
        HitResult result = player.pick(reachDistance, 1, true);

        if (result instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();


            BlockState first = level.getBlockState(pos);
            if (!first.canBeReplaced()) {
                pos = pos.relative(hitResult.getDirection());
            }
            first = level.getBlockState(pos);
            if (first.canBeReplaced()) {

                // manual check as we are raytracing to find pos
                if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hitResult.getDirection(), stack)) {
                    return InteractionResultHolder.fail(stack);
                }

                if (CompatHandler.FLAN && !FlanCompat.canPlace(player, pos)) {
                    return InteractionResultHolder.fail(stack);
                }

                BlockState bubble = ModRegistry.BUBBLE_BLOCK.get().defaultBlockState();


                level.setBlockAndUpdate(pos, bubble);
                SoundType soundtype = bubble.getSoundType();
                level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!(player.getAbilities().instabuild)) {
                    int max = this.getMaxDamage(stack);
                    this.setDamage(stack, Math.min(max, this.getDamage(stack) + CommonConfigs.Tools.BUBBLE_BLOWER_COST.get()));
                }

                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (this.getCharges(stack) != 0) {
            tooltip.add(Component.translatable("message.supplementaries.bubble_blower_tooltip", stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage()));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.getCharges(stack) > 0;
    }

    @SuppressWarnings("UnsafePlatformOnlyCall")
    private int getCharges(ItemStack stack) {
        return this.getMaxDamage(stack) - this.getDamage(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xe8a4e4;
    }

    @ForgeOverride
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @ForgeOverride
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @ForgeOverride
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        ListTag enchantments = EnchantedBookItem.getEnchantments(book);
        return enchantments.size() == 1 &&
                EnchantmentHelper.getEnchantmentId(enchantments.getCompound(0)).equals(
                        EnchantmentHelper.getEnchantmentId(ModRegistry.STASIS_ENCHANTMENT.get()));
    }

    //forge already has these
    @PlatformOnly(PlatformOnly.FABRIC)
    private void setDamage(ItemStack stack, int damage) {
        stack.getOrCreateTag().putInt("Damage", Math.max(0, damage));
    }

    @PlatformOnly(PlatformOnly.FABRIC)
    public int getMaxDamage(ItemStack stack) {
        return getMaxDamage();
    }

    @PlatformOnly(PlatformOnly.FABRIC)
    private int getDamage(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("Damage");
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int damage = this.getDamage(stack) + 1;
        if (damage > this.getMaxDamage(stack)) {
            entity.stopUsingItem();
            return;
        }
        if (!(entity instanceof Player player) || !(player.getAbilities().instabuild)) {
            this.setDamage(stack, damage);
        }

        int soundLength = 4;
        if (remainingUseDuration % soundLength == 0) {
            Player p = entity instanceof Player pl ? pl : null;
            level.playSound(p, entity, ModSounds.BUBBLE_BLOW.get(), entity.getSoundSource(),
                    1.0F, MthUtils.nextWeighted(level.random, 0.20f) + 0.95f);
        }
        if (level.isClientSide) {
            // runs for other players as well. I think
            Vec3 v = entity.getViewVector(0).normalize();
            double x = entity.getX() + v.x;
            double y = entity.getEyeY() + v.y - 0.12;
            double z = entity.getZ() + v.z;
            RandomSource r = entity.getRandom();
            v = v.scale(0.1 + r.nextFloat() * 0.1f);
            double dx = v.x + ((0.5 - r.nextFloat()) * 0.08);
            double dy = v.y + ((0.5 - r.nextFloat()) * 0.04);
            double dz = v.z + ((0.5 - r.nextFloat()) * 0.08);

            level.addParticle(ModParticles.SUDS_PARTICLE.get(), x, y, z, dx, dy, dz);
        }else{
            if (remainingUseDuration % 10 == 0) {
                level.gameEvent(entity, GameEvent.INSTRUMENT_PLAY, entity.position());
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, true);
            return true;
        }
        return false;
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, false);
            return true;
        }
        return false;
    }

    public <T extends LivingEntity> void animateHands(HumanoidModel<T> model, T entity, boolean leftHand) {

        ModelPart mainHand = leftHand ? model.leftArm : model.rightArm;
        int dir = (leftHand ? -1 : 1);

        float cr = (entity.isCrouching() ? 0.3F : 0.0F);

        float headXRot = MthUtils.wrapRad(model.head.xRot);
        float headYRot = MthUtils.wrapRad(model.head.yRot);

        float pitch = Mth.clamp(headXRot, -1.6f, 0.8f) + 0.55f - cr;
        mainHand.xRot = (float) (pitch - Math.PI / 2f) - dir * 0.3f * headYRot;

        float yaw = 0.7f * dir;
        mainHand.yRot = Mth.clamp(-yaw * Mth.cos(pitch + cr) + headYRot, -1.1f, 1.1f);//yaw;
        mainHand.zRot = -yaw * Mth.sin(pitch + cr);

        AnimationUtils.bobModelPart(mainHand, entity.tickCount, -dir);
    }


    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim

            float timeLeft = stack.getUseDuration() - (entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f12 = 1;//getPowerForTime(stack, timeLeft);

            if (f12 > 0.1F) {
                float f15 = Mth.sin((timeLeft - 0.1F) * 1.3F);
                float f18 = f12 - 0.1F;
                float f20 = f15 * f18;
                matrixStack.translate(0, f20 * 0.004F, 0);
            }

            matrixStack.translate(0, 0, f12 * 0.04F);
            matrixStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
            //matrixStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
        }
    }


}
