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

public class VoidArenaResetCommand extends AbstractPlayerCommand {

    public VoidArenaResetCommand() {
        super("reset", "Stop the Void Arena fight in this world and make the altar usable again");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        playerRef.sendMessage(Message.raw(VoidArenaManager.reset(world)
                ? "[Evoke] Void Arena reset, the altar can be activated again"
                : "[Evoke] This Void Arena has not been activated yet"));
    }
}
