package com.lexem.hexcodeevoke.components;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HexCreatureComponentTest {
    private static ChestTaskComponent task(Vector3d chest, String itemId) {
        return new ChestTaskComponent(ChestTaskComponent.TaskType.Store, chest, itemId, 1, false);
    }

    @Test
    void emptyLifeEssenceSurvivesClone() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        hexCreature.setLifeEssence(0);

        HexCreatureComponent clone = hexCreature.cloneSerializable();

        assertTrue(clone.hasLifeEssence());
        assertEquals(0, clone.getLifeEssence());
    }

    @Test
    void missingLifeEssenceStaysMissing() {
        HexCreatureComponent clone = new HexCreatureComponent().cloneSerializable();

        assertFalse(clone.hasLifeEssence());
    }

    @Test
    void cloneKeepsPersistedLists() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        hexCreature.setUUID("npc");
        hexCreature.setNameKey("server.npcRoles.Hex_Fairy.name");
        hexCreature.addMinionUUID("m1");
        hexCreature.addMinionUUID("m1");
        hexCreature.addMinionUUID("m2");
        hexCreature.updateChestMemory(new Vector3d(1, 2, 3), new String[]{"Wood"});
        hexCreature.addChestTask(task(new Vector3d(1, 2, 3), "Wood"));
        hexCreature.setTarget(new Vector3d(5, 6, 7), 3);

        HexCreatureComponent clone = hexCreature.cloneSerializable();

        assertEquals("npc", clone.getUUID());
        assertEquals("server.npcRoles.Hex_Fairy.name", clone.getNameKey());
        assertEquals(List.of("m1", "m2"), clone.getMinionUUIDs());
        assertEquals(1, clone.getListChestMemory().size());
        assertEquals(List.of("Wood"), clone.getRememberedItemIds());
        assertEquals(1, clone.getListChestTask().size());
        assertEquals(new Vector3d(5, 6, 7), clone.getTargetPosition());
        assertEquals(3, clone.getTargetGroupSize());
        assertNotSame(hexCreature.getListChestMemory().getFirst(), clone.getListChestMemory().getFirst());
    }

    @Test
    void cloneCopiesRuntimeButSerializableCloneDoesNot() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        Vector3d chest = new Vector3d(4, 5, 6);
        hexCreature.getRuntime().markChestVisited(chest);
        hexCreature.getRuntime().setSearchChestPosition(chest);

        HexCreatureComponent clone = (HexCreatureComponent) hexCreature.clone();
        HexCreatureComponent serializable = hexCreature.cloneSerializable();

        assertTrue(clone.getRuntime().wasChestVisitedRecently(chest, 60_000));
        assertEquals(chest, clone.getRuntime().getSearchChestPosition());
        assertNotSame(hexCreature.getRuntime(), clone.getRuntime());
        assertFalse(serializable.getRuntime().wasChestVisitedRecently(chest, 60_000));
        assertNull(serializable.getRuntime().getSearchChestPosition());
    }

    @Test
    void chestMemoryDropsOldestWhenFull() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        for (int i = 0; i < 130; i++) {
            hexCreature.addChestMemory(new ChestMemoryComponent(new Vector3d(i, 0, 0), new String[0]));
        }

        List<ChestMemoryComponent> memories = hexCreature.getListChestMemory();
        assertEquals(128, memories.size());
        assertEquals(new Vector3d(2, 0, 0), memories.getFirst().getChestPosition());
        assertThrows(UnsupportedOperationException.class, () -> memories.add(new ChestMemoryComponent()));
    }

    @Test
    void forgetChestRemovesMemoryAndTasksOfThatBlock() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        hexCreature.updateChestMemory(new Vector3d(1, 2, 3), new String[]{"Wood"});
        hexCreature.updateChestMemory(new Vector3d(9, 9, 9), new String[]{"Stone"});
        hexCreature.addChestTask(task(new Vector3d(1.5, 2.2, 3.9), "Wood"));
        ChestTaskComponent other = task(new Vector3d(9, 9, 9), "Stone");
        hexCreature.addChestTask(other);

        hexCreature.forgetChest(new Vector3d(1.1, 2.9, 3.4));

        assertEquals(List.of("Stone"), hexCreature.getRememberedItemIds());
        assertEquals(List.of(other), hexCreature.getListChestTask());
    }

    @Test
    void chestTasksCanBeRemovedByItemOrInstance() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        ChestTaskComponent wood = task(new Vector3d(1, 0, 0), "Wood");
        ChestTaskComponent stone = task(new Vector3d(2, 0, 0), "Stone");
        hexCreature.addChestTask(wood);
        hexCreature.addChestTask(stone);

        hexCreature.removeChestTasksByItemId("Wood");
        assertEquals(List.of(stone), hexCreature.getListChestTask());

        hexCreature.removeChestTask(stone);
        assertTrue(hexCreature.isListChestTaskEmpty());
    }

    @Test
    void updateChestMemoryReplacesItemsOfKnownChest() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        Vector3d chest = new Vector3d(1, 2, 3);
        hexCreature.updateChestMemory(chest, new String[]{"Wood"});
        hexCreature.updateChestMemory(chest, null);

        assertEquals(1, hexCreature.getListChestMemory().size());
        assertSame(hexCreature.findEmptyChestMemory(), hexCreature.getListChestMemory().getFirst());
        assertNull(hexCreature.findChestMemoryWithSpace());
    }

    @Test
    void entryCarriesSummaryAndAppliesNames() {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        hexCreature.setUUID("npc");
        hexCreature.setNameKey("key");
        hexCreature.setLifeEssence(2.5F);

        HexCreatureEntry entry = HexCreatureEntry.of(hexCreature);
        assertEquals("npc", entry.getUUID());
        assertEquals(2.5F, entry.getStoredLifeEssence(10));
        assertEquals(1F, entry.getStoredLifeEssence(1));

        entry.setName("Fifi");
        entry.setShowName(true);
        assertTrue(entry.applyTo(hexCreature));
        assertEquals("Fifi", hexCreature.getCustomName());
        assertTrue(hexCreature.getShowName());
        assertFalse(entry.applyTo(hexCreature));
    }

    @Test
    void entryWithoutLifeEssenceUsesFullCapacity() {
        HexCreatureEntry entry = HexCreatureEntry.of(new HexCreatureComponent());

        assertEquals(30F, entry.getStoredLifeEssence(30));
    }
}
