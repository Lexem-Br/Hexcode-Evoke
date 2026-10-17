package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;

import javax.annotation.Nonnull;

public final class UnloadedHexCreatureDespawner {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private UnloadedHexCreatureDespawner() {
    }

    public static void despawn(@Nonnull String npcUUID) {
        HexCreatureRegistry registry = HexCreatureRegistry.get();
        registry.revoke(npcUUID);
        HexCreatureRegistry.flush();

        HexCreatureRegistry.LastKnownLocation location = registry.getLastKnownLocation(npcUUID);
        if (location == null || location.getWorldName() == null) return;

        World world = Universe.get().getWorld(location.getWorldName());
        if (world == null) return;

        int x = MathUtil.floor(location.getX());
        int y = MathUtil.floor(location.getY());
        int z = MathUtil.floor(location.getZ());

        world.execute(() -> world.getChunkStore()
                .getChunkSectionReferenceAtBlockAsync(x, y, z, GetChunkFlags.SET_TICKING)
                .exceptionally(throwable -> {
                    LOGGER.atWarning().log("Unable to load section of Hex Creature " + npcUUID + ": " + throwable.getMessage());
                    return null;
                }));
    }
}
