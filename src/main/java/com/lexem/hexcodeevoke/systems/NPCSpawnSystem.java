package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexCreatureMinionsAsset;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import com.lexem.hexcodeevoke.utils.HexCreatureDespawner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class NPCSpawnSystem extends RefSystem<EntityStore> {

    public NPCSpawnSystem() {
    }

    @Override
    public void onEntityAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull AddReason addReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        NPCEntity npc = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        AllowedHexCreatureMinionsAsset.HexMinion hexMinions = AllowedHexCreatureMinionsAsset.getByEntityId(npc.getNPCTypeId());
        if (hexMinions != null) {
            HexCreatureMinionComponent hexMinion = store.getComponent(ref, HexCreatureMinionComponent.getComponentType());

            if (hexMinion == null) {
                commandBuffer.addComponent(ref, HexCreatureMinionComponent.getComponentType(), new HexCreatureMinionComponent());
            }
        }

        AllowedHexItemsAsset.HexItem hexCreatures = AllowedHexItemsAsset.getByEntityId(npc.getNPCTypeId());
        if (hexCreatures != null) {
            HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());

            if (hexCreature == null) {
                commandBuffer.addComponent(ref, HexCreatureComponent.getComponentType(), new HexCreatureComponent());
            } else if (addReason == AddReason.LOAD && isRevoked(ref, store, hexCreature)) {
                HexCreatureDespawner.despawn(store, ref, commandBuffer, true);
                commandBuffer.tryRemoveEntity(ref, RemoveReason.REMOVE);
            }
        }
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
    public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason removeReason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
