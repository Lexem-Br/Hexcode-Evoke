package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class TransportState {
    public static final String TRANSPORT_STATE = "Transport";
    public static final String TRANSPORT_MESSAGE = "EvokeTransportItems";
    private static final long RETRY_DELAY_MILLIS = 3_000L;

    public enum Target implements Supplier<String> {
        Deliver("Carrying items to the destination container"),
        Collect("Going to take items from the source container (two containers only)"),
        Wait("Waiting for more items where it was when the command was given (one container only)");

        private final String description;

        Target(String description) {
            this.description = description;
        }

        @Override
        public String get() {
            return this.description;
        }
    }

    public enum ContainerType implements Supplier<String> {
        Any("Any container"),
        Block("A block with an item container, like a chest or the Essence Tray"),
        Creature("The inventory of another Hex Creature");

        private final String description;

        ContainerType(String description) {
            this.description = description;
        }

        @Override
        public String get() {
            return this.description;
        }

        public boolean matches(@Nullable Container container) {
            if (container == null) return false;
            return this == Any || (this == Creature) == container.isCreature();
        }
    }

    public static class Container {
        public static final BuilderCodec<Container> CODEC = BuilderCodec
                .builder(Container.class, Container::new)
                .append(new KeyedCodec<>("Block", Vector3iUtil.CODEC), (container, value) -> container.block = value, container -> container.block).add()
                .append(new KeyedCodec<>("Creature", Codec.STRING), (container, value) -> container.creatureUUID = value, container -> container.creatureUUID).add()
                .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC), (container, value) -> container.position = value, container -> container.position).add()
                .append(new KeyedCodec<>("LifeEssenceOnly", Codec.BOOLEAN), (container, value) -> container.lifeEssenceOnly = value, container -> container.lifeEssenceOnly).add()
                .build();

        private Vector3i block;
        private String creatureUUID;
        private Vector3d position;
        private boolean lifeEssenceOnly;

        public Container() {
        }

        @Nonnull
        public static Container ofBlock(@Nonnull Vector3i block, boolean lifeEssenceOnly) {
            Container container = new Container();
            container.block = new Vector3i(block);
            container.position = new Vector3d(block.x + 0.5, block.y, block.z + 0.5);
            container.lifeEssenceOnly = lifeEssenceOnly;
            return container;
        }

        @Nonnull
        public static Container ofCreature(@Nonnull String creatureUUID, @Nonnull Vector3d position) {
            Container container = new Container();
            container.creatureUUID = creatureUUID;
            container.position = new Vector3d(position);
            return container;
        }

        @Nonnull
        public Container copy() {
            Container container = new Container();
            container.block = this.block != null ? new Vector3i(this.block) : null;
            container.creatureUUID = this.creatureUUID;
            container.position = this.position != null ? new Vector3d(this.position) : null;
            container.lifeEssenceOnly = this.lifeEssenceOnly;
            return container;
        }

        public boolean isCreature() {
            return this.creatureUUID != null;
        }

        public boolean isSame(@Nullable Container other) {
            if (other == null) return false;
            if (isCreature() || other.isCreature()) return Objects.equals(this.creatureUUID, other.creatureUUID);
            return Objects.equals(this.block, other.block);
        }

        @Nullable
        public Vector3i getBlock() {
            return this.block;
        }

        @Nullable
        public String getCreatureUUID() {
            return this.creatureUUID;
        }

        @Nullable
        public Vector3d getPosition() {
            return this.position;
        }

        public boolean setPosition(@Nonnull Vector3d position, double minChange) {
            if (this.position != null && this.position.distanceSquared(position) < minChange * minChange) return false;
            this.position = new Vector3d(position);
            return true;
        }

        public boolean isLifeEssenceOnly() {
            return this.lifeEssenceOnly;
        }

        public void setLifeEssenceOnly(boolean lifeEssenceOnly) {
            this.lifeEssenceOnly = lifeEssenceOnly;
        }
    }

    public static final BuilderCodec<TransportState> CODEC = BuilderCodec
            .builder(TransportState.class, TransportState::new)
            .append(new KeyedCodec<>("Source", Container.CODEC), (state, value) -> state.source = value, state -> state.source).add()
            .append(new KeyedCodec<>("Destination", Container.CODEC), (state, value) -> state.destination = value, state -> state.destination).add()
            .append(new KeyedCodec<>("WaitPosition", Vector3dUtil.CODEC), (state, value) -> state.waitPosition = value, state -> state.waitPosition).add()
            .build();

    private Container source;
    private Container destination;
    private Vector3d waitPosition;
    private long retryAt;

    public boolean isWithinDistance(@Nonnull Container container, @Nonnull Vector3d position, boolean transporting, double maxDistance) {
        Vector3d target = container.getPosition();
        if (target == null) return false;

        Vector3d from = startsLoop(container, transporting) ? this.destination.getPosition() : position;
        return from != null && from.distanceSquared(target) <= maxDistance * maxDistance;
    }

    private boolean startsLoop(@Nonnull Container container, boolean transporting) {
        return transporting && isActive() && !isLoop() && !this.destination.isSame(container);
    }

    public void assign(@Nonnull Container container, @Nonnull Vector3d position, boolean transporting) {
        if (!transporting || !isActive() || isLoop()) {
            this.source = null;
            this.destination = container.copy();
            this.waitPosition = new Vector3d(position);
        } else if (!this.destination.isSame(container)) {
            this.source = this.destination;
            this.destination = container.copy();
            this.waitPosition = null;
        }
        this.retryAt = 0;
    }

    public void clear() {
        this.source = null;
        this.destination = null;
        this.waitPosition = null;
        this.retryAt = 0;
    }

    public boolean isActive() {
        return this.destination != null;
    }

    public boolean isLoop() {
        return this.source != null && this.destination != null;
    }

    @Nullable
    public Container getSource() {
        return this.source;
    }

    @Nullable
    public Container getDestination() {
        return this.destination;
    }

    @Nullable
    public Container getContainer(@Nonnull Target target) {
        return switch (target) {
            case Deliver -> this.destination;
            case Collect -> this.source;
            case Wait -> null;
        };
    }

    @Nullable
    public Vector3d getWaitPosition() {
        return this.waitPosition;
    }

    public boolean canRetry() {
        return System.currentTimeMillis() >= this.retryAt;
    }

    public void delayRetry() {
        this.retryAt = System.currentTimeMillis() + RETRY_DELAY_MILLIS;
    }

    public static boolean supportsTransport(@Nullable StateSupport stateSupport) {
        StateMappingHelper stateHelper = stateSupport != null ? stateSupport.getStateHelper() : null;
        return stateHelper != null && stateHelper.getStateIndex(TRANSPORT_STATE) >= 0;
    }

    public static boolean isTransporting(@Nullable StateSupport stateSupport, @Nonnull HexCreatureComponent hexCreature) {
        StateMappingHelper stateHelper = stateSupport != null ? stateSupport.getStateHelper() : null;
        if (stateHelper == null) return false;

        String state = stateHelper.getStateName(stateSupport.getStateIndex());
        if (RefuelState.REFUEL_STATE.equals(state)) state = hexCreature.getRefuelState().getReturnState();
        return TRANSPORT_STATE.equals(state);
    }
}
