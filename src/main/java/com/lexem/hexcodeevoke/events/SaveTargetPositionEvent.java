package com.lexem.hexcodeevoke.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public record SaveTargetPositionEvent(
        @Nonnull Ref<EntityStore> playerRef,
        Vector3d targetPosition
) implements IEvent<Void> {
    public static void dispatch(Ref<EntityStore> playerRef, Vector3d targetPosition) {
        IEventDispatcher<SaveTargetPositionEvent, SaveTargetPositionEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(SaveTargetPositionEvent.class);

        if (dispatcher.hasListener()) {
            dispatcher.dispatch(new SaveTargetPositionEvent(playerRef, targetPosition));
        }
    }
}
