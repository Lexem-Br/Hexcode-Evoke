package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EvokerComponent implements Component<EntityStore>{
    private Vector3d targetPosition;
    private String[] hexCreatureUUIDs = new String[0];
    private int maxHexCreatures = 6;
    private String[] selectedHexCreatures = new String[0];

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
                    new KeyedCodec<>("HexCreatureUUIDs",  Codec.STRING_ARRAY),
                    (component, value) -> component.hexCreatureUUIDs = value,
                    component -> component.hexCreatureUUIDs
            ).add()
            .append(
                    new KeyedCodec<>("MaxHexCreatures",  Codec.INTEGER),
                    (component, value) -> component.maxHexCreatures = value,
                    component -> component.maxHexCreatures
            ).add()
            .append(
                    new KeyedCodec<>("SelectedHexCreatures",  Codec.STRING_ARRAY),
                    (component, value) -> component.selectedHexCreatures = value,
                    component -> component.selectedHexCreatures
            ).add()
            .build();

    public EvokerComponent(){}

    public EvokerComponent(Vector3d targetPosition, String[] hexCreatureUUIDs, int maxHexCreatures, String[] selectedHexCreatures) {
        this.targetPosition = targetPosition;
        this.hexCreatureUUIDs = hexCreatureUUIDs;
        this.maxHexCreatures = maxHexCreatures;
        this.selectedHexCreatures = selectedHexCreatures;
    }

    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public String[] getHexCreatureUUIDs() {
        return hexCreatureUUIDs;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        this.targetPosition = newTargetPosition;
    }

    public int getMaxHexCreatures() { return maxHexCreatures; }

    public void setMaxHexCreatures(int maxHexCreatures) { this.maxHexCreatures = maxHexCreatures; }

    public String[] getSelectedHexCreatures() { return selectedHexCreatures; }

    public void addHexCreatureUUID(String uuid) {
        String[] newArray = new String[hexCreatureUUIDs.length + 1];
        System.arraycopy(hexCreatureUUIDs, 0, newArray, 0, hexCreatureUUIDs.length);
        newArray[hexCreatureUUIDs.length] = uuid;
        hexCreatureUUIDs = newArray;
    }

    public void removeHexCreatureUUID(String uuid) {
        int count = 0;
        for (String s : hexCreatureUUIDs) {
            if (!s.equals(uuid)) count++;
        }

        String[] newArray = new String[count];
        int index = 0;
        for (String s : hexCreatureUUIDs) {
            if (!s.equals(uuid)) {
                newArray[index++] = s;
            }
        }
        hexCreatureUUIDs = newArray;
    }

    public boolean hexCreatureBelongsToPlayer(String uuid) {
        for (String s : hexCreatureUUIDs) {
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

    public void deleteUnusedHexCreatureUUID(World world, String[] hexCreatureUUIDs) {
        for (String uuidString : hexCreatureUUIDs) {
            UUID uuid = UUID.fromString(uuidString);
            Ref<EntityStore> npcESRef = world.getEntityStore().getRefFromUUID(uuid);
            if (npcESRef == null) {
                removeHexCreatureUUID(uuidString);
            }
        }
    }

    public boolean canAddHexCreature() {
        return hexCreatureUUIDs.length < maxHexCreatures;
    }

    public void selectAllHexCreatures() {
        this.selectedHexCreatures = this.hexCreatureUUIDs.clone();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new EvokerComponent(this.targetPosition, this.hexCreatureUUIDs, this.maxHexCreatures, this.selectedHexCreatures);
    }

    @Override
    public String toString() {
        return "EvokerComponent{" +
                "targetPosition=" + targetPosition +
                ", hexCreatureUUIDs=" + Arrays.toString(hexCreatureUUIDs) +
                ", maxHexCreatures=" + maxHexCreatures +
                ", selectedHexCreatures=" + Arrays.toString(selectedHexCreatures) +
                '}';
    }
}
