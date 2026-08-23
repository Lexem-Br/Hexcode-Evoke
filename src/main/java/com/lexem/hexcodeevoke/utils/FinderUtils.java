package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.math.util.MathUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;

public class FinderUtils {

    @FunctionalInterface
    public interface BlockValidator<T> {
        boolean isValid(Vector3i block, T context);
    }

    private FinderUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <T> Vector3i findNearestBlockBFS(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            BlockValidator<T> validator,
            T context
    ) {
        if (startPos == null || validator == null || context == null) {
            return null;
        }

        Vector3i startBlock = new Vector3i(
                MathUtil.floor(startPos.x()),
                MathUtil.floor(startPos.y()),
                MathUtil.floor(startPos.z())
        );

        Queue<Vector3i> queue = new LinkedList<>();
        Set<Vector3i> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        int currentLayer = 0;
        int nodesInCurrentLayer = 1;
        int nodesInNextLayer = 0;

        while (!queue.isEmpty() && currentLayer <= Math.max(horizontalRange, verticalRange)) {
            Vector3i current = queue.poll();
            nodesInCurrentLayer--;

            if (validator.isValid(current, context)) {
                return current;
            }

            Vector3i[] neighbors = getNeighbors(current);

            for (Vector3i neighbor : neighbors) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                int dx = Math.abs(neighbor.x() - startBlock.x());
                int dy = Math.abs(neighbor.y() - startBlock.y());
                int dz = Math.abs(neighbor.z() - startBlock.z());

                double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                if (horizontalDist > horizontalRange || Math.abs(dy) > verticalRange) {
                    continue;
                }

                visited.add(neighbor);
                queue.add(neighbor);
                nodesInNextLayer++;
            }

            if (nodesInCurrentLayer == 0) {
                currentLayer++;
                nodesInCurrentLayer = nodesInNextLayer;
                nodesInNextLayer = 0;
            }
        }

        return null;
    }

    private static Vector3i[] getNeighbors(Vector3i block) {
        if (block == null) {
            return new Vector3i[0];
        }

        return new Vector3i[] {
                new Vector3i(block.x(), block.y(), block.z() + 1),
                new Vector3i(block.x() - 1, block.y(), block.z()),
                new Vector3i(block.x() + 1, block.y(), block.z()),
                new Vector3i(block.x(), block.y() + 1, block.z()),
                new Vector3i(block.x(), block.y() - 1, block.z()),
                new Vector3i(block.x(), block.y(), block.z() - 1)
        };
    }
}