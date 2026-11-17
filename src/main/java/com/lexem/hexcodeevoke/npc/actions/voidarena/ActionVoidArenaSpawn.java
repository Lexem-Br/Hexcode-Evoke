package com.lexem.hexcodeevoke.npc.actions.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsConstants;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionSpawn;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.npc.actions.voidarena.builders.BuilderActionVoidArenaSpawn;
import com.lexem.hexcodeevoke.utils.voidarena.VoidArenaManager;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class ActionVoidArenaSpawn extends ActionSpawn {
    private final boolean emerge;
    private final double[] spawnOffsets;
    private final String spawnParticles;
    private final double[] scatterDistanceRange;
    private final double scatterHeight;
    private int nextOffset;
    private int nextScatter;
    private double scatterAngle;

    public ActionVoidArenaSpawn(@Nonnull BuilderActionVoidArenaSpawn builder, @Nonnull BuilderSupport support) {
        super(builder, support);
        this.emerge = builder.isEmerge(support);
        this.spawnOffsets = builder.getSpawnOffsets(support);
        this.spawnParticles = builder.getSpawnParticles(support);
        this.scatterDistanceRange = builder.getScatterDistanceRange(support);
        this.scatterHeight = builder.getScatterHeight(support);
    }

    @Override
    protected void postSpawn(@Nonnull NPCEntity npcComponent, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        placeOnSpawner(ref, store);
        playSpawnParticles(ref, store);
        super.postSpawn(npcComponent, ref, store);
        scatter(npcComponent, ref, store);
        if (emerge) VoidArenaManager.beginEmerge(ref, store);
    }

    private void scatter(@Nonnull NPCEntity npcComponent, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (scatterDistanceRange[1] <= 0 || parent == null || !parent.isValid()) return;
        TransformComponent spawner = store.getComponent(parent, TransformComponent.getComponentType());
        TransformComponent spawned = store.getComponent(ref, TransformComponent.getComponentType());
        if (spawner == null || spawned == null) return;

        if (nextScatter++ % 2 == 0) scatterAngle = RandomExtra.randomRange(0.0, Math.PI * 2.0);
        else scatterAngle += Math.PI;

        double distance = RandomExtra.randomRange(scatterDistanceRange[0], scatterDistanceRange[1]);
        double drop = Math.max(0.0, spawned.getPosition().y - spawner.getPosition().y);
        double gravity = PhysicsConstants.GRAVITY_ACCELERATION;
        double upSpeed = Math.sqrt(2.0 * gravity * scatterHeight);
        double flightTime = upSpeed / gravity + Math.sqrt(2.0 * (scatterHeight + drop) / gravity);
        double sideSpeed = distance / flightTime;

        npcComponent.getRole().setVelocity(new Vector3d(Math.cos(scatterAngle) * sideSpeed, upSpeed, Math.sin(scatterAngle) * sideSpeed), null, true);
    }

    private void playSpawnParticles(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (spawnParticles == null) return;
        TransformComponent spawned = store.getComponent(ref, TransformComponent.getComponentType());
        if (spawned == null) return;
        ParticleUtil.spawnParticleEffect(spawnParticles, new Vector3d(spawned.getPosition()), store);
    }

    private void placeOnSpawner(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        int points = spawnOffsets.length / 3;
        if (points == 0 || parent == null || !parent.isValid()) return;

        TransformComponent spawner = store.getComponent(parent, TransformComponent.getComponentType());
        TransformComponent spawned = store.getComponent(ref, TransformComponent.getComponentType());
        if (spawner == null || spawned == null) return;

        int index = (nextOffset++ % points) * 3;
        double right = spawnOffsets[index];
        double up = spawnOffsets[index + 1];
        double forward = spawnOffsets[index + 2];

        float yaw = spawner.getRotation().yaw();
        double forwardX = PhysicsMath.headingX(yaw);
        double forwardZ = PhysicsMath.headingZ(yaw);
        Vector3d origin = spawner.getPosition();
        spawned.setPosition(new Vector3d(
                origin.x - forwardZ * right + forwardX * forward,
                origin.y + up,
                origin.z + forwardX * right + forwardZ * forward));
    }
}
