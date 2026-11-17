package com.lexem.hexcodeevoke.utils.transport;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransportRoutingTest {
    private static final Vector3d NPC = new Vector3d(0, 64, 0);

    private static TransportState.Container chest(int x) {
        return TransportState.Container.ofBlock(new Vector3i(x, 64, 0), false);
    }

    @Test
    void cargoAlwaysGoesToTheDestination() {
        assertEquals(TransportState.Target.Deliver, TransportUtils.chooseTarget(true, false));
        assertEquals(TransportState.Target.Deliver, TransportUtils.chooseTarget(true, true));
    }

    @Test
    void withoutCargoLoopCollectsAndSingleContainerWaits() {
        assertEquals(TransportState.Target.Collect, TransportUtils.chooseTarget(false, true));
        assertEquals(TransportState.Target.Wait, TransportUtils.chooseTarget(false, false));
    }

    @Test
    void firstAssignmentSetsOnlyTheDestination() {
        TransportState state = new TransportState();
        TransportState.Container chest = chest(5);

        state.assign(chest, NPC, false);

        assertTrue(state.isActive());
        assertFalse(state.isLoop());
        assertNull(state.getSource());
        assertTrue(chest.isSame(state.getDestination()));
        assertNotSame(chest, state.getDestination());
        assertEquals(NPC, state.getWaitPosition());
    }

    @Test
    void secondContainerWhileTransportingStartsALoop() {
        TransportState state = new TransportState();
        state.assign(chest(5), NPC, false);

        state.assign(chest(10), NPC, true);

        assertTrue(state.isLoop());
        assertTrue(chest(5).isSame(state.getSource()));
        assertTrue(chest(10).isSame(state.getDestination()));
        assertNull(state.getWaitPosition());
        assertSame(state.getDestination(), state.getContainer(TransportState.Target.Deliver));
        assertSame(state.getSource(), state.getContainer(TransportState.Target.Collect));
        assertNull(state.getContainer(TransportState.Target.Wait));
    }

    @Test
    void sameContainerWhileTransportingKeepsTheSingleRoute() {
        TransportState state = new TransportState();
        state.assign(chest(5), NPC, false);

        state.assign(chest(5), new Vector3d(3, 64, 3), true);

        assertFalse(state.isLoop());
        assertEquals(NPC, state.getWaitPosition());
    }

    @Test
    void assigningDuringALoopRestartsWithANewDestination() {
        TransportState state = new TransportState();
        state.assign(chest(5), NPC, false);
        state.assign(chest(10), NPC, true);

        Vector3d moved = new Vector3d(2, 64, 2);
        state.assign(chest(20), moved, true);

        assertFalse(state.isLoop());
        assertTrue(chest(20).isSame(state.getDestination()));
        assertEquals(moved, state.getWaitPosition());
    }

    @Test
    void assigningWhenNotTransportingReplacesTheRoute() {
        TransportState state = new TransportState();
        state.assign(chest(5), NPC, false);

        state.assign(chest(10), NPC, false);

        assertFalse(state.isLoop());
        assertTrue(chest(10).isSame(state.getDestination()));
    }

    @Test
    void clearDeactivatesTheRoute() {
        TransportState state = new TransportState();
        state.assign(chest(5), NPC, false);
        state.assign(chest(10), NPC, true);

        state.clear();

        assertFalse(state.isActive());
        assertFalse(state.isLoop());
        assertNull(state.getSource());
        assertNull(state.getDestination());
        assertNull(state.getWaitPosition());
    }

    @Test
    void distanceForALoopIsMeasuredFromTheCurrentDestination() {
        TransportState state = new TransportState();
        state.assign(chest(100), NPC, false);

        Vector3d farFromDestination = new Vector3d(100, 64, 0);
        assertFalse(state.isWithinDistance(chest(10), farFromDestination, false, 20));
        assertTrue(state.isWithinDistance(chest(110), NPC, true, 20));
        assertFalse(state.isWithinDistance(chest(10), NPC, true, 20));
    }

    @Test
    void distanceForTheSameDestinationIsMeasuredFromTheNpc() {
        TransportState state = new TransportState();
        state.assign(chest(100), NPC, false);

        assertTrue(state.isWithinDistance(chest(100), new Vector3d(95, 64, 0), true, 20));
        assertFalse(state.isWithinDistance(chest(100), NPC, true, 20));
    }

    @Test
    void containersCompareByBlockOrCreature() {
        TransportState.Container creature = TransportState.Container.ofCreature("a", NPC);

        assertTrue(chest(1).isSame(chest(1)));
        assertFalse(chest(1).isSame(chest(2)));
        assertFalse(chest(1).isSame(creature));
        assertTrue(creature.isSame(TransportState.Container.ofCreature("a", new Vector3d(9, 9, 9))));
        assertFalse(creature.isSame(null));
    }

    @Test
    void containerTypeMatching() {
        TransportState.Container creature = TransportState.Container.ofCreature("a", NPC);

        assertTrue(TransportState.ContainerType.Any.matches(chest(1)));
        assertTrue(TransportState.ContainerType.Block.matches(chest(1)));
        assertFalse(TransportState.ContainerType.Block.matches(creature));
        assertTrue(TransportState.ContainerType.Creature.matches(creature));
        assertFalse(TransportState.ContainerType.Any.matches(null));
    }

    @Test
    void positionUpdatesOnlyAfterTheMinimumChange() {
        TransportState.Container creature = TransportState.Container.ofCreature("a", NPC);

        assertFalse(creature.setPosition(new Vector3d(0.5, 64, 0), 1.0));
        assertTrue(creature.setPosition(new Vector3d(2, 64, 0), 1.0));
        assertEquals(new Vector3d(2, 64, 0), creature.getPosition());
    }
}
