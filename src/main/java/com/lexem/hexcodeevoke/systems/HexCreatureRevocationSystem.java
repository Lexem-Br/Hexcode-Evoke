package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class HexCreatureRevocationSystem extends RefSystem<EntityStore> {
    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.BEFORE, RoleSystems.RoleActivateSystem.class)
    );

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        if (addReason != AddReason.LOAD) return;

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        assert hexCreature != null;

        if (!isRevoked(ref, store, hexCreature)) return;

        HexCreatureDespawner.despawn(store, ref, commandBuffer, true);
        commandBuffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
    }

    private static boolean isRevoked(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull HexCreatureComponent hexCreature
    ) {
        if (hexCreature.isMarkedToDespawn()) return true;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return false;

        return HexCreatureRegistry.get().isRevoked(uuidComponent.getUuid().toString());
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason removeReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return dependencies;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(NPCEntity.getComponentType(), HexCreatureComponent.getComponentType());
    }
}
