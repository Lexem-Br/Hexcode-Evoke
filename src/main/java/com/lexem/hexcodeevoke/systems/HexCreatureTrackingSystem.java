package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.common.UuidUtils;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureEntrySync;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUtils;
import com.lexem.hexcodeevoke.utils.wand.WandIndicatorUtils;

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

        String npcUUID = uuidComponent.getUuid().toString();
        HexCreatureRegistry.get().removeLastKnownLocation(npcUUID);
        applyOwnerEntry(ref, store, npcUUID);
    }

    private static void applyOwnerEntry(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String npcUUID) {
        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        Ref<EntityStore> ownerRef = hexCreature != null ? HexCreatureUtils.getOwnerRef(store, hexCreature) : null;
        if (ownerRef == null) return;

        EvokerComponent evoker = store.getComponent(ownerRef, EvokerComponent.getComponentType());
        HexCreatureEntry entry = evoker != null ? evoker.getHexCreatureByUUID(npcUUID) : null;
        if (entry == null) return;

        PlayerRef owner = store.getComponent(ownerRef, PlayerRef.getComponentType());
        HexCreatureEntrySync.applyToLoaded(entry, ref, store, owner != null ? owner.getLanguage() : null);
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
        UUID ownerUUID = hexCreature != null ? UuidUtils.parse(hexCreature.getEvokerUUID()) : null;
        if (ownerUUID == null) return;

        if (store.getExternalData().getRefFromUUID(ownerUUID) != null) {
            removeFromOwnerInWorld(store, ownerUUID, npcUUID, registry);
            return;
        }

        PlayerRef owner = Universe.get().getPlayer(ownerUUID);
        World ownerWorld = owner != null && owner.getWorldUuid() != null ? Universe.get().getWorld(owner.getWorldUuid()) : null;
        if (ownerWorld == null) {
            registry.markRemovedWhileOwnerAway(npcUUID);
            return;
        }

        ownerWorld.execute(() -> removeFromOwnerInWorld(ownerWorld.getEntityStore().getStore(), ownerUUID, npcUUID, registry));
    }

    private static void removeFromOwnerInWorld(
            @Nonnull Store<EntityStore> store,
            @Nonnull UUID ownerUUID,
            @Nonnull String npcUUID,
            @Nonnull HexCreatureRegistry registry
    ) {
        Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(ownerUUID);
        EvokerComponent evoker = ownerRef != null && ownerRef.isValid() ? store.getComponent(ownerRef, EvokerComponent.getComponentType()) : null;

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
