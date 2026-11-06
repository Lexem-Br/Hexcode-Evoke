package com.lexem.hexcodeevoke.utils;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public final class HoverPointFinder {
    private static final double WHOLE_EPSILON = 1.0E-4;
    private static final double HEIGHT_STEP = 0.25;
    private static final double MIN_HEIGHT = 0.25;
    private static final double ESCAPE_STEP = 0.25;
    private static final double MAX_ESCAPE_DISTANCE = 4.0;
    private static final double MAX_ESCAPE_DROP = 2.0;
    private static final double PROBE_STEP = 0.1;
    private static final int PUSH_PASSES = 2;

    private HoverPointFinder() {
    }

    @Nonnull
    public static Vector3d find(
            @Nonnull Vector3dc hit,
            double heightOffset,
            double clearance,
            @Nonnull Vector3dc lookOrigin,
            @Nonnull Predicate<Vector3dc> isFree
    ) {
        Vector3d point = new Vector3d(hit);
        if (!isWallHit(hit)) {
            for (double height = heightOffset; height >= MIN_HEIGHT; height -= HEIGHT_STEP) {
                point.set(hit.x(), hit.y() + height, hit.z());
                if (isFree.test(point)) break;
            }
        }

        if (!isFree.test(point) && !escape(point, lookOrigin, isFree)) return point;

        Vector3d probe = new Vector3d();
        for (int pass = 0; pass < PUSH_PASSES; pass++) {
            push(point, probe, 1, 0, 0, clearance, isFree);
            push(point, probe, 0, 0, 1, clearance, isFree);
            push(point, probe, 0, 1, 0, clearance, isFree);
        }
        return point;
    }

    private static boolean isWallHit(@Nonnull Vector3dc hit) {
        return isWhole(hit.x()) || isWhole(hit.z());
    }

    private static boolean isWhole(double value) {
        return Math.abs(value - Math.rint(value)) < WHOLE_EPSILON;
    }

    private static boolean escape(@Nonnull Vector3d point, @Nonnull Vector3dc lookOrigin, @Nonnull Predicate<Vector3dc> isFree) {
        Vector3d direction = new Vector3d(lookOrigin).sub(point);
        double length = direction.length();
        if (length < WHOLE_EPSILON) return false;
        direction.div(length);

        Vector3d candidate = new Vector3d();
        double maxDistance = Math.min(MAX_ESCAPE_DISTANCE, length);
        for (double distance = ESCAPE_STEP; distance <= maxDistance; distance += ESCAPE_STEP) {
            for (double drop = 0; drop <= MAX_ESCAPE_DROP; drop += ESCAPE_STEP) {
                candidate.set(direction).mul(distance).add(point);
                candidate.y -= drop;
                if (isFree.test(candidate)) {
                    point.set(candidate);
                    return true;
                }
            }
        }
        return false;
    }

    private static void push(
            @Nonnull Vector3d point,
            @Nonnull Vector3d probe,
            int axisX,
            int axisY,
            int axisZ,
            double clearance,
            @Nonnull Predicate<Vector3dc> isFree
    ) {
        double limit = clearance * 2;
        double positive = freeDistance(point, probe, axisX, axisY, axisZ, limit, isFree);
        double negative = freeDistance(point, probe, -axisX, -axisY, -axisZ, limit, isFree);

        double move = 0;
        if (positive < clearance && negative > positive) {
            move = -Math.min(clearance - positive, (negative - positive) / 2);
        } else if (negative < clearance && positive > negative) {
            move = Math.min(clearance - negative, (positive - negative) / 2);
        }
        point.add(axisX * move, axisY * move, axisZ * move);
    }

    private static double freeDistance(
            @Nonnull Vector3dc point,
            @Nonnull Vector3d probe,
            int axisX,
            int axisY,
            int axisZ,
            double limit,
            @Nonnull Predicate<Vector3dc> isFree
    ) {
        double free = 0;
        int steps = (int) Math.ceil(limit / PROBE_STEP);
        for (int step = 1; step <= steps; step++) {
            double distance = step * PROBE_STEP;
            probe.set(point).add(axisX * distance, axisY * distance, axisZ * distance);
            if (!isFree.test(probe)) break;
            free = distance;
        }
        return free;
    }
}
