package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterMinionHasActiveTask;

import javax.annotation.Nonnull;

public class FilterMinionHasActiveTask extends EntityFilterBase {
    protected boolean reverse;

    public FilterMinionHasActiveTask(@Nonnull BuilderFilterMinionHasActiveTask builder) {
        this.reverse = builder.getReverse();
    }

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        if (reverse) {
            return minionComponent.getChestTask().isFinished();
        } else {
            return !minionComponent.getChestTask().isFinished();
        }
    }

    @Override
    public int cost() {
        return 300;
    }
}