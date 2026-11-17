package com.lexem.hexcodeevoke.utils.block;

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
import com.lexem.hexcodeevoke.utils.inventory.InventoryUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FinderUtils {
    private static final double LEVEL_TOLERANCE = 0.5;

    private static final Map<Integer, List<int[]>> HORIZONTAL_OFFSETS = new ConcurrentHashMap<>();
    private static final Map<Long, int[]> VOLUME_OFFSETS = new ConcurrentHashMap<>();

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

    public static <T> Vector3i findNearestBlock(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            BlockValidator<T> validator,
            T context
    ) {
        if (startPos == null || validator == null || context == null) {
            return null;
        }

        int startX = MathUtil.floor(startPos.x());
        int startY = MathUtil.floor(startPos.y());
        int startZ = MathUtil.floor(startPos.z());
        int[] offsets = getVolumeOffsets(horizontalRange, verticalRange);
        Vector3i block = new Vector3i();

        for (int i = 0; i < offsets.length; i += 3) {
            block.set(startX + offsets[i], startY + offsets[i + 1], startZ + offsets[i + 2]);
            if (validator.isValid(block, context)) {
                return new Vector3i(block);
            }
        }

        return null;
    }

    public static boolean isWithinRange(Vector3d startPos, Vector3i block, int horizontalRange, int verticalRange) {
        if (startPos == null || block == null) return false;

        int dx = block.x - MathUtil.floor(startPos.x());
        int dy = block.y - MathUtil.floor(startPos.y());
        int dz = block.z - MathUtil.floor(startPos.z());
        return dx * dx + dz * dz <= horizontalRange * horizontalRange && Math.abs(dy) <= verticalRange;
    }

    public static <T> Vector3i findNearestBlockByLevel(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            BlockValidator<T> validator,
            T context
    ) {
        if (startPos == null || validator == null || context == null) {
            return null;
        }

        int startX = MathUtil.floor(startPos.x());
        int startY = MathUtil.floor(startPos.y() + LEVEL_TOLERANCE);
        int startZ = MathUtil.floor(startPos.z());
        List<int[]> offsets = getHorizontalOffsets(horizontalRange);

        for (int dy : getLevelOrder(verticalRange)) {
            for (int[] offset : offsets) {
                Vector3i block = new Vector3i(startX + offset[0], startY + dy, startZ + offset[1]);
                if (validator.isValid(block, context)) {
                    return block;
                }
            }
        }

        return null;
    }

    private static int[] getVolumeOffsets(int horizontalRange, int verticalRange) {
        int h = Math.max(0, horizontalRange);
        int v = Math.max(0, verticalRange);
        long key = ((long) h << 32) | v;

        return VOLUME_OFFSETS.computeIfAbsent(key, k -> {
            List<int[]> offsets = new ArrayList<>();
            for (int dx = -h; dx <= h; dx++) {
                for (int dz = -h; dz <= h; dz++) {
                    if (dx * dx + dz * dz > h * h) continue;

                    for (int dy = -v; dy <= v; dy++) {
                        offsets.add(new int[]{dx, dy, dz});
                    }
                }
            }

            offsets.sort(Comparator
                    .<int[]>comparingInt(o -> o[0] * o[0] + o[1] * o[1] + o[2] * o[2])
                    .thenComparingInt(o -> Math.abs(o[1]))
                    .thenComparingInt(o -> -o[1]));

            int[] flat = new int[offsets.size() * 3];
            for (int i = 0; i < offsets.size(); i++) {
                int[] offset = offsets.get(i);
                flat[i * 3] = offset[0];
                flat[i * 3 + 1] = offset[1];
                flat[i * 3 + 2] = offset[2];
            }
            return flat;
        });
    }

    private static List<int[]> getHorizontalOffsets(int range) {
        return HORIZONTAL_OFFSETS.computeIfAbsent(range, r -> {
            List<int[]> offsets = new ArrayList<>();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= r * r) offsets.add(new int[]{dx, dz});
                }
            }
            offsets.sort(Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
            return List.copyOf(offsets);
        });
    }

    private static int[] getLevelOrder(int verticalRange) {
        int[] order = new int[verticalRange * 2 + 1];
        int i = 0;
        for (int dy = 0; dy <= verticalRange; dy++) order[i++] = dy;
        for (int dy = -1; dy >= -verticalRange; dy--) order[i++] = dy;
        return order;
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

        int startX = MathUtil.floor(startPos.x());
        int startY = MathUtil.floor(startPos.y());
        int startZ = MathUtil.floor(startPos.z());
        int[] offsets = getVolumeOffsets(horizontalRange, verticalRange);
        Vector3d block = new Vector3d();

        for (int i = 0; i < offsets.length; i += 3) {
            block.set(startX + offsets[i], startY + offsets[i + 1], startZ + offsets[i + 2]);
            if (isChestBlock(block, world, npcRef, store, checkCanStore)) {
                chestPositions.add(new Vector3d(block));
            }
        }

        return chestPositions;
    }

    public static List<Vector3d> findAllChestsInRange(
            Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            World world
    ) {
        return findAllChestsInRange(startPos, horizontalRange, verticalRange, world, null, null, false);
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
}
