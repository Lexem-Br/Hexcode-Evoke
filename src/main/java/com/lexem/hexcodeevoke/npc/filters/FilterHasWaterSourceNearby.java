package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasWaterSourceNearby;
import com.lexem.hexcodeevoke.utils.WateringCanUtils;

import javax.annotation.Nonnull;

public class FilterHasWaterSourceNearby extends EntityFilterBase {
    private final double horizontalRange;
    private final double verticalRange;

    public FilterHasWaterSourceNearby(@Nonnull BuilderFilterHasWaterSourceNearby builder, @Nonnull BuilderSupport support) {
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
    }

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) return false;

        return WateringCanUtils.findNearestWater(transformComponent.getPosition(), horizontalRange, verticalRange, world) != null;
    }

    @Override
    public int cost() {
        return 100;
    }
}
