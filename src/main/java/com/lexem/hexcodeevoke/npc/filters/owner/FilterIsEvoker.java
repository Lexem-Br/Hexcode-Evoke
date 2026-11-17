package com.lexem.hexcodeevoke.npc.filters.owner;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUtils;

import javax.annotation.Nonnull;

public class FilterIsEvoker extends EntityFilterBase {

    public FilterIsEvoker() {}

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) { return false; }

        Ref<EntityStore> playerRef = HexCreatureUtils.getOwnerRef(store, hexCreature);
        if (playerRef == null) { return false; }

        return playerRef.getIndex() == targetRef.getIndex();
    }

    @Override
    public int cost() {
        return 300;
    }
}
