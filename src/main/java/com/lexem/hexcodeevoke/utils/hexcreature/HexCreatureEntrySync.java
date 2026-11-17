package com.lexem.hexcodeevoke.utils.hexcreature;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.DisplayNameSupport;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureEntry;
import com.lexem.hexcodeevoke.utils.common.UuidUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class HexCreatureEntrySync {

    private HexCreatureEntrySync() {
    }

    public static void applyToLoaded(
            @Nonnull EvokerComponent evoker,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nullable String language
    ) {
        for (HexCreatureEntry entry : evoker.getListHexCreatures()) {
            Ref<EntityStore> npcRef = UuidUtils.getRef(accessor, entry.getUUID());
            if (npcRef != null) applyToLoaded(entry, npcRef, accessor, language);
        }
    }

    public static void applyToLoaded(
            @Nonnull HexCreatureEntry entry,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull ComponentAccessor<EntityStore> accessor,
            @Nullable String language
    ) {
        HexCreatureComponent hexCreature = accessor.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null) return;

        HexCreatureNames.normalize(entry);
        HexCreatureNames.normalize(hexCreature);
        if (!entry.applyTo(hexCreature)) return;

        Dirty dirty = accessor.getComponent(npcRef, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();

        DisplayNameSupport displayNameSupport = accessor.getComponent(npcRef, DisplayNameSupport.getComponentType());
        if (displayNameSupport != null) {
            displayNameSupport.nominateDisplayName(hexCreature.getShowName() ? HexCreatureNames.text(hexCreature, language) : "");
        }
    }
}
