package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ChangeStatBehaviour;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import javax.annotation.Nonnull;

public final class WandIndicatorUtils {
    public static final String DEFAULT_STAT = "DeployablePreview";

    private WandIndicatorUtils() {
    }

    public static void refresh(@Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> accessor) {
        refresh(playerRef, accessor, DEFAULT_STAT);
    }

    public static void refresh(@Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull String statId) {
        if (!playerRef.isValid()) return;

        EvokerComponent evoker = accessor.getComponent(playerRef, EvokerComponent.getComponentType());
        if (evoker == null) return;

        EntityStatMap entityStatMap = accessor.getComponent(playerRef, EntityStatMap.getComponentType());
        if (entityStatMap == null) return;

        int statIndex = EntityStatType.getAssetMap().getIndex(statId);
        if (statIndex < 0) return;

        evoker.pruneSelection();

        Int2FloatMap statChanges = new Int2FloatOpenHashMap();
        statChanges.put(statIndex, evoker.getSelectedHexCreatures().length);
        entityStatMap.processStatChanges(EntityStatMap.Predictable.SELF, statChanges, ValueType.Absolute, ChangeStatBehaviour.Set);
    }
}
