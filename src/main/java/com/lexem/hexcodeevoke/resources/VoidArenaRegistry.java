package com.lexem.hexcodeevoke.resources;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.resources.UniverseResourceType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoidArenaRegistry {
    public static final String ID = "HexcodeEvoke_VoidArena";
    private static final long SEAL_MILLIS = 2L * 60L * 60L * 1000L;

    public static final BuilderCodec<VoidArenaRegistry> CODEC = BuilderCodec
            .builder(VoidArenaRegistry.class, VoidArenaRegistry::new)
            .append(new KeyedCodec<>("TemporaryPortals", new MapCodec<>(TemporaryPortal.CODEC, HashMap<String, TemporaryPortal>::new)),
                    (registry, value) -> {
                        registry.temporaryPortals.clear();
                        if (value != null) registry.temporaryPortals.putAll(value);
                    },
                    registry -> registry.temporaryPortals
            ).add()
            .append(new KeyedCodec<>("SealedInstances", new MapCodec<>(Codec.LONG, HashMap<String, Long>::new)),
                    (registry, value) -> {
                        registry.sealedInstances.clear();
                        if (value != null) registry.sealedInstances.putAll(value);
                    },
                    registry -> registry.sealedInstances
            ).add()
            .build();

    private static UniverseResourceType<VoidArenaRegistry> TYPE;

    private final Map<String, TemporaryPortal> temporaryPortals = new ConcurrentHashMap<>();
    private final Map<String, Long> sealedInstances = new ConcurrentHashMap<>();

    public VoidArenaRegistry() {
    }

    public static void setResourceType(UniverseResourceType<VoidArenaRegistry> type) {
        TYPE = type;
    }

    @Nonnull
    public static VoidArenaRegistry get() {
        return Universe.get().getResource(TYPE);
    }

    public static void flush() {
        Universe.get().flushResource(TYPE);
    }

    public void addTemporaryPortal(@Nonnull String worldName, int x, int y, int z, long expiresAt) {
        temporaryPortals.put(portalKey(worldName, x, y, z), new TemporaryPortal(worldName, x, y, z, expiresAt));
    }

    public void removeTemporaryPortal(@Nonnull TemporaryPortal portal) {
        temporaryPortals.remove(portalKey(portal.worldName, portal.x, portal.y, portal.z));
    }

    @Nonnull
    public List<TemporaryPortal> getExpiredPortals(@Nonnull String worldName, long now) {
        List<TemporaryPortal> expired = new ArrayList<>();
        for (TemporaryPortal portal : temporaryPortals.values()) {
            if (worldName.equals(portal.worldName) && portal.expiresAt <= now) expired.add(portal);
        }
        return expired;
    }

    public void seal(@Nonnull UUID instanceUUID) {
        long now = System.currentTimeMillis();
        sealedInstances.values().removeIf(sealedAt -> now - sealedAt > SEAL_MILLIS);
        sealedInstances.put(instanceUUID.toString(), now);
    }

    public boolean isSealed(@Nonnull UUID instanceUUID) {
        Long sealedAt = sealedInstances.get(instanceUUID.toString());
        return sealedAt != null && System.currentTimeMillis() - sealedAt <= SEAL_MILLIS;
    }

    @Nonnull
    private static String portalKey(@Nonnull String worldName, int x, int y, int z) {
        return worldName + "|" + x + "|" + y + "|" + z;
    }

    public static class TemporaryPortal {
        public static final BuilderCodec<TemporaryPortal> CODEC = BuilderCodec
                .builder(TemporaryPortal.class, TemporaryPortal::new)
                .append(new KeyedCodec<>("World", Codec.STRING), (portal, value) -> portal.worldName = value, portal -> portal.worldName).add()
                .append(new KeyedCodec<>("X", Codec.INTEGER), (portal, value) -> portal.x = value, portal -> portal.x).add()
                .append(new KeyedCodec<>("Y", Codec.INTEGER), (portal, value) -> portal.y = value, portal -> portal.y).add()
                .append(new KeyedCodec<>("Z", Codec.INTEGER), (portal, value) -> portal.z = value, portal -> portal.z).add()
                .append(new KeyedCodec<>("ExpiresAt", Codec.LONG), (portal, value) -> portal.expiresAt = value, portal -> portal.expiresAt).add()
                .build();

        private String worldName;
        private int x;
        private int y;
        private int z;
        private long expiresAt;

        public TemporaryPortal() {
        }

        public TemporaryPortal(String worldName, int x, int y, int z, long expiresAt) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.expiresAt = expiresAt;
        }

        public String getWorldName() { return worldName; }

        public int getX() { return x; }

        public int getY() { return y; }

        public int getZ() { return z; }

        public long getExpiresAt() { return expiresAt; }
    }
}
