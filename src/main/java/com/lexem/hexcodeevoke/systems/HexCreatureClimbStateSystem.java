package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.MovementStatesSystem;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class HexCreatureClimbStateSystem extends EntityTickingSystem<EntityStore> {
    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, MovementStatesSystem.class),
            new SystemGroupDependency<>(Order.BEFORE, EntityTrackerSystems.QUEUE_UPDATE_GROUP)
    );

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, MovementStatesComponent.getComponentType());
        NPCEntity npcComponent = archetypeChunk.getComponent(index, NPCEntity.getComponentType());
        if (movementStatesComponent == null || npcComponent == null) return;

        MovementStates movementStates = movementStatesComponent.getMovementStates();
        if (movementStates.climbing) return;

        Role role = npcComponent.getRole();
        if (role == null) return;

        if (!(role.getActiveMotionController() instanceof MotionControllerBase motionController)) return;

        if (motionController.getMotionKind() == MotionKind.ASCENDING && movementStatesComponent.getSentMovementStates().climbing) {
            movementStates.climbing = true;
        }
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return this.dependencies;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                NPCEntity.getComponentType(),
                MovementStatesComponent.getComponentType(),
                Query.or(HexCreatureComponent.getComponentType(), HexCreatureMinionComponent.getComponentType())
        );
    }
}
