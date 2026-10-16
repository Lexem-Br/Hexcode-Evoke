package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureTrackingSystem extends RefSystem<EntityStore> {

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (addReason != AddReason.LOAD) return;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        HexCreatureRegistry.get().removeLastKnownLocation(uuidComponent.getUuid().toString());
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason removeReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        String npcUUID = uuidComponent.getUuid().toString();
        HexCreatureRegistry registry = HexCreatureRegistry.get();

        if (removeReason == RemoveReason.UNLOAD) {
            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) return;

            String worldName = store.getExternalData().getWorld().getName();
            registry.setLastKnownLocation(npcUUID, worldName, transform.getPosition());
        } else {
            registry.forget(npcUUID);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return HexCreatureComponent.getComponentType();
    }
}
