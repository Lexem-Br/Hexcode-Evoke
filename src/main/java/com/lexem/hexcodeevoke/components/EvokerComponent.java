package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EvokerComponent implements Component<EntityStore> {
    private Vector3d targetPosition;
    private static final int BASE_MAX_HEX_CREATURES = 1;
    private String[] selectedHexCreatures = new String[0];
    private HexCreatureComponent[] listHexCreatures = new HexCreatureComponent[0];
    private String[] unlockedHexCreatures = new String[0];

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
                    new KeyedCodec<>("SelectedHexCreatures",  Codec.STRING_ARRAY),
                    (component, value) -> component.selectedHexCreatures = value,
                    component -> component.selectedHexCreatures
            ).add()
            .append(new KeyedCodec<>("ListHexCreatures", new ArrayCodec<>(HexCreatureComponent.CODEC, HexCreatureComponent[]::new)),
                    (component, objects) -> component.listHexCreatures = objects,
                    component -> component.listHexCreatures
            )
            .add()
            .append(
                    new KeyedCodec<>("UnlockedHexCreatures",  Codec.STRING_ARRAY),
                    (component, value) -> component.unlockedHexCreatures = value != null ? value : new String[0],
                    component -> component.unlockedHexCreatures
            ).add()
            .build();

    public EvokerComponent(){}

    public EvokerComponent(
            Vector3d targetPosition,
            String[] selectedHexCreatures
    ) {
        this.targetPosition = targetPosition;
        this.selectedHexCreatures = selectedHexCreatures;
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

    public HexCreatureComponent[] getListHexCreatures() {
        return listHexCreatures;
    }

    public String[] getSelectedHexCreatures() { return selectedHexCreatures; }

    public boolean hexCreatureBelongsToPlayer(String uuid) {
        for (String s : getHexCreatureUUIDs()) {
            if (s.equals(uuid)) return true;
        }
        return false;
    }

    public void addSelectedHexCreature(String uuid) {
        Set<String> uuidSet = new HashSet<>(Arrays.asList(selectedHexCreatures));
        if (uuidSet.contains(uuid)) { return; }

        String[] newArray = new String[selectedHexCreatures.length + 1];
        System.arraycopy(selectedHexCreatures, 0, newArray, 0, selectedHexCreatures.length);
        newArray[selectedHexCreatures.length] = uuid;
        selectedHexCreatures = newArray;
    }

    public void clearSelectedHexCreatures() {
        selectedHexCreatures = new String[0];
    }

    public void removeSelectedHexCreature(String uuid) {
        int count = 0;
        for (String s : selectedHexCreatures) {
            if (!s.equals(uuid)) count++;
        }

        String[] newArray = new String[count];
        int index = 0;
        for (String s : selectedHexCreatures) {
            if (!s.equals(uuid)) {
                newArray[index++] = s;
            }
        }
        selectedHexCreatures = newArray;
    }

    public HexCreatureComponent getHexCreatureByUUID(String npcUUID) {
        for (HexCreatureComponent hc : listHexCreatures) {
            if (npcUUID != null && npcUUID.equals(hc.getUUID())) {
                return hc;
            }
        }

        return null;
    }

    public String[] getHexCreatureUUIDs() {
        String[] uuids = new String[listHexCreatures.length];
        for (int i = 0; i < listHexCreatures.length; i++) {
            uuids[i] = listHexCreatures[i].getUUID();
        }
        return uuids;
    }

    public void addHexCreature(HexCreatureComponent hexCreature) {
        HexCreatureComponent[] newArray = new HexCreatureComponent[listHexCreatures.length + 1];
        System.arraycopy(listHexCreatures, 0, newArray, 0, listHexCreatures.length);
        newArray[listHexCreatures.length] = hexCreature;
        listHexCreatures = newArray;
    }

    public void removeHexCreature(HexCreatureComponent hexCreature) {
        if (hexCreature == null) return;
        removeHexCreatureByUUID(hexCreature.getUUID());
    }

    public void removeHexCreatureByUUID(String uuid) {
        if (uuid == null) return;

        removeSelectedHexCreature(uuid);

        int count = 0;
        for (HexCreatureComponent s : listHexCreatures) {
            if (!uuid.equals(s.getUUID())) count++;
        }

        HexCreatureComponent[] newArray = new HexCreatureComponent[count];
        int index = 0;
        for (HexCreatureComponent s : listHexCreatures) {
            if (!uuid.equals(s.getUUID())) {
                newArray[index++] = s;
            }
        }
        listHexCreatures = newArray;
    }

    public boolean pruneSelection() {
        Set<String> ownedUUIDs = new HashSet<>(Arrays.asList(getHexCreatureUUIDs()));

        int count = 0;
        for (String s : selectedHexCreatures) {
            if (ownedUUIDs.contains(s)) count++;
        }
        if (count == selectedHexCreatures.length) return false;

        String[] newArray = new String[count];
        int index = 0;
        for (String s : selectedHexCreatures) {
            if (ownedUUIDs.contains(s)) newArray[index++] = s;
        }
        selectedHexCreatures = newArray;
        return true;
    }

    public String[] getUnlockedHexCreatures() { return unlockedHexCreatures; }

    public boolean isHexCreatureUnlocked(String entityId) {
        for (String s : unlockedHexCreatures) {
            if (s.equals(entityId)) return true;
        }
        return false;
    }

    public boolean unlockHexCreature(String entityId) {
        if (entityId == null || entityId.isEmpty() || isHexCreatureUnlocked(entityId)) return false;

        String[] newArray = new String[unlockedHexCreatures.length + 1];
        System.arraycopy(unlockedHexCreatures, 0, newArray, 0, unlockedHexCreatures.length);
        newArray[unlockedHexCreatures.length] = entityId;
        unlockedHexCreatures = newArray;
        return true;
    }

    public boolean canAddHexCreature() {
        return listHexCreatures.length < getMaxHexCreatures();
    }

    public boolean canSelectHexCreature(int wandMaxSelection) {
        return selectedHexCreatures.length < wandMaxSelection;
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        EvokerComponent clone = new EvokerComponent(
                this.targetPosition,
                this.selectedHexCreatures.clone()
        );
        clone.listHexCreatures = this.listHexCreatures.clone();
        clone.unlockedHexCreatures = this.unlockedHexCreatures.clone();
        return clone;
    }

    @Override
    public String toString() {
        return "EvokerComponent{" +
                "targetPosition=" + targetPosition +
                ", selectedHexCreatures=" + Arrays.toString(selectedHexCreatures) +
                '}';
    }
}
