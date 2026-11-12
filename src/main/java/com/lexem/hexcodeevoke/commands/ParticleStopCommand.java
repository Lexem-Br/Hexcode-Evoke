package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.ParticleLoops;

import javax.annotation.Nonnull;

public class ParticleStopCommand extends AbstractPlayerCommand {
    public ParticleStopCommand() {
        super("particlestop", "Stop the particle spawns started with /evoker particleloop");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        int stopped = ParticleLoops.stop(playerRef.getUuid());
        playerRef.sendMessage(Message.raw(stopped == 0
                ? "[Evoke] No particle loops running"
                : "[Evoke] Stopped " + stopped + " particle loop(s)"));
    }
}
