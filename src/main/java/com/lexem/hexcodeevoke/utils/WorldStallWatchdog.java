package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldStallWatchdog {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final long STALL_NANOS = 10_000_000_000L;
    private static final long CHECK_INTERVAL_MILLIS = 2_000L;
    private static final Path DUMP_FILE = Path.of("evoke-world-stall.txt");

    private static final Map<Thread, Long> heartbeats = new ConcurrentHashMap<>();
    private static final Set<Thread> reported = ConcurrentHashMap.newKeySet();
    private static Thread watcher;

    private WorldStallWatchdog() {
    }

    public static void beat() {
        Thread thread = Thread.currentThread();
        heartbeats.put(thread, System.nanoTime());
        if (reported.remove(thread)) {
            LOGGER.atWarning().log("[EvokeWatchdog] World thread '%s' is ticking again", thread.getName());
        }
    }

    public static synchronized void start() {
        if (watcher != null) return;

        watcher = new Thread(WorldStallWatchdog::run, "HexcodeEvoke-WorldStallWatchdog");
        watcher.setDaemon(true);
        watcher.start();
        LOGGER.atInfo().log("[EvokeWatchdog] Started: dumps world thread stacks after %d s without a tick to %s",
                STALL_NANOS / 1_000_000_000L, DUMP_FILE.toAbsolutePath());
    }

    public static synchronized void stop() {
        if (watcher == null) return;

        watcher.interrupt();
        watcher = null;
        heartbeats.clear();
        reported.clear();
    }

    private static void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(CHECK_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                return;
            }

            try {
                check();
            } catch (Throwable t) {
                LOGGER.atSevere().withCause(t).log("[EvokeWatchdog] Check failed");
            }
        }
    }

    private static void check() {
        long now = System.nanoTime();
        for (Map.Entry<Thread, Long> entry : heartbeats.entrySet()) {
            Thread thread = entry.getKey();
            if (!thread.isAlive()) {
                heartbeats.remove(thread);
                reported.remove(thread);
                continue;
            }

            long stalledNanos = now - entry.getValue();
            if (stalledNanos > STALL_NANOS && reported.add(thread)) {
                report(thread, stalledNanos / 1_000_000_000L);
            }
        }
    }

    private static void report(@Nonnull Thread thread, long stalledSeconds) {
        StringBuilder worldStack = new StringBuilder();
        worldStack.append("[EvokeWatchdog] World thread '").append(thread.getName())
                .append("' has not ticked for ").append(stalledSeconds).append(" s (state ")
                .append(thread.getState()).append(")\n");
        for (StackTraceElement frame : thread.getStackTrace()) {
            worldStack.append("    at ").append(frame).append('\n');
        }
        LOGGER.atSevere().log("%s", worldStack);

        StringBuilder dump = new StringBuilder();
        dump.append("===== ").append(LocalDateTime.now()).append(" =====\n").append(worldStack).append('\n');
        dump.append("----- All threads -----\n");
        for (ThreadInfo info : ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)) {
            appendThreadInfo(dump, info);
        }
        dump.append('\n');

        try {
            Files.writeString(DUMP_FILE, dump, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            LOGGER.atSevere().log("[EvokeWatchdog] Full thread dump written to %s", DUMP_FILE.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("[EvokeWatchdog] Could not write %s", DUMP_FILE.toAbsolutePath());
        }
    }

    private static void appendThreadInfo(@Nonnull StringBuilder dump, @Nonnull ThreadInfo info) {
        dump.append('"').append(info.getThreadName()).append("\" id=").append(info.getThreadId())
                .append(' ').append(info.getThreadState());
        if (info.getLockName() != null) dump.append(" on ").append(info.getLockName());
        if (info.getLockOwnerName() != null) dump.append(" owned by \"").append(info.getLockOwnerName()).append('"');
        dump.append('\n');

        StackTraceElement[] frames = info.getStackTrace();
        MonitorInfo[] monitors = info.getLockedMonitors();
        for (int i = 0; i < frames.length; i++) {
            dump.append("    at ").append(frames[i]).append('\n');
            for (MonitorInfo monitor : monitors) {
                if (monitor.getLockedStackDepth() == i) dump.append("      - locked ").append(monitor).append('\n');
            }
        }
        for (LockInfo lock : info.getLockedSynchronizers()) {
            dump.append("    - locked synchronizer ").append(lock).append('\n');
        }
        dump.append('\n');
    }
}
