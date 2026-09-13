package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
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
import com.hypixel.hytale.server.npc.util.AimingHelper;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.List;

public class DropItemsUtils {
    public DropItemsUtils() {
    }

    public static void dropAllInventoryItems(Store<EntityStore> store, Ref<EntityStore> npcRef, ComponentAccessor<EntityStore> commandBuffer) {
        CombinedItemContainer everythingInventoryComponent = InventoryComponent.getCombined(store, npcRef, InventoryComponent.EVERYTHING);
        for (short i = 0; i < everythingInventoryComponent.getCapacity(); i++) {
            ItemStack itemStack = everythingInventoryComponent.getItemStack(i);
            if (itemStack != null) {
                double distance = RandomExtra.randomRange(0.2, 0.5);
                Vector3d direction = newDirection(npcRef, distance, store);
                if (direction != null) {
                    ItemUtils.throwItem(npcRef, commandBuffer, itemStack, direction, 100);
                }
            }
        }
    }

    public static void spawnDeathParticleEffect(@Nonnull Ref<EntityStore> ref, double distance, Store<EntityStore> store) {
        Vector3d direction = newDirection(ref, distance, store);
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

    public static Vector3d newDirection(@Nonnull Ref<EntityStore> ref, double distance, Store<EntityStore> store) {
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
