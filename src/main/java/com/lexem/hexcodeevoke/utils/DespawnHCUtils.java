package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.AimingHelper;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DespawnHCUtils {
    private final Ref<EntityStore> npcRef;
    private final Store<EntityStore> store;
    private final ComponentAccessor<EntityStore> commandBuffer;
    private final boolean dropHexItem;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public DespawnHCUtils(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull ComponentAccessor<EntityStore> commandBuffer, boolean dropHexItem) {
        this.store = store;
        this.npcRef = npcRef;
        this.commandBuffer = commandBuffer;
        this.dropHexItem = dropHexItem;
    }

    public DespawnHCUtils(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> npcRef, @Nonnull ComponentAccessor<EntityStore> commandBuffer) {
        this.store = store;
        this.npcRef = npcRef;
        this.commandBuffer = commandBuffer;
        this.dropHexItem = false;
    }

    public void despawnHexCreature() {
        NPCEntity npcComponent = store.getComponent(npcRef, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcComponent == null) return;

        dropAllInventoryItems();
        dropHCItem(npcComponent);

        spawnDeathParticleEffect(npcRef, 0);
        deleteHexCreatureUUIDFromEvoker();
        npcComponent.setToDespawn();
    }

    private void dropAllInventoryItems() {
        CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(store, npcRef, InventoryComponent.EVERYTHING);
        for (short i = 0; i < everythingInventoryComponent.getCapacity(); i++) {
            ItemStack itemStack = everythingInventoryComponent.getItemStack(i);
            if (itemStack != null) {
                double distance = RandomExtra.randomRange(0.2, 0.5);
                Vector3d direction = this.newDirection(npcRef, distance);
                if (direction != null) {
                    ItemUtils.throwItem(npcRef, commandBuffer, itemStack, direction, 100);
                }
            }
        }
    }

    private void dropHCItem(NPCEntity npcComponent) {
        HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null || hexCreatureComponent.getEvokerUUID() == null) { return; }

        String blockId = hexCreatureComponent.getBlockName();
        ItemStack hexDropItem = InventoryHelper.createItem(blockId);
        if (hexDropItem != null) {
            Role npcRole = npcComponent.getRole();

            if (dropHexItem || npcRole == null || Objects.equals(npcRole.getDropListId(), "Empty")) {
                UUID uuid = UUID.fromString(hexCreatureComponent.getEvokerUUID());
                Ref<EntityStore> refESPlayer = store.getExternalData().getRefFromUUID(uuid);

                if (refESPlayer != null) {
                    Vector3d direction = throwItem(refESPlayer, hexDropItem,  1);
                    if (direction == null) {
                        double distance = RandomExtra.randomRange(0.2, 0.4);
                        throwItem(npcRef, hexDropItem,  distance);
                    }
                } else {
                    double distance = RandomExtra.randomRange(0.2, 0.4);
                    throwItem(npcRef, hexDropItem,  distance);
                }
            }
        }
    }

    private Vector3d throwItem(Ref<EntityStore> ref, ItemStack hexDropItem, double distance) {
        Vector3d direction = this.newDirection(ref, distance);
        if (direction != null) {
            ItemUtils.throwItem(ref, commandBuffer, hexDropItem, direction, 100);
            spawnDeathParticleEffect(ref, 3);
        }
        return direction;
    }

    public void deleteHexCreatureUUIDFromEvoker() {
        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null || hexCreature.getUUID() == null) return;

        UUID playerUUID = UUID.fromString(hexCreature.getEvokerUUID());
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(playerUUID);
        if (playerRef == null) return;

        EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
        if (evoker == null) return;

        evoker.removeHexCreatureUUID(hexCreature.getUUID());
        evoker.removeSelectedHexCreature(hexCreature.getUUID());
    }

    public void spawnDeathParticleEffect(@Nonnull Ref<EntityStore> ref, double distance) {
        Vector3d direction = this.newDirection(ref, distance);
        if (direction == null) { return; }

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent != null) {
            float eyeHeight = 0.0F;
            ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
            if (modelComponent != null) {
                eyeHeight = modelComponent.getModel().getEyeHeight(ref, store);
            }

            Vector3d particlePos = new Vector3d(transformComponent.getPosition());
            particlePos.add(0.0F, eyeHeight, 0.0F).add(direction);

            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(
                    EntityModule.get().getPlayerSpatialResourceType()
            );
            List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(particlePos, 75.0, results);
            ParticleUtil.spawnParticleEffect("Effect_Death", particlePos, results, store);
        }
    }

    public Vector3d newDirection(@Nonnull Ref<EntityStore> ref, double distance) {
        Vector3d dropDirection;
        double[] dropSector =  new double[]{0.0, 0.0};
        float dropSectorStart = (float) (Math.PI / 180.0) * (float)dropSector[0];
        float dropSectorEnd = (float) (Math.PI / 180.0) * (float)dropSector[1];
        double throwSpeed = 100;
        float[] pitch = new float[2];

        ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
        float eyeHeight = modelComponent != null ? modelComponent.getModel().getEyeHeight(ref, store) : 0.0F;
        float height = -eyeHeight;

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) { return null; }

        HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
        Vector3d direction;
        if (headRotationComponent != null) {
            direction = headRotationComponent.getDirection();
        } else {
            Rotation3f rotation = transformComponent.getRotation();
            direction = Vector3dUtil.setYawPitch(rotation.yaw(), rotation.pitch(), new Vector3d());
        }

        dropDirection = direction;
        dropDirection.rotateY(RandomExtra.randomRange(dropSectorStart, dropSectorEnd));

        if (!AimingHelper.computePitch(distance, height, throwSpeed, 32.0, pitch)) {
            throw new IllegalStateException(
                    String.format("Error in computing pitch with distance %s, height %s, and speed %s that was not caught in validation", distance, height, throwSpeed)
            );
        } else {
            float heading = PhysicsMath.headingFromDirection(dropDirection.x, dropDirection.z);
            PhysicsMath.vectorFromAngles(heading, pitch[0], dropDirection).normalize();
        }

        return dropDirection;
    }
}
