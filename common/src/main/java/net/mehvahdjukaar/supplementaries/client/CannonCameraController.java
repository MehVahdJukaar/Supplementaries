package net.mehvahdjukaar.supplementaries.client;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
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

public class CannonCameraController {

    private static BlockPos cannonPos;
    private static boolean active;
    private static CameraType lastCameraType;
    private static CannonBlockTile cannon;
    private static HitResult hit;

    private static float cameraYaw;
    private static float cameraPitch;

    private static Trajectory trajectory;


    // account for actual target
    private static float gravity = 0.01f;
    private static float drag = 0.96f;
    private static float initialPow = 1f;

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

            Stopwatch watch = Stopwatch.createStarted();
            trajectory = findBestAngle(0.01f, target, gravity, drag, initialPow);
            long l = watch.elapsed().getNano();
            watch.reset();
            watch.start();
            var tr2 = findBestAngle(target, gravity, drag, initialPow, 0.1f, 20);
            long l2 = watch.elapsed().getNano();

            cannon.setPitch(-trajectory.angle * Mth.RAD_TO_DEG);
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
            tile.addRotation((float) yawIncrease * scale, 0);
        }
        //TODO: fix these
        cameraYaw += yawIncrease * scale;
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
                                        int packedLight, int packedOverlay, float partialTicks,
                                        float yaw) {
        // if (!active || cannon != blockEntity) return;
        if (hit != null) {
            Material circleMaterial = ModMaterials.CANNON_TARGET_MATERIAL;
            VertexConsumer circleBuilder = circleMaterial.buffer(buffer, RenderType::entityCutout);
            BlockPos pos = blockEntity.getBlockPos();
            int lu = LightTexture.FULL_BLOCK;
            int lv = LightTexture.FULL_SKY;

            if (hit instanceof BlockHitResult bh) {
                Vec3 targetVector = hit.getLocation().subtract(pos.getCenter());


                //rotate so we can work in 2d
                Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);

                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);

                poseStack.mulPose(Axis.YP.rotation(-yaw));

                VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
                Matrix4f matrix4f = poseStack.last().pose();
                Matrix3f matrix3f = poseStack.last().normal();
                consumer.vertex(matrix4f, 0, 0, 0).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
                consumer.vertex(matrix4f, 0, target.y, -target.x).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
                consumer.vertex(matrix4f, 0.01f, target.y, -target.x).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix4f, 0.01f, 0, 0).color(255, 0, 0, 255).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();


                boolean miss = hit.getType() != HitResult.Type.BLOCK && trajectory.miss;

                renderArrows(gravity, drag, initialPow, trajectory.angle, trajectory.finalTime, miss,
                        partialTicks, poseStack, buffer, packedLight);

                poseStack.popPose();


                if (miss) return;
                poseStack.pushPose();

                Vec3 targetVec = new Vec3(0, trajectory.point.y, -trajectory.point.x).yRot(-yaw);
                poseStack.translate(targetVec.x + 0.5, targetVec.y + 0.5 + 0.01, targetVec.z + 0.5);

                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                VertexUtil.addQuad(circleBuilder, poseStack, -2f, -2f, 2f, 2f, lu, lv);
                poseStack.popPose();


                BlockPos targetPos = bh.getBlockPos();
                VertexConsumer lines = buffer.getBuffer(RenderType.lines());
                poseStack.pushPose();
                Vec3 distance1 = targetPos.getCenter().subtract(pos.getCenter());

                AABB bb = new AABB(distance1, distance1.add(1, 1, 1)).inflate(0.01);
                LevelRenderer.renderLineBox(poseStack, lines, bb, 1.0F, 0, 0, 1.0F);

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
    public static Trajectory findBestAngle(float step, Vec2 targetPoint, float gravity, float drag, float initialPow) {
        boolean exitEarly = true; //whether to grab first or second result. this doesnt work tho
        float stopDistance = 0.01f;
        float targetSlope = targetPoint.y / targetPoint.x;
        float start = (float) (Mth.RAD_TO_DEG * Mth.atan2(targetPoint.y, targetPoint.x)) + 0.01f; //pitch
        float end = 90;

        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = start;
        float bestPointTime = 0;
        boolean miss = true;
        for (float angle = start; angle < end; angle += step) {
            float rad = angle * Mth.DEG_TO_RAD;
            float v0x = Mth.cos(rad) * initialPow;
            float v0y = Mth.sin(rad) * initialPow;
            var r = findLineIntersection(targetSlope, gravity, drag, v0x, v0y);

            if (r != null) {
                //TODO: exit early when we flip. infact replace with binary search

                Vec2 landPoint = r.getFirst();
                float distance = targetPoint.distanceToSqr(landPoint);
                if (distance < targetPoint.distanceToSqr(bestPoint)) {

                    bestPoint = landPoint;
                    bestAngle = rad;
                    bestPointTime = r.getSecond();
                    if (distance < stopDistance) {
                        miss = false;
                        if (exitEarly) break;
                    }
                }
            } else {
                int error = 0;
            }
        }
        return new Trajectory(bestPoint, bestAngle, bestPointTime, miss);
    }

    /**
     * Calculate the best angle to shoot a projectile at to hit a target, maximizing the distance to the target point.
     * Uses Secant method
     *
     * @param targetPoint   Target point
     * @param gravity       Gravity
     * @param drag          Drag (v multiplier)
     * @param initialPow    Initial velocity
     * @param tolerance     Tolerance for stopping the secant method
     * @param maxIterations Maximum number of iterations for the secant method
     * @return Trajectory object containing the best point, angle, time, and miss flag
     */
    public static Trajectory findBestAngle(Vec2 targetPoint, float gravity, float drag, float initialPow,
                                           float tolerance, int maxIterations) {
        float targetSlope = targetPoint.y / targetPoint.x;
        float startAngle = (float) (Math.atan2(targetPoint.y, targetPoint.x)) + 0.01f;
        float endAngle = (float) Math.PI / 2; // Maximum angle (90 degrees)

        // Initial guesses for the secant method
        float angle1 = startAngle;
        float angle2 = endAngle;

        // Initialize variables to store the best result
        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = startAngle;
        float bestPointTime = 0;
        boolean miss = true;

        // Perform secant method iterations
        for (int i = 0; i < maxIterations; i++) {
            // Calculate the velocities for the two angles
            float v0x1 = (float) (Math.cos(angle1) * initialPow);
            float v0y1 = (float) (Math.sin(angle1) * initialPow);
            float v0x2 = (float) (Math.cos(angle2) * initialPow);
            float v0y2 = (float) (Math.sin(angle2) * initialPow);

            // Calculate the intersection points for the two angles
            var r1 = findLineIntersection(targetSlope, gravity, drag, v0x1, v0y1);
            var r2 = findLineIntersection(targetSlope, gravity, drag, v0x2, v0y2);

            // Calculate distances from the target for the intersection points
            float distance1 = r1 != null ? targetPoint.distanceToSqr(r1.getFirst()) : Float.MAX_VALUE;
            float distance2 = r2 != null ? targetPoint.distanceToSqr(r2.getFirst()) : Float.MAX_VALUE;

            // Update the best result if a closer point is found
            if (distance1 < distance2) {
                bestPoint = r1.getFirst();
                bestAngle = angle1;
                bestPointTime = r1.getSecond();
                angle2 = angle1;
                angle1 -= tolerance; // Move angle1 closer to the best angle
            } else {
                bestPoint = r2.getFirst();
                bestAngle = angle2;
                bestPointTime = r2.getSecond();
                angle1 = angle2;
                angle2 += tolerance; // Move angle2 closer to the best angle
            }

            // Check if the distance is below the tolerance
            if (Math.abs(distance1 - distance2) < tolerance) {
                miss = false;
                break; // Stop iterating if the difference in distances is below the tolerance
            }
        }

        return new Trajectory(bestPoint, bestAngle, bestPointTime, miss);
    }

    private record Trajectory(Vec2 point, float angle, float finalTime, boolean miss) {
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
    public static Pair<Vec2, Float> findLineIntersection(float m, float g, float d, float V0x, float V0y) {
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

        float xNew = 0;
        float yNew = 0;
        float tNew = 0;
        for (int iter = 0; iter < maxIterations; iter++) {
            tNew = t2 - ((y2 - m * x2) * (t2 - t1)) / ((y2 - y1) - m * (x2 - x1));

            xNew = arcX(tNew, g, d, V0x);
            yNew = arcY(tNew, g, d, V0y);

            // Compute the error between the line and the point
            float error = yNew - m * xNew;

            // Check for convergence
            if (Math.abs(error) < tolerance) {
                break;
            }

            // Update the values for the next iteration
            t1 = t2;
            t2 = tNew;
            x1 = x2;
            x2 = xNew;
            y1 = y2;
            y2 = yNew;
        }
        return Pair.of(new Vec2(xNew, yNew), tNew);
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

        return (float) (V0x * inLog * (Math.pow(d, t) - 1));
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

    private static void renderArrows(float gravity, float drag, float initialPow, float angle, float finalTime,
                                     boolean miss,
                                     float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
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

            float ny = arcY(t, gravity, drag, Mth.sin(angle) * initialPow);
            float nx = -arcX(t, gravity, drag, Mth.cos(angle) * initialPow);

            float dis = (float) (Mth.length(nx - px, ny - py)) / scale;
            float textEnd = textureStart + dis;

            d += dis;
            py = ny;
            px = nx;

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

