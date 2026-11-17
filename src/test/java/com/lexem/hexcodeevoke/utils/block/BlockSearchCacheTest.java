package com.lexem.hexcodeevoke.utils.block;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BlockSearchCacheTest {
    private static final Vector3d START = new Vector3d(0.5, 0, 0.5);

    private static FinderUtils.BlockValidator<Set<Vector3i>> counting(AtomicInteger calls) {
        return (block, targets) -> {
            calls.incrementAndGet();
            return targets.contains(block);
        };
    }

    @Test
    void reusesTheCachedBlockWhileItIsValid() {
        AtomicInteger calls = new AtomicInteger();
        Set<Vector3i> targets = new HashSet<>(Set.of(new Vector3i(2, 0, 0)));
        BlockSearchCache cache = new BlockSearchCache();

        assertEquals(new Vector3i(2, 0, 0), cache.find(START, 3, 1, counting(calls), targets));
        calls.set(0);
        assertEquals(new Vector3i(2, 0, 0), cache.find(START, 3, 1, counting(calls), targets));
        assertEquals(1, calls.get());
    }

    @Test
    void searchesAgainWhenTheCachedBlockStopsMatching() {
        AtomicInteger calls = new AtomicInteger();
        Set<Vector3i> targets = new HashSet<>(Set.of(new Vector3i(1, 0, 0)));
        BlockSearchCache cache = new BlockSearchCache();
        cache.find(START, 3, 1, counting(calls), targets);

        targets.clear();
        targets.add(new Vector3i(0, 0, 3));

        assertEquals(new Vector3i(0, 0, 3), cache.find(START, 3, 1, counting(calls), targets));
    }

    @Test
    void searchesAgainWhenTheCachedBlockIsOutOfRange() {
        AtomicInteger calls = new AtomicInteger();
        Set<Vector3i> targets = new HashSet<>(Set.of(new Vector3i(2, 0, 0), new Vector3i(10, 0, 0)));
        BlockSearchCache cache = new BlockSearchCache();
        cache.find(START, 3, 1, counting(calls), targets);

        assertEquals(new Vector3i(10, 0, 0), cache.find(new Vector3d(10.5, 0, 0.5), 3, 1, counting(calls), targets));
    }

    @Test
    void waitsBeforeRetryingAfterAMiss() {
        AtomicInteger calls = new AtomicInteger();
        Set<Vector3i> targets = new HashSet<>();
        BlockSearchCache cache = new BlockSearchCache();

        assertNull(cache.find(START, 3, 1, counting(calls), targets));
        targets.add(new Vector3i(1, 0, 0));
        calls.set(0);

        assertNull(cache.find(START, 3, 1, counting(calls), targets));
        assertEquals(0, calls.get());
    }
}
