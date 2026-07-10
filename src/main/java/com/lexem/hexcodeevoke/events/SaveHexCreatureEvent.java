package com.lexem.hexcodeevoke.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public record SaveHexCreatureEvent(
        @Nonnull Ref<EntityStore> refESPlayer,
        @Nonnull Ref<EntityStore> refESNPC
) implements IEvent<Void> {
    public static void dispatch(Ref<EntityStore> refESPlayer, Ref<EntityStore> refESNPC) {
        IEventDispatcher<SaveHexCreatureEvent, SaveHexCreatureEvent> dispatcher =
                HytaleServer.get().getEventBus().dispatchFor(SaveHexCreatureEvent.class);

        if (dispatcher.hasListener()) {
            dispatcher.dispatch(new SaveHexCreatureEvent(refESPlayer, refESNPC));
        }
    }
}
