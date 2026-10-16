package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.resources.HexCreatureRegistry;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class PlayerJoinSystem extends RefSystem<EntityStore> {

    @Override
    public void onEntityAdded(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl AddReason addReason,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
        if (addReason != AddReason.LOAD) return;

        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());

        if (evoker == null) {
            commandBuffer.addComponent(ref, EvokerComponent.getComponentType(), new EvokerComponent());
        } else {
            migrateMarkedToDespawn(evoker);
        }
    }

    private static void migrateMarkedToDespawn(EvokerComponent evoker) {
        boolean migrated = false;
        for (HexCreatureComponent hexCreature : evoker.getListHexCreatures()) {
            if (!hexCreature.isMarkedToDespawn() || hexCreature.getUUID() == null) continue;

            HexCreatureRegistry.get().revoke(hexCreature.getUUID());
            evoker.removeHexCreatureByUUID(hexCreature.getUUID());
            evoker.removeSelectedHexCreature(hexCreature.getUUID());
            migrated = true;
        }

        if (migrated) HexCreatureRegistry.flush();
    }

    @Override
    public void onEntityRemove(
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl RemoveReason removeReason,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(PlayerRef.getComponentType());
    }
}
