package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexCreatureMinionsAsset;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.DespawnHCUtils;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class NPCJoinSystem extends RefSystem<EntityStore> {

    public NPCJoinSystem() {
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
            }
        }
    }

    @Override
    public void onEntityRemove(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull RemoveReason removeReason,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        NPCEntity npc = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null) return;

        AllowedHexItemsAsset.HexItem hexCreatures = AllowedHexItemsAsset.getByEntityId(npc.getNPCTypeId());
        if (hexCreatures == null) return;

        HexCreatureComponent hexCreatureComponent = store.getComponent(ref, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null || hexCreatureComponent.getEvokerUUID() == null) { return; }

        UUID playerUUID = UUID.fromString(hexCreatureComponent.getEvokerUUID());
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(playerUUID);
        if (playerRef == null) return;

        EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
        if (evoker == null || !evoker.hexCreatureBelongsToPlayer(hexCreatureComponent.getUUID())) return;

        DespawnHCUtils despawnHCUtils = new DespawnHCUtils(store, ref, commandBuffer);
        despawnHCUtils.despawnHexCreature();
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
