package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3d;

import java.util.UUID;

public class EvokerComponent implements Component<EntityStore>{
    private Vector3d targetPosition;
    private String[] hexCreatureUUIDs = new String[0];
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static ComponentType<EntityStore, EvokerComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, EvokerComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, EvokerComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<EvokerComponent> CODEC = BuilderCodec
            .builder(EvokerComponent.class, EvokerComponent::new)
            .append(
                    new KeyedCodec<>("TargetPosition", Vector3dUtil.CODEC),
                    (component, value) -> component.targetPosition = value,
                    component -> component.targetPosition
            ).add()
            .append(
                    new KeyedCodec<>("HexCreatureUUIDs",  Codec.STRING_ARRAY),
                    (component, value) -> component.hexCreatureUUIDs = value,
                    component -> component.hexCreatureUUIDs
            ).add()
            .build();

    public EvokerComponent(){
    }

    public EvokerComponent(Vector3d targetPosition){
        this.targetPosition = targetPosition;
    }

    public Vector3d getTargetPosition() {
        return targetPosition;
    }

    public String[] getHexCreatureUUIDs() {
        return hexCreatureUUIDs;
    }

    public void setTargetPosition(Vector3d newTargetPosition) {
        this.targetPosition = newTargetPosition;
    }

    public void addHexCreatureUUID(String uuid) {
        String[] newArray = new String[hexCreatureUUIDs.length + 1];
        System.arraycopy(hexCreatureUUIDs, 0, newArray, 0, hexCreatureUUIDs.length);
        newArray[hexCreatureUUIDs.length] = uuid;
        hexCreatureUUIDs = newArray;
    }

    public void removeHexCreatureUUID(String uuid) {
        int count = 0;
        for (String s : hexCreatureUUIDs) {
            if (!s.equals(uuid)) count++;
        }

        String[] newArray = new String[count];
        int index = 0;
        for (String s : hexCreatureUUIDs) {
            if (!s.equals(uuid)) {
                newArray[index++] = s;
            }
        }
        hexCreatureUUIDs = newArray;
    }

    public void deleteUnusedHexCreatureUUID(World world, String[] hexCreatureUUIDs) {
        for (String uuidString : hexCreatureUUIDs) {
            UUID uuid = UUID.fromString(uuidString);
            LOGGER.atInfo().log("uuid: %s", uuid);
            Ref<EntityStore> npcESRef = world.getEntityStore().getRefFromUUID(uuid);
            if (npcESRef == null) {
                LOGGER.atInfo().log("npcESRef invalid, uuidString: %s", uuidString);
                removeHexCreatureUUID(uuidString);
            }
        }
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new EvokerComponent(this.targetPosition);
    }

    @Override
    public String toString() {
        return "EvokerComponent{targetPosition=" + getTargetPosition() + "}";
    }
}
