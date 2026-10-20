package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasWateringCan;
import com.lexem.hexcodeevoke.utils.HeldItemUtils;
import com.lexem.hexcodeevoke.utils.WateringCanUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterHasWateringCan extends EntityFilterBase {
    private final boolean filled;

    public FilterHasWateringCan(@Nonnull BuilderFilterHasWateringCan builder, @Nonnull BuilderSupport support) {
        this.filled = builder.getFilled(support);
    }

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        if (filled) {
            return WateringCanUtils.findFilledCan(targetRef, store) != null;
        }
        return HeldItemUtils.findUsableHeldItem(targetRef, store, List.of(WateringCanUtils.EMPTY_CAN_ID)) != null;
    }

    @Override
    public int cost() {
        return 0;
    }
}
