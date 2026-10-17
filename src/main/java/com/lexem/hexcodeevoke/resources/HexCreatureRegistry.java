package com.lexem.hexcodeevoke.resources;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.resources.UniverseResourceType;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HexCreatureRegistry {
    public static final String ID = "HexcodeEvoke_HexCreatures";

    public static final BuilderCodec<HexCreatureRegistry> CODEC = BuilderCodec
            .builder(HexCreatureRegistry.class, HexCreatureRegistry::new)
            .append(new KeyedCodec<>("Revoked", Codec.STRING_ARRAY),
                    (registry, value) -> {
                        registry.revoked.clear();
                        if (value != null) registry.revoked.addAll(Arrays.asList(value));
                    },
                    registry -> registry.revoked.toArray(new String[0])
            ).add()
            .append(new KeyedCodec<>("LastKnownLocations", new MapCodec<>(LastKnownLocation.CODEC, HashMap<String, LastKnownLocation>::new)),
                    (registry, value) -> {
                        registry.lastKnownLocations.clear();
                        if (value != null) registry.lastKnownLocations.putAll(value);
                    },
                    registry -> registry.lastKnownLocations
            ).add()
            .append(new KeyedCodec<>("RemovedWhileOwnerAway", Codec.STRING_ARRAY),
                    (registry, value) -> {
                        registry.removedWhileOwnerAway.clear();
                        if (value != null) registry.removedWhileOwnerAway.addAll(Arrays.asList(value));
                    },
                    registry -> registry.removedWhileOwnerAway.toArray(new String[0])
            ).add()
            .build();

    private static UniverseResourceType<HexCreatureRegistry> TYPE;

    private final Set<String> revoked = ConcurrentHashMap.newKeySet();
    private final Map<String, LastKnownLocation> lastKnownLocations = new ConcurrentHashMap<>();
    private final Set<String> removedWhileOwnerAway = ConcurrentHashMap.newKeySet();

    public HexCreatureRegistry() {
    }

    public static void setResourceType(UniverseResourceType<HexCreatureRegistry> type) {
        TYPE = type;
    }

    @Nonnull
    public static HexCreatureRegistry get() {
        return Universe.get().getResource(TYPE);
    }

    public static void flush() {
        Universe.get().flushResource(TYPE);
    }

    public void revoke(@Nonnull String npcUUID) {
        revoked.add(npcUUID);
    }

    public boolean isRevoked(@Nonnull String npcUUID) {
        return revoked.contains(npcUUID);
    }

    public void setLastKnownLocation(@Nonnull String npcUUID, @Nonnull String worldName, @Nonnull Vector3d position) {
        lastKnownLocations.put(npcUUID, new LastKnownLocation(worldName, position.x, position.y, position.z));
    }

    @Nullable
    public LastKnownLocation getLastKnownLocation(@Nonnull String npcUUID) {
        return lastKnownLocations.get(npcUUID);
    }

    public void removeLastKnownLocation(@Nonnull String npcUUID) {
        lastKnownLocations.remove(npcUUID);
    }

    public void markRemovedWhileOwnerAway(@Nonnull String npcUUID) {
        removedWhileOwnerAway.add(npcUUID);
    }

    public boolean consumeRemovedWhileOwnerAway(@Nonnull String npcUUID) {
        return removedWhileOwnerAway.remove(npcUUID);
    }

    public void forget(@Nonnull String npcUUID) {
        revoked.remove(npcUUID);
        lastKnownLocations.remove(npcUUID);
    }

    public static class LastKnownLocation {
        public static final BuilderCodec<LastKnownLocation> CODEC = BuilderCodec
                .builder(LastKnownLocation.class, LastKnownLocation::new)
                .append(new KeyedCodec<>("World", Codec.STRING), (location, value) -> location.worldName = value, location -> location.worldName).add()
                .append(new KeyedCodec<>("X", Codec.DOUBLE), (location, value) -> location.x = value, location -> location.x).add()
                .append(new KeyedCodec<>("Y", Codec.DOUBLE), (location, value) -> location.y = value, location -> location.y).add()
                .append(new KeyedCodec<>("Z", Codec.DOUBLE), (location, value) -> location.z = value, location -> location.z).add()
                .build();

        private String worldName;
        private double x;
        private double y;
        private double z;

        public LastKnownLocation() {
        }

        public LastKnownLocation(String worldName, double x, double y, double z) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String getWorldName() { return worldName; }

        public double getX() { return x; }

        public double getY() { return y; }

        public double getZ() { return z; }
    }
}
