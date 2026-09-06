package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ChestTaskComponent implements Component<EntityStore> {
    public static final Codec<ChestTaskComponent.TaskType> TASKTYPE = new EnumCodec<>(ChestTaskComponent.TaskType.class);

    private TaskType taskType;
    private Vector3d chestPosition = new Vector3d();
    private String itemId;
    private int itemQuantity;
    private boolean isFinished = true;

    private static ComponentType<EntityStore, ChestTaskComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, ChestTaskComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, ChestTaskComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<ChestTaskComponent> CODEC = BuilderCodec
        .builder(ChestTaskComponent.class, ChestTaskComponent::new)
        .append(
                new KeyedCodec<>("TaskType", TASKTYPE),
                (component, value) -> component.taskType = value,
                component -> component.taskType
        ).add()
        .append(
            new KeyedCodec<>("ChestPosition", Vector3dUtil.CODEC),
            (component, v) -> component.chestPosition.set(v),
            component -> component.chestPosition)
        .add()
        .append(
                new KeyedCodec<>("ItemId", Codec.STRING),
                (component, value) -> component.itemId = value,
                component -> component.itemId
        ).add()
        .append(
                new KeyedCodec<>("ItemQuantity", Codec.INTEGER),
                (component, value) -> component.itemQuantity = value,
                component -> component.itemQuantity
        ).add()
        .append(
                new KeyedCodec<>("IsFinished", Codec.BOOLEAN),
                (component, value) -> component.isFinished = value,
                component -> component.isFinished
        ).add()
        .build();

    public ChestTaskComponent() {
    }

    public ChestTaskComponent(
            TaskType taskType,
            Vector3d chestPosition,
            String itemId,
            int itemQuantity,
            boolean isFinished
    ) {
        this.taskType = taskType;
        this.chestPosition = chestPosition;
        this.itemId = itemId;
        this.itemQuantity = itemQuantity;
        this.isFinished = isFinished;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Vector3d getChestPosition() {
        return chestPosition;
    }

    public void setChestPosition(Vector3d chestPosition) {
        this.chestPosition = chestPosition;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public enum TaskType implements Supplier<String> {
        Search("Search"),
        Store("Store");

        private final String value;

        TaskType(String value) {
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
        return new ChestTaskComponent(
                this.taskType,
                new Vector3d(this.chestPosition),
                this.itemId,
                this.itemQuantity,
                this.isFinished
        );
    }

    @Override
    public String toString() {
        return "ChestTaskComponent{" +
                "taskType=" + taskType +
                ", chestPosition=" + chestPosition +
                ", itemId='" + itemId + '\'' +
                ", itemQuantity=" + itemQuantity +
                ", isFinished=" + isFinished +
                '}';
    }
}
