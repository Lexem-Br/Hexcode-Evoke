package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class HexCreatureStateRestoreSystem extends RefSystem<EntityStore> {
    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, RoleSystems.RoleActivateSystem.class)
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
        if (hexCreature == null || hexCreature.getLastState() == null) return;

        StateSupport stateSupport = store.getComponent(ref, StateSupport.getComponentType());
        if (stateSupport == null) return;

        StateMappingHelper stateHelper = stateSupport.getStateHelper();
        if (stateHelper == null) return;

        int state = stateHelper.getStateIndex(hexCreature.getLastState());
        if (state < 0) return;

        int subState = hexCreature.getLastSubState() == null ? -1 : stateHelper.getSubStateIndex(state, hexCreature.getLastSubState());
        if (subState < 0) subState = stateHelper.getSubStateIndex(state, stateHelper.getDefaultSubState());
        if (subState < 0) return;

        stateSupport.setState(state, subState, true, true);
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
        return Query.and(HexCreatureComponent.getComponentType(), StateSupport.getComponentType());
    }
}
