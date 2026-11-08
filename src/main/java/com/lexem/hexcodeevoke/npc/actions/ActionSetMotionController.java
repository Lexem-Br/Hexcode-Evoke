package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSetMotionController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSetMotionController extends ActionBase {
    protected final String motionController;

    public ActionSetMotionController(@Nonnull BuilderActionSetMotionController builder) {
        super(builder);
        this.motionController = builder.getMotionController();
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        Role role = executionSupport.getRole();
        if (role == null || !role.hasMotionController(motionController)) return false;

        NPCEntity npcComponent = store.getComponent(npcRef, NPCEntity.getComponentType());
        if (npcComponent == null) return false;

        return role.setActiveMotionController(npcRef, npcComponent, motionController, store);
    }
}
