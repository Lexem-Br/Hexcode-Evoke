package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class HexCreatureMinionComponent implements Component<EntityStore> {

    private String UUID;
    private String ownerUUID;
    private String typeId;

    private static ComponentType<EntityStore, HexCreatureMinionComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, HexCreatureMinionComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, HexCreatureMinionComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<HexCreatureMinionComponent> CODEC = BuilderCodec
            .builder(HexCreatureMinionComponent.class, HexCreatureMinionComponent::new)
            .append(
                    new KeyedCodec<>("UUID",  Codec.STRING),
                    (component, value) -> component.UUID = value,
                    component -> component.UUID
            ).add()
            .append(
                    new KeyedCodec<>("OwnerUUID",  Codec.STRING),
                    (component, value) -> component.ownerUUID = value,
                    component -> component.ownerUUID
            ).add()
            .append(
                    new KeyedCodec<>("TypeId",  Codec.STRING),
                    (component, value) -> component.typeId = value,
                    component -> component.typeId
            ).add()
            .build();

    public HexCreatureMinionComponent() {
    }

    public HexCreatureMinionComponent(
            String UUID,
            String ownerUUID,
            String typeId
    ) {
        this.UUID = UUID;
        this.ownerUUID = ownerUUID;
        this.typeId = typeId;
    }

    public String getUUID() {
        return this.UUID;
    }

    public void setUUID(String uuid) {
        this.UUID = uuid;
    }

    public String getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public String getTypeId() {
        return (this.typeId == null) ? "" : this.typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HexCreatureMinionComponent(
                this.UUID,
                this.ownerUUID,
                this.typeId
        );
    }
}
