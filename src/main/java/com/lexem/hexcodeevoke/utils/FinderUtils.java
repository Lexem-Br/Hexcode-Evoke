package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;

public class FinderUtils {

    @FunctionalInterface
    public interface BlockValidator<T> {
        boolean isValid(Vector3i block, T context);
    }

    @FunctionalInterface
    public interface ChestValidator {
        boolean isValid(Vector3d block, World world, Ref<EntityStore> npcRef, Store<EntityStore> store, boolean checkCanStore);
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

    public static List<Vector3d> findAllChestsInRange(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            World world,
            Ref<EntityStore> npcRef,
            Store<EntityStore> store,
            boolean checkCanStore
    ) {
        List<Vector3d> chestPositions = new ArrayList<>();

        if (startPos == null || world == null) {
            return chestPositions;
        }

        Vector3d startBlock = new Vector3d(
                MathUtil.floor(startPos.x()),
                MathUtil.floor(startPos.y()),
                MathUtil.floor(startPos.z())
        );

        Queue<Vector3d> queue = new LinkedList<>();
        Set<Vector3d> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        int currentLayer = 0;
        int nodesInCurrentLayer = 1;
        int nodesInNextLayer = 0;

        while (!queue.isEmpty() && currentLayer <= Math.max(horizontalRange, verticalRange)) {
            Vector3d current = queue.poll();
            nodesInCurrentLayer--;

            if (isChestBlock(current, world, npcRef, store, checkCanStore)) {
                chestPositions.add(new Vector3d(current));
            }

            Vector3d[] neighbors = getNeighbors(current);

            for (Vector3d neighbor : neighbors) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                int dx = (int) Math.abs(neighbor.x() - startBlock.x());
                int dy = (int) Math.abs(neighbor.y() - startBlock.y());
                int dz = (int) Math.abs(neighbor.z() - startBlock.z());

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

        return chestPositions;
    }

    /**
     * Searches for all chests within the specified range using BFS, without checking storage capacity
     */
    public static List<Vector3d> findAllChestsInRange(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            World world
    ) {
        return findAllChestsInRange(startPos, horizontalRange, verticalRange, world, null, null, false);
    }

    public static List<Vector3d> findAllChestsInRange(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            ChestValidator validator,
            World world,
            Ref<EntityStore> npcRef,
            Store<EntityStore> store,
            boolean checkCanStore
    ) {
        List<Vector3d> chestPositions = new ArrayList<>();

        if (startPos == null || world == null || validator == null) {
            return chestPositions;
        }

        Vector3d startBlock = new Vector3d(
                MathUtil.floor(startPos.x()),
                MathUtil.floor(startPos.y()),
                MathUtil.floor(startPos.z())
        );

        Queue<Vector3d> queue = new LinkedList<>();
        Set<Vector3d> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        int currentLayer = 0;
        int nodesInCurrentLayer = 1;
        int nodesInNextLayer = 0;

        while (!queue.isEmpty() && currentLayer <= Math.max(horizontalRange, verticalRange)) {
            Vector3d current = queue.poll();
            nodesInCurrentLayer--;

            if (validator.isValid(current, world, npcRef, store, checkCanStore)) {
                chestPositions.add(new Vector3d(current));
            }

            Vector3d[] neighbors = getNeighbors(current);

            for (Vector3d neighbor : neighbors) {
                if (visited.contains(neighbor)) {
                    continue;
                }

                int dx = (int) Math.abs(neighbor.x() - startBlock.x());
                int dy = (int) Math.abs(neighbor.y() - startBlock.y());
                int dz = (int) Math.abs(neighbor.z() - startBlock.z());

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

        return chestPositions;
    }

    private static boolean isChestBlock(
            Vector3d block,
            World world,
            Ref<EntityStore> npcRef,
            Store<EntityStore> store,
            boolean checkCanStore
    ) {
        if (block == null || world == null) {
            return false;
        }

        long chunkIndex = ChunkUtil.indexChunkFromBlock(block.x, block.z);
        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
        if (chunkRef == null) return false;

        Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock((int)block.x, (int)block.y, (int)block.z);
        if (sectionRef == null) return false;

        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, (int)block.x, (int)block.y, (int)block.z);
        if (blockRef == null) return false;

        ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (itemContainerBlock == null) {
            return false;
        } else if (!checkCanStore || npcRef == null || store == null) {
            return true;
        }

        SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();
        return InventoryUtils.canAddAnyItemToContainerNPC(chestContainer, npcRef, store, true);
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

    private static Vector3d[] getNeighbors(Vector3d block) {
        if (block == null) {
            return new Vector3d[0];
        }

        return new Vector3d[] {
                new Vector3d(block.x(), block.y(), block.z() + 1),
                new Vector3d(block.x() - 1, block.y(), block.z()),
                new Vector3d(block.x() + 1, block.y(), block.z()),
                new Vector3d(block.x(), block.y() + 1, block.z()),
                new Vector3d(block.x(), block.y() - 1, block.z()),
                new Vector3d(block.x(), block.y(), block.z() - 1)
        };
    }
}