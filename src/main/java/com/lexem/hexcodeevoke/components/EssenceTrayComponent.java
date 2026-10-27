package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EssenceTrayComponent implements Component<ChunkStore> {
    public static final String ID = "Evoke_EssenceTray";
    public static final String FILLED_STATE = "Filled";

    private transient EventRegistration<?, ?> changeRegistration;
    private transient World world;
    private transient Vector3i position;

    private static ComponentType<ChunkStore, EssenceTrayComponent> TYPE;

    public static void setComponentType(ComponentType<ChunkStore, EssenceTrayComponent> type) {
        TYPE = type;
    }

    public static ComponentType<ChunkStore, EssenceTrayComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<EssenceTrayComponent> CODEC = BuilderCodec
            .builder(EssenceTrayComponent.class, EssenceTrayComponent::new)
            .build();

    public EssenceTrayComponent() {
    }

    public void setChangeRegistration(@Nullable EventRegistration<?, ?> registration) {
        if (changeRegistration != null && changeRegistration.isRegistered()) {
            changeRegistration.unregister();
        }
        changeRegistration = registration;
    }

    public void setLocation(@Nullable World world, @Nullable Vector3i position) {
        this.world = world;
        this.position = position;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @Nullable
    public Vector3i getPosition() {
        return position;
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        return new EssenceTrayComponent();
    }
}
