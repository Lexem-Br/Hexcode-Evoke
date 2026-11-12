package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Repeating particle spawns started by /evoker particleloop, kept per player so /evoker particlestop can cancel them. */
public final class ParticleLoops {
    private static final Map<UUID, List<ScheduledFuture<?>>> LOOPS = new ConcurrentHashMap<>();

    private ParticleLoops() {
    }

    public static int start(@Nonnull UUID owner, @Nonnull World world, @Nonnull String id, @Nonnull Vector3d position, float scale, double intervalSeconds) {
        Vector3d spawnPosition = new Vector3d(position);
        long intervalMs = Math.max(50L, Math.round(intervalSeconds * 1000.0));
        List<ScheduledFuture<?>> loops = LOOPS.computeIfAbsent(owner, key -> new CopyOnWriteArrayList<>());
        ScheduledFuture<?>[] self = new ScheduledFuture<?>[1];
        self[0] = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (!world.isAlive()) {
                cancel(owner, self[0]);
                return;
            }
            world.execute(() -> ParticleUtil.spawnParticleEffect(id, spawnPosition, 0.0F, 0.0F, 0.0F, scale, 1.0F, world.getEntityStore().getStore()));
        }, 0L, intervalMs, TimeUnit.MILLISECONDS);
        loops.add(self[0]);
        return loops.size();
    }

    public static int stop(@Nonnull UUID owner) {
        List<ScheduledFuture<?>> loops = LOOPS.remove(owner);
        if (loops == null) return 0;
        loops.forEach(loop -> loop.cancel(false));
        return loops.size();
    }

    public static void stopAll() {
        LOOPS.keySet().forEach(ParticleLoops::stop);
    }

    private static void cancel(@Nonnull UUID owner, ScheduledFuture<?> loop) {
        if (loop == null) return;
        loop.cancel(false);
        List<ScheduledFuture<?>> loops = LOOPS.get(owner);
        if (loops != null) loops.remove(loop);
    }
}
