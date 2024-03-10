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
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundSyncCannonRotationPacket;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static net.mehvahdjukaar.supplementaries.client.cannon.CannonTrajectory.*;

public class CannonController {

    private static BlockPos cannonPos;
    private static boolean active;
    private static CameraType lastCameraType;
    private static CannonBlockTile cannon;
    private static HitResult hit;
    private static boolean firstTick = true;

    private static float cameraYaw;
    private static float cameraPitch;
    private static boolean preferShootingDown = true;

    private static CannonTrajectory trajectory;


    // account for actual target
    private static float gravity = 0.01f;
    private static float drag = 0.96f;
    private static float initialPow = 1f;

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
        gravity = 0.03f;
        drag = 0.99F;
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
            float maxRange = 32;
            Vec3 endPos = start.add(lookDir2.scale(-maxRange));

            BlockHitResult hitResult = level
                    .clip(new ClipContext(start, endPos,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));

            hit = hitResult;


            Vec3 targetVector = hit.getLocation().subtract(cannon.getBlockPos().getCenter());
            //rotate so we can work in 2d
            Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);


            trajectory = findBestTrajectory(target, gravity, drag, initialPow, 0.01f, preferShootingDown);

            cannon.setPitch(-trajectory.angle() * Mth.RAD_TO_DEG);
        }
        return active;
    }

    public static void onKeyPressed(int key, int action, int modifiers) {
        if (Minecraft.getInstance().options.keyShift.matches(key, action)) {
            turnOff();
        }
        if (action == 0 && Minecraft.getInstance().options.keyJump.matches(key, action)) {
            preferShootingDown = !preferShootingDown;
        }
    }


    public static void onMouseClicked(boolean attack) {
        if (isActive()) {

        }
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

    public static void onPlayerRotated(double yawIncrease, double pitchIncrease) {
        Minecraft mc = Minecraft.getInstance();
        float scale = 0.2f;
        if (mc.level.getBlockEntity(cannonPos) instanceof CannonBlockTile tile) {
            tile.addRotation((float) yawIncrease * scale, 0);
        }
        //TODO: fix these
        cameraYaw += yawIncrease * scale;
        cameraPitch += pitchIncrease * scale;
        cameraPitch = Mth.clamp(cameraPitch, -90, 90);
    }


    public static void onClientTick(Minecraft mc) {
        if (active) {
            ClientLevel level = mc.level;
            if (level.getBlockEntity(cannonPos) instanceof CannonBlockTile tile) {
                cannon = tile;

                //TODO: optimize, dont send it didnt change
                ModNetwork.CHANNEL.sendToServer(new ServerBoundSyncCannonRotationPacket(
                        cannon.getYaw(0), cannon.getPitch(0), cannonPos));
            } else {
                turnOff();
            }
        }
    }

    public static void renderTrajectory(CannonBlockTile blockEntity, PoseStack poseStack, MultiBufferSource buffer,
                                        int packedLight, int packedOverlay, float partialTicks,
                                        float yaw) {
        // if (!active || cannon != blockEntity) return;
        if (hit != null) {

            BlockPos pos = blockEntity.getBlockPos();

            if (hit instanceof BlockHitResult bh) {

                Minecraft mc = Minecraft.getInstance();
                boolean debug = !mc.showOnlyReducedInfo() && mc.getEntityRenderDispatcher().shouldRenderHitBoxes();


                poseStack.pushPose();

                //rotate so we can work in 2d
                Vec3 targetVector = hit.getLocation().subtract(pos.getCenter());
                Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Axis.YP.rotation(-yaw));

                if (debug) renderTargetLine(poseStack, buffer, target);

                boolean missedBlock = hit.getType() != HitResult.Type.BLOCK && trajectory.miss();

                renderArrows(poseStack, buffer, partialTicks,
                        gravity, drag, initialPow, trajectory.angle(), trajectory.finalTime(), missedBlock);

                poseStack.popPose();


                if (!missedBlock) {
                    renderTargetCircle(poseStack, buffer, yaw);

                    if (debug) renderTargetBlock(poseStack, buffer, pos, bh);
                }
            }
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
        poseStack.translate(targetVec.x + 0.5, targetVec.y + 0.5 + 0.01, targetVec.z + 0.5);

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


    private static void renderArrows(PoseStack poseStack, MultiBufferSource buffer, float partialTicks,
                                     float gravity, float drag, float initialPow, float angle,
                                     double doubleFinalTime, boolean miss) {

        float finalTime = (float) doubleFinalTime;
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
        float maxT = finalTime + (miss ? 0 : step);
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

            double ny = arcY(t, gravity, drag, Mth.sin(angle) * initialPow);
            double nx = -arcX(t, gravity, drag, Mth.cos(angle) * initialPow);

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

