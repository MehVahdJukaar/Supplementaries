package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
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

public class CannonCameraController {

    private static BlockPos cannonPos;
    private static boolean active;
    private static CameraType lastCameraType;
    private static CannonBlockTile cannon;
    private static HitResult hit;

    private static float cameraYaw;
    private static float cameraPitch;
    private static boolean preferShootingDown = true;

    private static Trajectory trajectory;


    // account for actual target
    private static float gravity = 0.01f;
    private static float drag = 0.96f;
    private static float initialPow = 1f;

    public static void activateCannonCamera(BlockPos pos) {
        active = true;
        preferShootingDown = true;
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


            trajectory = findBestAngle(target, gravity, drag, initialPow, 0.01f);

            cannon.setPitch(-trajectory.angle * Mth.RAD_TO_DEG);
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

    public static void onInputUpdate(Input input) {

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
        double bestPointTime = 0;
        boolean miss = true;

        float bestDistance = Float.MAX_VALUE;

        // Perform secant method iterations
        int i;
        for (i = 0; i < maxIterations; i++) {
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
            float distToTarget;
            // Update the best result if a closer point is found
            if (distance1 < distance2) {
                bestPoint = r1.getFirst();
                bestAngle = angle1;
                bestPointTime = r1.getSecond();
                angle2 = angle1;
                angle1 -= tolerance; // Move angle1 closer to the best angle
                distToTarget = distance1;
            } else {
                bestPoint = r2.getFirst();
                bestAngle = angle2;
                bestPointTime = r2.getSecond();
                angle1 = angle2;
                angle2 += tolerance; // Move angle2 closer to the best angle
                distToTarget = distance2;
            }

            bestDistance = distToTarget;
            // Check if the distance increase is below the tolerance. Good enough result
            float distanceIncrease = Math.abs(distance1 - distance2);

            if (distanceIncrease < tolerance) {
                break; // Stop iterating if the difference in distances is below the tolerance
            }
            // Check if we hit precisely where we aimed for
            if (distToTarget < tolerance) {
                miss = false;
                break; // Stop iterating if the difference in distances is below the tolerance
            }
        }

        return new Trajectory(bestPoint, bestAngle, bestPointTime, miss);
    }

    public static Trajectory findBestAngle(Vec2 targetPoint, float gravity, float drag, float initialPow, float tolerance) {
        float start = (float) (Math.atan2(targetPoint.y, targetPoint.x)) + 0.01f; // Initial angle
        float end = (float) Math.PI / 2; // Maximum angle (90 degrees)

        Vec2 farAway = targetPoint.scale(1000);
        Trajectory furthestTrajectory = findBestAngle(farAway, gravity, drag, initialPow, tolerance, start, end);
        float peakAngle = furthestTrajectory.angle;

        //that function has 2 solutions. we need to reduce the angles we search so we converge on the first one
        //we can do this by using as max angle the angle that yeilds the highest distance (global maxima of the distance function)
        Trajectory solution;
        if (preferShootingDown) {
            solution = findBestAngle(targetPoint, gravity, drag, initialPow, tolerance, start, peakAngle);
        } else {
            solution = findBestAngle(targetPoint, gravity, drag, initialPow, tolerance, peakAngle, end);
        }
        return solution;
    }

    /**
     * Calculate the best angle such that the resulting trajectory is closest to the target point
     * Uses Golden-section search
     *
     * @param targetPoint Target point
     * @param gravity     Gravity
     * @param drag        Drag (v multiplier)
     * @param initialPow  Initial velocity
     * @param tolerance   Tolerance for stopping the search
     * @return Trajectory object containing the best point, angle, time, and miss flag
     */
    public static Trajectory findBestAngle(Vec2 targetPoint, float gravity, float drag, float initialPow, float tolerance,
                                           float start, float end) {
        float targetSlope = targetPoint.y / targetPoint.x;

        // Define golden ratio
        final float goldenRatio = MthUtils.PHI - 1;

        // Initialize variables to store the best result
        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = start;
        double bestPointTime = 0;
        boolean miss = true;

        // Define the search interval
        float startAngle = start;
        float endAngle = end;


        float midAngle1 = startAngle + goldenRatio * (endAngle - startAngle);
        float midAngle2 = endAngle - goldenRatio * (endAngle - startAngle);

        // Perform golden-section search iterations
        int iterNumber = 0;
        while (Math.abs(endAngle - startAngle) > tolerance) {

            iterNumber++;
            // Calculate the velocities for the two intermediate angles
            float v0x1 = (float) (Math.cos(midAngle1) * initialPow);
            float v0y1 = (float) (Math.sin(midAngle1) * initialPow);
            float v0x2 = (float) (Math.cos(midAngle2) * initialPow);
            float v0y2 = (float) (Math.sin(midAngle2) * initialPow);

            // Find the intersection points for the two intermediate angles
            var r1 = findLineIntersection(targetSlope, gravity, drag, v0x1, v0y1);
            var r2 = findLineIntersection(targetSlope, gravity, drag, v0x2, v0y2);

            // Calculate distances from the target for the intersection points
            float distance1 = r1 != null ? targetPoint.distanceToSqr(r1.getFirst()) : Float.MAX_VALUE;
            float distance2 = r2 != null ? targetPoint.distanceToSqr(r2.getFirst()) : Float.MAX_VALUE;

            if (midAngle1 < midAngle2) {
                Supplementaries.error();
            }

            float lastBestDist = targetPoint.distanceToSqr(bestPoint);


            // Update the search interval based on the comparison of distances
            if (distance1 < distance2) {
                bestPoint = r1.getFirst();
                bestAngle = midAngle1;
                bestPointTime = r1.getSecond();

                if (distance1 > lastBestDist && iterNumber != 1) {
                    Supplementaries.error();
                }
                startAngle = midAngle2;
                midAngle2 = midAngle1;
                midAngle1 = startAngle + goldenRatio * (endAngle - startAngle);
            } else {
                bestPoint = r2.getFirst();
                bestAngle = midAngle2;
                bestPointTime = r2.getSecond();

                if (distance2 > lastBestDist && iterNumber != 1) {
                    Supplementaries.error();
                }

                endAngle = midAngle1;
                midAngle1 = midAngle2;
                midAngle2 = endAngle - goldenRatio * (endAngle - startAngle);
            }

            // Update the best result if a closer point is found

            //if (lastBestDist < tolerance) {
            //  miss = false;
            // break; // Stop iterating if the difference in distances is below the tolerance
            // }
        }

        return new Trajectory(bestPoint, bestAngle, bestPointTime, miss);
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
    public static Trajectory findBestAngleBruteForce(float step, Vec2 targetPoint, float gravity, float drag, float initialPow) {
        boolean exitEarly = true; //whether to grab first or second result. this doesnt work tho
        float stopDistance = 0.01f;
        float targetSlope = targetPoint.y / targetPoint.x;
        float start = (float) (Mth.RAD_TO_DEG * Mth.atan2(targetPoint.y, targetPoint.x)) + 0.01f; //pitch
        float end = 90;

        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = start;
        double bestPointTime = 0;
        boolean miss = true;
        for (float angle = start; angle < end; angle += step) {
            float rad = angle * Mth.DEG_TO_RAD;
            float v0x = Mth.cos(rad) * initialPow;
            float v0y = Mth.sin(rad) * initialPow;
            var r = findLineIntersection(targetSlope, gravity, drag, v0x, v0y);

            if (r != null) {
                //TODO: exit early when we flip. infact replace with binary search

                Vec2 landPoint = r.getFirst();
                float landDist = targetPoint.distanceToSqr(landPoint);
                float lastBestDist = targetPoint.distanceToSqr(bestPoint);
                if (landDist < lastBestDist) {

                    bestPoint = landPoint;
                    bestAngle = rad;
                    bestPointTime = r.getSecond();
                    if (landDist < stopDistance) {
                        miss = false;
                        if (exitEarly) break;
                    }
                }
            } else {
                Supplementaries.error();
            }
        }
        return new Trajectory(bestPoint, bestAngle, bestPointTime, miss);
    }

    private record Trajectory(Vec2 point, float angle, double finalTime, boolean miss) {
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

    public static Pair<Vec2, Double> findLineIntersection(float m, float g, float d, float V0x, float V0y) {
        return findLineIntersectionSlow(m, g, d, V0x, V0y);
    }

    /**
     * calculate intersection of line with projectile trajectory using secant method.
     * Note that this will struggle a lot with very steep functions
     *
     * @param m   line slope
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0x initial velocity
     * @param V0y initial velocity
     * @return intersection point
     */
    public static Pair<Vec2, Double> findLineIntersectionUnreliable(float m, float g, float d, float V0x, float V0y) {
        float slopeAt0 = V0y / V0x;
        if (slopeAt0 < m) {
            // no solution if line is steeper than projectile initial slope
            // should actually never occur as we always use angles steeper than that
            return null;
        }
        float tolerance = 0.01f; // Tolerance for convergence
        int maxIterations = 20; // Maximum number of iterations
        double t1 = 20f; // Initial guess for t1
        double t2 = 50000f; // Initial guess for t2. set big to avoid falling onto solution at 0

        // Apply the secant method to find the intersection
        double x1 = arcX(t1, g, d, V0x);
        double x2 = arcX(t2, g, d, V0x);
        double y1 = arcY(t1, g, d, V0y);
        double y2 = arcY(t2, g, d, V0y);

        //(9,10) (11,10)
        double xNew = 0;
        double yNew = 0;
        double tNew = 0;
        for (int iter = 0; iter < maxIterations && t1 != t2; iter++) {

            tNew = t2 - ((y2 - m * x2) * (t2 - t1)) / ((y2 - y1) - m * (x2 - x1));

            if (!Double.isFinite(tNew)) {
                break;
            }

            xNew = arcX(tNew, g, d, V0x);
            yNew = arcY(tNew, g, d, V0y);


            // Compute the error between the line and the point
            double error = yNew - m * xNew;

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
        if (tNew < 0) {
            int error = 0;
            //should never happen
            //return null;
        }
        return Pair.of(new Vec2((float) xNew, (float) yNew), tNew);
    }

    /**
     * Bisection (binary search) method. Slower but doesn't fail with steep functions due to double limit
     */
    public static Pair<Vec2, Double> findLineIntersectionSlow(float m, float g, float d, float V0x, float V0y) {
        float slopeAt0 = V0y / V0x;
        if (slopeAt0 < m) {
            return null;
        }
        double low = 2;//getPeakTime(g, d, V0y); // Initial lower bound for binary search
        double high = 1000; // Initial upper bound for binary search
        float precision = 0.01f; // Precision for terminating the search

        // Perform binary search
        int iter = 0;
        int maxIter = 1000;
        while (iter++ < maxIter) {
            double midTime = (low + high) / 2.0; // Calculate midpoint

            double yNew = arcY(midTime, g, d, V0y); // Calculate y value of the curve at midpoint
            double xNew = arcX(midTime, g, d, V0x);
            double yLine = m * xNew; // Calculate y value of the line at midpoint

            // Check if we have found the intersection
            if (Math.abs(yNew - yLine) < precision) {
                //print iter number
                return Pair.of(new Vec2((float) xNew, (float) yNew), midTime); // Return the value of t at the intersection
            } else if (yNew > yLine) {
                low = midTime; // Adjust lower bound if y value of curve is less than y value of line
            } else {
                high = midTime; // Adjust upper bound if y value of curve is greater than y value of line
            }
        }

        return null;// (low + high) / 2.0f; // Return the approximate value of t at the intersection
    }

    /**
     * The equation for the Y position of the projectile in terms of time
     *
     * @param t   time
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0y initial velocity
     */
    public static double arcY(double t, float g, float d, float V0y) {
        float k = g / (d - 1);
        double inLog = 1 / Math.log(d);
        return ((V0y - k) * inLog * (Math.pow(d, t) - 1) + k * t);
    }

    /**
     * The equation for the X position of the projectile in terms of time
     *
     * @param t   time
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0x initial velocity
     */
    public static double arcX(double t, float g, float d, float V0x) {
        double inLog = 1 / Math.log(d);

        return (V0x * inLog * (Math.pow(d, t) - 1));
    }


    private static void renderArrows(float gravity, float drag, float initialPow, float angle, double doubleFinalTime,
                                     boolean miss,
                                     float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {

        float finalTime = (float) doubleFinalTime;
        if (finalTime > 100000) {
            int error = 1;
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

