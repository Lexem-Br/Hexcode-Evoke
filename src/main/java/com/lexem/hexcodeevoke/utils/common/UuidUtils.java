package com.lexem.hexcodeevoke.utils.common;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class UuidUtils {
    private UuidUtils() {
    }

    @Nullable
    public static UUID parse(@Nullable String uuid) {
        if (uuid == null || uuid.isEmpty()) return null;

        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public static Ref<EntityStore> getRef(@Nonnull ComponentAccessor<EntityStore> accessor, @Nullable String uuid) {
        return getRef(accessor.getExternalData(), uuid);
    }

    @Nullable
    public static Ref<EntityStore> getRef(@Nonnull EntityStore entityStore, @Nullable String uuid) {
        UUID parsed = parse(uuid);
        if (parsed == null) return null;

        Ref<EntityStore> ref = entityStore.getRefFromUUID(parsed);
        return ref != null && ref.isValid() ? ref : null;
    }
}
