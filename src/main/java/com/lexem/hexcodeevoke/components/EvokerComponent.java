package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class EvokerComponent implements Component<EntityStore> {
    private static final int BASE_MAX_HEX_CREATURES = 0;
    private static final String NO_WORLD = "";

    private String activeWorld = NO_WORLD;
    private final Map<String, WorldBook> worldBooks = new LinkedHashMap<>();
    private WorldBook legacyBook;
    private final Set<String> unlockedHexCreatures = new LinkedHashSet<>();

    private static ComponentType<EntityStore, EvokerComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, EvokerComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, EvokerComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<EvokerComponent> CODEC = BuilderCodec
            .builder(EvokerComponent.class, EvokerComponent::new)
            .append(
                    new KeyedCodec<>("ActiveWorld", Codec.STRING),
                    (component, value) -> component.activeWorld = value != null ? value : NO_WORLD,
                    component -> component.activeWorld
            ).add()
            .append(
                    new KeyedCodec<>("Worlds", new MapCodec<>(WorldBook.CODEC, LinkedHashMap<String, WorldBook>::new)),
                    (component, value) -> component.replaceWorldBooks(value),
                    component -> component.savedWorldBooks()
            ).add()
            .append(
                    new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC),
                    (component, value) -> {
                        if (value != null) component.legacyBook().targetPosition = value;
                    },
                    component -> component.legacyBook != null ? component.legacyBook.targetPosition : null
            ).add()
            .append(
                    new KeyedCodec<>("SelectedHexCreatures", Codec.STRING_ARRAY),
                    (component, value) -> {
                        if (value != null) replace(component.legacyBook().selectedHexCreatures, value);
                    },
                    component -> component.legacyBook != null ? component.legacyBook.selectedHexCreatures.toArray(new String[0]) : null
            ).add()
            .append(new KeyedCodec<>("ListHexCreatures", new ArrayCodec<>(HexCreatureEntry.CODEC, HexCreatureEntry[]::new)),
                    (component, objects) -> {
                        if (objects != null) component.legacyBook().replaceHexCreatures(objects);
                    },
                    component -> component.legacyBook != null ? component.legacyBook.listHexCreatures.toArray(new HexCreatureEntry[0]) : null
            ).add()
            .append(
                    new KeyedCodec<>("UnlockedHexCreatures", Codec.STRING_ARRAY),
                    (component, value) -> replace(component.unlockedHexCreatures, value),
                    component -> component.unlockedHexCreatures.toArray(new String[0])
            ).add()
            .build();

    public EvokerComponent() {
    }

    private static <T> void replace(@Nonnull Collection<T> target, @Nullable T[] values) {
        target.clear();
        if (values == null) return;

        for (T value : values) {
            if (value != null) target.add(value);
        }
    }

    @Nonnull
    private WorldBook legacyBook() {
        if (legacyBook == null) legacyBook = new WorldBook();
        return legacyBook;
    }

    private void replaceWorldBooks(@Nullable Map<String, WorldBook> books) {
        worldBooks.clear();
        if (books == null) return;

        books.forEach((world, book) -> {
            if (world != null && book != null) worldBooks.put(world, book);
        });
    }

    @Nonnull
    private Map<String, WorldBook> savedWorldBooks() {
        Map<String, WorldBook> saved = new LinkedHashMap<>();
        worldBooks.forEach((world, book) -> {
            if (!book.isEmpty()) saved.put(world, book);
        });
        return saved;
    }

    @Nonnull
    private WorldBook book() {
        return worldBooks.computeIfAbsent(activeWorld, world -> new WorldBook());
    }

    @Nonnull
    public String getActiveWorld() {
        return activeWorld;
    }

    public void setActiveWorld(@Nullable String worldName) {
        activeWorld = worldName != null ? worldName : NO_WORLD;
    }

    public boolean hasLegacyData() {
        return legacyBook != null;
    }

    public void migrateLegacyData(@Nonnull Function<String, String> worldOfHexCreature) {
        if (legacyBook == null) return;

        WorldBook legacy = legacyBook;
        legacyBook = null;

        for (HexCreatureEntry entry : legacy.listHexCreatures) {
            String world = entry.getUUID() != null ? worldOfHexCreature.apply(entry.getUUID()) : null;
            WorldBook target = worldBooks.computeIfAbsent(world != null ? world : activeWorld, name -> new WorldBook());
            target.addHexCreature(entry);
            if (legacy.selectedHexCreatures.contains(entry.getUUID())) target.selectedHexCreatures.add(entry.getUUID());
        }

        if (legacy.targetPosition != null && book().targetPosition == null) book().targetPosition = legacy.targetPosition;
    }

    public Vector3d getTargetPosition() {
        return book().targetPosition;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        book().targetPosition = newTargetPosition;
    }

    public int getMaxHexCreatures() {
        int max = BASE_MAX_HEX_CREATURES;
        Set<String> counted = new HashSet<>();
        for (AllowedHexItemsAsset.HexItem hexItem : AllowedHexItemsAsset.getAll()) {
            AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
            String entityId = mainItem.getEntityId();
            if (!AllowedHexItemsAsset.isEvokeHexCreature(entityId) || !counted.add(entityId)) continue;
            if (!mainItem.requiresUnlock() || unlockedHexCreatures.contains(entityId)) max++;
        }
        return max;
    }

    @Nonnull
    public List<HexCreatureEntry> getListHexCreatures() {
        return List.copyOf(book().listHexCreatures);
    }

    @Nonnull
    public List<HexCreatureEntry> getHexCreaturesInAllWorlds() {
        List<HexCreatureEntry> entries = new ArrayList<>();
        for (WorldBook book : worldBooks.values()) {
            entries.addAll(book.listHexCreatures);
        }
        return entries;
    }

    @Nonnull
    public List<String> getSelectedHexCreatures() {
        return List.copyOf(book().selectedHexCreatures);
    }

    public int getSelectedCount() {
        return book().selectedHexCreatures.size();
    }

    public boolean hasSelection() {
        return !book().selectedHexCreatures.isEmpty();
    }

    public boolean isSelected(@Nullable String uuid) {
        return uuid != null && book().selectedHexCreatures.contains(uuid);
    }

    public boolean hexCreatureBelongsToPlayer(@Nullable String uuid) {
        return getHexCreatureByUUID(uuid) != null;
    }

    public void addSelectedHexCreature(@Nullable String uuid) {
        if (uuid != null) book().selectedHexCreatures.add(uuid);
    }

    public void clearSelectedHexCreatures() {
        book().selectedHexCreatures.clear();
    }

    public void removeSelectedHexCreature(@Nullable String uuid) {
        book().selectedHexCreatures.remove(uuid);
    }

    @Nullable
    public HexCreatureEntry getHexCreatureByUUID(@Nullable String npcUUID) {
        return book().getHexCreatureByUUID(npcUUID);
    }

    @Nonnull
    public List<String> getHexCreatureUUIDs() {
        return book().getHexCreatureUUIDs();
    }

    @Nonnull
    public List<String> getHexCreatureUUIDsInAllWorlds() {
        List<String> uuids = new ArrayList<>();
        for (WorldBook book : worldBooks.values()) {
            uuids.addAll(book.getHexCreatureUUIDs());
        }
        return uuids;
    }

    public int getHexCreatureCount() {
        return book().listHexCreatures.size();
    }

    public void addHexCreature(@Nonnull HexCreatureEntry hexCreature) {
        book().addHexCreature(hexCreature);
    }

    public boolean removeHexCreatureByUUID(@Nullable String uuid) {
        if (uuid == null) return false;

        boolean removed = false;
        for (WorldBook book : worldBooks.values()) {
            removed |= book.removeHexCreature(uuid);
        }
        return removed;
    }

    public boolean pruneSelection() {
        WorldBook book = book();
        return book.selectedHexCreatures.removeIf(uuid -> book.getHexCreatureByUUID(uuid) == null);
    }

    @Nonnull
    public List<String> getUnlockedHexCreatures() {
        return List.copyOf(unlockedHexCreatures);
    }

    public boolean isHexCreatureUnlocked(@Nullable String entityId) {
        return entityId != null && unlockedHexCreatures.contains(entityId);
    }

    public boolean unlockHexCreature(@Nullable String entityId) {
        if (entityId == null || entityId.isEmpty()) return false;

        return unlockedHexCreatures.add(entityId);
    }

    public boolean canAddHexCreature() {
        return getHexCreatureCount() < getMaxHexCreatures();
    }

    public boolean canSelectHexCreature(int wandMaxSelection) {
        return getSelectedCount() < wandMaxSelection;
    }

    @Nonnull
    @Override
    public EvokerComponent clone() {
        ExtraInfo extraInfo = new ExtraInfo();
        return CODEC.decode(CODEC.encode(this, extraInfo), extraInfo);
    }

    @Override
    public String toString() {
        return "EvokerComponent{" +
                "activeWorld=" + activeWorld +
                ", worlds=" + worldBooks.keySet() +
                ", selectedHexCreatures=" + book().selectedHexCreatures +
                '}';
    }

    public static class WorldBook {
        public static final BuilderCodec<WorldBook> CODEC = BuilderCodec
                .builder(WorldBook.class, WorldBook::new)
                .append(
                        new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC),
                        (book, value) -> book.targetPosition = value,
                        book -> book.targetPosition
                ).add()
                .append(
                        new KeyedCodec<>("SelectedHexCreatures", Codec.STRING_ARRAY),
                        (book, value) -> replace(book.selectedHexCreatures, value),
                        book -> book.selectedHexCreatures.toArray(new String[0])
                ).add()
                .append(new KeyedCodec<>("ListHexCreatures", new ArrayCodec<>(HexCreatureEntry.CODEC, HexCreatureEntry[]::new)),
                        (book, objects) -> book.replaceHexCreatures(objects),
                        book -> book.listHexCreatures.toArray(new HexCreatureEntry[0])
                ).add()
                .build();

        private Vector3d targetPosition;
        private final Set<String> selectedHexCreatures = new LinkedHashSet<>();
        private final List<HexCreatureEntry> listHexCreatures = new ArrayList<>();

        public WorldBook() {
        }

        private boolean isEmpty() {
            return targetPosition == null && selectedHexCreatures.isEmpty() && listHexCreatures.isEmpty();
        }

        private void replaceHexCreatures(@Nullable HexCreatureEntry[] entries) {
            listHexCreatures.clear();
            if (entries == null) return;

            for (HexCreatureEntry entry : entries) {
                if (entry != null) addHexCreature(entry);
            }
        }

        @Nullable
        private HexCreatureEntry getHexCreatureByUUID(@Nullable String npcUUID) {
            if (npcUUID == null) return null;

            for (HexCreatureEntry hc : listHexCreatures) {
                if (npcUUID.equals(hc.getUUID())) return hc;
            }
            return null;
        }

        @Nonnull
        private List<String> getHexCreatureUUIDs() {
            List<String> uuids = new ArrayList<>(listHexCreatures.size());
            for (HexCreatureEntry entry : listHexCreatures) {
                uuids.add(entry.getUUID());
            }
            return uuids;
        }

        private void addHexCreature(@Nonnull HexCreatureEntry hexCreature) {
            if (getHexCreatureByUUID(hexCreature.getUUID()) != null) return;

            listHexCreatures.add(hexCreature);
        }

        private boolean removeHexCreature(@Nonnull String uuid) {
            selectedHexCreatures.remove(uuid);
            return listHexCreatures.removeIf(entry -> uuid.equals(entry.getUUID()));
        }
    }
}
