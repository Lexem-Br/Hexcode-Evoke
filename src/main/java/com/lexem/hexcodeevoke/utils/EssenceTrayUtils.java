package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.lexem.hexcodeevoke.components.EssenceTrayComponent;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class EssenceTrayUtils {
    private static final Map<World, Set<Vector3i>> TRAYS = Collections.synchronizedMap(new WeakHashMap<>());

    private EssenceTrayUtils() {
    }

    public static void register(@Nonnull World world, @Nonnull Vector3i pos) {
        TRAYS.computeIfAbsent(world, w -> ConcurrentHashMap.newKeySet()).add(new Vector3i(pos));
    }

    public static void unregister(@Nonnull World world, @Nonnull Vector3i pos) {
        Set<Vector3i> trays = TRAYS.get(world);
        if (trays != null) trays.remove(pos);
    }

    @Nullable
    public static Vector3i findNearestFilledTray(@Nonnull World world, @Nonnull Vector3d from, double range) {
        Set<Vector3i> trays = TRAYS.get(world);
        if (trays == null || trays.isEmpty()) return null;

        Vector3i nearest = null;
        double nearestDistance = range * range;
        for (Vector3i tray : trays) {
            double distance = from.distanceSquared(getCenter(tray));
            if (distance > nearestDistance || countLifeEssence(world, tray) <= 0) continue;

            nearest = tray;
            nearestDistance = distance;
        }
        return nearest != null ? new Vector3i(nearest) : null;
    }

    @Nonnull
    public static Vector3d getCenter(@Nonnull Vector3i tray) {
        return new Vector3d(tray.x + 0.5, tray.y, tray.z + 0.5);
    }

    public static int countLifeEssence(@Nonnull World world, @Nonnull Vector3i tray) {
        ItemContainer container = getContainer(world, tray);
        if (container == null) return 0;

        int count = 0;
        for (short slot = 0; slot < container.getCapacity(); slot++) {
            ItemStack itemStack = container.getItemStack(slot);
            if (LifeEssenceUtils.isLifeEssence(itemStack)) count += itemStack.getQuantity();
        }
        return count;
    }

    public static int takeLifeEssence(@Nonnull World world, @Nonnull Vector3i tray, int max) {
        ItemContainer container = max > 0 ? getContainer(world, tray) : null;
        if (container == null) return 0;

        int taken = 0;
        for (short slot = 0; slot < container.getCapacity() && taken < max; slot++) {
            ItemStack itemStack = container.getItemStack(slot);
            if (!LifeEssenceUtils.isLifeEssence(itemStack)) continue;

            int quantity = Math.min(itemStack.getQuantity(), max - taken);
            if (container.removeItemStackFromSlot(slot, quantity).succeeded()) taken += quantity;
        }
        return taken;
    }

    @Nullable
    private static ItemContainer getContainer(@Nonnull World world, @Nonnull Vector3i tray) {
        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, tray, null);
        if (blockRef == null) return null;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        if (chunkStore.getComponent(blockRef, EssenceTrayComponent.getComponentType()) == null) return null;

        ItemContainerBlock containerBlock = chunkStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        return containerBlock != null ? containerBlock.getItemContainer() : null;
    }
}
