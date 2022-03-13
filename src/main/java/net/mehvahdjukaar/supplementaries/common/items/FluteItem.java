package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.api.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.selene.api.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.selene.util.TwoHandedAnimation;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.items.FluteItemRenderer;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

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

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        CompoundTag c = stack.getTagElement("Pet");
        if (c == null && (
                target instanceof TamableAnimal animal && animal.isTame() && animal.getOwnerUUID().equals(playerIn.getUUID()))
                || target.getType().is(ModTags.FLUTE_PET)) {
            if (target instanceof AbstractHorse horse && !horse.isTamed()) return InteractionResult.PASS;
            //if(target instanceof FoxEntity && ! ((FoxEntity)target).isTrustedUUID(p_213497_1_.getUniqueID())return ActionResultType.PASS;
            CompoundTag com = new CompoundTag();
            com.putString("Name", target.getName().getString());
            com.putUUID("UUID", target.getUUID());
            com.putInt("ID", target.getId());

            stack.addTagElement("Pet", com);
            playerIn.setItemInHand(hand, stack);
            playerIn.getCooldowns().addCooldown(this, 20);
            return InteractionResult.sidedSuccess(playerIn.level.isClientSide);
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return this.interactLivingEntity(stack, player, livingEntity, player.getUsedItemHand()).consumesAction();
        }
        return false;
    }

    //TODO: figure out continous use
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        super.use(worldIn, playerIn, handIn);
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {

            double x = playerIn.getX();
            double y = playerIn.getY();
            double z = playerIn.getZ();
            int r = ServerConfigs.cached.FLUTE_RADIUS;
            CompoundTag com = stack.getTagElement("Pet");
            if (com != null) {
                Entity entity = worldIn.getEntity(com.getInt("ID"));
                int maxDist = ServerConfigs.cached.FLUTE_DISTANCE * ServerConfigs.cached.FLUTE_DISTANCE;
                if (entity instanceof LivingEntity pet) {
                    if (pet.level == playerIn.level && pet.distanceToSqr(playerIn) < maxDist) {
                        if (pet.randomTeleport(x, y, z, false)) {
                            pet.stopSleeping();
                        }
                    }
                }

            } else {
                AABB bb = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
                List<Entity> entities = worldIn.getEntities(playerIn, bb, (e) -> e instanceof TamableAnimal);
                for (Entity e : entities) {
                    TamableAnimal pet = ((TamableAnimal) e);
                    if (pet.isTame() && !pet.isOrderedToSit() && pet.getOwnerUUID().equals(playerIn.getUUID())) {
                        pet.randomTeleport(x, y, z, false);
                    }
                }
            }
            playerIn.getCooldowns().addCooldown(this, 20);
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
            tooltip.add(new TextComponent(tag.getString("Name")).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void spawnNoteParticle(Level level, LivingEntity entity, int note) {
        if (!ClientConfigs.cached.FLUTE_PARTICLES) return;
        //default base
        Vec3 bx = new Vec3(1, 0, 0);
        Vec3 by = new Vec3(0, 1, 0);
        Vec3 bz = new Vec3(0, 0, 1);

        float toRad = (float) (Math.PI / 180f);
        float xRot = -entity.getXRot() * toRad;
        float yRot = -Mth.wrapDegrees(entity.yHeadRot) * toRad;
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

        level.addParticle(ParticleTypes.NOTE, x, y, z, (double) level.random.nextInt(24) / 24.0D, 0.0D, 0.0D);
    }

    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack matrixStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
        //is using item
        if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
            //bow anim
            int mirror = entity.getMainArm() == HumanoidArm.RIGHT ^ hand == InteractionHand.MAIN_HAND ? -1 : 1;

            matrixStack.translate(-0.4 * mirror, 0.2, 0);

            float timeLeft = (float) stack.getUseDuration() - ((float) entity.getUseItemRemainingTicks() - partialTicks + 1.0F);

            float sin = Mth.sin((timeLeft - 0.1F) * 1.3F);

            matrixStack.translate(0, sin * 0.0038F, 0);
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(90));

            matrixStack.scale(1.0F * mirror, -1.0F * mirror, -1.0F);
        }
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, false);
            twoHanded.setTwoHanded(true);
            return true;
        }
        return false;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand, TwoHandedAnimation twoHanded) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            this.animateHands(model, entity, true);
            twoHanded.setTwoHanded(true);
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

        float headXRot = RotHlpr.wrapRad(model.head.xRot);
        float headYRot = RotHlpr.wrapRad(model.head.yRot);

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


        offHand.yRot = (float) Mth.clamp((RotHlpr.wrapRad(mainHand.yRot) - 1 * mirror) * 0.2, -0.15, 0.15) + 1.1f * mirror;
        offHand.xRot = RotHlpr.wrapRad(mainHand.xRot - 0.06f) ;


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

            ItemTransforms.TransformType transform;

            poseStack.pushPose();

            boolean leftHand = humanoidArm == HumanoidArm.LEFT;
            // entity.swingTime == 0
            if (entity.getUseItem() == stack) {
                ModelPart head = parentModel.getHead();

                //hax
                float oldRot = head.xRot;
                head.xRot = getMaxHeadXRot(RotHlpr.wrapRad(oldRot));
                head.translateAndRotate(poseStack);
                head.xRot = oldRot;

                CustomHeadLayer.translateToHead(poseStack, false);

                poseStack.translate(0, -4.25 / 16f, -8.5 / 16f);
                if (leftHand) poseStack.mulPose(Vector3f.XP.rotationDegrees(-90));

                transform = ItemTransforms.TransformType.HEAD;
            } else {
                //default rendering
                parentModel.translateToHand(humanoidArm, poseStack);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

                poseStack.translate((float) (leftHand ? -1 : 1) / 16.0F, 0.125D, -0.625D);

                transform = leftHand ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
            }

            //let the model handle it
            //poseStack.translate(0, -0.0625D, 0.0D);
            //poseStack.mulPose(Vector3f.XP.rotationDegrees(-30));
            Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, transform,
                    leftHand, poseStack, bufferSource, light);

            poseStack.popPose();
        }
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        ClientRegistry.registerISTER(consumer, FluteItemRenderer::new);
    }
    //TODO: fix animation when shifting

}
