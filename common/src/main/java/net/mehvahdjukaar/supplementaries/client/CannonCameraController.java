package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;

public class CannonCameraController {

    private static BlockPos cannonPos;
    private static boolean active;
    private static CameraType lastCameraType;
    private static CannonBlockTile cannon;
    private static HitResult hit;

    private static float cameraYaw;
    private static float cameraPitch;

    public static void activateCannonCamera(BlockPos pos) {
        active = true;
        cannonPos = pos;
        Minecraft mc = Minecraft.getInstance();
        lastCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        mc.gui.setOverlayMessage(Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage()), false);
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean setupCamera(Camera camera, BlockGetter level, Entity entity,
                                      boolean detached, boolean thirdPersonReverse, float partialTick) {
        if (active && cannon != null) {
            float yaw = cannon.getYaw(partialTick);
            float pitch = cannon.getPitch(partialTick);

            Vec3 start = cannonPos.getCenter().add(0, 2, 0);

            //base pos. assume empty
            camera.setPosition(start);

            camera.setRotation(180 + yaw, cameraPitch);

            camera.move(-camera.getMaxZoom(4), 0, 0);


            // find hit result
            Vec3 lookDir2 = Vec3.directionFromRotation(-cameraPitch, yaw);
            float maxRange = 32;
            Vec3 endPos = start.add(lookDir2.scale(-maxRange));

            BlockHitResult hitResult = level
                    .clip(new ClipContext(start, endPos,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));

            if (hitResult.getType() == BlockHitResult.Type.BLOCK) {
                endPos = hitResult.getLocation();
            }
            hit = hitResult;
        }
        return active;
    }

    public static void onKeyPressed(int key, int action, int modifiers) {
        if (Minecraft.getInstance().options.keyShift.matches(key, action)) {
            turnOff();
        }
    }

    private static void turnOff() {
        active = false;
        if (lastCameraType != null) {
            Minecraft.getInstance().options.setCameraType(lastCameraType);
        }
    }

    public static void onMouseClicked(boolean attack) {
        if (isActive()) {

        }
    }

    public static void onInputUpdate(Input instance) {
    }

    public static void onPlayerRotated(double yawIncrease, double pitchIncrease) {
        Minecraft mc = Minecraft.getInstance();
        float scale = 0.2f;
        if (mc.level.getBlockEntity(cannonPos) instanceof CannonBlockTile tile) {
            tile.addRotation((float) yawIncrease * scale, (float) pitchIncrease * scale);
        }
        cameraYaw += yawIncrease;
        cameraPitch += pitchIncrease * scale;
        cameraPitch = Mth.clamp(cameraPitch, -90, 90);
    }


    public static void onClientTick(Minecraft mc) {
        cannon = null;
        if (active) {
            ClientLevel level = mc.level;
            if (level.getBlockEntity(cannonPos) instanceof CannonBlockTile tile) {
                cannon = tile;

            } else {
                turnOff();
            }
        }
    }

    public static void renderTrajectory(CannonBlockTile blockEntity, PoseStack poseStack, MultiBufferSource buffer,
                                        int packedLight, int packedOverlay) {
        if (active && hit != null && blockEntity == cannon) {
            Material material = ModMaterials.CANNON_TARGET_MATERIAL;
            var builder = material.buffer(buffer, RenderType::entityCutout);
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();
            int upLight = LevelRenderer.getLightColor(level, pos.above(2));
            int lu = upLight & '\uffff';
            int lv = upLight >> 16 & '\uffff';

            if (hit instanceof BlockHitResult bh) {
                poseStack.pushPose();
                Vec3 targetVector = hit.getLocation().subtract(pos.getCenter());

                targetVector = new Vec3(targetVector.x + 0.5, targetVector.y + 0.5, targetVector.z + 0.5);
                poseStack.translate(targetVector.x, targetVector.y + 0.01, targetVector.z);

                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                VertexUtil.addQuad(builder, poseStack, -2f, -2f, 2f, 2f, lu, lv);
                poseStack.popPose();


                BlockPos targetPos = bh.getBlockPos();
                VertexConsumer lines = buffer.getBuffer(RenderType.lines());
                poseStack.pushPose();
                Vec3 distance1 = targetPos.getCenter().subtract(pos.getCenter());

                AABB bb = new AABB(distance1, distance1.add(1, 1, 1)).inflate(0.01);
                LevelRenderer.renderLineBox(poseStack, lines, bb, 1.0F, 0, 0, 1.0F);
                poseStack.popPose();

                //rotate so we can work in 2d

                poseStack.pushPose();

                poseStack.mulPose(Axis.YP.rotation((float) (Mth.atan2(targetVector.y, targetVector.x))));
                Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);

                // account for actual target
           //     var trajectory = findBestAngle(0.1f, target, 0.01f, 0.96f, 1f);

                renderLeash(new Vec3(0, 0, 0), new Vec3(target.x, target.y, 0), 0, poseStack, buffer, packedLight);

                poseStack.popPose();
            }
        }
    }

    /**
     * calculate the best angle to shoot a projectile at to hit a target, maximising distance to target point
     *
     * @param step        iteration step
     * @param targetPoint target point
     * @param gravity     gravity
     * @param drag        drag (v multiplier)
     * @param initialPow  initial velocity
     */
    public static TrajectoryResult findBestAngle(float step, Vec2 targetPoint, float gravity, float drag, float initialPow) {
        float targetSlope = targetPoint.y / targetPoint.x;
        float start = (float) (Mth.RAD_TO_DEG * Math.atan(targetSlope)); //pitch
        float end = 90;

        Vec2 best = null;
        double prevLen = 0;
        float bestAngle = start;
        for (float angle = start; angle < end; angle += step) {
            float rad = angle * Mth.DEG_TO_RAD;
            float v0x = Mth.cos(rad) * initialPow;
            float v0y = Mth.sin(rad) * initialPow;
            Vec2 v = findLineIntersection(targetSlope, gravity, drag, v0x, v0y);

            if (v != null) {
                //TODO: exit early when we flip. infact replace with binary search
                double len = Mth.lengthSquared(v.x, v.y);
                if (len > prevLen) {
                    prevLen = len;
                    best = v;
                    bestAngle = angle;
                }
            } else {
                int error = 0;
            }
        }
        return new TrajectoryResult(best, bestAngle);
    }

    private record TrajectoryResult(Vec2 point, float angle) {
    }


    public void runEvery1Second() {

        // pos = pos + velocity

        // velocity = velocity*0.99


        // velocity = velocity + Vec3(0, -0.05, 0)

        // d = 0.99
        // g = 0.05
        // vely = d^t - g*(d^t-1)/(d-1)
        // y = (1-g/d-1)*1/ln(d)*d^t+t*g/(d-1)
    }


    /**
     * calculate intersection of line with projectile trajectory using secant method
     *
     * @param m   line slope
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0x initial velocity
     * @param V0y initial velocity
     * @return intersection point
     */
    public static Vec2 findLineIntersection(float m, float g, float d, float V0x, float V0y) {
        float slopeAt0 = V0y / V0x;
        if (slopeAt0 < m) {
            // no solution if line is steeper than projectile initial slope
            // should actually never occur as we always use angles steeper than that
            return null;
        }
        float tolerance = 0.01f; // Tolerance for convergence
        int maxIterations = 20; // Maximum number of iterations
        float t1 = 20f; // Initial guess for t1
        float t2 = 50000f; // Initial guess for t2. set big to avoid falling onto solution at 0

        // Apply the secant method to find the intersection
        float x1 = arcX(t1, g, d, V0x);
        float x2 = arcX(t2, g, d, V0x);
        float y1 = arcY(t1, g, d, V0y);
        float y2 = arcY(t2, g, d, V0y);

        for (int iter = 0; iter < maxIterations; iter++) {
            float tNew = t2 - ((y2 - m * x2) * (t2 - t1)) / ((y2 - y1) - m * (x2 - x1));

            float xNew = arcX(tNew, g, d, V0x);
            float yNew = arcY(tNew, g, d, V0y);

            // Compute the error between the line and the point
            float error = yNew - m * xNew;

            // Check for convergence
            if (Math.abs(error) < tolerance) {
                return new Vec2(xNew, yNew);
            }

            // Update the values for the next iteration
            t1 = t2;
            t2 = tNew;
            x1 = x2;
            x2 = xNew;
            y1 = y2;
            y2 = yNew;
        }
        // return best approximation
        // should also never occur
        return new Vec2(x2, y2);
    }


    /**
     * The equation for the Y position of the projectile in terms of time
     *
     * @param t   time
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0y initial velocity
     */
    public static float arcY(float t, float g, float d, float V0y) {
        float k = g / (d - 1);
        double inLog = 1 / Math.log(d);
        return (float) ((V0y - k) * inLog * (Math.pow(d, t) - 1) + k * t);
    }

    /**
     * The equation for the X position of the projectile in terms of time
     *
     * @param t   time
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0x initial velocity
     */
    public static float arcX(float t, float g, float d, float V0x) {
        double inLog = 1 / Math.log(d);

        return (float) (inLog * (Math.pow(d, t) - 1) + V0x * t);
    }

    /*
    public static float estimateBestAngle(Vec2 targetPos, float vel, float g, int maxIter, Vec2 bestOne, float minAngle, float maxAngle) {

        while (maxIter-- > 0) {
            float angle = (minAngle + maxAngle) / 2;
            Vec2 currentVec = calculateFurthestPoint(targetPos, angle, vel, g);
            float currentError = currentVec.distanceToSqr(targetPos);
            float oldError = bestOne.distanceToSqr(targetPos);
            if (currentError < oldError) {
                bestOne = currentVec;
                maxAngle = angle;
            } else {
                minAngle = angle;
            }
        }
    }

    public static Vec2 calculateFurthestPoint(Vec2 targetPos, float angle, float vel, float g) {

        // Equation of the line passing through origin and target position: y = mx
        float m = targetPos.y / targetPos.x;
        System.out.println("Equation of the line passing through origin and target position:");
        System.out.println("y = " + m + "x");
        //x = (y) / m

        float c = Mth.cos(angle*Mth.DEG_TO_RAD);
        float s = Mth.sin(angle*Mth.DEG_TO_RAD);
        float y = (c * m - s) * (2 * m * c * vel * vel) / g;
        float x = y / m;
        return new Vec2(x, y);
    }*/



    /*
    public static void reset(){
        if (resetOverlaysAfterDismount) {
            resetOverlaysAfterDismount = false;
            OverlayToggleHandler.disable(ClientHandler.cameraOverlay);
            OverlayToggleHandler.enable(VanillaGuiOverlay.JUMP_BAR);
            OverlayToggleHandler.enable(VanillaGuiOverlay.EXPERIENCE_BAR);
            OverlayToggleHandler.enable(VanillaGuiOverlay.POTION_ICONS);
        }
    }

    public class OverlayToggleHandler {
        private static final Map<IGuiOverlay, Boolean> OVERLAY_STATES = new HashMap<>();

        private OverlayToggleHandler() {}

        @SubscribeEvent
        public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
            IGuiOverlay overlay = event.getOverlay().overlay();

            if (OVERLAY_STATES.containsKey(overlay) && !isEnabled(overlay))
                event.setCanceled(true);
        }

        public static boolean isEnabled(VanillaGuiOverlay overlay) {
            return isEnabled(GuiOverlayManager.findOverlay(overlay.id()).overlay());
        }

        public static boolean isEnabled(NamedGuiOverlay overlay) {
            return isEnabled(overlay.overlay());
        }

        public static boolean isEnabled(IGuiOverlay overlay) {
            return OVERLAY_STATES.get(overlay);
        }

        public static void enable(VanillaGuiOverlay overlay) {
            enable(overlay.type().overlay());
        }

        public static void enable(NamedGuiOverlay overlay) {
            enable(overlay.overlay());
        }

        public static void enable(IGuiOverlay overlay) {
            OVERLAY_STATES.put(overlay, true);
        }

        public static void disable(VanillaGuiOverlay overlay) {
            disable(overlay.type().overlay());
        }

        public static void disable(NamedGuiOverlay overlay) {
            disable(overlay.overlay());
        }

        public static void disable(IGuiOverlay overlay) {
            OVERLAY_STATES.put(overlay, false);
        }
    }*/


    private static void renderLeash(Vec3 startPos, Vec3 endPos, float partialTicks, PoseStack matrixStack,
                                                MultiBufferSource buffer, int light) {
        matrixStack.pushPose();

        float px = (float) -endPos.x;
        float py = (float) -endPos.y;
        float pz = (float) -endPos.z;
        matrixStack.translate(startPos.x, startPos.y, startPos.z);

        float size = 0.025F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = matrixStack.last().pose();
        float n = (Mth.invSqrt(px * px + pz * pz) * size / 2.0F);
        float o = (pz * n);
        float p = (px * n);

        int maxSegments = 24;
        for (int iterNumber = 0; iterNumber <= maxSegments; ++iterNumber) {
            addVertexPair(vertexConsumer, matrix4f, px, py, pz, light, size, size, o, p, iterNumber / maxSegments, false);
        }

        for (int iterNumber = maxSegments; iterNumber >= 0; --iterNumber) {
            addVertexPair(vertexConsumer, matrix4f, px, py, pz, light, size, 0.0F, o, p, iterNumber / maxSegments, true);
        }

        matrixStack.popPose();
    }

    private static void addVertexPair(VertexConsumer consumer, Matrix4f matrix, float px, float py, float pz,
                                      int light, float m, float n, float o, float p,
                                      int r, boolean flipped) {

        int q = 1;
        float v = q % 2 == (flipped ? 1 : 0) ? 0.7F : 1.0F;
        float red = 0.5F * v;
        float green = 0.4F * v;
        float blue = 0.3F * v;
        float z = px * r;
        float aa = py > 0.0F ? py * r * r : py - py * (1.0F - r) * (1.0F - r);
        float ab = pz * r;
        consumer.vertex(matrix, z - o, aa + n, ab + p).color(red, green, blue, 1.0F).uv2(light).endVertex();
        consumer.vertex(matrix, z + o, aa + m - n, ab - p).color(red, green, blue, 1.0F).uv2(light).endVertex();
    }
}

