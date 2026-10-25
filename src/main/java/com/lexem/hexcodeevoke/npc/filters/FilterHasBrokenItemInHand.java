package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasBrokenItemInHand;
import com.lexem.hexcodeevoke.utils.HeldItemUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterHasBrokenItemInHand extends EntityFilterBase {
    protected final List<String> items;

    public FilterHasBrokenItemInHand(@Nonnull BuilderFilterHasBrokenItemInHand builder, @Nonnull BuilderSupport support) {
        String[] itemArray = builder.getItems(support);
        this.items = itemArray != null ? List.of(itemArray) : List.of();
    }

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        return HeldItemUtils.findBrokenHeldItem(targetRef, store, items) != null;
    }

    @Override
    public int cost() {
        return 0;
    }
}
