package com.lexem.hexcodeevoke.utils.block;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinderUtilsTest {
    private static final FinderUtils.BlockValidator<Set<Vector3i>> IN_SET = (block, targets) -> targets.contains(block);

    @Test
    void findsTheClosestMatchingBlock() {
        Set<Vector3i> targets = Set.of(new Vector3i(13, 64, 10), new Vector3i(11, 64, 10), new Vector3i(10, 70, 10));

        Vector3i found = FinderUtils.findNearestBlock(new Vector3d(10.5, 64.2, 10.5), 5, 8, IN_SET, targets);

        assertEquals(new Vector3i(11, 64, 10), found);
    }

    @Test
    void respectsBothRanges() {
        Vector3d start = new Vector3d(0.5, 10, 0.5);

        assertNull(FinderUtils.findNearestBlock(start, 3, 2, IN_SET, Set.of(new Vector3i(4, 10, 0))));
        assertNull(FinderUtils.findNearestBlock(start, 3, 2, IN_SET, Set.of(new Vector3i(0, 13, 0))));
        assertEquals(new Vector3i(3, 12, 0), FinderUtils.findNearestBlock(start, 3, 2, IN_SET, Set.of(new Vector3i(3, 12, 0))));
    }

    @Test
    void returnsNullForMissingInput() {
        assertNull(FinderUtils.findNearestBlock(null, 3, 3, IN_SET, Set.of()));
        assertNull(FinderUtils.findNearestBlock(new Vector3d(), 3, 3, null, Set.of()));
    }

    @Test
    void returnedBlockIsACopy() {
        Vector3i target = new Vector3i(1, 0, 0);
        Vector3i found = FinderUtils.findNearestBlock(new Vector3d(0.5, 0, 0.5), 2, 0, IN_SET, Set.of(target));

        found.add(5, 5, 5);
        assertEquals(new Vector3i(1, 0, 0), target);
    }

    @Test
    void isWithinRangeUsesTheBlockUnderTheStart() {
        Vector3d start = new Vector3d(0.9, 5.9, 0.9);

        assertTrue(FinderUtils.isWithinRange(start, new Vector3i(3, 7, 0), 3, 2));
        assertFalse(FinderUtils.isWithinRange(start, new Vector3i(3, 8, 0), 3, 2));
        assertFalse(FinderUtils.isWithinRange(start, new Vector3i(3, 5, 1), 3, 2));
        assertFalse(FinderUtils.isWithinRange(null, new Vector3i(), 3, 2));
    }

    @Test
    void levelSearchPrefersTheCurrentLevel() {
        Set<Vector3i> targets = Set.of(new Vector3i(0, 4, 0), new Vector3i(3, 5, 0));

        Vector3i found = FinderUtils.findNearestBlockByLevel(new Vector3d(0.5, 5, 0.5), 4, 2, IN_SET, targets);

        assertEquals(new Vector3i(3, 5, 0), found);
    }
}
