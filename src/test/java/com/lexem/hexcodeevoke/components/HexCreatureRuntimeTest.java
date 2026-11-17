package com.lexem.hexcodeevoke.components;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexCreatureRuntimeTest {
    @Test
    void unreachableChestsMatchTheWholeBlockAndExpire() throws InterruptedException {
        HexCreatureRuntime runtime = new HexCreatureRuntime();
        runtime.markChestUnreachable(new Vector3d(1.2, 2.8, -3.5), 60_000);
        runtime.markChestUnreachable(new Vector3d(7, 7, 7), 1);

        assertTrue(runtime.isChestUnreachable(new Vector3d(1.9, 2.1, -3.1)));
        assertFalse(runtime.isChestUnreachable(new Vector3d(2, 2, -3)));

        Thread.sleep(5);
        assertFalse(runtime.isChestUnreachable(new Vector3d(7, 7, 7)));
    }

    @Test
    void withoutUnreachableChestsFiltersTheList() {
        HexCreatureRuntime runtime = new HexCreatureRuntime();
        Vector3d blocked = new Vector3d(1, 1, 1);
        Vector3d open = new Vector3d(2, 1, 1);
        runtime.markChestUnreachable(blocked, 60_000);

        assertEquals(List.of(open), runtime.withoutUnreachableChests(List.of(blocked, open)));
    }

    @Test
    void visitedChestsCanBeCleared() {
        HexCreatureRuntime runtime = new HexCreatureRuntime();
        Vector3d chest = new Vector3d(3, 4, 5);
        runtime.markChestVisited(chest);

        assertTrue(runtime.wasChestVisitedRecently(chest, 60_000));
        runtime.clearVisitedChests();
        assertFalse(runtime.wasChestVisitedRecently(chest, 60_000));
    }

    @Test
    void lifeEssenceTimersAccumulate() {
        HexCreatureRuntime runtime = new HexCreatureRuntime();

        assertFalse(runtime.pollLifeEssenceCheck(0.4F, 1F));
        assertTrue(runtime.pollLifeEssenceCheck(0.7F, 1F));
        assertFalse(runtime.pollLifeEssenceCheck(0.1F, 1F));

        assertEquals(2, runtime.pollLifeEssenceDrainSteps(2.5F, 1F));
        assertEquals(1, runtime.pollLifeEssenceDrainSteps(0.5F, 1F));
    }

    @Test
    void copyIsIndependent() {
        HexCreatureRuntime runtime = new HexCreatureRuntime();
        Vector3d anchor = new Vector3d(1, 2, 3);
        runtime.setExploreAnchor(anchor);
        runtime.setLowLifeEssenceWarned(true);

        HexCreatureRuntime copy = runtime.copy();
        runtime.markChestVisited(anchor);

        assertEquals(anchor, copy.getExploreAnchor());
        assertNotSame(anchor, copy.getExploreAnchor());
        assertTrue(copy.isLowLifeEssenceWarned());
        assertFalse(copy.wasChestVisitedRecently(anchor, 60_000));
        assertNotSame(runtime.getCrawlState(), copy.getCrawlState());
    }
}
