package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.WandIndicatorUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

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
            boolean revokedByOwner = registry.isRevoked(npcUUID);
            registry.forget(npcUUID);
            if (!revokedByOwner) removeFromOwner(ref, store, npcUUID, registry);
        }
    }

    private static void removeFromOwner(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String npcUUID,
            @Nonnull HexCreatureRegistry registry
    ) {
        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return;

        Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreature.getEvokerUUID()));
        EvokerComponent evoker = ownerRef != null ? store.getComponent(ownerRef, EvokerComponent.getComponentType()) : null;

        if (evoker == null) {
            registry.markRemovedWhileOwnerAway(npcUUID);
            return;
        }

        if (!evoker.hexCreatureBelongsToPlayer(npcUUID)) return;

        evoker.removeHexCreatureByUUID(npcUUID);
        WandIndicatorUtils.refresh(ownerRef, store);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return HexCreatureComponent.getComponentType();
    }
}
