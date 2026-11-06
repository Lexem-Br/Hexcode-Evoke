package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class FairyFlightState {
    private static final long ACTIVE_TIMEOUT_NANOS = 250_000_000L;
    private static final double TWO_PI = Math.PI * 2;
    private static final double WOBBLE_CROSS_RATIO = 1.31;
    private static final double CALM_WOBBLE_FACTOR = 0.25;
    private static final double FLIT_VERTICAL_FACTOR = 0.5;
    private static final double PERSONAL_VERTICAL_FACTOR = 0.5;
    private static final double ARRIVAL_BRAKE_MARGIN = 0.8;
    private static final double AVOID_SPEED = 9.0;
    private static final double AVOID_UP_BIAS = 0.8;
    private static final double LIFT_SPEED = 7.0;
    private static final double LIFT_TIME = 0.35;
    private static final double LIFT_MIN_DISTANCE = 1.5;
    private static final double PHASE_DELAY = 0.4;
    private static final double PHASE_SPEED = 9.0;
    private static final double PHASE_MAX_TIME = 3.0;
    private static final double PHASE_FREE_TIME = 0.1;
    private static final double STUCK_MIN_DISTANCE = 1.0;
    private static final double STUCK_MOVE_RATIO = 0.4;
    private static final double FACE_MOTION_ENTER_SPEED = 5.0;
    private static final double FACE_MOTION_EXIT_SPEED = 3.0;
    private static final double YAW_RATE = 12.0;
    private static final double TILT_RATE = 9.0;
    private static final double ACCELERATION_RATE = 12.0;
    private static final double LEAN_ACCELERATION_FACTOR = 0.08;
    private static final double CLIMB_TILT_FACTOR = 0.35;
    private static final double ANIMATION_SPEED_RATE = 8.0;
    private static final double MOVING_ENTER_SPEED = 2.5;
    private static final double MOVING_EXIT_SPEED = 1.0;
    private static final double FAST_ENTER_SPEED = 8.0;
    private static final double FAST_EXIT_SPEED = 6.0;

    private final Vector3d velocity = new Vector3d();
    private final Vector3d goal = new Vector3d();
    private final Vector3d toGoal = new Vector3d();
    private final Vector3d desired = new Vector3d();
    private final Vector3d delta = new Vector3d();
    private final Vector3d smoothedAcceleration = new Vector3d();
    private final Vector3d flitOffset = new Vector3d();
    private final Vector3d personalOffset = new Vector3d();
    private final Vector3d anchor = new Vector3d();
    private boolean hasAnchor;
    private double wobbleX;
    private double wobbleZ;
    private double bob;
    private double flitTime;
    private double goalDistance;
    private boolean dashing;
    private boolean phasing;
    private double stuckTime;
    private double liftTime;
    private double phaseTime;
    private double phaseFreeTime;
    private double trailTime;
    private double animationSpeed;
    private boolean moving;
    private boolean fast;
    private boolean facingMotion;
    private float yaw;
    private float pitch;
    private float roll;
    private long lastActiveNanos;

    public FairyFlightState() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.wobbleX = random.nextDouble(TWO_PI);
        this.wobbleZ = random.nextDouble(TWO_PI);
        this.bob = random.nextDouble(TWO_PI);
        double angle = random.nextDouble(TWO_PI);
        double radius = Math.sqrt(random.nextDouble());
        this.personalOffset.set(Math.cos(angle) * radius, random.nextDouble(-1, 1) * PERSONAL_VERTICAL_FACTOR, Math.sin(angle) * radius);
    }

    public boolean isActive() {
        return this.lastActiveNanos != 0 && System.nanoTime() - this.lastActiveNanos < ACTIVE_TIMEOUT_NANOS;
    }

    public void markActive() {
        this.lastActiveNanos = System.nanoTime();
    }

    public void begin(float yaw, float pitch, float roll) {
        this.velocity.zero();
        this.smoothedAcceleration.zero();
        this.hasAnchor = false;
        this.dashing = false;
        this.phasing = false;
        this.stuckTime = 0;
        this.liftTime = 0;
        this.flitTime = 0;
        this.animationSpeed = 0;
        this.moving = false;
        this.fast = false;
        this.facingMotion = false;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    @Nonnull
    public Vector3dc holdAnchor(@Nonnull Vector3dc position, double brakeAcceleration) {
        if (!this.hasAnchor) {
            double brakeTime = this.velocity.length() / (2 * brakeAcceleration);
            this.anchor.set(this.velocity).mul(brakeTime).add(position);
            this.hasAnchor = true;
        }
        return this.anchor;
    }

    public void clearAnchor() {
        this.hasAnchor = false;
    }

    public void steer(@Nonnull FairyFlightSettings settings, @Nonnull Vector3dc position, @Nonnull Vector3dc target, @Nullable Vector3dc avoidPoint, boolean calm, double dt) {
        this.updateGoal(settings, target, calm, dt);
        this.toGoal.set(this.goal).sub(position);
        this.goalDistance = this.toGoal.length();

        if (this.goalDistance > settings.dashDistance()) {
            this.dashing = true;
        } else if (this.goalDistance < settings.dashStopDistance()) {
            this.dashing = false;
        }

        boolean hardAcceleration = this.dashing || this.velocity.length() > settings.maxSpeed();
        double acceleration = hardAcceleration ? settings.dashAcceleration() : settings.acceleration();
        double maxSpeed = this.dashing ? settings.dashSpeed() : settings.maxSpeed();
        if (this.phasing) maxSpeed = Math.min(maxSpeed, PHASE_SPEED);

        double gain = this.dashing ? settings.dashGain() : settings.gain();
        double arrivalSpeed = Math.sqrt(2 * acceleration * ARRIVAL_BRAKE_MARGIN * this.goalDistance);
        double approachSpeed = Math.min(this.goalDistance * gain, Math.min(maxSpeed, arrivalSpeed));
        this.desired.set(this.toGoal).mul(this.goalDistance > 1.0E-6 ? approachSpeed / this.goalDistance : 0);

        if (avoidPoint != null && settings.avoidRadius() > 0) {
            this.addAvoidance(settings.avoidRadius(), position, avoidPoint);
        }
        if (this.liftTime > 0) {
            this.liftTime -= dt;
            this.desired.y += LIFT_SPEED;
        }

        double desiredSpeed = this.desired.length();
        if (desiredSpeed > maxSpeed) this.desired.mul(maxSpeed / desiredSpeed);

        double maxDelta = acceleration * dt;
        this.delta.set(this.desired).sub(this.velocity);
        double deltaLength = this.delta.length();
        if (deltaLength > maxDelta) this.delta.mul(maxDelta / deltaLength);
        this.velocity.add(this.delta);

        this.smoothedAcceleration.lerp(this.delta.div(dt), 1 - Math.exp(-ACCELERATION_RATE * dt));
        this.updateAnimationState(dt);
    }

    private void updateGoal(@Nonnull FairyFlightSettings settings, @Nonnull Vector3dc target, boolean calm, double dt) {
        this.wobbleX = advancePhase(this.wobbleX, settings.wobblePeriod(), dt);
        this.wobbleZ = advancePhase(this.wobbleZ, settings.wobblePeriod() * WOBBLE_CROSS_RATIO, dt);
        this.bob = advancePhase(this.bob, settings.bobPeriod(), dt);

        this.updateFlit(settings, calm, dt);

        double wobble = settings.wobbleRadius() * (calm ? CALM_WOBBLE_FACTOR : 1);
        this.goal.set(
                target.x() + Math.cos(this.wobbleX) * wobble + this.flitOffset.x + this.personalOffset.x * settings.personalRadius(),
                target.y() + Math.sin(this.bob) * settings.bobHeight() + this.flitOffset.y + this.personalOffset.y * settings.personalRadius(),
                target.z() + Math.sin(this.wobbleZ) * wobble + this.flitOffset.z + this.personalOffset.z * settings.personalRadius()
        );
    }

    private static double advancePhase(double phase, double period, double dt) {
        return period > 0 ? (phase + TWO_PI * dt / period) % TWO_PI : phase;
    }

    private void updateFlit(@Nonnull FairyFlightSettings settings, boolean calm, double dt) {
        if (calm || settings.flitRadius() <= 0) {
            this.flitOffset.zero();
            return;
        }

        this.flitTime -= dt;
        if (this.flitTime > 0) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble(TWO_PI);
        double radius = settings.flitRadius() * Math.sqrt(random.nextDouble());
        this.flitOffset.set(Math.cos(angle) * radius, random.nextDouble(-1, 1) * settings.flitRadius() * FLIT_VERTICAL_FACTOR, Math.sin(angle) * radius);
        this.flitTime = settings.flitMaxInterval() > settings.flitMinInterval()
                ? random.nextDouble(settings.flitMinInterval(), settings.flitMaxInterval())
                : settings.flitMinInterval();
    }

    private void addAvoidance(double radius, @Nonnull Vector3dc position, @Nonnull Vector3dc point) {
        double goalX = this.goal.x - point.x();
        double goalY = this.goal.y - point.y();
        double goalZ = this.goal.z - point.z();
        if (goalX * goalX + goalY * goalY + goalZ * goalZ < radius * radius) return;

        double lengthSquared = this.toGoal.lengthSquared();
        if (lengthSquared < 1.0E-4) return;

        double along = ((point.x() - position.x()) * this.toGoal.x + (point.y() - position.y()) * this.toGoal.y + (point.z() - position.z()) * this.toGoal.z) / lengthSquared;
        along = Math.max(0, Math.min(1, along));

        double awayX = position.x() + this.toGoal.x * along - point.x();
        double awayY = position.y() + this.toGoal.y * along - point.y();
        double awayZ = position.z() + this.toGoal.z * along - point.z();
        double distance = Math.sqrt(awayX * awayX + awayY * awayY + awayZ * awayZ);
        if (distance >= radius) return;

        if (distance < 1.0E-3) {
            awayX = 0;
            awayY = 1;
            awayZ = 0;
        } else {
            awayX /= distance;
            awayY /= distance;
            awayZ /= distance;
        }
        awayY += AVOID_UP_BIAS;
        double awayLength = Math.sqrt(awayX * awayX + awayY * awayY + awayZ * awayZ);
        if (awayLength < 1.0E-3) {
            awayX = 0;
            awayY = 1;
            awayZ = 0;
            awayLength = 1;
        }

        double push = (1 - distance / radius) * Math.max(AVOID_SPEED, this.desired.length()) / awayLength;
        this.desired.add(awayX * push, awayY * push, awayZ * push);
    }

    public void onMoved(boolean blocked, boolean blockedSideways, double movedRatio, double dt) {
        if (blockedSideways && this.goalDistance > LIFT_MIN_DISTANCE) {
            this.liftTime = LIFT_TIME;
        }

        if (blocked && movedRatio < STUCK_MOVE_RATIO && this.goalDistance > STUCK_MIN_DISTANCE) {
            this.stuckTime += dt;
            if (this.stuckTime >= PHASE_DELAY) {
                this.phasing = true;
                this.phaseTime = 0;
                this.phaseFreeTime = 0;
                this.stuckTime = 0;
            }
        } else {
            this.stuckTime = Math.max(0, this.stuckTime - dt);
        }
    }

    public void updatePhasing(boolean free, double dt) {
        this.phaseTime += dt;
        this.phaseFreeTime = free ? this.phaseFreeTime + dt : 0;
        if (this.phaseFreeTime >= PHASE_FREE_TIME || this.phaseTime >= PHASE_MAX_TIME) {
            this.phasing = false;
            this.stuckTime = 0;
        }
    }

    public void stopPhasing() {
        this.phasing = false;
        this.stuckTime = 0;
        this.liftTime = 0;
    }

    public void updatePose(@Nonnull FairyFlightSettings settings, @Nullable Float evokerLookYaw, double dt) {
        double horizontalSpeedSquared = this.velocity.x * this.velocity.x + this.velocity.z * this.velocity.z;
        double faceMotionSpeed = this.facingMotion ? FACE_MOTION_EXIT_SPEED : FACE_MOTION_ENTER_SPEED;
        this.facingMotion = horizontalSpeedSquared > faceMotionSpeed * faceMotionSpeed;
        float targetYaw = this.yaw;
        if (this.facingMotion) {
            targetYaw = PhysicsMath.headingFromDirection(this.velocity.x, this.velocity.z);
        } else if (settings.matchEvokerLook() && evokerLookYaw != null) {
            targetYaw = evokerLookYaw;
        }
        this.yaw = PhysicsMath.normalizeAngle(this.yaw + NPCPhysicsMath.turnAngle(this.yaw, targetYaw) * (float) (1 - Math.exp(-YAW_RATE * dt)));

        double leanX = (this.velocity.x + this.smoothedAcceleration.x * LEAN_ACCELERATION_FACTOR) / settings.tiltFullSpeed();
        double leanZ = (this.velocity.z + this.smoothedAcceleration.z * LEAN_ACCELERATION_FACTOR) / settings.tiltFullSpeed();
        double leanLength = Math.sqrt(leanX * leanX + leanZ * leanZ);
        if (leanLength > 1) {
            leanX /= leanLength;
            leanZ /= leanLength;
        }

        double forwardX = PhysicsMath.headingX(this.yaw);
        double forwardZ = PhysicsMath.headingZ(this.yaw);
        double climb = Math.max(-1, Math.min(1, this.velocity.y / settings.tiltFullSpeed()));
        double targetPitch = settings.maxTilt() * (climb * CLIMB_TILT_FACTOR - (leanX * forwardX + leanZ * forwardZ));
        double targetRoll = settings.maxTilt() * (leanX * forwardZ - leanZ * forwardX);

        double blend = 1 - Math.exp(-TILT_RATE * dt);
        this.pitch += (float) ((targetPitch - this.pitch) * blend);
        this.roll += (float) ((targetRoll - this.roll) * blend);
    }

    private void updateAnimationState(double dt) {
        this.animationSpeed += (this.velocity.length() - this.animationSpeed) * (1 - Math.exp(-ANIMATION_SPEED_RATE * dt));
        this.moving = this.animationSpeed > (this.moving ? MOVING_EXIT_SPEED : MOVING_ENTER_SPEED);
        this.fast = this.animationSpeed > (this.fast ? FAST_EXIT_SPEED : FAST_ENTER_SPEED);
    }

    public boolean shouldEmitTrail(double minSpeed, double interval, double dt) {
        this.trailTime -= dt;
        if (this.trailTime > 0 || this.animationSpeed < minSpeed) return false;

        this.trailTime = interval;
        return true;
    }

    @Nonnull
    public Vector3d getVelocity() {
        return this.velocity;
    }

    public boolean isPhasing() {
        return this.phasing;
    }

    public boolean isMoving() {
        return this.moving;
    }

    public boolean isFast() {
        return this.fast;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getRoll() {
        return this.roll;
    }
}
