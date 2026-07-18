package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class NPCJoinSystem extends RefSystem<EntityStore> {
    private final HexCreatureUtils hexCreatureUtils = new HexCreatureUtils();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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

        Map.Entry<String, String> hexCreatures = HexItemRegistery.getByEntityId(npc.getNPCTypeId());
        if (hexCreatures == null) return;

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());

        if (hexCreature == null) {
            commandBuffer.addComponent(ref, HexCreatureComponent.getComponentType(), new HexCreatureComponent());
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

        Map.Entry<String, String> hexCreatures = HexItemRegistery.getByEntityId(npc.getNPCTypeId());
        if (hexCreatures == null) return;

        World world = commandBuffer.getExternalData().getWorld();
        hexCreatureUtils.deleteHexCreatureUUIDFromEvoker(ref, store, world);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return NPCEntity.getComponentType();
    }
}
