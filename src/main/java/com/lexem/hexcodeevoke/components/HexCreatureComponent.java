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
            .build();

    public HexCreatureComponent() {
    }

    public HexCreatureComponent(String UUID, String evokerUUID, String evokerName) {
        this.UUID = UUID;
        this.evokerUUID = evokerUUID;
        this.evokerName = evokerName;
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
        return this.evokerName;
    }

    public void setEvokerName(String evokerName) {
        this.evokerName = evokerName;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HexCreatureComponent(this.UUID, this.evokerUUID, this.evokerName);
    }

    @Override
    public String toString() {
        return "HexCreatureComponent{" +
                "UUID='" + UUID + '\'' +
                ", evokerUUID='" + evokerUUID + '\'' +
                ", evokerName='" + evokerName + '\'' +
                '}';
    }
}
