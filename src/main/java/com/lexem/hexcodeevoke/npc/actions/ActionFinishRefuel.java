package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionFinishRefuel;
import com.lexem.hexcodeevoke.utils.RefuelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionFinishRefuel extends ActionBase {
    protected final String defaultState;

    public ActionFinishRefuel(@Nonnull BuilderActionFinishRefuel builder) {
        super(builder);
        this.defaultState = builder.getDefaultState();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null) return false;

        RefuelState refuel = hexCreature.getRefuelState();
        String state = refuel.getReturnState();
        String subState = refuel.getReturnSubState();
        refuel.finish();

        StateSupport stateSupport = executionSupport.getStateSupport();
        StateMappingHelper stateHelper = stateSupport.getStateHelper();
        int stateIndex = state != null && stateHelper != null ? stateHelper.getStateIndex(state) : -1;
        if (stateIndex < 0 || RefuelState.REFUEL_STATE.equals(state)) {
            state = this.defaultState;
            subState = null;
        } else if (subState != null && stateHelper.getSubStateIndex(stateIndex, subState) < 0) {
            subState = null;
        }

        stateSupport.setState(npcRef, state, subState, store);
        return true;
    }
}
