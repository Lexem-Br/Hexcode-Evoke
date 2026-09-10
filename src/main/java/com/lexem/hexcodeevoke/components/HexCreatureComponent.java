package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexCreatureComponent implements Component<EntityStore> {

    private String UUID;
    private String evokerUUID;
    private String evokerName;
    private String name;
    private String typeId;
    private String blockName;
    private boolean showName = false;
    private String[] minionUUIDs = new String[0];
    private ChestMemoryComponent[] listChestMemory = new ChestMemoryComponent[0];
    private ChestTaskComponent[] listChestTask = new ChestTaskComponent[0];

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
                    new KeyedCodec<>("UUID", Codec.STRING),
                    (component, value) -> component.UUID = value,
                    component -> component.UUID
            ).add()
            .append(
                    new KeyedCodec<>("EvokerUUID", Codec.STRING),
                    (component, value) -> component.evokerUUID = value,
                    component -> component.evokerUUID
            ).add()
            .append(
                    new KeyedCodec<>("EvokerName", Codec.STRING),
                    (component, value) -> component.evokerName = value,
                    component -> component.evokerName
            ).add()
            .append(
                    new KeyedCodec<>("Name", Codec.STRING),
                    (component, value) -> component.name = value,
                    component -> component.name
            ).add()
            .append(
                    new KeyedCodec<>("TypeId", Codec.STRING),
                    (component, value) -> component.typeId = value,
                    component -> component.typeId
            ).add()
            .append(
                    new KeyedCodec<>("BlockName", Codec.STRING),
                    (component, value) -> component.blockName = value,
                    component -> component.blockName
            ).add()
            .append(
                    new KeyedCodec<>("ShowName", Codec.BOOLEAN),
                    (component, value) -> component.showName = value,
                    component -> component.showName
            ).add()
            .append(
                    new KeyedCodec<>("MinionUUIDs", Codec.STRING_ARRAY),
                    (component, value) -> component.minionUUIDs = value,
                    component -> component.minionUUIDs
            ).add()
            .append(new KeyedCodec<>("ListChestMemory", new ArrayCodec<>(ChestMemoryComponent.CODEC, ChestMemoryComponent[]::new)),
                    (component, objects) -> component.listChestMemory = objects,
                    component -> component.listChestMemory
            )
            .add()
            .append(new KeyedCodec<>("ListChestTask", new ArrayCodec<>(ChestTaskComponent.CODEC, ChestTaskComponent[]::new)),
                    (component, objects) -> component.listChestTask = objects,
                    component -> component.listChestTask
            )
            .add()
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

    public ChestMemoryComponent[] getListChestMemory() {
        return listChestMemory;
    }

    public void setListChestMemory(ChestMemoryComponent[] listChestMemory) {
        this.listChestMemory = listChestMemory;
    }

    public ChestTaskComponent[] getListChestTask() {
        return listChestTask;
    }

    public void setListChestTask(ChestTaskComponent[] listChestTask) {
        this.listChestTask = listChestTask;
    }

    public boolean isListChestTaskEmpty() {
        return listChestTask == null || listChestTask.length == 0;
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

    public void addChestTask(ChestTaskComponent chestTask) {
        ChestTaskComponent[] newArray = new ChestTaskComponent[listChestTask.length + 1];
        System.arraycopy(listChestTask, 0, newArray, 0, listChestTask.length);
        newArray[listChestTask.length] = chestTask;
        listChestTask = newArray;
    }

    public void removeChestTask(ChestTaskComponent chestTask) {
        int count = 0;
        for (ChestTaskComponent s : listChestTask) {
            if (!s.equals(chestTask)) count++;
        }

        ChestTaskComponent[] newArray = new ChestTaskComponent[count];
        int index = 0;
        for (ChestTaskComponent s : listChestTask) {
            if (!s.equals(chestTask)) {
                newArray[index++] = s;
            }
        }
        listChestTask = newArray;
    }

    public void removeChestTasksByItemId(String itemId) {
        if (itemId == null || itemId.isEmpty() || listChestTask == null || listChestTask.length == 0) {
            return;
        }

        List<ChestTaskComponent> remainingTasks = new ArrayList<>();
        for (ChestTaskComponent task : listChestTask) {
            if (task != null && !itemId.equals(task.getItemId())) {
                remainingTasks.add(task);
            }
        }

        listChestTask = remainingTasks.toArray(new ChestTaskComponent[0]);
    }

    public void addChestMemory(ChestMemoryComponent chestMemory) {
        ChestMemoryComponent[] newArray = new ChestMemoryComponent[listChestMemory.length + 1];
        System.arraycopy(listChestMemory, 0, newArray, 0, listChestMemory.length);
        newArray[listChestMemory.length] = chestMemory;
        listChestMemory = newArray;
    }

    public void updateChestMemory(Vector3d chestPosition, String[] itemsId) {
        if (chestPosition == null || listChestMemory == null) {
            return;
        }

        String[] safeItemsId = itemsId != null ? itemsId : new String[0];

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory == null) continue;

            Vector3d pos = memory.getChestPosition();
            if (pos != null && pos.equals(chestPosition)) {
                memory.setListItemsId(safeItemsId);
                return;
            }
        }

        ChestMemoryComponent newMemory = new ChestMemoryComponent(chestPosition, safeItemsId);
        addChestMemory(newMemory);
    }

    public ChestMemoryComponent findEmptyChestMemory() {
        if (listChestMemory == null) return null;

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory != null && memory.getListItemsId() != null && memory.getListItemsId().length == 0) {
                return memory;
            }
        }

        return null;
    }

    public ChestMemoryComponent findChestMemoryWithSpace() {
        if (listChestMemory == null) return null;

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory != null && memory.getListItemsId() != null && memory.getListItemsId().length > 0) {
                return memory;
            }
        }

        return null;
    }

    public ChestMemoryComponent findNearestChestMemory(Vector3d position) {
        if (listChestMemory == null || listChestMemory.length == 0 || position == null) {
            return null;
        }

        ChestMemoryComponent nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory == null) continue;

            Vector3d chestPos = memory.getChestPosition();
            if (chestPos == null) continue;

            double distance = position.distance(chestPos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = memory;
            }
        }

        return nearest;
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
