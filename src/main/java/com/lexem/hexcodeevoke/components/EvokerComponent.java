package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EvokerComponent implements Component<EntityStore> {
    private static final int BASE_MAX_HEX_CREATURES = 1;

    private Vector3d targetPosition;
    private final Set<String> selectedHexCreatures = new LinkedHashSet<>();
    private final List<HexCreatureEntry> listHexCreatures = new ArrayList<>();
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
                    new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC),
                    (component, value) -> component.targetPosition = value,
                    component -> component.targetPosition
            ).add()
            .append(
                    new KeyedCodec<>("SelectedHexCreatures", Codec.STRING_ARRAY),
                    (component, value) -> replace(component.selectedHexCreatures, value),
                    component -> component.selectedHexCreatures.toArray(new String[0])
            ).add()
            .append(new KeyedCodec<>("ListHexCreatures", new ArrayCodec<>(HexCreatureEntry.CODEC, HexCreatureEntry[]::new)),
                    (component, objects) -> component.replaceHexCreatures(objects),
                    component -> component.listHexCreatures.toArray(new HexCreatureEntry[0])
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

    private void replaceHexCreatures(@Nullable HexCreatureEntry[] entries) {
        listHexCreatures.clear();
        if (entries == null) return;

        for (HexCreatureEntry entry : entries) {
            if (entry != null) addHexCreature(entry);
        }
    }

    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        this.targetPosition = newTargetPosition;
    }

    public int getMaxHexCreatures() {
        int max = BASE_MAX_HEX_CREATURES;
        for (String entityId : unlockedHexCreatures) {
            if (AllowedHexItemsAsset.isEvokeHexCreature(entityId) && AllowedHexItemsAsset.getMainEntityId(entityId).equals(entityId)) max++;
        }
        return max;
    }

    @Nonnull
    public List<HexCreatureEntry> getListHexCreatures() {
        return List.copyOf(listHexCreatures);
    }

    @Nonnull
    public List<String> getSelectedHexCreatures() {
        return List.copyOf(selectedHexCreatures);
    }

    public int getSelectedCount() {
        return selectedHexCreatures.size();
    }

    public boolean hasSelection() {
        return !selectedHexCreatures.isEmpty();
    }

    public boolean isSelected(@Nullable String uuid) {
        return uuid != null && selectedHexCreatures.contains(uuid);
    }

    public boolean hexCreatureBelongsToPlayer(@Nullable String uuid) {
        return getHexCreatureByUUID(uuid) != null;
    }

    public void addSelectedHexCreature(@Nullable String uuid) {
        if (uuid != null) selectedHexCreatures.add(uuid);
    }

    public void clearSelectedHexCreatures() {
        selectedHexCreatures.clear();
    }

    public void removeSelectedHexCreature(@Nullable String uuid) {
        selectedHexCreatures.remove(uuid);
    }

    @Nullable
    public HexCreatureEntry getHexCreatureByUUID(@Nullable String npcUUID) {
        if (npcUUID == null) return null;

        for (HexCreatureEntry hc : listHexCreatures) {
            if (npcUUID.equals(hc.getUUID())) return hc;
        }
        return null;
    }

    @Nonnull
    public List<String> getHexCreatureUUIDs() {
        List<String> uuids = new ArrayList<>(listHexCreatures.size());
        for (HexCreatureEntry entry : listHexCreatures) {
            uuids.add(entry.getUUID());
        }
        return uuids;
    }

    public int getHexCreatureCount() {
        return listHexCreatures.size();
    }

    public void addHexCreature(@Nonnull HexCreatureEntry hexCreature) {
        if (getHexCreatureByUUID(hexCreature.getUUID()) != null) return;

        listHexCreatures.add(hexCreature);
    }

    public void removeHexCreatureByUUID(@Nullable String uuid) {
        if (uuid == null) return;

        removeSelectedHexCreature(uuid);
        listHexCreatures.removeIf(entry -> uuid.equals(entry.getUUID()));
    }

    public boolean pruneSelection() {
        return selectedHexCreatures.removeIf(uuid -> getHexCreatureByUUID(uuid) == null);
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
        return listHexCreatures.size() < getMaxHexCreatures();
    }

    public boolean canSelectHexCreature(int wandMaxSelection) {
        return selectedHexCreatures.size() < wandMaxSelection;
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
                "targetPosition=" + targetPosition +
                ", selectedHexCreatures=" + selectedHexCreatures +
                '}';
    }
}
