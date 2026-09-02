package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterIsOnStatus;

import javax.annotation.Nonnull;

public class FilterIsOnStatus extends EntityFilterBase {
    protected HexCreatureMinionComponent.Status status;

    public FilterIsOnStatus(@Nonnull BuilderFilterIsOnStatus builder) {
        this.status = builder.getStatus();
    }

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        if (this.status == null) return false;

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        return minionComponent.getStatus() == status;
    }

    @Override
    public int cost() {
        return 300;
    }
}