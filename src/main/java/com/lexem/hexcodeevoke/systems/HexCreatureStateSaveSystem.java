package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.RefuelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HexCreatureStateSaveSystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        HexCreatureComponent hexCreature = archetypeChunk.getComponent(index, HexCreatureComponent.getComponentType());
        StateSupport stateSupport = archetypeChunk.getComponent(index, StateSupport.getComponentType());
        if (hexCreature == null || stateSupport == null) return;

        StateMappingHelper stateHelper = stateSupport.getStateHelper();
        if (stateHelper == null) return;

        int state = stateSupport.getStateIndex();
        String stateName = stateHelper.getStateName(state);
        String subStateName = stateHelper.getSubStateName(state, stateSupport.getSubStateIndex());

        RefuelState refuel = hexCreature.getRefuelState();
        if (RefuelState.REFUEL_STATE.equals(stateName) && refuel.getReturnState() != null) {
            stateName = refuel.getReturnState();
            subStateName = refuel.getReturnSubState();
        }

        if (!hexCreature.setLastState(stateName, subStateName)) return;

        Dirty dirty = archetypeChunk.getComponent(index, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(HexCreatureComponent.getComponentType(), StateSupport.getComponentType());
    }
}
