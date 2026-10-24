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
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.systems.MovementStatesSystem;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.BlockUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class HexCreatureClimbStateSystem extends EntityTickingSystem<EntityStore> {
    private static final double STEP_PROBE_DISTANCE = 0.6;
    private static final double STEP_PROBE_LIFT = 0.05;
    private static final int SMALL_STEP_HEIGHT = 1;

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
        HexCreatureComponent hexCreature = archetypeChunk.getComponent(index, HexCreatureComponent.getComponentType());
        boolean jumpSmallSteps = hexCreature != null && AllowedHexItemsAsset.jumpsSmallStepsByEntityId(hexCreature.getTypeId());
        if (movementStates.climbing && !jumpSmallSteps) return;

        Role role = npcComponent.getRole();
        if (role == null) return;

        if (!(role.getActiveMotionController() instanceof MotionControllerBase motionController)) return;

        boolean ascending = motionController.getMotionKind() == MotionKind.ASCENDING;
        if (jumpSmallSteps) {
            if (!ascending) {
                hexCreature.setAscentSmallStep(null);
            } else {
                Boolean smallStep = hexCreature.getAscentSmallStep();
                if (smallStep == null) {
                    TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                    smallStep = transform == null || isSmallStep(transform, store.getExternalData().getWorld());
                    hexCreature.setAscentSmallStep(smallStep);
                }
                if (smallStep) {
                    movementStates.climbing = false;
                    movementStates.jumping = true;
                    return;
                }
            }
            if (movementStates.climbing) return;
        }

        if (ascending && movementStatesComponent.getSentMovementStates().climbing) {
            movementStates.climbing = true;
        }
    }

    private static boolean isSmallStep(@Nonnull TransformComponent transform, @Nonnull World world) {
        Vector3d position = transform.getPosition();
        float yaw = transform.getRotation().yaw();
        int x = (int) Math.floor(position.x + PhysicsMath.headingX(yaw) * STEP_PROBE_DISTANCE);
        int z = (int) Math.floor(position.z + PhysicsMath.headingZ(yaw) * STEP_PROBE_DISTANCE);
        int y = (int) Math.floor(position.y + STEP_PROBE_LIFT);

        int height = 0;
        while (height <= SMALL_STEP_HEIGHT && isSolid(world, x, y + height, z)) {
            height++;
        }
        return height <= SMALL_STEP_HEIGHT;
    }

    private static boolean isSolid(@Nonnull World world, int x, int y, int z) {
        BlockType blockType = BlockUtils.getBlockTypeByPosition(new Vector3i(x, y, z), world);
        return blockType != null && blockType.getMaterial() == BlockMaterial.Solid;
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
