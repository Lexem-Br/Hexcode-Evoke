package com.lexem.hexcodeevoke.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record SaveHexCreatureMinionEvent(
        @Nonnull Ref<EntityStore> hcRef,
        @Nonnull Ref<EntityStore> minionRef,
        @Nonnull String entityId
) implements IEvent<Void> {
    public static void dispatch(Ref<EntityStore> hcRef, Ref<EntityStore> minionRef, String entityId) {
        IEventDispatcher<SaveHexCreatureMinionEvent, SaveHexCreatureMinionEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(SaveHexCreatureMinionEvent.class);

        if (dispatcher.hasListener()) {
            dispatcher.dispatch(new SaveHexCreatureMinionEvent(hcRef, minionRef, entityId));
        }
    }
}
