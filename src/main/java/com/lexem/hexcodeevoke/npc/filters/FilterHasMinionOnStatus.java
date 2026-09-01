package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasMinionOnStatus;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterHasMinionOnStatus extends EntityFilterBase {
    protected HexCreatureMinionComponent.Status status;

    public FilterHasMinionOnStatus(@Nonnull BuilderFilterHasMinionOnStatus builder) {
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

        HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        String[] minionUUIDs = hexCreatureComponent.getMinionUUIDs();
        if (minionUUIDs == null) return false;

        for (String uuidString : minionUUIDs) {
            UUID uuid = UUID.fromString(uuidString);

            Ref<EntityStore> minionRef = store.getExternalData().getRefFromUUID(uuid);
            if (minionRef == null) continue;

            HexCreatureMinionComponent minionComponent = store.getComponent(minionRef, HexCreatureMinionComponent.getComponentType());
            if (minionComponent == null) continue;

            if (minionComponent.getStatus() == this.status) return true;
        }

        return false;
    }

    @Override
    public int cost() {
        return 300;
    }
}