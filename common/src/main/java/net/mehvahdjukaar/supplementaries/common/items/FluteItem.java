package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluteItem extends InstrumentItem implements IThirdPersonAnimationProvider,
        IThirdPersonSpecialItemRenderer, IFirstPersonAnimationProvider {

    public FluteItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.is(Items.BAMBOO);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        var tag = pStack.getTag();
        if (tag == null) return false;
        return tag.contains("Pet") || super.isFoil(pStack);
    }

    /*
    @Override
    @PlatformOnly(PlatformOnly.FABRIC)
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        if (interactWithPet(stack, playerIn, target, hand)) {
            return InteractionResult.sidedSuccess(playerIn.level.isClientSide);
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }


    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return interactWithPet(stack, player, livingEntity, player.getUsedItemHand());
        }
        return false;
    }*/

    //now called from forge event
    public static boolean interactWithPet(ItemStack stack, Player player, Entity target, InteractionHand hand) {
        if (!(target instanceof LivingEntity)) return false;
        CompoundTag c = stack.getTagElement("Pet");
        if (c != null) return false;
        if (target instanceof TamableAnimal animal && animal.isTame() && animal.getOwnerUUID().equals(player.getUUID())
                || target.getType().is(ModTags.FLUTE_PET)) {

            if (target instanceof AbstractHorse horse && !horse.isTamed()) return false;
            else if (target instanceof Fox fox && !fox.trusts(player.getUUID())) return false;

            CompoundTag com = new CompoundTag();
            com.putString("Name", target.getName().getString());
            com.putUUID("UUID", target.getUUID());

            stack.addTagElement("Pet", com);
            player.setItemInHand(hand, stack);
            player.getCooldowns().addCooldown(stack.getItem(), 20);
            return true;
        }
        return false;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        super.use(level, player, handIn);
        ItemStack stack = player.getItemInHand(handIn);
        if (level instanceof ServerLevel serverLevel) {

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            int r = CommonConfigs.Tools.FLUTE_RADIUS.get();
            CompoundTag com = stack.getTagElement("Pet");
            if (com != null) {
                Entity entity = serverLevel.getEntity(com.getUUID("UUID"));
                int maxDist = CommonConfigs.Tools.FLUTE_DISTANCE.get() * CommonConfigs.Tools.FLUTE_DISTANCE.get();
                if (entity instanceof LivingEntity pet) {
                    if (pet.level() == player.level() && pet.distanceToSqr(player) < maxDist) {
                        if (pet.randomTeleport(x, y, z, false)) {
                            pet.stopSleeping();
                        }
                    }
                }

            } else {
                AABB bb = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
                List<Entity> entities = level.getEntities(player, bb, TamableAnimal.class::isInstance);
                for (Entity e : entities) {
                    TamableAnimal pet = ((TamableAnimal) e);
                    if (pet.isTame() && !pet.isOrderedToSit() && pet.getOwnerUUID().equals(player.getUUID())) {
                        pet.randomTeleport(x, y, z, false);
                    }
                }
            }
            player.getCooldowns().addCooldown(this, 20);
        }
        //consumes on both so doesn't swing hand
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity entity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, entity, pTimeCharged);
        pStack.hurtAndBreak(1, entity, (en) -> en.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag tag = stack.getTagElement("Pet");
        if (tag != null) {
            tooltip.add(Component.literal(tag.getString("Name")).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void spawnNoteParticle(Level level, LivingEntity entity, int note) {
        if (!ClientConfigs.Items.FLUTE_PARTICLES.get()) return;
        //default base
        Vec3 bx = new Vec3(1, 0, 0);
        Vec3 by = new Vec3(0, 1, 0);
        Vec3 bz = new Vec3(0, 0, 1);

        float xRot = -entity.getXRot() * Mth.DEG_TO_RAD;
        float yRot = -Mth.wrapDegrees(entity.yHeadRot) * Mth.DEG_TO_RAD;
        //apply rotation matrix
        bx = bx.xRot(xRot).yRot(yRot);
        by = by.xRot(xRot).yRot(yRot);
        bz = bz.xRot(xRot).yRot(yRot);

        //rotate a vector on y axis
        Vec3 armVec = new Vec3(0, 0, 0.28 + level.random.nextFloat() * 0.5);

        int mirror = entity.getMainArm() == HumanoidArm.RIGHT ^ entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? -1 : 1;

        armVec = armVec.yRot((float) (-Math.PI / 2f * mirror)).add(0, 0.15, 0.1);

        //new vector is rotated on y axis relative to the rotated base
        Vec3 newV = bx.scale(armVec.x)
                .add(by.scale(armVec.y))
                .add(bz.scale(armVec.z));

        double x = entity.getX() + newV.x;
        double y = entity.getEyeY() + newV.y;
        double z = entity.getZ() + newV.z;

        SimpleParticleType particle = entity.isUnderWater() ? ParticleTypes.BUBBLE : ParticleTypes.NOTE;

        level.addParticle(particle, x, y, z, level.random.nextInt(24) / 24.0D, 0.0D, 0.0D);
    }

    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim
            int mirror = entity.getMainArm() == HumanoidArm.RIGHT ^ hand == InteractionHand.MAIN_HAND ? -1 : 1;

            matrixStack.translate(-0.4 * mirror, 0.2, 0);

            float timeLeft = stack.getUseDuration() - (entity.getUseItemRemainingTicks() - partialTicks + 1.0F);

            float sin = Mth.sin((timeLeft - 0.1F) * 1.3F);

            matrixStack.translate(0, sin * 0.0038F, 0);
            matrixStack.mulPose(Axis.ZN.rotationDegrees(90));

            matrixStack.scale(1.0F * mirror, -1.0F * mirror, -1.0F);
        }
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isTwoHanded() {
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, true);
            return true;
        }
        return false;
    }

    private <T extends LivingEntity> void animateHands(HumanoidModel<T> model, T entity, boolean leftHand) {

        ModelPart mainHand = leftHand ? model.leftArm : model.rightArm;
        ModelPart offHand = leftHand ? model.rightArm : model.leftArm;

        Vec3 bx = new Vec3(1, 0, 0);
        Vec3 by = new Vec3(0, 1, 0);
        Vec3 bz = new Vec3(0, 0, 1);

        float headXRot = MthUtils.wrapRad(model.head.xRot);
        float headYRot = MthUtils.wrapRad(model.head.yRot);

        //head rot + hand offset from flute
        float downFacingRot = Mth.clamp(headXRot, 0f, 0.8f);

        float xRot = getMaxHeadXRot(headXRot) - (entity.isCrouching() ? 1F : 0.0F)
                - 0.3f + downFacingRot * 0.5f;

        bx = bx.xRot(xRot);
        by = by.xRot(xRot);
        bz = bz.xRot(xRot);

        Vec3 armVec = new Vec3(0, 0, 1);

        float mirror = leftHand ? -1 : 1;

        //Rotate hand vector on y axis
        armVec = armVec.yRot(-0.99f * mirror);

        //change hand vector onto direction vector basis
        Vec3 newV = bx.scale(armVec.x).add(by.scale(armVec.y)).add(bz.scale(armVec.z));


        float yaw = (float) Math.atan2(-newV.x, newV.z);
        float len = (float) newV.length();

        float pitch = (float) Math.asin(newV.y / len);

        mainHand.yRot = (yaw + headYRot * 1.4f - 0.1f * mirror) - 0.5f * downFacingRot * mirror;
        mainHand.xRot = (float) (pitch - Math.PI / 2f);


        offHand.yRot = (float) Mth.clamp((MthUtils.wrapRad(mainHand.yRot) - 1 * mirror) * 0.2, -0.15, 0.15) + 1.1f * mirror;
        offHand.xRot = MthUtils.wrapRad(mainHand.xRot - 0.06f);


        //shoulder joint hackery
        float offset = leftHand ? -Mth.clamp(headYRot, -1, 0) :
                Mth.clamp(headYRot, 0, 1);

        // model.rightArm.x = -5.0F + offset * 2f;
        mainHand.z = -offset * 0.95f;

        // model.leftArm.x = -model.rightArm.x;
        // model.leftArm.z = -model.rightArm.z;

        //hax. unbobs left arm
        AnimationUtils.bobModelPart(model.leftArm, entity.tickCount, 1.0F);
        AnimationUtils.bobModelPart(model.rightArm, entity.tickCount, -1.0F);
    }

    public static float getMaxHeadXRot(float xRot) {
        return Mth.clamp(xRot, (-(float) Math.PI / 2.5F), ((float) Math.PI / 2F));
    }

    @Override
    public <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        if (!stack.isEmpty()) {

            ItemDisplayContext transform;

            poseStack.pushPose();

            boolean leftHand = humanoidArm == HumanoidArm.LEFT;
            // entity.swingTime == 0
            if (entity.getUseItem() == stack) {
                ModelPart head = parentModel.getHead();

                //hax
                float oldRot = head.xRot;
                head.xRot = getMaxHeadXRot(MthUtils.wrapRad(oldRot));
                head.translateAndRotate(poseStack);
                head.xRot = oldRot;

                CustomHeadLayer.translateToHead(poseStack, false);

                poseStack.translate(0, -4.25 / 16f, -8.5 / 16f);
                if (leftHand) poseStack.mulPose(RotHlpr.XN90);

                transform = ItemDisplayContext.HEAD;
            } else {
                //default rendering
                parentModel.translateToHand(humanoidArm, poseStack);
                poseStack.mulPose(RotHlpr.XN90);
                poseStack.mulPose(RotHlpr.Y180);

                poseStack.translate((leftHand ? -1 : 1) / 16.0F, 0.125D, -0.625D);

                transform = leftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            //let the model handle it
            //poseStack.translate(0, -0.0625D, 0.0D);
            //poseStack.mulPose(Axis.XP.rotationDegrees(-30));
            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(entity, stack, transform,
                    leftHand, poseStack, bufferSource, light);

            poseStack.popPose();
        }
    }

    //TODO: fix animation when shifting

}
