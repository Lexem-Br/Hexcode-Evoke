package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionStartRefuel;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionStartRefuel extends ActionBase {
    private final Vector3d trayPosition = new Vector3d();

    public ActionStartRefuel(@Nonnull BuilderActionStartRefuel builder) {
        super(builder);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        IPositionProvider positionProvider = sensorInfo != null ? sensorInfo.getPositionProvider() : null;
        if (hexCreature == null || transform == null || positionProvider == null || !positionProvider.providePosition(this.trayPosition)) return false;

        StateSupport stateSupport = executionSupport.getStateSupport();
        StateMappingHelper stateHelper = stateSupport.getStateHelper();
        String state = stateHelper != null ? stateHelper.getStateName(stateSupport.getStateIndex()) : null;
        String subState = stateHelper != null ? stateHelper.getSubStateName(stateSupport.getStateIndex(), stateSupport.getSubStateIndex()) : null;

        Vector3i tray = new Vector3i((int) Math.floor(this.trayPosition.x), (int) Math.floor(this.trayPosition.y), (int) Math.floor(this.trayPosition.z));
        hexCreature.getRefuelState().start(tray, transform.getPosition(), state, subState);
        return true;
    }
}
