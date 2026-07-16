package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class HexCreatureComponent implements Component<EntityStore> {

    private String UUID;
    private String evokerUUID;
    private String evokerName;
    private String name;
    private String typeId;
    private String blockName;

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
            .build();

    public HexCreatureComponent() {
    }

    public HexCreatureComponent(String UUID, String evokerUUID, String evokerName, String name, String typeId, String blockName) {
        this.UUID = UUID;
        this.evokerUUID = evokerUUID;
        this.evokerName = evokerName;
        this.name = name;
        this.typeId = typeId;
        this.blockName = blockName;
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

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HexCreatureComponent(this.UUID, this.evokerUUID, this.evokerName, this.name, this.typeId, this.blockName);
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
                '}';
    }
}
