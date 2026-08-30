package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.Arrays;

public class HexCreatureComponent implements Component<EntityStore> {

    private String UUID;
    private String evokerUUID;
    private String evokerName;
    private String name;
    private String typeId;
    private String blockName;
    private boolean showName = false;
    private String[] minionUUIDs = new String[0];

    private static ComponentType<EntityStore, HexCreatureComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, HexCreatureComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, HexCreatureComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<HexCreatureComponent> CODEC = BuilderCodec
            .builder(HexCreatureComponent.class, HexCreatureComponent::new)
            .append(
                    new KeyedCodec<>("UUID",  Codec.STRING),
                    (component, value) -> component.UUID = value,
                    component -> component.UUID
            ).add()
            .append(
                    new KeyedCodec<>("EvokerUUID",  Codec.STRING),
                    (component, value) -> component.evokerUUID = value,
                    component -> component.evokerUUID
            ).add()
            .append(
                    new KeyedCodec<>("EvokerName",  Codec.STRING),
                    (component, value) -> component.evokerName = value,
                    component -> component.evokerName
            ).add()
            .append(
                    new KeyedCodec<>("Name",  Codec.STRING),
                    (component, value) -> component.name = value,
                    component -> component.name
            ).add()
            .append(
                    new KeyedCodec<>("TypeId",  Codec.STRING),
                    (component, value) -> component.typeId = value,
                    component -> component.typeId
            ).add()
            .append(
                    new KeyedCodec<>("BlockName",  Codec.STRING),
                    (component, value) -> component.blockName = value,
                    component -> component.blockName
            ).add()
            .append(
                    new KeyedCodec<>("ShowName",  Codec.BOOLEAN),
                    (component, value) -> component.showName = value,
                    component -> component.showName
            ).add()
            .append(
                    new KeyedCodec<>("MinionUUIDs",  Codec.STRING_ARRAY),
                    (component, value) -> component.minionUUIDs = value,
                    component -> component.minionUUIDs
            ).add()
            .build();

    public HexCreatureComponent() {
    }

    public HexCreatureComponent(
            String UUID,
            String evokerUUID,
            String evokerName,
            String name,
            String typeId,
            String blockName,
            boolean showName,
            String[] minionUUIDs
    ) {
        this.UUID = UUID;
        this.evokerUUID = evokerUUID;
        this.evokerName = evokerName;
        this.name = name;
        this.typeId = typeId;
        this.blockName = blockName;
        this.showName = showName;
        this.minionUUIDs = minionUUIDs;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String uuid) {
        this.UUID = uuid;
    }

    public String getEvokerUUID() {
        return this.evokerUUID;
    }

    public void setEvokerUUID(String evokerUUID) {
        this.evokerUUID = evokerUUID;
    }

    public String getEvokerName() {
        return (this.evokerName == null) ? "" : this.evokerName;
    }

    public void setEvokerName(String evokerName) {
        this.evokerName = evokerName;
    }

    public String getName() {
        return (this.name == null) ? getTypeId() : this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeId() {
        return (this.typeId == null) ? "" : this.typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public boolean getShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public String[] getMinionUUIDs() {
        return minionUUIDs;
    }

    public void setMinionUUIDs(String[] minionUUIDs) {
        this.minionUUIDs = minionUUIDs;
    }

    public void addMinionUUID(String uuid) {
        String[] newArray = new String[minionUUIDs.length + 1];
        System.arraycopy(minionUUIDs, 0, newArray, 0, minionUUIDs.length);
        newArray[minionUUIDs.length] = uuid;
        minionUUIDs = newArray;
    }

    public void deleteUnusedMinionUUID(World world) {
        for (String uuidString : minionUUIDs) {
            java.util.UUID uuid = java.util.UUID.fromString(uuidString);
            Ref<EntityStore> npcESRef = world.getEntityStore().getRefFromUUID(uuid);
            if (npcESRef == null) {
                removeMinionUUID(uuidString);
            }
        }
    }

    public void removeMinionUUID(String uuid) {
        int count = 0;
        for (String s : minionUUIDs) {
            if (!s.equals(uuid)) count++;
        }

        String[] newArray = new String[count];
        int index = 0;
        for (String s : minionUUIDs) {
            if (!s.equals(uuid)) {
                newArray[index++] = s;
            }
        }
        minionUUIDs = newArray;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HexCreatureComponent(
                this.UUID,
                this.evokerUUID,
                this.evokerName,
                this.name,
                this.typeId,
                this.blockName,
                this.showName,
                this.minionUUIDs
        );
    }

    @Override
    public String toString() {
        return "HexCreatureComponent{" +
                "UUID='" + UUID + '\'' +
                ", evokerUUID='" + evokerUUID + '\'' +
                ", evokerName='" + evokerName + '\'' +
                ", name='" + name + '\'' +
                ", typeId='" + typeId + '\'' +
                ", blockName='" + blockName + '\'' +
                ", showName=" + showName +
                ", minionUUIDs=" + Arrays.toString(minionUUIDs) +
                '}';
    }
}
