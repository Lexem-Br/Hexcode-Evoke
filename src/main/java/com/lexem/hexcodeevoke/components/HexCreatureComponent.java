package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.common.UuidUtils;
import com.lexem.hexcodeevoke.utils.transport.TransportState;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class HexCreatureComponent implements Component<EntityStore>, HexCreatureName {
    private static final Random RANDOM = new Random();

    private static final int MAX_CHEST_MEMORY = 128;

    private String UUID;
    private String evokerUUID;
    private String name;
    private String nameKey;
    private String typeId;
    private String blockName;
    private boolean showName = false;
    private final Set<String> minionUUIDs = new LinkedHashSet<>();
    private final List<ChestMemoryComponent> listChestMemory = new ArrayList<>();
    private final List<ChestTaskComponent> listChestTask = new ArrayList<>();
    private boolean markedToDespawn = false;
    private String lastState;
    private String lastSubState;
    private TransportState transportState = new TransportState();
    private Vector3d targetPosition;
    private int targetGroupSize = 1;
    private float lifeEssence = -1;
    private HexCreatureRuntime runtime = new HexCreatureRuntime();

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
            .append(new KeyedCodec<>("NameKey", Codec.STRING), (component, value) -> component.nameKey = value, component -> component.nameKey).add()
            .append(new KeyedCodec<>("TypeId", Codec.STRING), (component, value) -> component.typeId = value, component -> component.typeId).add()
            .append(new KeyedCodec<>("BlockName", Codec.STRING), (component, value) -> component.blockName = value, component -> component.blockName).add()
            .append(new KeyedCodec<>("ShowName", Codec.BOOLEAN), (component, value) -> component.showName = value, component -> component.showName).add()
            .append(new KeyedCodec<>("MinionUUIDs", Codec.STRING_ARRAY), (component, value) -> replace(component.minionUUIDs, value), component -> component.minionUUIDs.toArray(new String[0])).add()
            .append(new KeyedCodec<>("ListChestMemories", new ArrayCodec<>(ChestMemoryComponent.CODEC, ChestMemoryComponent[]::new)), (component, objects) -> replace(component.listChestMemory, objects), component -> component.listChestMemory.toArray(new ChestMemoryComponent[0])).add()
            .append(new KeyedCodec<>("ListChestTasks", new ArrayCodec<>(ChestTaskComponent.CODEC, ChestTaskComponent[]::new)), (component, objects) -> replace(component.listChestTask, objects), component -> component.listChestTask.toArray(new ChestTaskComponent[0])).add()
            .append(new KeyedCodec<>("MarkedToDespawn", Codec.BOOLEAN), (component, value) -> component.markedToDespawn = value, component -> component.markedToDespawn).add()
            .append(new KeyedCodec<>("LastState", Codec.STRING), (component, value) -> component.lastState = value, component -> component.lastState).add()
            .append(new KeyedCodec<>("LastSubState", Codec.STRING), (component, value) -> component.lastSubState = value, component -> component.lastSubState).add()
            .append(new KeyedCodec<>("LifeEssence", Codec.FLOAT), (component, value) -> component.lifeEssence = value > 0 ? value : -1, component -> component.lifeEssence).add()
            .append(new KeyedCodec<>("StoredLifeEssence", Codec.FLOAT), (component, value) -> component.lifeEssence = value >= 0 ? value : -1, component -> component.lifeEssence).add()
            .append(new KeyedCodec<>("Transport", TransportState.CODEC), (component, value) -> component.transportState = value != null ? value : new TransportState(), component -> component.transportState.isActive() ? component.transportState : null).add()
            .append(new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC), (component, value) -> component.targetPosition = value, component -> component.targetPosition).add()
            .append(new KeyedCodec<>("TargetGroupSize", Codec.INTEGER), (component, value) -> component.targetGroupSize = value != null && value > 0 ? value : 1, component -> component.targetGroupSize).add()
            .build();

    public HexCreatureComponent() {
    }

    private static <T> void replace(@Nonnull Collection<T> target, @Nullable T[] values) {
        target.clear();
        if (values == null) return;

        for (T value : values) {
            if (value != null) target.add(value);
        }
    }

    @Nonnull
    public HexCreatureRuntime getRuntime() {
        return runtime;
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

    @Nullable
    public String getCustomName() {
        return this.name;
    }

    @Nullable
    public String getNameKey() {
        return this.nameKey;
    }

    public void setNameKey(@Nullable String nameKey) {
        this.nameKey = nameKey;
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

    @Nonnull
    public List<String> getMinionUUIDs() {
        return List.copyOf(minionUUIDs);
    }

    public int getMinionCount() {
        return minionUUIDs.size();
    }

    @Nonnull
    public List<ChestMemoryComponent> getListChestMemory() {
        return Collections.unmodifiableList(listChestMemory);
    }

    @Nonnull
    public List<ChestTaskComponent> getListChestTask() {
        return Collections.unmodifiableList(listChestTask);
    }

    public boolean isListChestTaskEmpty() {
        return listChestTask.isEmpty();
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
        if (uuid != null) minionUUIDs.add(uuid);
    }

    public void deleteUnusedMinionUUID(World world) {
        minionUUIDs.removeIf(uuid -> UuidUtils.getRef(world.getEntityStore(), uuid) == null);
    }

    public void removeMinionUUID(String uuid) {
        minionUUIDs.remove(uuid);
    }

    public void addChestTask(ChestTaskComponent chestTask) {
        if (chestTask != null) listChestTask.add(chestTask);
    }

    public void removeChestTask(ChestTaskComponent chestTask) {
        listChestTask.removeIf(task -> task.equals(chestTask));
    }

    public void forgetChest(Vector3d chestPosition) {
        if (chestPosition == null) return;

        listChestMemory.removeIf(memory -> isSameBlock(memory.getChestPosition(), chestPosition));
        listChestTask.removeIf(task -> isSameBlock(task.getChestPosition(), chestPosition));
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
        if (itemId == null || itemId.isEmpty()) return;

        listChestTask.removeIf(task -> itemId.equals(task.getItemId()));
    }

    public void addChestMemory(ChestMemoryComponent chestMemory) {
        if (chestMemory == null) return;

        while (listChestMemory.size() >= MAX_CHEST_MEMORY) {
            listChestMemory.removeFirst();
        }
        listChestMemory.add(chestMemory);
    }

    public void updateChestMemory(Vector3d chestPosition, String[] itemsId) {
        if (chestPosition == null) return;

        String[] safeItemsId = itemsId != null ? itemsId : new String[0];

        for (ChestMemoryComponent memory : listChestMemory) {
            Vector3d pos = memory.getChestPosition();
            if (pos != null && pos.equals(chestPosition)) {
                memory.setListItemsId(safeItemsId);
                return;
            }
        }

        addChestMemory(new ChestMemoryComponent(chestPosition, safeItemsId));
    }

    public ChestMemoryComponent findEmptyChestMemory() {
        return findEmptyChestMemory(List.of());
    }

    public ChestMemoryComponent findEmptyChestMemory(List<Vector3d> excludedPositions) {
        List<ChestMemoryComponent> validMemories = new ArrayList<>();

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory.getListItemsId() != null
                    && memory.getListItemsId().length == 0
                    && !isExcluded(memory.getChestPosition(), excludedPositions)) {
                validMemories.add(memory);
            }
        }

        if (validMemories.isEmpty()) return null;

        return validMemories.get(RANDOM.nextInt(validMemories.size()));
    }

    public ChestMemoryComponent findChestMemoryWithSpace() {
        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory.getListItemsId() != null && memory.getListItemsId().length > 0) {
                return memory;
            }
        }

        return null;
    }

    public ChestMemoryComponent findNearestChestMemory(Vector3d position) {
        return findNearestChestMemory(position, List.of());
    }

    public ChestMemoryComponent findNearestChestMemory(Vector3d position, List<Vector3d> excludedPositions) {
        if (position == null) return null;

        ChestMemoryComponent nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ChestMemoryComponent memory : listChestMemory) {
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

    public List<String> getRememberedItemIds() {
        Set<String> itemIds = new LinkedHashSet<>();

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory.getListItemsId() == null) continue;

            for (String itemId : memory.getListItemsId()) {
                if (itemId != null && !itemId.isEmpty()) itemIds.add(itemId);
            }
        }
        return new ArrayList<>(itemIds);
    }

    public List<ChestMemoryComponent> findChestMemoriesWithItem(String itemId, Vector3d position) {
        List<ChestMemoryComponent> memories = new ArrayList<>();
        if (itemId == null) return memories;

        for (ChestMemoryComponent memory : listChestMemory) {
            if (memory.getChestPosition() == null || memory.getListItemsId() == null) continue;
            if (Arrays.asList(memory.getListItemsId()).contains(itemId)) memories.add(memory);
        }

        if (position != null) {
            memories.sort((a, b) -> Double.compare(position.distanceSquared(a.getChestPosition()), position.distanceSquared(b.getChestPosition())));
        }
        return memories;
    }

    @Nonnull
    public TransportState getTransportState() {
        return transportState;
    }

    @Nullable
    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public int getTargetGroupSize() {
        return targetGroupSize;
    }

    public void setTarget(@Nonnull Vector3d targetPosition, int groupSize) {
        this.targetPosition = new Vector3d(targetPosition);
        this.targetGroupSize = Math.max(1, groupSize);
    }

    public void markToDespawn() {
        markedToDespawn = true;
    }

    public boolean hasLifeEssence() {
        return lifeEssence >= 0;
    }

    public float getLifeEssence() {
        return Math.max(0, lifeEssence);
    }

    public void setLifeEssence(float lifeEssence) {
        this.lifeEssence = Math.max(0, Math.round(lifeEssence * 1000f) / 1000f);
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        HexCreatureComponent clone = cloneSerializable();
        clone.runtime = this.runtime.copy();
        return clone;
    }

    @Nonnull
    @Override
    public HexCreatureComponent cloneSerializable() {
        ExtraInfo extraInfo = new ExtraInfo();
        return CODEC.decode(CODEC.encode(this, extraInfo), extraInfo);
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
                ", minionUUIDs=" + minionUUIDs +
                ", listChestMemory=" + listChestMemory +
                ", listChestTask=" + listChestTask +
                ", markedToDespawn=" + markedToDespawn +
                ", lastState='" + lastState + '\'' +
                ", lastSubState='" + lastSubState + '\'' +
                ", lifeEssence=" + lifeEssence +
                '}';
    }
}
