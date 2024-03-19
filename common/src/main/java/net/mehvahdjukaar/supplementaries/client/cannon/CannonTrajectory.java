package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record CannonTrajectory(Vec2 point, float angle, double finalTime, boolean miss,
                               float gravity, float drag, float v0x, float v0y) {

    public static CannonTrajectory of(Vec2 point, float angle, double finalTime, boolean miss, float gravity, float drag, float pow) {
        return new CannonTrajectory(point, angle, finalTime, miss, gravity, drag, (float) (Math.cos(angle) * pow), (float) (Math.sin(angle) * pow));
    }

    @Nullable
    public static CannonTrajectory findBestTrajectory(Vec2 targetPoint, float gravity, float drag, float initialPow,
                                                      boolean preferShootingDown) {

        double targetAngle = Math.atan2(targetPoint.y, targetPoint.x);

        if (gravity == 0) {
            // simple line
            float finalDist = targetPoint.length();
            double ld = Math.log(drag);

            // inverse equation with no gravity
            double arg = 1 + finalDist * ld / initialPow;
            double t;
            boolean miss = false;
            if (arg < 0) {
                // we cant reach
                miss = true;
                // that number is slope at which we stop time
                t = Math.log(0.4 / initialPow) / Math.log(drag);
            } else t = Math.log(arg) / ld;

            float v0x = Mth.cos((float) targetAngle) * initialPow;
            float v0y = Mth.sin((float) targetAngle) * initialPow;

            float arcx = (float) arcX(t, gravity, drag, v0x);
            float arcy = (float) arcY(t, gravity, drag, v0y);

            Vec2 pointHit = new Vec2(arcx, arcy);
            return new CannonTrajectory(pointHit, (float) targetAngle, t,
                    miss, gravity, drag, v0x, v0y);
        }

        if (initialPow == 0) return null;
        float tolerance = 0.001f;

        float start = (float) targetAngle + 0.01f; // Initial angle
        float end = (float) Math.PI / 2; // Maximum angle (90 degrees)

        Vec2 farAway = targetPoint.scale(1000);
        // calculate trajectory that gives max distance = global maxima. 2 roots we need are either to its right or left angle wise
        CannonTrajectory furthestTrajectory = findBestTrajectoryGoldenSection(farAway, gravity, drag, initialPow,
                0.01f,
                tolerance, start, end);
        float peakAngle = furthestTrajectory.angle();

        // that function has 2 solutions. we need to reduce the angles we search, so we converge on the first one
        // we can do this by using as max angle the angle that yields the highest distance (global maxima of the distance function)
        CannonTrajectory solution;
        if (preferShootingDown) {
            solution = findBestTrajectoryGoldenSection(targetPoint, gravity, drag, initialPow,
                    0.001f,
                    tolerance, start, peakAngle);
        } else {
            solution = findBestTrajectoryGoldenSection(targetPoint, gravity, drag, initialPow,
                    0.001f,
                    tolerance, peakAngle, end);
        }
        return solution;
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
    private static CannonTrajectory findBestTrajectoryBruteForce(float step, Vec2 targetPoint, float gravity,
                                                                 float drag, float initialPow) {
        boolean exitEarly = true; //whether to grab first or second result. this doesnt work tho
        float stopDistance = 0.01f;
        float targetSlope = targetPoint.y / targetPoint.x;
        float start = (float) (Mth.RAD_TO_DEG * Mth.atan2(targetPoint.y, targetPoint.x)) + 0.01f; //pitch
        float end = 90;

        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = start;
        double bestPointTime = 0;
        float bestV0x = 0;
        float bestV0y = 0;

        boolean miss = true;


        for (float angle = start; angle < end; angle += step) {
            float rad = angle * Mth.DEG_TO_RAD;
            float v0x = Mth.cos(rad) * initialPow;
            float v0y = Mth.sin(rad) * initialPow;
            var r = findLineIntersection(targetSlope, gravity, drag, v0x, v0y, stopDistance);

            if (r != null) {

                Vec2 landPoint = r.getFirst();
                float landDist = targetPoint.distanceToSqr(landPoint);
                float lastBestDist = targetPoint.distanceToSqr(bestPoint);
                if (landDist < lastBestDist) {

                    bestPoint = landPoint;
                    bestAngle = rad;
                    bestPointTime = r.getSecond();
                    bestV0x = v0x;
                    bestV0y = v0y;
                    if (landDist < stopDistance) {
                        miss = false;
                        bestPoint = targetPoint;
                        if (exitEarly) break;
                    }

                }
            } else {
                Supplementaries.error();
            }
        }
        return new CannonTrajectory(bestPoint, bestAngle, bestPointTime, miss, gravity, drag, bestV0x, bestV0y);
    }


    /**
     * Calculate the best angle to shoot a projectile at to hit a target, maximizing the distance to the target point.
     * Uses Secant method. Very fast. can only work when theres a global maxima
     *
     * @param targetPoint   Target point
     * @param gravity       Gravity
     * @param drag          Drag (v multiplier)
     * @param initialPow    Initial velocity
     * @param tolerance     Tolerance for stopping the secant method
     * @param maxIterations Maximum number of iterations for the secant method
     * @return Trajectory object containing the best point, angle, time, and miss flag
     */
    public static CannonTrajectory findBestTrajectorySecant(Vec2 targetPoint, float gravity, float drag, float initialPow,
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
        float bestV0x = 0;
        float bestV0y = 0;

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
            var r1 = findLineIntersection(targetSlope, gravity, drag, v0x1, v0y1, tolerance);
            var r2 = findLineIntersection(targetSlope, gravity, drag, v0x2, v0y2, tolerance);

            // Calculate distances from the target for the intersection points
            float distance1 = r1 != null ? targetPoint.distanceToSqr(r1.getFirst()) : Float.MAX_VALUE;
            float distance2 = r2 != null ? targetPoint.distanceToSqr(r2.getFirst()) : Float.MAX_VALUE;
            float distToTarget;
            // Update the best result if a closer point is found
            if (distance1 < distance2) {
                bestPoint = r1.getFirst();
                bestAngle = angle1;
                bestPointTime = r1.getSecond();
                bestV0x = v0x1;
                bestV0y = v0y1;

                angle2 = angle1;
                angle1 -= tolerance; // Move angle1 closer to the best angle
                distToTarget = distance1;
            } else {
                bestPoint = r2.getFirst();
                bestAngle = angle2;
                bestPointTime = r2.getSecond();
                bestV0x = v0x2;
                bestV0y = v0y2;

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
                bestPoint = targetPoint;
                break; // Stop iterating if the difference in distances is below the tolerance
            }
        }

        return new CannonTrajectory(bestPoint, bestAngle, bestPointTime, miss, gravity, drag, bestV0x, bestV0y);
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
    private static CannonTrajectory findBestTrajectoryGoldenSection(Vec2 targetPoint, float gravity, float drag, float initialPow,
                                                                    float angleTolerance, float tolerance,
                                                                    float start, float end) {
        float targetSlope = targetPoint.y / targetPoint.x;

        // Define golden ratio
        final float goldenRatio = MthUtils.PHI - 1;

        // Initialize variables to store the best result
        Vec2 bestPoint = new Vec2(0, 0);
        float bestAngle = start;
        double bestPointTime = 0;
        float bestV0x = 0;
        float bestV0y = 0;


        boolean miss = true;

        // Define the search interval
        float startAngle = start;
        float endAngle = end;


        float midAngle1 = startAngle + goldenRatio * (endAngle - startAngle);
        float midAngle2 = endAngle - goldenRatio * (endAngle - startAngle);

        // Perform golden-section search iterations
        int iterNumber = 0;
        while (Math.abs(endAngle - startAngle) > angleTolerance) {

            iterNumber++;
            // Calculate the velocities for the two intermediate angles
            float v0x1 = (float) (Math.cos(midAngle1) * initialPow);
            float v0y1 = (float) (Math.sin(midAngle1) * initialPow);
            float v0x2 = (float) (Math.cos(midAngle2) * initialPow);
            float v0y2 = (float) (Math.sin(midAngle2) * initialPow);

            // Find the intersection points for the two intermediate angles
            var r1 = findLineIntersection(targetSlope, gravity, drag, v0x1, v0y1, tolerance);
            var r2 = findLineIntersection(targetSlope, gravity, drag, v0x2, v0y2, tolerance);

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
                bestV0x = v0x1;
                bestV0y = v0y1;

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
                bestV0x = v0x2;
                bestV0y = v0y2;

                if (distance2 > lastBestDist && iterNumber != 1) {
                    Supplementaries.error();
                }

                endAngle = midAngle1;
                midAngle1 = midAngle2;
                midAngle2 = endAngle - goldenRatio * (endAngle - startAngle);
            }
            if (endAngle < startAngle) {
                Supplementaries.error();
            }

            // Update the best result if a closer point is found
            // has to be bigger than line search tolerance otherwise we wont find a solution
            if (lastBestDist < (tolerance * 10)) {
                bestPoint = targetPoint;
                miss = false;
                break; // Stop iterating if the difference in distances is below the tolerance
            }
        }

        return new CannonTrajectory(bestPoint, bestAngle, bestPointTime, miss, gravity, drag, bestV0x, bestV0y);
    }

    public static Pair<Vec2, Double> findLineIntersection(float m, float g, float d, float V0x, float V0y, float precision) {
        return findLineIntersectionBisection(m, g, d, V0x, V0y, precision);
    }

    /**
     * calculate intersection of line with projectile trajectory using secant method.
     * Note that this will struggle a lot with very steep functions
     * UNRELIABLE for steep function but way faster
     *
     * @param m   line slope
     * @param g   gravity
     * @param d   drag (v multiplier)
     * @param V0x initial velocity
     * @param V0y initial velocity
     * @return intersection point
     */
    private static Pair<Vec2, Double> findLineIntersectionSecant(float m, float g, float d, float V0x, float V0y) {
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
     * Bisection (binary search) method.
     * Slower but doesn't fail with steep functions due to double limit
     */
    private static Pair<Vec2, Double> findLineIntersectionBisection(float m, float g, float d, float V0x, float V0y,
                                                                    float precision) {
        float slopeAt0 = V0y / V0x;
        if (slopeAt0 < m) {
            Supplementaries.error();
            return null;
        }
        double low = 0;//getPeakTime(g, d, V0y); // Initial lower bound for binary search
        double high = 1000; // Initial upper bound for binary search

        // Perform binary search
        int iter = 0;
        int maxIter = 50;
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

        Supplementaries.error();
        return null;// (low + high) / 2.0f; // Return the approximate value of t at the intersection
    }


    private static void projectileEquation() {
        // pos = pos + velocity

        // velocity = velocity*0.99


        // velocity = velocity + Vec3(0, -0.05, 0)

        // d = 0.99
        // g = 0.05
        // vely = d^t - g*(d^t-1)/(d-1)
        // y = (1-g/d-1)*1/ln(d)*d^t+t*g/(d-1)
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

        //vox* inlog *
    }

    public double getX(double t) {
        return arcX(t, this.gravity, this.drag, this.v0x);
    }

    public double getY(double t) {
        return arcY(t, this.gravity, this.drag, this.v0y);
    }

    public BlockPos getHitPos(BlockPos cannonPos, float yaw) {
        Vec2 v = this.point;
        Vec3 localPos = new Vec3(0, v.y - 1, -v.x).yRot(-yaw);
        return BlockPos.containing(cannonPos.getCenter().add(localPos));
    }


}
