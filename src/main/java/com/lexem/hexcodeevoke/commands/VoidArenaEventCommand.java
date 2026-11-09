package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;

public class VoidArenaEventCommand extends AbstractPlayerCommand {

    public VoidArenaEventCommand() {
        super("event", "Start the Void Rift world event now, skipping the wait");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        if (VoidArenaManager.isArenaWorld(world)) {
            playerRef.sendMessage(Message.raw("[Evoke] The Void Rift event can't start inside the Void Arena"));
            return;
        }

        if (VoidArenaManager.stopEventNow(world)) {
            playerRef.sendMessage(Message.raw("[Evoke] Closed the previous Void Rift event"));
            // The stopped event leaves the world event manager once its removal is processed.
            world.execute(() -> start(playerRef, world));
            return;
        }

        start(playerRef, world);
    }

    private static void start(@Nonnull PlayerRef playerRef, @Nonnull World world) {
        boolean started = VoidArenaManager.startEventNow(world);
        playerRef.sendMessage(Message.raw(started
                ? "[Evoke] Void Rift event started. The rift appears in the wilderness around players and is marked on the map"
                : "[Evoke] Could not start the Void Rift event, check the server log"));
    }
}
