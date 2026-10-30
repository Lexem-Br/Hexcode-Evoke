package com.lexem.hexcodeevoke.utils;

import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SurfaceCrawlState {
    private static final long ACTIVE_TIMEOUT_NANOS = 250_000_000L;
    private static final double TURN_BLEND_DISTANCE = 0.25;

    private Vector3d goal;
    private boolean planFailed;
    private List<SurfaceNavigator.Waypoint> path;
    private int segment;
    private double segmentProgress;
    private SurfaceNavigator.Node currentNode;
    private final Vector3d forward = new Vector3d(0, 0, -1);
    private final Vector3d posePosition = new Vector3d();
    private final float[] poseRotation = new float[3];
    private long lastActiveNanos;
    private boolean moving;

    public void setGoal(@Nullable Vector3d goal) {
        this.goal = goal != null ? new Vector3d(goal) : null;
        this.planFailed = false;
        this.path = null;
    }

    @Nullable
    public Vector3d getGoal() {
        return this.goal;
    }

    public boolean needsPlan() {
        return this.goal != null && this.path == null && !this.planFailed;
    }

    public boolean hasPath() {
        return this.goal != null && this.path != null;
    }

    public void setPath(@Nullable List<SurfaceNavigator.Waypoint> path) {
        this.path = path;
        this.planFailed = path == null;
        this.segment = 0;
        this.segmentProgress = 0;
    }

    public void invalidatePath() {
        this.path = null;
        this.planFailed = false;
    }

    @Nullable
    public SurfaceNavigator.Node getCurrentNode() {
        return this.currentNode;
    }

    public boolean isActive() {
        return this.lastActiveNanos != 0 && System.nanoTime() - this.lastActiveNanos < ACTIVE_TIMEOUT_NANOS;
    }

    public void stop() {
        this.lastActiveNanos = 0;
        this.goal = null;
        this.path = null;
    }

    public boolean isMoving() {
        return this.moving;
    }

    @Nonnull
    public Vector3d getPosePosition() {
        return this.posePosition;
    }

    @Nonnull
    public float[] getPoseRotation() {
        return this.poseRotation;
    }

    public boolean advance(double distance, @Nonnull SurfaceNavigator navigator) {
        List<SurfaceNavigator.Waypoint> waypoints = this.path;
        if (waypoints == null || waypoints.isEmpty()) return false;

        double remaining = distance;
        while (this.segment < waypoints.size() - 1) {
            SurfaceNavigator.Waypoint from = waypoints.get(this.segment);
            SurfaceNavigator.Waypoint to = waypoints.get(this.segment + 1);
            double length = from.position().distance(to.position());
            if (this.segmentProgress + remaining < length) {
                this.segmentProgress += remaining;
                break;
            }

            remaining -= length - this.segmentProgress;
            this.segment++;
            this.segmentProgress = 0;
            if (to.node() != null) {
                this.currentNode = to.node();
                SurfaceNavigator.Node next = this.nextNode(waypoints);
                if (next != null && !navigator.isValid(next)) {
                    this.invalidatePath();
                    break;
                }
            }
        }

        this.moving = this.segment < waypoints.size() - 1;
        this.updatePose(waypoints);
        this.lastActiveNanos = System.nanoTime();
        return true;
    }

    public void hold(@Nonnull Vector3d position) {
        this.moving = false;
        this.posePosition.set(position);
        this.lastActiveNanos = System.nanoTime();
    }

    public boolean isArrived() {
        return this.path != null && this.segment >= this.path.size() - 1;
    }

    @Nullable
    public SurfaceNavigator.Waypoint getLastWaypoint() {
        return this.path != null && !this.path.isEmpty() ? this.path.get(this.path.size() - 1) : null;
    }

    public void setStartNode(@Nullable SurfaceNavigator.Node node) {
        this.currentNode = node;
    }

    @Nullable
    private SurfaceNavigator.Node nextNode(@Nonnull List<SurfaceNavigator.Waypoint> waypoints) {
        for (int i = this.segment + 1; i < waypoints.size(); i++) {
            SurfaceNavigator.Node node = waypoints.get(i).node();
            if (node != null) return node;
        }
        return null;
    }

    private void updatePose(@Nonnull List<SurfaceNavigator.Waypoint> waypoints) {
        int last = waypoints.size() - 1;
        if (this.segment >= last) {
            SurfaceNavigator.Waypoint end = waypoints.get(last);
            this.posePosition.set(end.position());
            SurfaceNavigator.orient(this.forward, end.up(), this.poseRotation);
            return;
        }

        SurfaceNavigator.Waypoint from = waypoints.get(this.segment);
        SurfaceNavigator.Waypoint to = waypoints.get(this.segment + 1);
        double length = from.position().distance(to.position());
        double t = length > 1.0E-6 ? this.segmentProgress / length : 1;
        from.position().lerp(to.position(), t, this.posePosition);
        Vector3d up = new Vector3d(from.up()).lerp(to.up(), t).normalize();

        Vector3d direction = direction(from, to);
        if (direction != null) {
            Vector3d blended = new Vector3d(direction);
            double toEnd = length - this.segmentProgress;
            if (toEnd < TURN_BLEND_DISTANCE && this.segment + 2 <= last) {
                Vector3d next = direction(to, waypoints.get(this.segment + 2));
                if (next != null) blended.lerp(next, 0.5 * (1 - toEnd / TURN_BLEND_DISTANCE));
            } else if (this.segmentProgress < TURN_BLEND_DISTANCE && this.segment > 0) {
                Vector3d previous = direction(waypoints.get(this.segment - 1), from);
                if (previous != null) blended.set(previous).lerp(direction, 0.5 + 0.5 * this.segmentProgress / TURN_BLEND_DISTANCE);
            }
            Vector3d projected = new Vector3d(blended).sub(new Vector3d(up).mul(blended.dot(up)));
            if (projected.lengthSquared() > 1.0E-8) this.forward.set(projected.normalize());
        }
        SurfaceNavigator.orient(this.forward, up, this.poseRotation);
    }

    @Nullable
    private static Vector3d direction(@Nonnull SurfaceNavigator.Waypoint from, @Nonnull SurfaceNavigator.Waypoint to) {
        Vector3d direction = new Vector3d(to.position()).sub(from.position());
        return direction.lengthSquared() > 1.0E-8 ? direction.normalize() : null;
    }
}
