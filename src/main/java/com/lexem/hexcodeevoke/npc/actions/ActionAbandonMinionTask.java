package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionAbandonMinionTask;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionAbandonMinionTask extends ActionBase {
    private static final int TELEPORT_ANGLES = 8;

    protected final double unreachableSeconds;
    protected final double teleportDistance;
    protected final double teleportRadius;
    protected final boolean forgetChest;

    public ActionAbandonMinionTask(@Nonnull BuilderActionAbandonMinionTask builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.unreachableSeconds = builder.getUnreachableSeconds(support);
        this.teleportDistance = builder.getTeleportDistance(support);
        this.teleportRadius = builder.getTeleportRadius(support);
        this.forgetChest = builder.getForgetChest();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        ChestTaskComponent chestTask = minionComponent.getChestTask();
        if (chestTask != null) chestTask.setFinished(true);

        Ref<EntityStore> motherRef = store.getExternalData().getRefFromUUID(UUID.fromString(minionComponent.getOwnerUUID()));
        if (motherRef == null || !motherRef.isValid()) return true;

        HexCreatureComponent mother = store.getComponent(motherRef, HexCreatureComponent.getComponentType());
        if (mother != null && chestTask != null && this.forgetChest) {
            mother.removeChestTask(chestTask);
            mother.forgetChest(chestTask.getChestPosition());
            mother.markChestUnreachable(chestTask.getChestPosition(), (long) (this.unreachableSeconds * 1000));
        }

        this.teleportNearMother(npcRef, motherRef, executionSupport, store);
        return true;
    }

    private void teleportNearMother(@Nonnull Ref<EntityStore> npcRef, @Nonnull Ref<EntityStore> motherRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
        TransformComponent motherTransform = store.getComponent(motherRef, TransformComponent.getComponentType());
        if (transformComponent == null || motherTransform == null) return;

        Vector3d position = transformComponent.getPosition();
        Vector3d motherPosition = motherTransform.getPosition();
        if (position.distanceSquared(motherPosition) <= this.teleportDistance * this.teleportDistance) return;

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        Vector3d candidate = new Vector3d();
        for (int i = 0; i < TELEPORT_ANGLES; i++) {
            double angle = Math.PI * 2 * i / TELEPORT_ANGLES;
            candidate.set(motherPosition.x + Math.cos(angle) * this.teleportRadius, motherPosition.y, motherPosition.z + Math.sin(angle) * this.teleportRadius);
            if (motionController == null || motionController.isValidPosition(candidate, store)) {
                position.set(candidate);
                return;
            }
        }

        position.set(motherPosition);
    }
}
