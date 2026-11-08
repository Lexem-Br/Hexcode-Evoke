package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSnapToBlockCenter;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSnapToBlockCenter extends ActionBase {
    private static final double QUARTER_TURN = Math.PI / 2.0;

    protected final boolean snapRotation;

    public ActionSnapToBlockCenter(@Nonnull BuilderActionSnapToBlockCenter builder) {
        super(builder);
        this.snapRotation = builder.getSnapRotation();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (transformComponent == null) return false;

        Vector3d position = transformComponent.getPosition();
        position.set(Math.floor(position.x) + 0.5, position.y, Math.floor(position.z) + 0.5);

        if (this.snapRotation) {
            Rotation3f rotation = transformComponent.getRotation();
            rotation.setYaw((float) (Math.round(rotation.yaw() / QUARTER_TURN) * QUARTER_TURN));
        }
        return true;
    }
}
