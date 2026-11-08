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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.MovementStatesSystem;
import com.hypixel.hytale.server.npc.systems.RoleSystems;
import com.hypixel.hytale.server.npc.systems.SteeringSystem;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.FairyFlightState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class HexCreatureFlightSystem extends EntityTickingSystem<EntityStore> {
    private final Set<Dependency<EntityStore>> dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, RoleSystems.BehaviourTickSystem.class),
            new SystemDependency<>(Order.AFTER, SteeringSystem.class),
            new SystemDependency<>(Order.AFTER, MovementStatesSystem.class),
            new SystemDependency<>(Order.AFTER, HexCreatureClimbStateSystem.class),
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
        HexCreatureComponent hexCreature = archetypeChunk.getComponent(index, HexCreatureComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        MovementStatesComponent movementStatesComponent = archetypeChunk.getComponent(index, MovementStatesComponent.getComponentType());
        if (hexCreature == null || transform == null || movementStatesComponent == null) return;

        FairyFlightState flight = hexCreature.getFlightState();
        if (!flight.isActive()) return;

        transform.getRotation().setYaw(flight.getYaw());
        transform.getRotation().setPitch(flight.getPitch());
        transform.getRotation().setRoll(flight.getRoll());

        boolean moving = flight.isMoving();
        boolean fast = flight.isFast();
        MovementStates movementStates = movementStatesComponent.getMovementStates();
        movementStates.flying = true;
        movementStates.climbing = false;
        movementStates.falling = false;
        movementStates.jumping = false;
        movementStates.swimming = false;
        movementStates.idle = !moving;
        movementStates.horizontalIdle = !moving;
        movementStates.walking = moving && !fast;
        movementStates.running = moving && fast;
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
                HexCreatureComponent.getComponentType(),
                TransformComponent.getComponentType(),
                MovementStatesComponent.getComponentType()
        );
    }
}
