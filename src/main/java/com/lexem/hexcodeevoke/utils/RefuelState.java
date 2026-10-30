package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;

public class RefuelState {
    public static final String REFUEL_STATE = "Refuel";
    private static final Set<String> NO_RETURN_STATES = Set.of("Following", "Recall");
    private static final long COOLDOWN_MILLIS = 30_000L;

    public enum Phase implements Supplier<String> {
        Tray("The Essence Tray the Hex Creature is going to"),
        Return("The place the Hex Creature was before going to the tray");

        private final String description;

        Phase(String description) {
            this.description = description;
        }

        @Override
        public String get() {
            return this.description;
        }
    }

    private Vector3i tray;
    private Vector3d returnPosition;
    private String returnState;
    private String returnSubState;
    private Phase phase;
    private long phaseSince;
    private long cooldownUntil;

    public void start(@Nonnull Vector3i tray, @Nonnull Vector3d position, @Nullable String state, @Nullable String subState) {
        this.tray = new Vector3i(tray);
        this.returnState = state;
        this.returnSubState = subState;
        this.returnPosition = state == null || NO_RETURN_STATES.contains(state) ? null : new Vector3d(position);
        this.phase = null;
    }

    public void finish() {
        this.tray = null;
        this.returnPosition = null;
        this.returnState = null;
        this.returnSubState = null;
        this.phase = null;
        this.cooldownUntil = System.currentTimeMillis() + COOLDOWN_MILLIS;
    }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() < this.cooldownUntil;
    }

    @Nullable
    public Vector3i getTray() {
        return this.tray;
    }

    @Nullable
    public Vector3d getReturnPosition() {
        return this.returnPosition;
    }

    @Nullable
    public String getReturnState() {
        return this.returnState;
    }

    @Nullable
    public String getReturnSubState() {
        return this.returnSubState;
    }

    public double getPhaseSeconds(@Nonnull Phase phase) {
        long now = System.currentTimeMillis();
        if (this.phase != phase) {
            this.phase = phase;
            this.phaseSince = now;
        }
        return (now - this.phaseSince) / 1000.0;
    }

    public static boolean isRefueling(@Nullable StateSupport stateSupport) {
        StateMappingHelper stateHelper = stateSupport != null ? stateSupport.getStateHelper() : null;
        return stateHelper != null && REFUEL_STATE.equals(stateHelper.getStateName(stateSupport.getStateIndex()));
    }

    public static boolean isRefueling(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        return isRefueling(store.getComponent(ref, StateSupport.getComponentType()));
    }
}
