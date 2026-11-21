package com.lexem.hexcodeevoke.components;

import org.junit.jupiter.api.Test;

import com.hypixel.hytale.codec.ExtraInfo;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvokerComponentTest {
    private static HexCreatureEntry entry(String uuid) {
        HexCreatureComponent hexCreature = new HexCreatureComponent();
        hexCreature.setUUID(uuid);
        hexCreature.setTypeId("Hex_Fairy");
        return HexCreatureEntry.of(hexCreature);
    }

    @Test
    void addHexCreatureIgnoresDuplicates() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addHexCreature(entry("a"));
        evoker.addHexCreature(entry("a"));
        evoker.addHexCreature(entry("b"));

        assertEquals(List.of("a", "b"), evoker.getHexCreatureUUIDs());
        assertEquals(2, evoker.getHexCreatureCount());
        assertTrue(evoker.hexCreatureBelongsToPlayer("b"));
        assertFalse(evoker.hexCreatureBelongsToPlayer("c"));
        assertFalse(evoker.hexCreatureBelongsToPlayer(null));
    }

    @Test
    void removeHexCreatureAlsoDropsSelection() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addHexCreature(entry("a"));
        evoker.addHexCreature(entry("b"));
        evoker.addSelectedHexCreature("a");
        evoker.addSelectedHexCreature("b");

        evoker.removeHexCreatureByUUID("a");

        assertNull(evoker.getHexCreatureByUUID("a"));
        assertEquals(List.of("b"), evoker.getSelectedHexCreatures());
    }

    @Test
    void selectionKeepsOrderAndSkipsDuplicates() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addSelectedHexCreature("b");
        evoker.addSelectedHexCreature("a");
        evoker.addSelectedHexCreature("b");
        evoker.addSelectedHexCreature(null);

        assertEquals(List.of("b", "a"), evoker.getSelectedHexCreatures());
        assertEquals(2, evoker.getSelectedCount());
        assertTrue(evoker.isSelected("a"));
        assertTrue(evoker.canSelectHexCreature(3));
        assertFalse(evoker.canSelectHexCreature(2));

        evoker.clearSelectedHexCreatures();
        assertFalse(evoker.hasSelection());
    }

    @Test
    void pruneSelectionDropsCreaturesNotOwned() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addHexCreature(entry("a"));
        evoker.addSelectedHexCreature("a");
        evoker.addSelectedHexCreature("ghost");

        assertTrue(evoker.pruneSelection());
        assertEquals(List.of("a"), evoker.getSelectedHexCreatures());
        assertFalse(evoker.pruneSelection());
    }

    @Test
    void unlockHexCreatureOnlyOnce() {
        EvokerComponent evoker = new EvokerComponent();

        assertTrue(evoker.unlockHexCreature("Hex_Fairy"));
        assertFalse(evoker.unlockHexCreature("Hex_Fairy"));
        assertFalse(evoker.unlockHexCreature(""));
        assertFalse(evoker.unlockHexCreature(null));
        assertTrue(evoker.isHexCreatureUnlocked("Hex_Fairy"));
        assertEquals(List.of("Hex_Fairy"), evoker.getUnlockedHexCreatures());
    }

    @Test
    void returnedListsAreSnapshots() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addHexCreature(entry("a"));
        evoker.addSelectedHexCreature("a");

        List<String> selected = evoker.getSelectedHexCreatures();
        List<HexCreatureEntry> entries = evoker.getListHexCreatures();
        evoker.removeHexCreatureByUUID("a");

        assertEquals(List.of("a"), selected);
        assertEquals(1, entries.size());
        assertThrows(UnsupportedOperationException.class, () -> selected.add("b"));
    }

    @Test
    void cloneKeepsListsAndCopiesEntries() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.addHexCreature(entry("a"));
        evoker.addHexCreature(entry("b"));
        evoker.addSelectedHexCreature("b");
        evoker.unlockHexCreature("Hex_Fairy");

        EvokerComponent clone = evoker.clone();

        assertEquals(List.of("a", "b"), clone.getHexCreatureUUIDs());
        assertEquals(List.of("b"), clone.getSelectedHexCreatures());
        assertEquals(List.of("Hex_Fairy"), clone.getUnlockedHexCreatures());
        HexCreatureEntry original = evoker.getHexCreatureByUUID("a");
        HexCreatureEntry copied = clone.getHexCreatureByUUID("a");
        assertNotNull(copied);
        assertNotSame(original, copied);
    }

    @Test
    void eachWorldHasItsOwnBook() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.setActiveWorld("default");
        evoker.addHexCreature(entry("a"));
        evoker.addHexCreature(entry("b"));
        evoker.addSelectedHexCreature("a");

        evoker.setActiveWorld("other");
        assertEquals(0, evoker.getHexCreatureCount());
        assertFalse(evoker.hasSelection());
        assertFalse(evoker.hexCreatureBelongsToPlayer("a"));
        evoker.addHexCreature(entry("c"));

        assertEquals(List.of("c"), evoker.getHexCreatureUUIDs());
        assertEquals(List.of("a", "b", "c"), evoker.getHexCreatureUUIDsInAllWorlds());

        evoker.setActiveWorld("default");
        assertEquals(List.of("a", "b"), evoker.getHexCreatureUUIDs());
        assertEquals(List.of("a"), evoker.getSelectedHexCreatures());
    }

    @Test
    void removeHexCreatureFindsItInAnyWorld() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.setActiveWorld("default");
        evoker.addHexCreature(entry("a"));
        evoker.addSelectedHexCreature("a");
        evoker.setActiveWorld("other");

        assertTrue(evoker.removeHexCreatureByUUID("a"));
        assertFalse(evoker.removeHexCreatureByUUID("a"));

        evoker.setActiveWorld("default");
        assertEquals(0, evoker.getHexCreatureCount());
        assertFalse(evoker.hasSelection());
    }

    @Test
    void worldBooksSurviveEncoding() {
        EvokerComponent evoker = new EvokerComponent();
        evoker.setActiveWorld("default");
        evoker.addHexCreature(entry("a"));
        evoker.setActiveWorld("other");
        evoker.addHexCreature(entry("b"));
        evoker.addSelectedHexCreature("b");
        evoker.setActiveWorld("empty");
        evoker.getHexCreatureCount();

        ExtraInfo extraInfo = new ExtraInfo();
        BsonDocument document = EvokerComponent.CODEC.encode(evoker, extraInfo).asDocument();
        EvokerComponent decoded = EvokerComponent.CODEC.decode(document, extraInfo);

        assertFalse(document.containsKey("ListHexCreatures"));
        assertFalse(document.getDocument("Worlds").containsKey("empty"));
        assertFalse(decoded.hasLegacyData());
        decoded.setActiveWorld("other");
        assertEquals(List.of("b"), decoded.getSelectedHexCreatures());
        decoded.setActiveWorld("default");
        assertEquals(List.of("a"), decoded.getHexCreatureUUIDs());
    }

    @Test
    void legacyBookIsSplitByWorldOfEachCreature() {
        EvokerComponent old = new EvokerComponent();
        old.setActiveWorld("default");
        old.addHexCreature(entry("a"));
        old.addHexCreature(entry("b"));
        old.addSelectedHexCreature("a");
        old.addSelectedHexCreature("b");
        ExtraInfo extraInfo = new ExtraInfo();
        BsonDocument book = EvokerComponent.CODEC.encode(old, extraInfo).asDocument().getDocument("Worlds").getDocument("default");
        BsonDocument legacy = new BsonDocument()
                .append("ListHexCreatures", book.get("ListHexCreatures"))
                .append("SelectedHexCreatures", new BsonArray(List.of(new BsonString("a"), new BsonString("b"))));

        EvokerComponent evoker = EvokerComponent.CODEC.decode(legacy, extraInfo);
        assertTrue(evoker.hasLegacyData());
        evoker.setActiveWorld("current");
        evoker.migrateLegacyData(uuid -> uuid.equals("b") ? "far" : null);

        assertFalse(evoker.hasLegacyData());
        assertEquals(List.of("a"), evoker.getHexCreatureUUIDs());
        assertEquals(List.of("a"), evoker.getSelectedHexCreatures());
        evoker.setActiveWorld("far");
        assertEquals(List.of("b"), evoker.getHexCreatureUUIDs());
        assertEquals(List.of("b"), evoker.getSelectedHexCreatures());
    }
}
