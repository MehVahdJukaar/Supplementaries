package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.ModRenderTypes;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import static net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectory.findBestTrajectory;

public class CannonController {

    private static BlockPos cannonPos;
    private static boolean active;
    private static CameraType lastCameraType;
    private static CannonBlockTile cannon;
    private static HitResult hit;
    private static boolean firstTick = true;

    private static float cameraYaw;
    private static float cameraPitch;
    private static boolean needsToUpdateServer;
    private static boolean preferShootingDown = true;

    @Nullable
    private static CannonTrajectory trajectory;

    public static void activateCannonCamera(BlockPos pos) {
        active = true;
        firstTick = true;
        preferShootingDown = true;
        cannonPos = pos;
        Minecraft mc = Minecraft.getInstance();
        lastCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        mc.gui.setOverlayMessage(Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage()), false);
    }

    public static void turnOff() {
        active = false;
        cannon = null;
        if (lastCameraType != null) {
            Minecraft.getInstance().options.setCameraType(lastCameraType);
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean setupCamera(Camera camera, BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse, float partialTick) {

        if (active && cannon != null) {
            //do all setup here
            float yaw = cannon.getYaw(partialTick);
            float pitch = cannon.getPitch(partialTick);

            Vec3 start = cannonPos.getCenter().add(0, 2, 0);

            //base pos. assume empty
            camera.setPosition(start);

            camera.setRotation(180 + yaw, cameraPitch);

            camera.move(-camera.getMaxZoom(4), 0, -1);


            // find hit result
            Vec3 lookDir2 = Vec3.directionFromRotation(-cameraPitch, yaw);
            float maxRange = 128;
            Vec3 endPos = start.add(lookDir2.scale(-maxRange));

            BlockHitResult hitResult = level
                    .clip(new ClipContext(start, endPos,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));

            hit = hitResult;


            Vec3 targetVector = hit.getLocation().subtract(cannon.getBlockPos().getCenter());
            //rotate so we can work in 2d
            Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);
            target = target.add(target.normalized().scale(0.05f));

            trajectory = findBestTrajectory(target,
                    cannon.getProjectileGravity(), cannon.getProjectileDrag(), cannon.getFirePower(), preferShootingDown);

            if (trajectory != null) {
                cannon.setPitch(-trajectory.angle() * Mth.RAD_TO_DEG);
            }
        }
        return active;
    }

    public static void onPlayerRotated(double yawIncrease, double pitchIncrease) {
        if (active && cannon != null) {
            Minecraft mc = Minecraft.getInstance();
            float scale = 0.2f;
            cannon.addRotation((float) yawIncrease * scale, 0);
            //TODO: fix these
            cameraYaw += yawIncrease * scale;
            cameraPitch += pitchIncrease * scale;
            cameraPitch = Mth.clamp(cameraPitch, -90, 90);
            needsToUpdateServer = true;
        }
    }


    public static void onKeyPressed(int key, int action, int modifiers) {
        if (action != GLFW.GLFW_PRESS) return;
        if (Minecraft.getInstance().options.keyShift.matches(key, action)) {
            turnOff();
        }
        if (Minecraft.getInstance().options.keyJump.matches(key, action)) {
            preferShootingDown = !preferShootingDown;
            needsToUpdateServer = true;
        }
        if (Minecraft.getInstance().options.keyAttack.matches(key, action)) {
            if (cannon != null && cannon.canFire()) {
                ModNetwork.CHANNEL.sendToServer(new ServerBoundSyncCannonPacket(
                        cannon.getYaw(0),
                        cannon.getPitch(0), cannon.getFirePower(), true, cannon.getBlockPos()));
            }
        }
    }


    public static void onMouseClicked(boolean attack) {
    }

    public static void onInputUpdate(Input input) {
        if (firstTick) {
            // resets input
            firstTick = false;
            input.down = false;
            input.jumping = false;
            input.up = false;
            input.left = false;
            input.right = false;
            input.shiftKeyDown = false;
            input.forwardImpulse = 0;
            input.leftImpulse = 0;
        }
    }


    public static void onClientTick(Minecraft mc) {
        if (!active) return;
        ClientLevel level = mc.level;
        if (level.getBlockEntity(cannonPos) instanceof CannonBlockTile tile && !tile.isRemoved()) {
            cannon = tile;

            if (needsToUpdateServer) {
                needsToUpdateServer = false;
                ModNetwork.CHANNEL.sendToServer(new ServerBoundSyncCannonPacket(
                        cannon.getYaw(0), cannon.getPitch(0), cannon.getFirePower(),
                        false, cannonPos));
            }
        } else {
            turnOff();
        }
    }

    public static void renderTrajectory(CannonBlockTile blockEntity, PoseStack poseStack, MultiBufferSource buffer,
                                        int packedLight, int packedOverlay, float partialTicks,
                                        float yaw) {
        //if (!active || cannon != blockEntity) return;
        if (hit != null && trajectory != null && !blockEntity.getProjectile().isEmpty()) {

            BlockPos cannonPos = blockEntity.getBlockPos();


            Minecraft mc = Minecraft.getInstance();
            boolean debug = !mc.showOnlyReducedInfo() && mc.getEntityRenderDispatcher().shouldRenderHitBoxes();


            poseStack.pushPose();

            //rotate so we can work in 2d
            Vec3 targetVector = hit.getLocation().subtract(cannonPos.getCenter());
            Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotation(-yaw));

            if (debug) renderTargetLine(poseStack, buffer, target);

            boolean hitAir = mc.level.getBlockState(trajectory.getHitPos(cannonPos, yaw)).isAir();

            renderArrows(poseStack, buffer, partialTicks,
                    trajectory, hitAir);

            poseStack.popPose();


            if (!hitAir) renderTargetCircle(poseStack, buffer, yaw);

            if (debug && hit instanceof BlockHitResult bh) renderTargetBlock(poseStack, buffer, cannonPos, bh);
        }
    }

    private static void renderTargetBlock(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos, BlockHitResult bh) {
        poseStack.pushPose();

        BlockPos targetPos = bh.getBlockPos();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        Vec3 distance1 = targetPos.getCenter().subtract(pos.getCenter());

        AABB bb = new AABB(distance1, distance1.add(1, 1, 1)).inflate(0.01);
        LevelRenderer.renderLineBox(poseStack, lines, bb, 1.0F, 0, 0, 1.0F);

        poseStack.popPose();
    }

    private static void renderTargetCircle(PoseStack poseStack, MultiBufferSource buffer, float yaw) {
        poseStack.pushPose();

        Material circleMaterial = ModMaterials.CANNON_TARGET_MATERIAL;
        VertexConsumer circleBuilder = circleMaterial.buffer(buffer, RenderType::entityCutout);

        Vec3 targetVec = new Vec3(0, trajectory.point().y, -trajectory.point().x).yRot(-yaw);
        poseStack.translate(targetVec.x + 0.5, targetVec.y + 0.5 + 0.05, targetVec.z + 0.5);

        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        int lu = LightTexture.FULL_BLOCK;
        int lv = LightTexture.FULL_SKY;
        VertexUtil.addQuad(circleBuilder, poseStack, -2f, -2f, 2f, 2f, lu, lv);
        poseStack.popPose();
    }

    private static void renderTargetLine(PoseStack poseStack, MultiBufferSource buffer, Vec2 target) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        consumer.vertex(matrix4f, 0, 0, 0).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, 0, target.y, -target.x).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix4f, 0.01f, target.y, -target.x).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(matrix4f, 0.01f, 0, 0).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
    }


    private static void renderArrows(PoseStack poseStack, MultiBufferSource buffer, float partialTicks, CannonTrajectory trajectory, boolean hitAir) {

        float finalTime = (float) trajectory.finalTime();
        if (finalTime > 100000) {
            Supplementaries.error();
            return;
        }

        poseStack.pushPose();

        float scale = 1;
        float size = 2.5f / 16f * scale;
        VertexConsumer consumer = buffer.getBuffer(ModRenderTypes.CANNON_TRAJECTORY);
        Matrix4f matrix = poseStack.last().pose();

        float py = 0;
        float px = 0;
        float d = -(System.currentTimeMillis() % 1000) / 1000f;
        float step = finalTime / (int) finalTime;
        float maxT = finalTime + (hitAir ? 0 : step);
        for (float t = step; t < maxT; t += step) {

            float textureStart = d % 1;
            consumer.vertex(matrix, -size, py, px)
                    .color(1, 1, 1, 1.0F)
                    .uv(0, textureStart)
                    .endVertex();
            consumer.vertex(matrix, size, py, px)
                    .color(1, 1, 1, 1.0F)
                    .uv(5 / 16f, textureStart)
                    .endVertex();

            double ny = trajectory.getY(t);
            double nx = -trajectory.getX(t);

            float dis = (float) (Mth.length(nx - px, ny - py)) / scale;
            float textEnd = textureStart + dis;

            d += dis;
            py = (float) ny;
            px = (float) nx;

            int alpha = (t + step >= maxT) ? 0 : 1;
            consumer.vertex(matrix, size, py, px)
                    .color(1f, 1f, 1f, alpha)
                    .uv(5 / 16f, textEnd)
                    .endVertex();
            consumer.vertex(matrix, -size, py, px)
                    .color(1f, 1f, 1f, alpha)
                    .uv(0, textEnd)
                    .endVertex();
        }

        poseStack.popPose();
    }

}

