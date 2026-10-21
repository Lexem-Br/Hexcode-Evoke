package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class HexCreatureComponent implements Component<EntityStore> {
    private static final Random RANDOM = new Random();
    public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int MAX_CHEST_MEMORY = 128;

    private String UUID;
    private String evokerUUID;
    private String name;
    private String typeId;
    private String blockName;
    private boolean showName = false;
    private String[] minionUUIDs = new String[0];
    private ChestMemoryComponent[] listChestMemory = new ChestMemoryComponent[0];
    private ChestTaskComponent[] listChestTask = new ChestTaskComponent[0];
    private boolean markedToDespawn = false;
    private String lastState;
    private String lastSubState;
    // Not saved: chests a minion couldn't reach, skipped by new tasks until the time runs out
    private Map<Long, Long> unreachableChests = new HashMap<>();

    private static ComponentType<EntityStore, HexCreatureComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, HexCreatureComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, HexCreatureComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<HexCreatureComponent> CODEC = BuilderCodec
            .builder(HexCreatureComponent.class, HexCreatureComponent::new)
            .append(new KeyedCodec<>("UUID", Codec.STRING), (component, value) -> component.UUID = value, component -> component.UUID).add()
            .append(new KeyedCodec<>("EvokerUUID", Codec.STRING), (component, value) -> component.evokerUUID = value, component -> component.evokerUUID).add()
            .append(new KeyedCodec<>("Name", Codec.STRING), (component, value) -> component.name = value, component -> component.name).add()
            .append(new KeyedCodec<>("TypeId", Codec.STRING), (component, value) -> component.typeId = value, component -> component.typeId).add()
            .append(new KeyedCodec<>("BlockName", Codec.STRING), (component, value) -> component.blockName = value, component -> component.blockName).add()
            .append(new KeyedCodec<>("ShowName", Codec.BOOLEAN), (component, value) -> component.showName = value, component -> component.showName).add()
            .append(new KeyedCodec<>("MinionUUIDs", Codec.STRING_ARRAY), (component, value) -> component.minionUUIDs = value, component -> component.minionUUIDs).add()
            .append(new KeyedCodec<>("ListChestMemories", new ArrayCodec<>(ChestMemoryComponent.CODEC, ChestMemoryComponent[]::new)), (component, objects) -> component.listChestMemory = objects, component -> component.listChestMemory).add()
            .append(new KeyedCodec<>("ListChestTasks", new ArrayCodec<>(ChestTaskComponent.CODEC, ChestTaskComponent[]::new)), (component, objects) -> component.listChestTask = objects, component -> component.listChestTask).add()
            .append(new KeyedCodec<>("MarkedToDespawn", Codec.BOOLEAN), (component, value) -> component.markedToDespawn = value, component -> component.markedToDespawn).add()
            .append(new KeyedCodec<>("LastState", Codec.STRING), (component, value) -> component.lastState = value, component -> component.lastState).add()
            .append(new KeyedCodec<>("LastSubState", Codec.STRING), (component, value) -> component.lastSubState = value, component -> component.lastSubState).add()
            .build();

    public HexCreatureComponent() {
    }

    public HexCreatureComponent(
            String UUID,
            String evokerUUID,
            String name,
            String typeId,
            String blockName,
            boolean showName,
            String[] minionUUIDs,
            boolean markedToDespawn
    ) {
        this.UUID = UUID;
        this.evokerUUID = evokerUUID;
        this.name = name;
        this.typeId = typeId;
        this.blockName = blockName;
        this.showName = showName;
        this.minionUUIDs = minionUUIDs;
        this.markedToDespawn = markedToDespawn;
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

    public ChestTaskComponent[] getListChestTask() {
        return listChestTask;
    }

    public boolean isListChestTaskEmpty() {
        return listChestTask == null || listChestTask.length == 0;
    }

    public boolean isMarkedToDespawn() {
        return markedToDespawn;
    }

    public String getLastState() {
        return lastState;
    }

    public String getLastSubState() {
        return lastSubState;
    }

    public boolean setLastState(String state, String subState) {
        if (Objects.equals(lastState, state) && Objects.equals(lastSubState, subState)) return false;

        lastState = state;
        lastSubState = subState;
        return true;
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

    public void forgetChest(Vector3d chestPosition) {
        if (chestPosition == null) return;

        if (listChestMemory != null) {
            List<ChestMemoryComponent> remainingMemories = new ArrayList<>();
            for (ChestMemoryComponent memory : listChestMemory) {
                if (memory != null && !isSameBlock(memory.getChestPosition(), chestPosition)) {
                    remainingMemories.add(memory);
                }
            }
            listChestMemory = remainingMemories.toArray(new ChestMemoryComponent[0]);
        }

        if (listChestTask != null) {
            List<ChestTaskComponent> remainingTasks = new ArrayList<>();
            for (ChestTaskComponent task : listChestTask) {
                if (task != null && !isSameBlock(task.getChestPosition(), chestPosition)) {
                    remainingTasks.add(task);
                }
            }
            listChestTask = remainingTasks.toArray(new ChestTaskComponent[0]);
        }
    }

    public void markChestUnreachable(Vector3d chestPosition, long durationMillis) {
        if (chestPosition == null) return;
        unreachableChests.put(blockKey(chestPosition), System.currentTimeMillis() + durationMillis);
    }

    public boolean isChestUnreachable(Vector3d chestPosition) {
        if (chestPosition == null || unreachableChests.isEmpty()) return false;

        long now = System.currentTimeMillis();
        unreachableChests.values().removeIf(expiresAt -> expiresAt <= now);
        return unreachableChests.containsKey(blockKey(chestPosition));
    }

    public List<Vector3d> withoutUnreachableChests(List<Vector3d> chestPositions) {
        if (unreachableChests.isEmpty()) return chestPositions;

        List<Vector3d> reachable = new ArrayList<>();
        for (Vector3d chestPosition : chestPositions) {
            if (!isChestUnreachable(chestPosition)) reachable.add(chestPosition);
        }
        return reachable;
    }

    private static long blockKey(Vector3d position) {
        long x = (long) Math.floor(position.x) & 0x3FFFFFFL;
        long y = (long) Math.floor(position.y) & 0xFFFL;
        long z = (long) Math.floor(position.z) & 0x3FFFFFFL;
        return (x << 38) | (y << 26) | z;
    }

    private static boolean isExcluded(Vector3d position, List<Vector3d> excludedPositions) {
        for (Vector3d excluded : excludedPositions) {
            if (isSameBlock(position, excluded)) return true;
        }
        return false;
    }

    private static boolean isSameBlock(Vector3d a, Vector3d b) {
        if (a == null || b == null) return false;

        return Math.floor(a.x) == Math.floor(b.x)
                && Math.floor(a.y) == Math.floor(b.y)
                && Math.floor(a.z) == Math.floor(b.z);
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
        if (chestMemory == null) {
            return;
        }

        if (listChestMemory == null) {
            listChestMemory = new ChestMemoryComponent[0];
        }

        if (listChestMemory.length < MAX_CHEST_MEMORY) {
            ChestMemoryComponent[] newArray = new ChestMemoryComponent[listChestMemory.length + 1];
            System.arraycopy(listChestMemory, 0, newArray, 0, listChestMemory.length);
            newArray[listChestMemory.length] = chestMemory;
            listChestMemory = newArray;
        } else {
            ChestMemoryComponent[] newArray = new ChestMemoryComponent[MAX_CHEST_MEMORY];
            System.arraycopy(listChestMemory, 1, newArray, 0, MAX_CHEST_MEMORY - 1);
            newArray[MAX_CHEST_MEMORY - 1] = chestMemory;
            listChestMemory = newArray;
        }
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
        return findEmptyChestMemory(List.of());
    }

    public ChestMemoryComponent findEmptyChestMemory(List<Vector3d> excludedPositions) {
        if (listChestMemory == null || listChestMemory.length == 0) {
            return null;
        }

        List<ChestMemoryComponent> validMemories = new ArrayList<>();

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory != null
                    && memory.getListItemsId() != null
                    && memory.getListItemsId().length == 0
                    && !isExcluded(memory.getChestPosition(), excludedPositions)) {
                validMemories.add(memory);
            }
        }

        if (validMemories.isEmpty()) {
            return null;
        }

        return validMemories.get(RANDOM.nextInt(validMemories.size()));
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
        return findNearestChestMemory(position, List.of());
    }

    public ChestMemoryComponent findNearestChestMemory(Vector3d position, List<Vector3d> excludedPositions) {
        if (listChestMemory == null || listChestMemory.length == 0 || position == null) {
            return null;
        }

        ChestMemoryComponent nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory == null) continue;

            Vector3d chestPos = memory.getChestPosition();
            if (chestPos == null || isExcluded(chestPos, excludedPositions)) continue;

            double distance = position.distance(chestPos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = memory;
            }
        }

        return nearest;
    }

    public void markToDespawn() {
        markedToDespawn = true;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        HexCreatureComponent clone = new HexCreatureComponent(
                this.UUID,
                this.evokerUUID,
                this.name,
                this.typeId,
                this.blockName,
                this.showName,
                this.minionUUIDs,
                this.markedToDespawn
        );
        clone.listChestMemory = this.listChestMemory;
        clone.listChestTask = this.listChestTask;
        clone.lastState = this.lastState;
        clone.lastSubState = this.lastSubState;
        clone.unreachableChests = this.unreachableChests;
        return clone;
    }

    @Override
    public String toString() {
        return "HexCreatureComponent{" +
                "UUID='" + UUID + '\'' +
                ", evokerUUID='" + evokerUUID + '\'' +
                ", name='" + name + '\'' +
                ", typeId='" + typeId + '\'' +
                ", blockName='" + blockName + '\'' +
                ", showName=" + showName +
                ", minionUUIDs=" + Arrays.toString(minionUUIDs) +
                ", listChestMemory=" + Arrays.toString(listChestMemory) +
                ", listChestTask=" + Arrays.toString(listChestTask) +
                ", markedToDespawn=" + markedToDespawn +
                ", lastState='" + lastState + '\'' +
                ", lastSubState='" + lastSubState + '\'' +
                '}';
    }
}
