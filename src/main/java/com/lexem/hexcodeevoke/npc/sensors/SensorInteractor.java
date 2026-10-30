package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.corecomponents.SensorWithEntityFilters;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorInteractor;

import javax.annotation.Nonnull;

public class SensorInteractor extends SensorWithEntityFilters {

    public SensorInteractor(@Nonnull BuilderSensorInteractor builder, @Nonnull BuilderSupport support) {
        super(builder, builder.getFilters(support, null, ComponentContext.SensorTarget));
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) return false;

        Ref<EntityStore> interactor = executionSupport.getStateSupport().getInteractionIterationTarget();
        if (interactor == null || !interactor.isValid()) return false;

        return matchesFilters(ref, interactor, executionSupport, store);
    }

    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
