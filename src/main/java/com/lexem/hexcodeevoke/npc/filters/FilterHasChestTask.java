package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasChestTask;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterHasChestTask extends EntityFilterBase {
    protected ChestTaskComponent.TaskType taskType;

    public FilterHasChestTask(@Nonnull BuilderFilterHasChestTask builder) {
        this.taskType = builder.getTaskType();
    }

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        if (this.taskType == null) return false;

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        for (ChestTaskComponent chestTask : hexCreatureComponent.getListChestTask()) {
            if (chestTask.getTaskType() == taskType) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int cost() {
        return 300;
    }
}