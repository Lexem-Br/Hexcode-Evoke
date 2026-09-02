package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class HexCreatureMinionComponent implements Component<EntityStore> {
    public static final Codec<Status> STATUS = new EnumCodec<>(Status.class);

    private String UUID;
    private String ownerUUID;
    private String typeId;
    private Status status;

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
            .append(
                    new KeyedCodec<>("Status", STATUS),
                    (component, value) -> component.status = value,
                    component -> component.status
            ).add()
            .build();

    public HexCreatureMinionComponent() {
    }

    public HexCreatureMinionComponent(
            String UUID,
            String ownerUUID,
            String typeId,
            Status status
    ) {
        this.UUID = UUID;
        this.ownerUUID = ownerUUID;
        this.typeId = typeId;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status implements Supplier<String>  {
        Standby("Standby"),
        Storing("Storing");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String get()  {
            return this.value;
        }
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HexCreatureMinionComponent(
                this.UUID,
                this.ownerUUID,
                this.typeId,
                this.status
        );
    }
}
