package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.utils.block.BlockSearchCache;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public abstract class BlockFinderFilter extends EntityFilterBase {
    private static final int COST = 100;

    private final BlockSearchCache search = new BlockSearchCache();
    private final int horizontalRange;
    private final int verticalRange;

    protected BlockFinderFilter(double horizontalRange, double verticalRange) {
        this.horizontalRange = (int) Math.ceil(horizontalRange);
        this.verticalRange = (int) Math.ceil(verticalRange);
    }

    protected abstract boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world);

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return false;

        return search.find(transform.getPosition(), horizontalRange, verticalRange, this::isTargetBlock, store.getExternalData().getWorld()) != null;
    }

    @Override
    public int cost() {
        return COST;
    }
}
