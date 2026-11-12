package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeflectableProjectileComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, DeflectableProjectileComponent> TYPE;

    private final String deflectedConfig;
    private final double range;
    private final float lifetime;
    private final String particles;
    private final String soundEventId;

    public DeflectableProjectileComponent(@Nonnull String deflectedConfig, double range, float lifetime, @Nullable String particles, @Nullable String soundEventId) {
        this.deflectedConfig = deflectedConfig;
        this.range = range;
        this.lifetime = lifetime;
        this.particles = particles;
        this.soundEventId = soundEventId;
    }

    public static void setComponentType(ComponentType<EntityStore, DeflectableProjectileComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, DeflectableProjectileComponent> getComponentType() {
        return TYPE;
    }

    @Nonnull
    public String getDeflectedConfig() {
        return deflectedConfig;
    }

    public double getRange() {
        return range;
    }

    public float getLifetime() {
        return lifetime;
    }

    @Nullable
    public String getParticles() {
        return particles;
    }

    @Nullable
    public String getSoundEventId() {
        return soundEventId;
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        return new DeflectableProjectileComponent(deflectedConfig, range, lifetime, particles, soundEventId);
    }
}
