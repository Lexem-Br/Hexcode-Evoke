package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EvokerComponent implements Component<EntityStore> {
    private Vector3d targetPosition;
    private int maxHexCreatures = 6;
    private String[] selectedHexCreatures = new String[0];
    private HexCreatureComponent[] listHexCreatures = new HexCreatureComponent[0];

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
                    new KeyedCodec<>("MaxHexCreatures",  Codec.INTEGER),
                    (component, value) -> component.maxHexCreatures = value,
                    component -> component.maxHexCreatures
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
            .build();

    public EvokerComponent(){}

    public EvokerComponent(
            Vector3d targetPosition,
            int maxHexCreatures,
            String[] selectedHexCreatures
    ) {
        this.targetPosition = targetPosition;
        this.maxHexCreatures = maxHexCreatures;
        this.selectedHexCreatures = selectedHexCreatures;
    }

    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        this.targetPosition = newTargetPosition;
    }

    public int getMaxHexCreatures() { return maxHexCreatures; }

    public void setMaxHexCreatures(int maxHexCreatures) { this.maxHexCreatures = maxHexCreatures; }

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
            if (hc.getUUID().equals(npcUUID)) {
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
        int count = 0;
        for (HexCreatureComponent s : listHexCreatures) {
            if (!s.equals(hexCreature)) count++;
        }

        HexCreatureComponent[] newArray = new HexCreatureComponent[count];
        int index = 0;
        for (HexCreatureComponent s : listHexCreatures) {
            if (!s.equals(hexCreature)) {
                newArray[index++] = s;
            }
        }
        listHexCreatures = newArray;
    }

    //TODO: Fix this
//    public void deleteUnusedHexCreatureUUID(Store<EntityStore> store) {
//        for (String uuidString : getHexCreatureUUIDs()) {
//            UUID uuid = UUID.fromString(uuidString);
//            Ref<EntityStore> npcESRef = store.getExternalData().getRefFromUUID(uuid);
//            if (npcESRef == null) {
//                removeHexCreatureUUID(uuidString);
//            }
//        }
//    }

    public boolean canAddHexCreature() {
        return listHexCreatures.length < maxHexCreatures;
    }

    public boolean canSelectHexCreature(int wandMaxSelection) {
        return selectedHexCreatures.length < wandMaxSelection;
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new EvokerComponent(
                this.targetPosition,
                this.maxHexCreatures,
                this.selectedHexCreatures
        );
    }

    @Override
    public String toString() {
        return "EvokerComponent{" +
                "targetPosition=" + targetPosition +
                ", maxHexCreatures=" + maxHexCreatures +
                ", selectedHexCreatures=" + Arrays.toString(selectedHexCreatures) +
                '}';
    }
}
