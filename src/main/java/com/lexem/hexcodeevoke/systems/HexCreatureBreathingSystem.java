package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreathingCheckEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureBreathingSystem extends EntityEventSystem<EntityStore, BreathingCheckEvent> {

    public HexCreatureBreathingSystem() {
        super(BreathingCheckEvent.class);
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreathingCheckEvent event
    ) {
        HexCreatureComponent hexCreature = archetypeChunk.getComponent(index, HexCreatureComponent.getComponentType());
        if (hexCreature != null && hexCreature.getCrawlState().isActive()) {
            event.setCanBreathe(true);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return HexCreatureComponent.getComponentType();
    }
}
