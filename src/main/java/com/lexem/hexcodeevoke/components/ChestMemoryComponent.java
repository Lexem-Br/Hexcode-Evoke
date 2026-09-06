package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nullable;

public class ChestMemoryComponent implements Component<EntityStore> {
    private Vector3d chestPosition = new Vector3d();
    private String[] listItemsId = new String[0];

    private static ComponentType<EntityStore, ChestMemoryComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, ChestMemoryComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, ChestMemoryComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<ChestMemoryComponent> CODEC = BuilderCodec
        .builder(ChestMemoryComponent.class, ChestMemoryComponent::new)
        .append(
            new KeyedCodec<>("ChestPosition", Vector3dUtil.CODEC),
            (component, v) -> component.chestPosition.set(v),
            component -> component.chestPosition)
        .add()
        .append(
                new KeyedCodec<>("ListItemsId",  Codec.STRING_ARRAY),
                (component, value) -> component.listItemsId = value,
                component -> component.listItemsId)
        .add()
        .build();

    public ChestMemoryComponent() {
    }

    public ChestMemoryComponent(
            Vector3d chestPosition,
            String[] listItemsId
    ) {
        this.chestPosition = chestPosition;
        this.listItemsId = listItemsId;
    }

    public Vector3d getChestPosition() {
        return chestPosition;
    }

    public void setChestPosition(Vector3d chestPosition) {
        this.chestPosition = chestPosition;
    }

    public String[] getListItemsId() {
        return listItemsId;
    }

    public void setListItemsId(String[] listItemsId) {
        this.listItemsId = listItemsId;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new ChestMemoryComponent(
                new Vector3d(this.chestPosition),
                this.listItemsId
        );
    }

}
